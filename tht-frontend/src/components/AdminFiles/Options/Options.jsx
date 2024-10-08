import { useEffect, useRef, useState } from "react";
import { TestResultAPI } from "../../../api/TestResultAPI";
import "./Options.scss";
import { useLoader } from "../../loader/LoaderContext";
import { RefObjUriConstants } from "../../../constants/refObjUri_constants";
import { TestResultRelationAPI } from "../../../api/TestResultRelationAPI";
import { Button, notification } from "antd";
import { ManualQuestionTypeConstants } from "../../../constants/testcase_constants";

/* 
	Options Component.

	This component displays all the options present for a particular testcase.

	Props:-
		1. testcaseResultInfo:- Details of the concerned parent Testcase question.
		2. setSelectedOption:- Function responsible for defining the selected option.
		3. currentQuestion:- Details regarding current question, which defines the question being single/multi select. 
		4. testcaseOptionId:- The unique identifier for the particular question.
		5. setIsModified:- The flag which indicates if any changes have been made in an question.
*/
export default function Options(props) {
	const { refId, testcaseResultInfo ,setSelectedOptions, currentQuestion ,testcaseOptionId ,setIsModified} = props;
	const [options, setOptions] = useState([]);
	const [currentOptions, setCurrentOptions] = useState([]);
	const [testResultRelationInfos, setTestResultRelations] = useState([]);
	const { showLoader, hideLoader } = useLoader();
	const inputRef = useRef(new Array());

	// This useEffect fetches all the concerned options for a specific testcase.
	useEffect(() => {
		showLoader();
		TestResultRelationAPI.getTestcaseResultRelatedObject(
			testcaseResultInfo.id,
			RefObjUriConstants.TESTCASE_OPTION_REFOBJURI
		).then((res) => {
			if (res && res.length > 0) {
				hideLoader();
				res.sort((a, b) => (a.rank ?? Number.MAX_SAFE_INTEGER) - (b.rank ?? Number.MAX_SAFE_INTEGER));
				setOptions(res);
			} else {
				hideLoader();
				notification.error({
					className:"notificationError",
					message: "Oops! something went wrong ,No answer found!",
					placement: "bottomRight",
				});
			}
		}).catch((error) => {
			hideLoader();
		});
	}, []);

	// This useEffect updates the selected option and checks whether there have been any change in the previous 
	// answers.
	useEffect(()=>{
		const selectedOption = testResultRelationInfos.filter(item => item.selected).map(item => item.refId);
		setIsModified(JSON.stringify(selectedOption) !== JSON.stringify(currentOptions));
	},[currentOptions]);

	useEffect(() => {
		showLoader();
		TestResultRelationAPI.getTestcaseResultRelationInfosByTestcaseResultIdAndRefObjUri(
			testcaseResultInfo.id,
			RefObjUriConstants.TESTCASE_OPTION_REFOBJURI
		).then((res) => {
			if (res && res.content.length > 0) {
				hideLoader();
				setTestResultRelations(res.content);
			} else {
				hideLoader();
				notification.error({
					className:"notificationError",
					message: "Oops! something went wrong ,No Result Related Data found!",
					placement: "bottomRight",
				});
			}
		}).catch((error) => {
			hideLoader();
		});
	}, []);

	useEffect(() => {
		if (options.length > 0 && testResultRelationInfos.length > 0) {
			options.forEach((option) => {
				let isSelected = isOptionSelected(option.id);
				if(isSelected){
					setCurrentOptions(prevOptions => [...prevOptions, option.id]);
				}
			});
		}
	}, [options, testResultRelationInfos]);

	useEffect(() => {
		setSelectedOptions(currentOptions);
	},[currentOptions]);

	const handleChange = (e) => {
		if(currentQuestion.questionType === ManualQuestionTypeConstants.SINGLE_SELECT){
			setCurrentOptions([e.target.value]);
		}
		else if(currentQuestion.questionType === ManualQuestionTypeConstants.MULTI_SELECT){
			let selectedVals = getSelectedCheckboxValuesByName();
			setCurrentOptions(selectedVals);
		}
	}

	const isOptionSelected = (testcaseOptionId) => {
		var testResultRelationInfo = testResultRelationInfos.filter((resultRelationInfo) => resultRelationInfo.refId == testcaseOptionId);
		if(!!testResultRelationInfo[0]){
			return testResultRelationInfo[0].selected;
		}else{
			notification.error({
				className:"notificationError",
				message: "There is something wrong in filtering result relation info for testOptionId",
				placement: "bottomRight",
			});
		}
	}

	const getSelectedCheckboxValuesByName = () => {
        // Find all input elements with the given name using refs
        const inputs = inputRef.current.filter(ref => !!ref && ref.checked);
        // Map the selected inputs to get their values
        const selectedValues = inputs.map(input => input.value);
        return selectedValues;
    };

	const onLabelClick = (e, index) => {
		e.preventDefault();// Stop event propagation
		if (inputRef.current[index]) {
			inputRef.current[index].click();
		}
	}


	return (
		<div id="options">
		<div className="custom-multiselect field-checkbox">
			{options && options.map((option, index) => (
				<div className={currentOptions.includes(option.id) ? "field-box option-selected" : "field-box"} key={option.id}>
					<div className="option-item">
						<input
							key={option.id}
							ref={(element) => element && inputRef.current.indexOf(element) == -1 && inputRef.current.push(element)}
							type="checkbox"
							id={option.id}
							name={option.id}
							value={option.id}
							checked={currentOptions.includes(option.id)}
							onChange={(e) => handleChange(e)}
							autoComplete="off"
						/>
						<label onClick={(e) => onLabelClick(e, index)} className={currentQuestion.questionType === ManualQuestionTypeConstants.MULTI_SELECT ? "label-before-no-radius cursor-pointer" : "cursor-pointer"} htmlFor={option.id} >{option.name}</label>
					</div>
				</div>
			))}
		</div>
		</div>
	);
}