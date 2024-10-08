import React, { useEffect, useState } from "react";
import { Modal, notification } from "antd";
import { UserAPI } from "../../api/UserAPI";
import { store } from "../../store/store";
import { useDispatch } from "react-redux";
import { userinfo_success } from "../../reducers/UserInfoReducer";

const CompanyModal = () => {
  const [companyName, setCompanyName] = useState("");
  const [userInfo, setUserInfo] = useState();
  const [userDetails, setUserDetails] = useState();
  const dispatch = useDispatch();
  const fetchUser = () => {
    UserAPI.viewUser()
      .then((res) => {
        setUserDetails(res);
        setCompanyName(res.companyName || "");
      })
      .catch((error) => {});
  };

  useEffect(() => {
    const userInfo = store.getState().userInfoSlice;
    setUserInfo(userInfo);
    setCompanyName(userInfo.companyName || "");
    fetchUser();
  }, []);

  const handleSubmit = (event) => {
    event.preventDefault(); 

    const body = {
      ...userDetails,
      companyName,
    };

    UserAPI.UpdateExistingUser(body)
      .then((response) => {
        setUserInfo((prevUserInfo) => ({
          ...prevUserInfo,
          companyName: companyName,
        }));
        dispatch(userinfo_success(body));
        notification.success({
          className: "notificationSuccess",
          placement: "top",
          message: `Company name has been updated successfully!`,
        });
      })
      .catch((error) => {});
  };

  return (
    <div>
      <Modal  cancelButtonProps={{id:"addCompanyName-cancelButton"}}
          okButtonProps={{id:"addCompanyName-okButton"}} open={!userInfo?.companyName && userInfo?.roleIds[0] === "role.assessee"} footer={null} closable={false}>
        <div id="updateCompanyName">
          <h6>
            Please add your company name first to dive into the application.
          </h6>
          <form onSubmit={handleSubmit}>
            <div className="custom-input mb-3 mt-4">
              <input
                type="text"
                className="form-control"
                id="CompnayModal-companyName"
                placeholder="Company Name"
                value={companyName}
                onChange={(e) => setCompanyName(e.target.value)}
              />
            </div>
            <div className="my-4 text-end">
              <button
                type="submit"
                style={{ marginLeft: "1rem" }}
                className="btn btn-primary btn-blue btn-submit py-1"
                disabled={!companyName}
              >
                Submit
              </button>
            </div>
          </form>
        </div>
      </Modal>
    </div>
  );
};

export default CompanyModal;
