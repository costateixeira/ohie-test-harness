import React, { Fragment, useEffect, useState } from "react";
import "./_registrationApplication.scss";
import { useNavigate, useParams } from "react-router-dom";
import { useFormik } from "formik";
import { ComponentAPI } from "../../../api/ComponentAPI.js";
import { useLoader } from "../../loader/LoaderContext.js";
import { TestRequestAPI } from "../../../api/TestRequestAPI.js";
import { TestcaseVariableAPI } from "../../../api/TestcaseVariableAPI.js";
import { TestCaseAPI } from "../../../api/TestCaseAPI.js";
import { notification, Empty } from "antd";
import { TestRequestStateConstants } from "../../../constants/test_requests_constants.js";
import {
  CREATE_VALIDATION,
  UPDATE_VALIDATION,
} from "../../../constants/validation_constants.js";
import { store } from "../../../store/store.js";
import { Popover } from "antd";
import { InfoCircleOutlined } from "@ant-design/icons";
import { useDispatch } from "react-redux";
import { set_header } from "../../../reducers/homeReducer.jsx";
import { SpecificationAPI } from "../../../api/SpecificationAPI.js";
import { ROLE_ID_ASSESSEE } from "../../../constants/role_constants.js";
const RegisterApplication = () => {
  const navigate = useNavigate();
  const { showLoader, hideLoader } = useLoader();
  const [components, setComponents] = useState([]);
  const [userId, setUserId] = useState();
  const [showPassword, setShowPassword] = useState(false);
  const [touched, setTouched] = useState({});
  const [meta, setMeta] = useState(); //only used when updating test request.
  const { testRequestId } = useParams();
  const dispatch = useDispatch();
  const [selectedComponents, setSelectedComponents] = useState({});
  const [defaultSelectedComponents, setDefaultSelectedComponents] = useState({});
  const [testRequestValues, setTestRequestValues] = useState([]);

  // A custom validation function. This must return an object
  // which keys are symmetrical to our values/initialValues
  const validate = (values) => {
    const errors = {};

    if (!values.name.trim()) {
      errors.name = "Application Name is required";
    } else if (values.name.trim().length < 3) {
      errors.name = "Application name must be of minimum 3 characters";
    } else if (values.name.trim().length > 50) {
      errors.name = "Application name must have less than 50 characters.";
    }

    // if (values.description === "") {
    //   errors.description = "Application Description is required";
    // } else if (values.description.length > 1000) {
    //   errors.description =
    //     "Application description must have less than 1000 characters.";
    // }

    values.testRequestUrls.forEach((url, index) => {
      const componentId = modifiedComponentId(url.componentId);

      if (!url.username.trim()) {
        errors[`testRequestUrls[${componentId}].username`] =
          "Username is required";
      } else if (url.username.trim().length > 255) {
        errors[`testRequestUrls[${componentId}].username`] =
          "Username must have less than 255 characters";
      }

      if (!url.password.trim()) {
        errors[`testRequestUrls[${componentId}].password`] =
          "Password is required";
      } else if (url.password.trim().length > 255) {
        errors[`testRequestUrls[${componentId}].password`] =
          "Password must have less than 255 characters";
      }

      if (!url.fhirApiBaseUrl.trim()) {
        errors[`testRequestUrls[${componentId}].fhirApiBaseUrl`] =
          "fhirApiBaseUrl is required";
      } else if (url.fhirApiBaseUrl.trim().length > 255) {
        errors[`testRequestUrls[${componentId}].fhirApiBaseUrl`] =
          "fhirApiBaseUrl must have less than 255 characters";
      }
      if (url.websiteUIBaseUrl.length > 255) {
        errors[
          `testRequestUrls[${modifiedComponentId(
            url.componentId
          )}].websiteUIBaseUrl`
        ] = "websiteUIBaseUrl must have less than 255 characters";
      }

      values.testRequestValues.forEach((testRequestValue, index) => {
        if (testRequestValue.testRequestValueInput == null || !testRequestValue.testRequestValueInput.trim()) {
          errors[`testRequestValues[${index}].testRequestValueInput`] =
            testRequestValue.key + " is required";
        }
        if (testRequestValue.testRequestValueInput && testRequestValue.testRequestValueInput.length > 255) {
          errors[`testRequestValues[${index}].testRequestValueInput`] =
            testRequestValue.key + " must have less than 255 characters";
        }

      })
    });

    return errors;
  };

  const modifiedComponentId = (componentId) => {
    return componentId.replace(/\./g, "");
  };

  const formik = useFormik({
    initialValues: {
      name: "",
      state: TestRequestStateConstants.TEST_REQUEST_STATUS_PENDING,
      description: "",
      assesseeId: "",
      testRequestUrls: [],
      testRequestValues: []
    },
    validate,
    onSubmit: (values) => {
      formik.values.assesseeId = userId;
      if (testRequestId) {
        const data = { ...values, id: testRequestId, meta: meta };
        showLoader();
        TestRequestAPI.validateTestRequest(UPDATE_VALIDATION, data)
          .then((res) => {
            if (res.length == 0) {
              TestRequestAPI.updateTestRequest(data)
                .then((res) => {
                  notification.success({
                    className: "notificationSuccess",
                    placement: "top",
                    message: "Test Request Updated Successfully.",
                  });
                  hideLoader();
                  navigate("/testing-requests");
                })
                .catch(() => { });
            } else {
              res.forEach((err) => {
                notification.error({
                  className: "notificationError",
                  message: err.message,
                  placement: "bottomRight",
                });
              });
              hideLoader();
            }
          })
          .catch(() => { });
      } else {
        showLoader();
        TestRequestAPI.validateTestRequest(CREATE_VALIDATION, values)
          .then((res) => {
            if (res.length == 0) {
              TestRequestAPI.createTestRequest(values)
                .then((res) => {
                  notification.success({
                    className: "notificationSuccess",
                    placement: "top",
                    message: `Your application testing request has been successfully created and sent to admin for approval.`,
                  });
                  hideLoader();
                  navigate("/testing-requests");
                })
                .catch((error) => {
                  hideLoader();
                });
            } else {
              res.forEach((err) => {
                notification.error({
                  className: "notificationError",
                  message: err.message,
                  placement: "bottomRight",
                });
              });
              hideLoader();
            }
          })
          .catch((error) => { });
      }
    },
  });

  const addOrRemoveTestUrlsAndValues = (selectedComponent, newTestRequestValues, updatedSelectedComponents, prevSelectedComponents, isSelected) => {
    var tvalues = formik.getFieldHelpers("testRequestValues");
    var turls = formik.getFieldHelpers("testRequestUrls");
    const key = modifiedComponentId(selectedComponent.id);

    if (isSelected) {
      tvalues.setValue([...formik.values.testRequestValues, ...newTestRequestValues]);
      updatedSelectedComponents[selectedComponent.id] = newTestRequestValues;

      setDefaultSelectedComponents((prevDefaultSelectedComponents) => ({
        ...prevDefaultSelectedComponents,
        [selectedComponent.id]: newTestRequestValues
      }));


      const touchedFields = {};
      if (!!newTestRequestValues) {
        newTestRequestValues.forEach((trv) => {
          touchedFields[trv.key] = false;
        })
      }

      setTouched((prevTouched) => ({
        ...prevTouched,
        [key]: {
          username: false,
          password: false,
          fhirApiBaseUrl: false,
          websiteUIBaseUrl: false,
          ...touchedFields
        },
      }));


      turls.setValue([
        ...formik.values.testRequestUrls,
        {
          username: "",
          password: "",
          fhirApiBaseUrl: "",
          websiteUIBaseUrl: "",
          componentId: selectedComponent.id,
        },
      ]);


    } else {
      delete updatedSelectedComponents[selectedComponent.id];

      const deselectedIds = new Set(
        (prevSelectedComponents[selectedComponent.id] || []).map(v => v.testcaseVariableId)
      );

      const updatedTestRequestValues = formik.values.testRequestValues.filter(
        value => !deselectedIds.has(value.testcaseVariableId)
      );

      tvalues.setValue(updatedTestRequestValues);
      turls.setValue(
        formik.values.testRequestUrls.filter(
          (url) => url.componentId !== selectedComponent.id
        )
      );

      setTouched((prevTouched) => {
        const { [key]: removedKey, ...remainingTouched } = prevTouched;
        return remainingTouched;
      });
    }
  };

  const onComponentSelected = (index, e) => {
    const selectedComponent = components[index];
    selectedComponent.isSelected = e.target.checked;

    setSelectedComponents((prevSelectedComponents = {}) => {
      const updatedSelectedComponents = { ...prevSelectedComponents };
      const newTestRequestValues = [];

      if (selectedComponent.isSelected) {
        TestcaseVariableAPI.getTestcaseVariablesByComponentId(selectedComponent.id)
          .then((res) => {

            let promises = [];

            res.map((testcaseVariable) => {
              if (testcaseVariable.roleId === ROLE_ID_ASSESSEE) {
                const promise = TestcaseVariableAPI.getTestcaseVariablesById(testcaseVariable.id)
                  .then((testcaseVariableRes) => {
                    return TestCaseAPI.getTestCasesById(testcaseVariableRes.testcaseId)
                      .then(testCaseRes => ({
                        testcaseVariableRes,
                        testCaseRes
                      }));
                  })
                  .then(({ testcaseVariableRes, testCaseRes }) => {
                    return SpecificationAPI.getSpecificationById(testCaseRes.specificationId)
                      .then(specificationRes => ({
                        testcaseVariableRes,
                        testCaseRes,
                        specificationRes
                      }));
                  })
                  .then(({ testcaseVariableRes, testCaseRes, specificationRes }) => {
                    newTestRequestValues.push({
                      key: testcaseVariableRes.testcaseVariableKey,
                      testcaseVariableId: testcaseVariableRes.id,
                      testRequestValueInput: testcaseVariableRes.defaultValue,
                      testcaseName: testCaseRes.name,
                      specificationName: specificationRes.name,
                    })
                  })
                  .catch((error) => {
                  });

                promises.push(promise);
              }
            });

            // Wait for all promises to resolve
            Promise.all(promises)
              .then(() => {
                addOrRemoveTestUrlsAndValues(selectedComponent, newTestRequestValues, updatedSelectedComponents, prevSelectedComponents, true);

              })
              .catch((error) => {
              });
            hideLoader();

          })
          .catch((err) => {
            hideLoader();
          });
      } else {
        addOrRemoveTestUrlsAndValues(selectedComponent, newTestRequestValues, updatedSelectedComponents, prevSelectedComponents, false);
      }

      setSelectedComponents(updatedSelectedComponents);
      setComponents([...components]);

      return updatedSelectedComponents;
    });

  }


  const handleBlur = (key, componentId) => {
    setTouched((prevTouched) => {
      const updatedTouched = {
        ...prevTouched,
        [modifiedComponentId(componentId)]: {
          ...prevTouched[modifiedComponentId(componentId)],
          [key]: true,
        },
      };
      return updatedTouched;
    });
  };

  const handleInputChange = (event) => {
    const { name, value } = event.target;
    formik.setFieldValue(name, value);

    setTestRequestValues((prevState) => ({
      ...prevState,
      [name]: value,
    }));
  };


  useEffect(() => {
    if (testRequestId) {
      showLoader();
      dispatch(set_header("Update Application"));
      TestRequestAPI.getTestRequestsById(testRequestId).then((res) => {
        formik.values.name = res.name;
        formik.values.description = res.description;
        formik.values.testRequestUrls = res.testRequestUrls;

        let promises = [];
        const prevSelectedComponents = new Map();
        const defaultTrvs = new Map();

        // Create promises to fetch testcase variables and related data
        res.testRequestValues.forEach(trv => {
          const promise = TestcaseVariableAPI.getTestcaseVariablesById(trv.testcaseVariableId)
            .then((testcaseVariableRes) => {
              return TestCaseAPI.getTestCasesById(testcaseVariableRes.testcaseId)
                .then(testCaseRes => ({
                  testcaseVariableRes,
                  testCaseRes
                }));
            })
            .then(({ testcaseVariableRes, testCaseRes }) => {
              return SpecificationAPI.getSpecificationById(testCaseRes.specificationId)
                .then(specificationRes => ({
                  testcaseVariableRes,
                  testCaseRes,
                  specificationRes
                }));
            })
            .then(({ testcaseVariableRes, testCaseRes, specificationRes }) => {
              prevSelectedComponents.set(trv.testcaseVariableId, {
                key: testcaseVariableRes.testcaseVariableKey,
                testcaseVariableId: trv.testcaseVariableId,
                testRequestValueInput: trv.testRequestValueInput || testcaseVariableRes.defaultValue,
                id: trv.id,
                testcaseName: testCaseRes.name,
                specificationName: specificationRes.name,
                testRequestId: res.id
              })


              defaultTrvs.set(trv.testcaseVariableId, {
                key: testcaseVariableRes.testcaseVariableKey,
                testcaseVariableId: trv.testcaseVariableId,
                testRequestValueInput: testcaseVariableRes.defaultValue,
                id: trv.id,
                testcaseName: testCaseRes.name,
                specificationName: specificationRes.name,
                testRequestId: res.id
              });

              const componentId = specificationRes.componentId;
              if (!prevSelectedComponents.hasOwnProperty(componentId)) {
                prevSelectedComponents[componentId] = [];
              }
              if (!defaultTrvs.hasOwnProperty(componentId)) {
                defaultTrvs[componentId] = [];
              }
              prevSelectedComponents[componentId].push(prevSelectedComponents.get(trv.testcaseVariableId));
              defaultTrvs[componentId].push(defaultTrvs.get(trv.testcaseVariableId));
            })
            .catch((error) => {
            });

          promises.push(promise);
        });

        // Wait for all promises to resolve
        Promise.all(promises)
          .then(() => {
            setSelectedComponents(prevSelectedComponents);
            setDefaultSelectedComponents(defaultTrvs);
            formik.values.testRequestValues = Array.from(prevSelectedComponents.values());
          })
          .catch((error) => {
          });
        setMeta(res.meta);
      });
    } else {
      dispatch(set_header("Register Application"));
      showLoader();
    }

    const userInfo = store.getState().userInfoSlice;
    setUserId(userInfo.id);
    const params = {};
    params.state = "component.status.active";
    params.sort = "rank,asc";
    ComponentAPI.getComponents(params)
      .then((res) => {
        setComponents(res.content);
        hideLoader();
      })
      .catch((err) => {
        hideLoader();
      });
  }, []);

  return (
    <div id="registerApplication">
      {components?.length > 0 ? (
        <div id="wrapper">
          <div className="col-lg-9 col-xl-7 col-xxl-5 col-md-11 mx-auto pt-5">
            <div className="form-bg-white">
              <span className="heading-line-up">Test Request Details</span>

              <div className="row">
                <div className="col-12">
                  <div className="custom-input mb-3">
                    <label htmlFor="name" className="form-label">
                      Application Name<span style={{ color: "red" }}>*</span>
                    </label>
                    <input
                      id="name"
                      name="name"
                      type="text"
                      className={`form-control ${formik.touched.name && formik.errors.name
                        ? "is-invalid"
                        : ""
                        }`}
                      placeholder="Application Name"
                      value={formik.values.name}
                      onChange={formik.handleChange}
                      onBlur={formik.handleBlur}
                      autoComplete="off"
                      required
                    />
                    {formik.touched.name && formik.errors.name && (
                      <div className="error-message">{formik.errors.name}</div>
                    )}
                    {formik.values.testRequestUrls.every(
                      (url) => url.componentId === null
                    ) && (
                        <p className="compError">
                          At least one component has to be selected*
                        </p>
                      )}
                  </div>
                </div>
              </div>
              {formik.errors.testRequestUrls ? (
                <div className="error-message">
                  {formik.errors.testRequestUrls}
                </div>
              ) : null}
              {components.map((component, index) => {
                const isChecked = formik.values.testRequestUrls.some(
                  (selectedComponent) =>
                    selectedComponent.componentId === component.id
                );
                component.isSelected = isChecked;
                return (
                  <Fragment key={index}>
                    <div className="row mt-2">
                      <div className="col-12">
                        <div className="field-box d-flex align-items-center">
                          <input
                            id={index}
                            checked={isChecked}
                            type="checkbox"
                            className="field-checkbox component-checkbox"
                            name="component"
                            onChange={(e) => {
                              onComponentSelected(index, e);
                            }}
                            autoComplete="off"
                          />
                          <label
                            htmlFor={index}
                            className="form-label m-0 ms-2 align-middle d-block w-100 cursor-pointer"
                          >
                            {component.name}
                          </label>
                        </div>
                      </div>
                    </div>
                    <div className={component.isSelected ? "form-bg-white mt-3" : ""}>
                      {formik.values.testRequestUrls.map((url, index) => {
                        return (
                          <Fragment key={index}>
                            {url.componentId == component.id ? (
                              <div className="">
                                <span className="heading-line-up font-size-16 bg-white">
                                  {component.name} Details
                                </span>
                                <div className="row">
                                  <div className="col-12">
                                    {" "}
                                    <label
                                      htmlFor="username"
                                      className="form-label"
                                    >
                                      {" "}
                                      Credentials
                                      <span style={{ color: "red" }}>*</span>
                                      <Popover
                                        placement="topLeft"
                                        title={
                                          <div
                                            style={{
                                              maxWidth: "450px",
                                              fontWeight: "normal",
                                            }}
                                          >
                                            <p>
                                              Please provide the username and
                                              password that testers will use to
                                              log in to your application/website.
                                            </p>
                                            <p>
                                              {" "}
                                              These credentials will also be
                                              utilized by the system to execute
                                              test scripts for testing purposes.
                                            </p>
                                          </div>
                                        }
                                      >
                                        <InfoCircleOutlined
                                          style={{
                                            marginLeft: "0.5rem",
                                            marginTop: "0.7rem",
                                          }}
                                        />
                                      </Popover>
                                    </label>
                                  </div>
                                  <div className="col-sm-6 col-12">
                                    <div className="custom-input mb-3">
                                      <input
                                        id={
                                          "testRequestUrls[" +
                                          index +
                                          "].username"
                                        }
                                        name={
                                          "testRequestUrls[" +
                                          index +
                                          "].username"
                                        }
                                        type="text"
                                        className={`form-control ${touched?.[
                                          modifiedComponentId(url.componentId)
                                        ]?.username &&
                                          formik.errors[
                                          "testRequestUrls[" +
                                          modifiedComponentId(
                                            url.componentId
                                          ) +
                                          "].username"
                                          ]
                                          ? "is-invalid"
                                          : ""
                                          }`}
                                        placeholder="Username"
                                        value={
                                          formik.values.testRequestUrls[index]
                                            .username
                                        }
                                        onChange={formik.handleChange}
                                        onBlur={() =>
                                          handleBlur("username", url.componentId)
                                        }
                                        autoComplete="off"
                                      />
                                      {touched?.[
                                        modifiedComponentId(url.componentId)
                                      ]?.username &&
                                        formik.errors[
                                        "testRequestUrls[" +
                                        modifiedComponentId(url.componentId) +
                                        "].username"
                                        ] && (
                                          <div className="error-message">
                                            {
                                              formik.errors[
                                              "testRequestUrls[" +
                                              modifiedComponentId(
                                                url.componentId
                                              ) +
                                              "].username"
                                              ]
                                            }
                                          </div>
                                        )}
                                    </div>
                                  </div>
                                  <div className=" custom-input col-sm-6 col-12">
                                    <div className=" input-group position-relative z-0">
                                      <input
                                        id={
                                          "testRequestUrls[" +
                                          index +
                                          "].password"
                                        }
                                        name={
                                          "testRequestUrls[" +
                                          index +
                                          "].password"
                                        }
                                        type={showPassword ? "text" : "password"}
                                        className={`form-control ${touched?.[
                                          modifiedComponentId(url.componentId)
                                        ]?.password &&
                                          formik.errors[
                                          "testRequestUrls[" +
                                          modifiedComponentId(
                                            url.componentId
                                          ) +
                                          "].password"
                                          ]
                                          ? "is-invalid"
                                          : ""
                                          }`}
                                        placeholder="Password"
                                        value={
                                          formik.values.testRequestUrls[index]
                                            .password
                                        }
                                        onChange={formik.handleChange}
                                        onBlur={() =>
                                          handleBlur("password", url.componentId)
                                        }
                                        autoComplete="off"
                                      />
                                      {!(
                                        touched?.[
                                          modifiedComponentId(url.componentId)
                                        ]?.password &&
                                        formik.errors[
                                        "testRequestUrls[" +
                                        modifiedComponentId(url.componentId) +
                                        "].password"
                                        ]
                                      ) && (
                                          <button
                                            id="#RegisterApplication-showPassword"
                                            className="btn btn-outline-secondary color"
                                            type="button"
                                            onClick={() => {
                                              setShowPassword(!showPassword);
                                            }}
                                          >
                                            <i
                                              className={`bi ${showPassword
                                                ? "bi-eye-slash"
                                                : "bi-eye"
                                                }`}
                                            ></i>
                                          </button>
                                        )}
                                    </div>
                                    <div>
                                      {touched?.[
                                        modifiedComponentId(url.componentId)
                                      ]?.password &&
                                        formik.errors[
                                        "testRequestUrls[" +
                                        modifiedComponentId(url.componentId) +
                                        "].password"
                                        ] && (
                                          <div className="error-message">
                                            {
                                              formik.errors[
                                              "testRequestUrls[" +
                                              url.componentId.replace(
                                                /\./g,
                                                ""
                                              ) +
                                              "].password"
                                              ]
                                            }
                                          </div>
                                        )}
                                    </div>
                                  </div>
                                </div>

                                <div className="row">
                                  <div className="col-12 ">
                                    <div className="custom-input">
                                      <label
                                        htmlFor="fhirApiBaseUrl"
                                        className="form-label"
                                      >
                                        Website/UI URL:{" "}
                                        <Popover
                                          placement="topLeft"
                                          title={
                                            <div
                                              style={{
                                                maxWidth: "450px",
                                                fontWeight: "normal",
                                              }}
                                            >
                                              {" "}
                                              Please provide link to your
                                              application/ website. Make sure the
                                              URL is accurate and includes the
                                              correct protocol (e.g., http:// or
                                              https://).
                                            </div>
                                          }
                                        >
                                          <InfoCircleOutlined
                                            style={{
                                              marginLeft: "0.5rem",
                                              marginTop: "0.7rem",
                                            }}
                                          />
                                        </Popover>
                                      </label>
                                      <input
                                        id={
                                          "testRequestUrls[" +
                                          index +
                                          "].websiteUIBaseUrl"
                                        }
                                        name={
                                          "testRequestUrls[" +
                                          index +
                                          "].websiteUIBaseUrl"
                                        }
                                        type="text"
                                        className={`form-control ${touched?.[
                                          modifiedComponentId(url.componentId)
                                        ]?.websiteUIBaseUrl &&
                                          formik.errors[
                                          "testRequestUrls[" +
                                          modifiedComponentId(
                                            url.componentId
                                          ) +
                                          "].websiteUIBaseUrl"
                                          ]
                                          ? "is-invalid"
                                          : ""
                                          }`}
                                        placeholder="../website-ui-base-url/"
                                        value={
                                          formik.values.testRequestUrls[index]
                                            .websiteUIBaseUrl
                                        }
                                        onChange={formik.handleChange}
                                        onBlur={() =>
                                          handleBlur(
                                            "websiteUIBaseUrl",
                                            url.componentId
                                          )
                                        }
                                        autoComplete="off"
                                      />
                                    </div>
                                    {touched?.[
                                      modifiedComponentId(url.componentId)
                                    ]?.fhirApiBaseUrl &&
                                      formik.errors[
                                      "testRequestUrls[" +
                                      modifiedComponentId(url.componentId) +
                                      "].websiteUIBaseUrl"
                                      ] && (
                                        <div className="error-message">
                                          {
                                            formik.errors[
                                            "testRequestUrls[" +
                                            modifiedComponentId(
                                              url.componentId
                                            ) +
                                            "].websiteUIBaseUrl"
                                            ]
                                          }
                                        </div>
                                      )}
                                  </div>
                                  <div className="col-12 mt-3">
                                    <div className="custom-input">
                                      <label
                                        htmlFor="fhirApiBaseUrl"
                                        className="form-label"
                                      >
                                        FHIR API Base URL:{" "}
                                        <span style={{ color: "red" }}>*</span>
                                        <Popover
                                          placement="topLeft"
                                          title={
                                            <div
                                              style={{
                                                maxWidth: "450px",
                                                fontWeight: "normal",
                                              }}
                                            >
                                              {" "}
                                              Please provide the base URL of the
                                              FHIR API endpoint. This URL will be
                                              used to execute testing on the
                                              specified API. Make sure to include
                                              the correct protocol (e.g., http://
                                              or https://) and endpoint path.
                                            </div>
                                          }
                                        >
                                          <InfoCircleOutlined
                                            style={{
                                              marginLeft: "0.5rem",
                                              marginTop: "0.7rem",
                                            }}
                                          />
                                        </Popover>
                                      </label>
                                      <input
                                        id={
                                          "testRequestUrls[" +
                                          index +
                                          "].fhirApiBaseUrl"
                                        }
                                        name={
                                          "testRequestUrls[" +
                                          index +
                                          "].fhirApiBaseUrl"
                                        }
                                        type="text"
                                        className={`form-control ${touched?.[
                                          modifiedComponentId(url.componentId)
                                        ]?.fhirApiBaseUrl &&
                                          formik.errors[
                                          "testRequestUrls[" +
                                          modifiedComponentId(
                                            url.componentId
                                          ) +
                                          "].fhirApiBaseUrl"
                                          ]
                                          ? "is-invalid"
                                          : ""
                                          }`}
                                        placeholder="../base-url/"
                                        value={
                                          formik.values.testRequestUrls[index]
                                            .fhirApiBaseUrl
                                        }
                                        onChange={formik.handleChange}
                                        onBlur={() =>
                                          handleBlur(
                                            "fhirApiBaseUrl",
                                            url.componentId
                                          )
                                        }
                                        autoComplete="off"
                                      />
                                    </div>
                                    {touched?.[
                                      modifiedComponentId(url.componentId)
                                    ]?.fhirApiBaseUrl &&
                                      formik.errors[
                                      "testRequestUrls[" +
                                      modifiedComponentId(url.componentId) +
                                      "].fhirApiBaseUrl"
                                      ] && (
                                        <div className="error-message">
                                          {
                                            formik.errors[
                                            "testRequestUrls[" +
                                            modifiedComponentId(
                                              url.componentId
                                            ) +
                                            "].fhirApiBaseUrl"
                                            ]
                                          }
                                        </div>
                                      )}
                                  </div>
                                </div>
                              </div>
                            ) : null}
                          </Fragment>
                        );
                      })}
                      {
                        component.isSelected === true && selectedComponents[component.id]?.length > 0 && (
                          <div className="custom-param">
                            <label>Custom Parameters</label>
                          </div>
                        )
                      }
                      {
                        formik.values.testRequestValues
                          ?.map((testRequestValue, originalIndex) =>
                            selectedComponents[component.id]?.some(
                              selectedValue => selectedValue.testcaseVariableId === testRequestValue.testcaseVariableId
                            ) ? { ...testRequestValue, originalIndex } : null
                          )
                          .filter(testRequestValue => testRequestValue !== null)
                          .map((testRequestValueWithIndex) => (
                            <div className="row ">
                              <div className="col-12 ">
                                <div className="custom-input mt-3">
                                  <label
                                    htmlFor={testRequestValueWithIndex.key}
                                    className="form-label"
                                  >
                                    {testRequestValueWithIndex.key}:{" "}
                                    <span style={{ color: "red" }}>*</span>
                                    <Popover
                                      placement="topLeft"
                                      title={
                                        <div
                                          style={{
                                            maxWidth: "450px",
                                            fontWeight: "normal",
                                          }}
                                        >
                                          {" "}
                                          Please provide the value for testcase {testRequestValueWithIndex.testcaseName} of specification {testRequestValueWithIndex.specificationName}
                                        </div>
                                      }
                                    >
                                      <InfoCircleOutlined
                                        style={{
                                          marginLeft: "0.5rem",
                                          marginTop: "0.7rem",
                                        }}
                                      />
                                    </Popover>
                                  </label>
                                  <div className="row">
                                    <div className="col-11">
                                      <input
                                        id={
                                          "testRequestValues[" +
                                          testRequestValueWithIndex.originalIndex +
                                          "].testRequestValueInput"
                                        }
                                        name={
                                          "testRequestValues[" +
                                          testRequestValueWithIndex.originalIndex +
                                          "].testRequestValueInput"
                                        }
                                        type="text"
                                        className={`form-control ${touched?.[
                                          modifiedComponentId(component.id)
                                        ]?.[testRequestValueWithIndex.key] &&
                                          formik.errors[
                                          "testRequestValues[" +
                                          testRequestValueWithIndex.originalIndex +
                                          "].testRequestValueInput"
                                          ]
                                          ? "is-invalid"
                                          : ""
                                          }`}
                                        placeholder={
                                          testRequestValueWithIndex.key
                                        }
                                        value={
                                          formik.values.testRequestValues[testRequestValueWithIndex.originalIndex]
                                            .testRequestValueInput
                                        }
                                        onChange={handleInputChange}
                                        onBlur={() => {
                                          handleBlur(
                                            testRequestValueWithIndex.key,
                                            component.id
                                          )
                                        }
                                        }
                                        autoComplete="off"
                                      />
                                      {
                                        touched?.[
                                        modifiedComponentId(component.id)
                                        ]?.[testRequestValueWithIndex.key] &&
                                        formik.errors[
                                        "testRequestValues[" +
                                        testRequestValueWithIndex.originalIndex +
                                        "].testRequestValueInput"
                                        ] && (
                                          <div className="error-message">
                                            {
                                              formik.errors[
                                              "testRequestValues[" +
                                              testRequestValueWithIndex.originalIndex
                                              +
                                              "].testRequestValueInput"
                                              ]
                                            }
                                          </div>
                                        )
                                      }
                                    </div>
                                    <div className="col-1 refresh-icon" onClick={() => {
                                      defaultSelectedComponents[component.id]?.forEach((defaultValue, index) => {
                                        if (defaultValue.key === testRequestValueWithIndex.key) {
                                          formik.setFieldValue(`testRequestValues[${testRequestValueWithIndex.originalIndex}].testRequestValueInput`, defaultValue.testRequestValueInput);
                                        }
                                      })
                                    }}>
                                      <i className="bi bi-arrow-repeat"></i>
                                    </div>
                                  </div>
                                </div>
                              </div>
                            </div>
                          ))}

                      {
                        component.isSelected === true && selectedComponents[component.id]?.length > 0 && (

                          <div className="my-3 cst-btn-group margin mb-3">
                            <button
                              id="registerApplication-reset"
                              type="button"
                              className="btn cst-btn-default"
                              onClick={() => {
                                defaultSelectedComponents[component.id]?.forEach((defaultValue) => {
                                  const matchIndex = formik.values.testRequestValues.findIndex(
                                    (testRequestValue) => testRequestValue.testcaseVariableId === defaultValue.testcaseVariableId
                                  );

                                  if (matchIndex !== -1) {
                                    formik.setFieldValue(`testRequestValues[${matchIndex}].testRequestValueInput`, defaultValue.testRequestValueInput);
                                  }
                                });

                                formik.setTouched({});
                              }}
                            >
                              Reset Custom Paramaters
                            </button>
                          </div>
                        )}
                    </div>
                  </Fragment>
                );
              })}

            </div>
            <div className="text-end">
              <button
                className="btn btn-primary btn-white mx-2"
                id="#RegisterApplication-cancel"
                onClick={() => {
                  navigate("/testing-requests");
                }}
              >
                Cancel
              </button>
              <button
                id="#RegisterApplication-handleSubnit"
                disabled={!(formik.isValid && formik.dirty)}
                type="button"
                onClick={formik.handleSubmit}
                className="btn btn-primary btn-blue"
              >
                {testRequestId ? "Update" : "Submit"}
              </button>
            </div>
          </div>
        </div>
      ) : (
        <div id="wrapper">
          <Empty description="No Components Available for Testing" width="400" className="py-5" imageStyle={{
            height: 200, // Adjust the height of the image
          }} />
        </div>
      )}
    </div>
  );
};

export default RegisterApplication;
