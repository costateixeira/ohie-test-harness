import React, { Fragment, useEffect, useState } from "react";
import "./testingRequest.scss";
import {
  TestRequestStateConstants,
  TestRequestStateConstantNames,
  StateBadgeClasses,
  TestRequestActionStateLabels,
} from "../../../constants/test_requests_constants.js";
import { USER_ROLES } from "../../../constants/role_constants.js";
import { TestRequestAPI } from "../../../api/TestRequestAPI.js";
import { useLoader } from "../../loader/LoaderContext.js";
import ComponentIdConnector from "../../connectors/ComponentIdConnector/ComponentIdConnector.js";
import { notification, Modal, Empty } from "antd";
import { formatDate } from "../../../utils/utils.js";
import { useNavigate } from "react-router-dom";
import { store } from "../../../store/store.js";
import { Pagination, PaginationItem } from "@mui/material";
import unsorted from "../../../styles/images/unsorted.png";
import sortedUp from "../../../styles/images/sort-up.png";
import sortedDown from "../../../styles/images/sort-down.png";
import { InfoCircleOutlined } from "@ant-design/icons";
import { SearchOutlined } from "@ant-design/icons";
import { Popover } from "antd";
import { DatePicker } from "antd";

import moment from "moment";
const TestingRequests = () => {
  const testRequestStates = [
    ...TestRequestActionStateLabels,
    { label: "All", value: "" },
  ];
  const [currentPage, setCurrentPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const { showLoader, hideLoader } = useLoader();
  const [filterState, setFilterState] = useState("");
  const [userRoles, setUserRoles] = useState([USER_ROLES.ROLE_ID_ASSESSEE]);
  const [testRequests, setTestRequests] = useState([]);
  const [showPassword, setShowPassword] = useState(false);
  const pageSize = 10;

  const navigate = useNavigate();
  const [sortDirection, setSortDirection] = useState({
    name: "desc",
    createdAt: "desc",
    default: "acs",
  });
  const [sortFieldName, setSortFieldName] = useState("default");
  const handleChangePage = (event, newPage) => {
    setCurrentPage(newPage);
    fetchTestRequests(sortFieldName, sortDirection, newPage);
  };

  const [testRequestSearchFilter, setTestRequestSearchFilter] = useState({
    name: "",
    requestDate: "",
    state: "",
  });

  const updateFilter = (field, value) => {
    setTestRequestSearchFilter((prevFilter) => ({
      ...prevFilter,
      [field]: value,
    }));
  };

  const handleTestRequestSearch = () => {
    setCurrentPage(1);
    fetchTestRequests(sortFieldName, sortDirection, 1);
  };

  const [filterDate, setFilterDate] = useState();

  const fetchTestRequests = (sortFieldName, sortDirection, newPage) => {
    let params = {};
    params.page = newPage - 1;
    params.sort = `${sortFieldName},${sortDirection[sortFieldName]}`;
    params.size = pageSize;

    const filteredTestRequestSearchFilter = Object.keys(testRequestSearchFilter)
      .filter((key) => {
        const value = testRequestSearchFilter[key];
        return typeof value === "string" ? value.trim() !== "" : !!value;
      })
      .reduce((acc, key) => {
        if (typeof testRequestSearchFilter[key] === "string")
          acc[key] = testRequestSearchFilter[key].trim();
        else acc[key] = testRequestSearchFilter[key];
        return acc;
      }, {});
    params = { ...params, ...filteredTestRequestSearchFilter };

    showLoader();
    TestRequestAPI.getTestRequestsByFilter(params)
      .then((res) => {
        hideLoader();
        setTestRequests(res.content);
        setTotalPages(res.totalPages);
      })
      .catch((err) => {
        hideLoader();
      });
  };

  const togglePasswordVisibility = (testUrl) => {
    if (testUrl.showPass) {
      testUrl.showPass = !testUrl.showPass;
    } else {
      testUrl.showPass = true;
    }
    setShowPassword(!showPassword);
  };
  useEffect(() => {
    const userInfo = store.getState().userInfoSlice;
    setUserRoles(userInfo.roleIds);
    fetchTestRequests(sortFieldName, sortDirection, currentPage);
  }, [filterState]);

  const handleSort = (field) => {
    setSortFieldName(field);
    const newSortDirection = { ...sortDirection };
    newSortDirection[field] = sortDirection[field] === "asc" ? "desc" : "asc";
    setSortDirection(newSortDirection);
    fetchTestRequests(field, newSortDirection, currentPage);
  };

  const renderSortIcon = (fieldName) => {
    if (sortFieldName === fieldName) {
      return (
        <img
          className="cursor-pointer"
          style={{ width: "8px" }}
          src={sortDirection[fieldName] === "asc" ? sortedUp : sortedDown}
        ></img>
      );
    }
    return (
      <img
        className="cursor-pointer"
        style={{ width: "10px" }}
        src={unsorted}
      />
    );
  };

  const changeState = (testRequestId, updatedState, index, proceedAnyways) => {
    showLoader();
    TestRequestAPI.validateChangeState(testRequestId, updatedState)
      .then((validationResults) => {
        let warnings = [];
        let errors = [];
        for (let validationResult of validationResults) {
          if (validationResult.level === "WARN") {
            warnings.push(validationResult.message);
          } else if (validationResult.level === "ERROR") {
            errors.push(validationResult.message);
          }
        }
        if (!!errors.length) {
          hideLoader();
          errors.forEach((error, i) => {
            notification.error({
              className: "notificationError",
              message: "Error",
              description: error,
              placement: "bottomRight",
            });
          });
        } else if (!!warnings.length && !proceedAnyways) {
          hideLoader();
          Modal.confirm({
            okButtonProps:{id:`editQuestion-testRequestStatusUpdate-okButton`},
            cancelButtonProps:{id:`editQuestion-testRequestStatusUpdate-cancelButton`},
            title: "Test Request Status Update",
            content: (
              <div>
                <p>
                  <strong>Please review the following warnings:</strong>
                </p>
                <ul style={{ paddingLeft: 20 }}>
                  {warnings.map((warning, i) => (
                    <li key={index}>{warning}</li>
                  ))}
                </ul>
              </div>
            ),
            okText: "Procced Anyway",
            cancelText: "Cancel",
            width: 600,
            onOk() {
              changeState(testRequestId, updatedState, index, true);
            },
          });
        } else {
          TestRequestAPI.changeState(testRequestId, updatedState)
            .then((res) => {
              notification.success({
                className: "notificationSuccess",
                placement: "top",
                message: "Status updated successfully!",
              });
              testRequests[index] = res;
              setTestRequests(testRequests);
              hideLoader();
            })
            .catch((err) => {
              hideLoader();
            });
        }
      })
      .catch((err) => {
        hideLoader();
      });
  };

  const toggleRow = (trid) => {
    setTestRequests((trs) => {
      return trs.map((tr) => {
        if (tr.id === trid) {
          tr.class = tr.class === "show" ? "hide" : "show";
        } else {
          tr.class = "hide";
        }
        return tr;
      });
    });
  };

  return (
    <div id="testingRequest">
      <div id="wrapper">
        <div className="col-12">
          <div className="row mb-2 justify-content-between">
            <div className="col-lg-4 col-md-4 col-sm-5 col-xxl-2 col-xl-3 col-12">
              <div className="custom-input custom-input-sm">
                
              </div>
            </div>

            {userRoles.includes(USER_ROLES.ROLE_ID_ASSESSEE) && (
              <div className="col-lg-4 col-md-6 col-sm-7 col-xl-3 col-12">
                <div className="d-flex align-items-baseline justify-content-end">
                  <button
                    id="#TestingRequests-registerTestRequest"
                    onClick={() => {
                      navigate("/register-application");
                    }}
                    type="button"
                    className="btn btn-sm btn-outline-secondary menu-like-item d-flex align-items-center py-0"
                  >
                    <i className="bi bi-plus fs-3"></i>
                    Register Test Request
                  </button>
                </div>
              </div>
            )}
          </div>

          <div className="table-responsive">
            <table className="data-table capitalize-words">
              <thead>
                <tr>
                  <th className="app-name-column">
                    APPLICATION NAME
                    <span
                      className="ps-1"
                      href="#"
                      onClick={() => handleSort("name")}
                      id="#TestingRequests-sortName"
                    >
                      {renderSortIcon("name")}
                    </span>
                    <div className="filter-box">
                      <input
                        id="TestingRequestNameSearchFilter"
                        type="text"
                        placeholder="Search by Name"
                        className="form-control filter-input"
                        value={testRequestSearchFilter.name}
                        onChange={(e) => updateFilter("name", e.target.value)}
                      />
                    </div>
                  </th>
                  <th className="date-column">
                    REQUEST DATE{" "}
                    <span
                      className="ps-1"
                      href="#"
                      onClick={() => handleSort("createdAt")}
                      id="#TestingRequests-sortCreatedAt"
                    >
                      {renderSortIcon("createdAt")}
                    </span>
                    <div className="filter-box">
                      <DatePicker
                        id="TestRequestDateSearchFilter"
                        className="form-control filter-input"
                        placeholder="Select Date"
                        value={filterDate}
                        onChange={(date) => {
                          setFilterDate(date);
                          updateFilter(
                            "requestDate",
                            date ? date.format("YYYY-MM-DD") : null
                          );
                        }}
                      />
                    </div>
                  </th>

                  <th className="status-column">
                    STATUS{" "}
                    <div className="filter-box">
                      <select
                        id="TestRequestStatusSearchFilter"
                        className="form-select custom-select custom-select-sm filter-input"
                        aria-label="Default select example"
                        value={testRequestSearchFilter.state}
                        onChange={(e) => {
                          updateFilter("state", e.target.value);
                        }}
                      >
                        {testRequestStates.map((state) => (
                          <option value={state.value} key={state.value}>
                            {state.label}
                          </option>
                        ))}
                      </select>
                    </div>
                  </th>
                  <th className="actions-column">
                    <span
                      className={
                        userRoles.includes(USER_ROLES.ROLE_ID_ADMIN)
                          ? "mx-2"
                          : undefined
                      }
                    >
                      Actions
                      <span
                        className="ps-1"
                        href="#"
                        onClick={() => handleSort("default")}
                        id="#TestingRequests-sortDefault"
                      >
                        {renderSortIcon("default")}
                      </span>
                      <div className="filter-box">
                        <button
                          className="search-button"
                          onClick={handleTestRequestSearch}
                          id="handleTestRequestSearch"
                        >
                          <SearchOutlined />
                          Search
                        </button>
                      </div>
                    </span>
                  </th>
                  <th className="empty"></th>
                </tr>
              </thead>
              <tbody>
                {testRequests.length === 0 ? (
                  <>
                    <tr>
                      <td className="text-center" colSpan={12}>
                        <Empty description="No Record Found." />
                      </td>
                    </tr>
                  </>
                ) : null}
                {testRequests.map((testRequest, index) => {
                  const formattedDate = moment(
                    testRequest.meta.createdAt
                  ).format("Do MMMM, YYYY");
                  return (
                    <Fragment key={testRequest.id}>
                      <tr
                        className={index % 2 == 0 ? "even" : "odd"}
                        key={testRequest.id}
                      >
                        <td className="fw-bold">{testRequest.name}</td>
                        <td>{formattedDate}</td>
                        <td>
                          {testRequest?.state !==
                          TestRequestStateConstants.TEST_REQUEST_STATUS_REJECTED ? (
                            <Fragment>
                              <span
                                className={`status badge ${
                                  StateBadgeClasses[testRequest.state]
                                }`}
                              >
                                {TestRequestStateConstantNames[
                                  testRequest.state
                                ].toLowerCase()}
                              </span>
                            </Fragment>
                          ) : (
                            <Fragment>
                              <span
                                className={`status badge ${
                                  StateBadgeClasses[testRequest.state]
                                }`}
                              >
                                {TestRequestStateConstantNames[
                                  testRequest.state
                                ].toLowerCase()}
                              </span>
                              <Popover
                                title={<div>{testRequest?.message}</div>}
                              >
                                <InfoCircleOutlined style={{marginLeft:"0.5rem", marginTop:"0.7rem"}}/>
                              </Popover>
                            </Fragment>
                          )}
                        </td>
                      <td className=" no-wrap text-left">
                        {userRoles.includes(USER_ROLES.ROLE_ID_ADMIN) &&
                        testRequest.state ==
                          TestRequestStateConstants.TEST_REQUEST_STATUS_PENDING ? (
                          <>
                            <span
                             id={`TestingRequests-${index}-approve`}
                            className="cursor-pointer"
                              onClick={() => {
                                changeState(
                                  testRequest.id,
                                  TestRequestStateConstants.TEST_REQUEST_STATUS_ACCEPTED,
                                  index
                                );
                              }}
                            >
                            <i className="bi bi-check-circle-fill text-green-50 font-size-16"></i>{" "}
                              APPROVE{" "}
                            </span>
                            <span
                              className="cursor-pointer ps-3"
                              id={`TestingRequests-${index}-reject`}
                              onClick={() => {
                                changeState(
                                  testRequest.id,
                                  TestRequestStateConstants.TEST_REQUEST_STATUS_REJECTED,
                                  index
                                );
                              }}
                            >
                              <i className="bi bi-x-circle-fill text-red font-size-16"></i>{" "}
                                REJECT{" "}
                            </span>
                          </>
                        ) : null}
                        {
                          testRequest.state ==
                          TestRequestStateConstants.TEST_REQUEST_STATUS_PUBLISHED 
                          && 
                          <button
                          id={`TestingRequests-${index}-report`}
                              className="cursor-pointer glossy-button glossy-button--gold d-flex align-items-center"
                              onClick={() => {navigate(`/application-report/${testRequest.id}`)}}
                            >
                              <i className="bi bi-file-text text-green-50 font-size-16"></i>{" "}
                              REPORT{" "}
                          </button>
                        }
                           {testRequest.state ===
                            TestRequestStateConstants.TEST_REQUEST_STATUS_PENDING && (
                            <span
                            id={`TestingRequests-${index}-edit`}
                              className="cursor-pointer font-size-12 text-blue fw-bold"
                              onClick={() => {
                                navigate(
                                  `/register-application/${testRequest.id}`
                                );
                              }}
                            >
                              <i className="bi bi-pencil-square font-size-16 "></i>{" "}
                              EDIT
                            </span>
                          )}
                        </td>
                        <td>
                          <span
                           id={`TestingRequests-${index}-toggleRow`}
                            onClick={() => toggleRow(testRequest.id)}
                            type="button"
                            className="approval-action-button float-end my-auto display"
                          >
                            {testRequest.class === "show" ? (
                              <i className="bi bi-arrow-up-circle-fill fs-5"></i>
                            ) : (
                              <i className="bi bi-arrow-down-circle-fill fs-5"></i>
                            )}
                          </span>
                        </td>
                      </tr>
                      <tr
                        className={"collapse " + testRequest.class}
                        key={"collapseable--" + testRequest.id}
                      >
                      <td colSpan="8" className="hiddenRow m-0 p-0 field-box">
                        <div id="Accordion" className="p-3 table-accordion-bg">
                            <table className="data-table-inner capitialize-words">
                              <thead>
                                  <tr>
                                    <th style={{width:'20%'}}>Component</th>
                                    <th style={{width:'20%'}}>Fhir Api Base Url</th>
                                    <th style={{width:'20%'}}>Website/UI Base Url</th>
                                    <th style={{width:'20%'}}>Username</th>
                                    <th style={{width:'20%'}}>Password</th>
                                    <th></th>
                                  </tr>
                                </thead>
                                <tbody>
                                  {testRequest.testRequestUrls.length > 0 &&
                                    testRequest.testRequestUrls.map(
                                      (testUrls) => (
                                        <tr
                                          id={testUrls.componentId}
                                          key={testUrls.componentId}
                                        >
                                          <td className="fw-bold">
                                            <ComponentIdConnector
                                              componentId={testUrls.componentId}
                                            ></ComponentIdConnector>
                                          </td>
                                          <td className="no-capitalization">  
                                            {testUrls.fhirApiBaseUrl}
                                          </td>
                                          <td className="no-capitalization">
                                            {testUrls.websiteUIBaseUrl}
                                          </td>
                                          <td className="toLowerCase-words">
                                            {testUrls.username}
                                          </td>
                                          <td
                                            className="toLowerCase-words"
                                            key={testRequest.id}
                                          >
                                            {testUrls.showPass
                                              ? testUrls.password
                                              : "*********"}
                                          </td>
                                          <td>
                                            <i
                                             id={`TestingRequests-${index}-togglePasswordVisibility`}
                                              className={`bi ${
                                                testUrls.showPass
                                                  ? "bi-eye-fill"
                                                  : "bi-eye-slash-fill"
                                              } padding-icon`}
                                              key={testUrls.componentId}
                                              onClick={() =>
                                                togglePasswordVisibility(
                                                  testUrls
                                                )
                                              }
                                            ></i>{" "}
                                          </td>
                                        </tr>
                                      )
                                    )}
                                </tbody>
                              </table>
                          </div>
                        </td>
                      </tr>
                    </Fragment>
                  );
                })}
              </tbody>
            </table>
          </div>
        </div>
        {totalPages > 1 && (
          <Pagination
            className="pagination-ui"
            count={totalPages}
            showFirstButton
            showLastButton
            page={currentPage}
            onChange={handleChangePage}
            variant="outlined"
            shape="rounded"
            renderItem={(item) => {
              if (item.type === "page") {
                return (
                  <PaginationItem
                    {...item}
                    id={`testingRequests-page-${item.page}`}
                    component="button"
                    onClick={() => handleChangePage(null, item.page)}
                  />
                );
              } else if (item.type === "previous") {
                return (
                  <PaginationItem
                    {...item}
                    id={`testingRequests-previous-page-button-${item.page}`}
                    component="button"
                    onClick={() => handleChangePage(null, currentPage - 1)}
                  />
                );
              } else if (item.type === "next") {
                return (
                  <PaginationItem
                    {...item}
                    id={`testingRequests-next-page-button-${item.page}`}
                    component="button"
                    onClick={() => handleChangePage(null, currentPage + 1)}
                  />
                );
              } else if (item.type === "first") {
                return (
                  <PaginationItem
                    {...item}
                    id="testingRequests-first-page-button"
                    component="button"
                    onClick={() => handleChangePage(null, 1)}
                  />
                );
              } else if (item.type === "last") {
                return (
                  <PaginationItem
                    {...item}
                    id="testingRequests-last-page-button"
                    component="button"
                    onClick={() => handleChangePage(null, totalPages)}
                  />
                );
              }
              return null;
            }}
          />
        )}
      </div>
    </div>
  );
};

export default TestingRequests;
