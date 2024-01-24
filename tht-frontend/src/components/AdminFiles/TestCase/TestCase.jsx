import { Fragment, useEffect, useState } from "react";
import { TestResultAPI } from "../../../api/TestResultAPI";
import Options from "../Options/Options";

export default function TestCase(props) {
	const { specificationId, testRequestId } = props;
	const [manualQuestions, setManualQuestions] = useState([]);
	useEffect(() => {
		TestResultAPI.getQuestions(specificationId)
			.then((res) => {
				setManualQuestions(res.content);
			})
			.catch((error) => {
				throw error;
			});
	}, []);

	// const question = manualQuestions.filter((questions) => {
	// 	return questions.parentTestcaseResultId == specificationId;
	// });
	console.log(specificationId);
	return (
		<Fragment>
			<div className="col-12 non-fuctional-requirement">
				<div className="container">
					<div className="row heading">
						<div className="col-md-9 col-12 p-0">
							<h2>Question</h2>
						</div>
						{/* <div className="col-md-3 col-12 d-md-flex d-none p-0">
							<h2 className="border-left">Reference</h2>
						</div> */}
					</div>
					{manualQuestions.map((question) => {
						const segments = question.refId.split(".");
						const Specification = segments.slice(-3).join(".").toUpperCase();
						return (
							<div className="row question-box" key={question.id}>
								<div className="col-12 p-0 question">
									<h2><b>{Specification + " " +  question.name + " "}</b></h2>
									<Options refId={question.refId}></Options>
								</div>
							</div>
						);
					})}
				</div>
			</div>
		</Fragment>
	);
}
