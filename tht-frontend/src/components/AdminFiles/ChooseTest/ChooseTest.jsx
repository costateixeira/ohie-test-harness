import workflow_logo from "../../../styles/images/workflow-testing.png";
import functional_logo from "../../../styles/images/functional-testing.png";
import { Link, useNavigate, useParams } from "react-router-dom";
import "./choose-test.scss";
import { TestResultAPI } from "../../../api/TestResultAPI";
import { Fragment, useEffect, useState } from "react";
import { RefObjUriConstants } from "../../../constants/refObjUri_constants";
import { notification, Progress, Button, Breadcrumb } from "antd";
import { CheckCircleFilled, SyncOutlined } from "@ant-design/icons";
import { Spin } from "antd";
import { TestcaseResultStateConstants } from "../../../constants/testcaseResult_constants";
import { TestRequestAPI } from "../../../api/TestRequestAPI";
import { useDispatch } from "react-redux";
import { set_header } from "../../../reducers/homeReducer";
import WebSocketService from "../../../api/WebSocketService";
import VerificationGuidelines from "./Verification-Guidelines/VerificationGuidelines";
import { AutomatedVerificationGuidelines, ManualVerificationGuidelines } from "../../../constants/guidelines_constants";
import {  TestRequestStateConstants } from "../../../constants/test_requests_constants";
import TesterAutomatedModal from "./TesterAutomatedModal/TesterAutomatedModal.jsx"
import { TestcaseVariableAPI } from "../../../api/TestcaseVariableAPI.js";
/* 
  Choose Test page

  The tester can decide on whether to start the manual testing or the automate testcases from here.
*/
export default function ChooseTest() {
  const { testRequestId } = useParams();
  const { TESTCASE_REFOBJURI, TESTREQUEST_REFOBJURI } = RefObjUriConstants;
  const [testRequestInfo, setTestRequestInfo] = useState();
  const [totalManualTestcaseResults, setTotalManualTestcaseResults] = useState(0);
  const [totalAutomatedTestcaseResults, setTotalAutomatedTestcaseResults] = useState(0);
  const [totalFinishedManual, setTotalFinishedManual] = useState(0);
  const [totalFinishedAutomated, setTotalFinishedAutomated] = useState(0);
  const [totalAllManual, setTotalAllManual] = useState(0);
  const [totalAllAutomated, setTotalAllAutomated] = useState(0);
  const [totalInprogressAutomated, setTotalInprogressAutomated] = useState(0);
  const [testcaseResults, setTestCaseResults] = useState([]);
  const [submitButtonFlag, setSubmitButtonFlag]=useState(false);
  const [resultFlag, setResultFlag]=useState(false);
  const [isTesterModalOpen, setIsTesterModalOpen] = useState(false);
  const navigate = useNavigate();
  const dispatch = useDispatch();
  const { stompClient, webSocketConnect, webSocketDisconnect } = WebSocketService();
  const [ isViewMode,setIsViewMode] = useState(true);;
  /* 
  This useEffect fetches all the testcases, both manual and automated.
  And based on the data recieved, the progress is tracked.
  This helps the tester in knowing the number of completed and remaining testcases.
  Also initiates the web socket connection to keep track of any automated testcases already running.
  */
  useEffect(() => {
    var totalManual = 0;
    var totalFinishedManual = 0;
    var totalInprogressAutomated = 0;
    var totalAutomated = 0;
    var totalFinishedAutomated = 0;
    var totalAllAutomated = 0;
    var totalAllMenual = 0;
    testcaseResults.forEach((testcaseResult) => {
      if (
        testcaseResult.state !==
        TestcaseResultStateConstants.TESTCASE_RESULT_STATUS_DRAFT
      ) {
        if (!!testcaseResult.manual) {
          totalManual++;
          if (
            testcaseResult.state ===
            TestcaseResultStateConstants.TESTCASE_RESULT_STATUS_SKIP ||
            testcaseResult.state ===
            TestcaseResultStateConstants.TESTCASE_RESULT_STATUS_FINISHED
          ) {
            totalFinishedManual++;
          }
        }
        if (!!testcaseResult.automated) {
          totalAutomated++;
          if (
            testcaseResult.state ===
            TestcaseResultStateConstants.TESTCASE_RESULT_STATUS_SKIP ||
            testcaseResult.state ===
            TestcaseResultStateConstants.TESTCASE_RESULT_STATUS_FINISHED
          ) {
            totalFinishedAutomated++;
          }
        }
      }
      if (
        !!testcaseResult.automated &&
        testcaseResult.state ===
        TestcaseResultStateConstants.TESTCASE_RESULT_STATUS_INPROGRESS
      ) {
        totalInprogressAutomated++;
      }
      if (!!testcaseResult.manual) {
        totalAllMenual++;
      }
      if (!!testcaseResult.automated) {
        totalAllAutomated++;
      }
    });
    setTotalAllManual(totalAllMenual);
    setTotalAllAutomated(totalAllAutomated);
    setTotalFinishedManual(totalFinishedManual);
    setTotalFinishedAutomated(totalFinishedAutomated);
    setTotalManualTestcaseResults(totalManual);
    setTotalAutomatedTestcaseResults(totalAutomated);
    setTotalInprogressAutomated(totalInprogressAutomated);

    // Start the WebSocket Connection
    if (
      (totalFinishedManual < totalManual ||
        totalFinishedAutomated < totalAutomated ||
        totalAutomated === 0 ||
        totalManual === 0) &&
      stompClient === null &&
      testcaseResults.length > 0
    ) {
      webSocketConnect();
    }

    // Disconnect the WebSocket onnce all of the testcase are finished
    if (
      totalFinishedManual === totalManual &&
      totalFinishedAutomated === totalAutomated &&
      totalAutomated > 0 &&
      totalManual > 0
    ) {
      webSocketDisconnect();
    }
  }, [testcaseResults]);

  // This function keeps real-time track of the progress for the testcases.
  const loadProgress = () => {
    const params = {
      testRequestId: testRequestId,
      refObjUri: TESTCASE_REFOBJURI,
    };
    TestResultAPI.fetchCasesForProgressBar(params)
      .then((res) => {
        setTestCaseResults(res.data.content);
        setSubmitButtonFlag(true);
        setResultFlag(true);
      })
      .catch((error) => {

      });
  };

    // This function initiates testcases for both manual and automates testing.
  const handleStartTesting = (manual, automated) => {
    const params = {
      testRequestId,
      refObjUri: TESTREQUEST_REFOBJURI,
      refId: testRequestId,
    };
    if (!!manual) {
      params.manual = true;
    }
    if (!!automated) {
      params.automated = true;
    }
    TestResultAPI.startTests(params)
      .then((response) => {
        notification.success({
          className: "notificationSuccess",
          placement: "top",
          message: "Testing has been started successfully!",
        });
        if (!!manual) {
          navigate(`/manual-testing/${testRequestId}`);
        }
        if (!!automated) {
          navigate(`/automated-testing/${testRequestId}`);
        }
        loadProgress();
      }).catch((error) => {

      });
  };

  // This function fetches details regarding testcase.
  const testCaseInfo = () => {
    TestRequestAPI.getTestRequestsById(testRequestId)
      .then((res) => {
        setTestRequestInfo(res);
        if(res.state === TestRequestStateConstants.TEST_REQUEST_STATUS_FINISHED || 
          res.state === TestRequestStateConstants.TEST_REQUEST_STATUS_PUBLISHED
        ){
          setIsViewMode(true);
          dispatch(set_header(`${res.name} - View Only`));
        }
        else{
          setIsViewMode(false);
        dispatch(set_header(res.name));
        }
      }).catch((error) => {

      });
  };


const handleOpenModal = () => {
  TestRequestAPI.getTestRequestsById(testRequestId)
    .then((res) => {
      const selectedComponentsIds = res.testRequestUrls.map(testRequestUrl => testRequestUrl.componentId);

      const testcaseVariablesPromises = selectedComponentsIds.map(selectedComponentId => 
        TestcaseVariableAPI.getTestcaseVariablesByComponentId(selectedComponentId)
      );

      return Promise.all(testcaseVariablesPromises);
    })
    .then((allTestcaseVariables) => {
      const tcvForTester = allTestcaseVariables.flat().filter(testcaseVariable => 
        testcaseVariable.roleId === "role.tester"
      );

      if (tcvForTester.length > 0) {
        setIsTesterModalOpen(true);
      }
      else{
        handleStartTesting(false, true);
      }
    })
    .catch((err) => {
      console.error(err);
    });
}

  // This useEffect keeps tracks of testcases being completed behind the scenes, and updates the UI.
  useEffect(() => {
    if (stompClient && stompClient.connected) {
      testcaseResults.forEach((testcaseResult, index) => {
        // Listener for the testcase if in pending
        if (
          testcaseResult.state !==
          TestcaseResultStateConstants.TESTCASE_RESULT_STATUS_FINISHED
        ) {
          const destination = '/testcase-result/' + (!!testcaseResult.manual ? 'manual/' : 'automated/') + testcaseResult.id;
          var subscription = stompClient.subscribe(destination, (msg) => {
            const parsedTestcaseResult = JSON.parse(msg.body);
            setTestCaseResults((prevTestcaseResults) => {
              const updatedTestcaseResults = [...prevTestcaseResults];
              updatedTestcaseResults[index] = parsedTestcaseResult;
              return updatedTestcaseResults;
            });
            if (
              parsedTestcaseResult?.state === "testcase.result.status.finished"
            ) {
              subscription.unsubscribe();
            }
          });
        }
      });
    }
  }, [stompClient]);

  // Default useEffect which fetches progress, fetches testcaseInfo. 
  // Also Disconnects the web-socket connection once the component is unmounted. 
  useEffect(() => {
    loadProgress();
    testCaseInfo();
    return () => {
      // Disconnect WebSocket when component unmounts
      webSocketDisconnect();
    };
  }, []);

  // This function is responsible for finishing a test-request. 
  // Once this function is called, the choose-page will no longer be available. 
  // Report is generated only after this function is called.
  const submitHandler=()=>{
    TestRequestAPI.changeState(testRequestId, "test.request.status.finished")
        .then((res) => {
          notification.success({
            className: "notificationSuccess",
            placement: "top",
            message:"Verification submitted successfully!",
          });
          const newTab = window.open(`/application-report/${testRequestId}`, '_blank');
          newTab.focus();      
          navigate(`/applications`);

        })
        .catch((err) => {       
        }); 
  }
  return (
    <div id="chooseTest">
      <div id="wrapper">
        <div className="col-12 pt-3">
          <Breadcrumb className="custom-breadcrumb">
          <Breadcrumb.Item>
            <Link to="/applications" className="breadcrumb-item" id="chooseTest-navtoApplications">
              Applications
            </Link>
          </Breadcrumb.Item>
            <Breadcrumb.Item className="breadcrumb-item">{testRequestInfo?.name}</Breadcrumb.Item>
          </Breadcrumb>
          <hr className="hr-light"/>
          <h5 className="mt-3">{isViewMode ? "View your responses." : "Choose Verification Type"}</h5>
          <p className="text-gray">
            {isViewMode ? "View your responses to manual testcases or view your results for automated testcases."
            : "Select the type to start verifying application with OpenHIE."}
          </p>
          <div className="d-flex flex-wrap">
            {!!testcaseResults && totalAllManual !==0 
            && 
            <div className="testing-grid">
            <div className="icon-box">
              <img src={functional_logo} />
            </div>
            <div className="text-box">
              <h6 className="">Manual Verification</h6>
              <p className="mb-0">
                If you need more info, please check out{" "}
                <span className="fixed-button">
                  <a
                    className="text-blue"
                    data-bs-toggle="offcanvas"
                    href="#manualTesting"
                    aria-controls="manualTesting"
                  >
                    Guideline.
                  </a>
                </span>
                <div
                  className="offcanvas offcanvas-end"
                  tabIndex="-1"
                  id="manualTesting"
                  aria-labelledby="manualTestingLabel"
                >
                  <div id="mySidebar" class="sidebar-right">
                    <VerificationGuidelines title="Manual Verification Guidelines" guidelines={ManualVerificationGuidelines}/>
                  </div>
                </div>
              </p>
              {totalManualTestcaseResults == 0 && (
                <button
                  id="chooseTest-manualVerification"
                  className="btn btn-primary  btn-sm mt-4 "
                  onClick={() => {
                    handleStartTesting(true, null);
                  }}
                >
                  Start Verification
                </button>
              )}
              {totalManualTestcaseResults != 0 && (
                <Fragment>
                  <Progress
                    percent={Math.floor(
                      (totalFinishedManual /
                        (!!totalFinishedManual
                          ? totalAllManual
                          : totalManualTestcaseResults)) *
                      100
                    )}
                    format={() => {
                      if (
                        Math.floor(
                          (totalFinishedManual /
                            (!!totalFinishedManual
                              ? totalAllManual
                              : totalManualTestcaseResults)) *
                          100
                        ) === 100
                      ) {
                        return <CheckCircleFilled color="#52C41A" />;
                      } else {
                        return (
                          <span>
                            {totalFinishedManual}/
                            {!!totalFinishedManual
                              ? totalAllManual
                              : totalManualTestcaseResults}
                          </span>
                        );
                      }
                    }}
                  />
                  <Button className="btn start-test-button mt-2"
                    id="chooseTest-resumeManualVerification"
                    onClick={() => navigate(`/manual-testing/${testRequestId}`)}
                  >
                    {
                      isViewMode ? "Show Results" : 
                      totalAllManual === totalFinishedManual ?
                        "Modify" :
                        "Resume"
                    }
                  </Button>
                </Fragment>
              )}
            </div>
          </div>}
           {!!testcaseResults && totalAllAutomated !== 0 && 
            <div className="testing-grid">
              <div className="icon-box">
                <img src={workflow_logo} />
              </div>
              <div className="text-box">
                <h6 className="">Automated Verification</h6>
                <p className="mb-0">
                  If you need more info, please check out{" "}
                  <span className="fixed-button">
                    <a
                      className="text-blue"
                      data-bs-toggle="offcanvas"
                      href="#automatedTesting"
                      aria-controls="automatedTesting"
                    >
                      Guideline.
                    </a>
                  </span>
                  <div
                    className="offcanvas offcanvas-end"
                    tabIndex="-1"
                    id="automatedTesting"
                    aria-labelledby="automatedTestingLabel"
                  >
                    <div id="mySidebar" class="sidebar-right">
                     <VerificationGuidelines title="Automated Verification Guidelines" guidelines={AutomatedVerificationGuidelines}/>
                    </div>
                  </div>
                </p>
                {totalAutomatedTestcaseResults == 0 && (
                  <button
                  id="chooseTest-startAutomatedVerification"
                    className="btn btn-primary small btn-sm mt-4 "
                    onClick={handleOpenModal}
                    disabled={!resultFlag}
                  >
                    Start Verification
                    {!resultFlag && <Spin size="small"/>}
                  </button>
                )}

                {totalAutomatedTestcaseResults != 0 && (
                  <Fragment>
                    <Progress
                      percent={Math.floor(
                        (totalFinishedAutomated /
                          (!!totalFinishedAutomated
                            ? totalAllAutomated
                            : totalAutomatedTestcaseResults)) *
                        100
                      )}
                      format={() => {
                        if (
                          Math.floor(
                            (totalFinishedAutomated /
                              (!!totalFinishedAutomated
                                ? totalAllAutomated
                                : totalAutomatedTestcaseResults)) *
                            100
                          ) === 100
                        ) {
                          return <CheckCircleFilled color="#52C41A" />;
                        } else {
                          return (
                            <div>
                              {" "}
                              {totalInprogressAutomated != 1 ? (
                                <div></div>
                              ) : (
                                <SyncOutlined spin />
                              )}
                              <span className="ml-2">
                                {" "}
                                {totalFinishedAutomated}/
                                {!!totalFinishedAutomated
                                  ? totalAllAutomated
                                  : totalAutomatedTestcaseResults}

                              </span>
                            </div>
                          );
                        }
                      }}
                      strokeColor={
                        totalInprogressAutomated != 1
                          ? Math.floor(
                            (totalFinishedAutomated /
                              (!!totalFinishedAutomated
                                ? totalAllAutomated
                                : totalAutomatedTestcaseResults)) *
                            100
                          ) === 100
                            ? "#52C41A"
                            : "red"
                          : "blue"
                      }
                      status={
                        Math.floor(
                          (totalFinishedAutomated /
                            (!!totalFinishedAutomated
                              ? totalAllAutomated
                              : totalAutomatedTestcaseResults)) *
                          100
                        ) === 100
                          ? "inactive"
                          : "active"
                      }
                    />
                    <Button className="btn start-test-button"
                    id="chooseTest-resumeAutomatedVerification"
                      onClick={() =>
                        navigate(`/automated-testing/${testRequestId}`)
                      }
                    >
                      Show Result
                    </Button>
                  </Fragment>
                )}
                {/* <div className="progress-bar-line"></div> */}
              </div>
            </div>
            }
          </div>
        </div>
          {!isViewMode && !!submitButtonFlag && totalAllManual===totalFinishedManual && totalAllAutomated===totalFinishedAutomated?
          <div className="message-grid">
            <p>Verification has been successfully completed. Please click the button below to submit the results.</p>
            <button className="submit-btn cst-btn-group btn" id="chooseTest-Submit" onClick={submitHandler}>Submit</button>
          </div>  
          :
          <></>}
        <div>
          <TesterAutomatedModal
            isTesterModalOpen={isTesterModalOpen}
            setIsTesterModalOpen={setIsTesterModalOpen}
            currentTestRequestId={testRequestId}
            handleStartTesting={handleStartTesting}
          ></TesterAutomatedModal>
        </div>
      </div>
    </div>
  );
}
