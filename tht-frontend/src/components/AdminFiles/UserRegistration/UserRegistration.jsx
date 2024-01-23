import React, { Fragment, useEffect, useState } from "react";
import sortIcon from "../../../styles/images/sort-icon.png";
import "./userRegistration.scss";
import { UserAPI } from "../../../api/UserAPI";
import { notification } from "antd";
import { Pagination } from "@mui/material";
import { userBadgeClasses } from "../../../constants/user_constants";
const UserRegistration = () => {
  const [availableUsers, setAvailableUsers] = useState([]);
  const [filter, setFilter] = useState("");
  const [sortDirection, setSortDirection] = useState({
    name: "desc",
    email: "desc",
    state: "desc",
    createdAt: "desc",
  });
  const [sortFieldName, setSortFieldName] = useState("createdAt");
  const [currentPage, setCurrentPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const pendingstate = "user.status.approval.pending";

  const fetchUserByState = (
    sortFieldName,
    sortDirection,
    currentPage,
    pageSize
  ) => {
 
    UserAPI.getUserByState(sortFieldName, sortDirection, currentPage, pageSize)
      .then((res) => {
        setAvailableUsers(res.content);
        setTotalPages(res.totalPages);
      })
      .catch((error) => {
        console.log("Error fetching users:", error);
      });
  };

  const handleSort = (newSortFieldName) => {
    setSortFieldName(newSortFieldName);
    const newSortDirection = { ...sortDirection };
    newSortDirection[newSortFieldName] =
      sortDirection[newSortFieldName] === "asc" ? "desc" : "asc";
    setSortDirection(newSortDirection);
    fetchUserByState(
      newSortFieldName,
      newSortDirection[newSortFieldName],
      currentPage,
      pageSize
    );
  };

  const changeState = (userId, state, newState) => {
    UserAPI.changeState(userId, state)
      .then((res) => {
        notification.success({
          description: `Request has been ${newState}`,
          placement: "bottom-left",
        });
        fetchUserByState(
          sortFieldName,
          sortDirection[sortFieldName],
          currentPage,
          pageSize
        );
      })
      .catch((error) => {
        notification.warning({
          description: "Request Change failed.",
          placement: "bottom-left",
        });
        throw error;
      });
  };

  const handleChangePage = (event, newPage) => {
    setCurrentPage(newPage);
    fetchUserByState(
      sortFieldName,
      sortDirection[sortFieldName],
      newPage,
      pageSize
    );
  };

  useEffect(() => {
    fetchUserByState(
      sortFieldName,
      sortDirection[sortFieldName],
      currentPage,
      pageSize
    );
  }, [filter]);
  return (
    <div id="wrapper">
      <div className="col-12">
        <div className="row mb-2 justify-content-between">
          <div className="col-lg-4 col-md-6 col-sm-7 col-xl-3 col-12">
            <div className="d-flex align-items-baseline ">
              <span className="pe-3 text-nowrap">Status :</span>
              <div className="mb-3">
                <select
                  className="form-select custom-select custom-select-sm"
                  aria-label="Default select example"
                  value={filter}
                  onChange={(e) => {
                    setFilter(e.target.value);
                  }}
                >
                  <option value="">All</option>
                  <option value="user.status.active">Accepted</option>
                  <option value="user.status.rejected">Rejected</option>
                  <option value="user.status.approval.pending">Pending</option>
                </select>
              </div>
            </div>
          </div>
        </div>
        <div className="table-responsive">
          <table className=" data-table">
            <thead>
              <tr>
                <th>
                  NAME
                  <a
                    className="ps-1"
                    href="#"
                    onClick={() => handleSort("name")}
                  >
                    <img src={sortIcon} alt="e" />
                  </a>
                </th>
                <th>
                  Email
                  <a
                    className="ps-1"
                    href="# "
                    onClick={() => handleSort("email")}
                  >
                    <img src={sortIcon} alt="e" />
                  </a>
                </th>
                <th>requested date</th>
                <th>
                  Status
                  <a
                    className="ps-1"
                    href="# "
                    onClick={() => handleSort("state")}
                  >
                    <img src={sortIcon} alt="e" />
                  </a>
                </th>
                <th>CHOOSE ACTION</th>
              </tr>
            </thead>
            <tbody>
              {availableUsers.length === 0 ? (
                <tr>
                  <td className="text-center" colSpan="6">
                    There are no user registration requests for this state
                  </td>
                </tr>
              ) : null}
              {availableUsers.map((user) => {
                const formattedDate = new Date(
                  user.meta.createdAt
                ).toLocaleDateString("en-GB", {
                  day: "numeric",
                  month: "short",
                  year: "numeric",
                });
                let currentStatus = "";
                if (user.state === "user.status.verification.pending") {
                  currentStatus = "Email not verified";
                } else if (user.state) {
                  const parts = user.state.split(".");

                  currentStatus = parts[parts.length - 1];
                }
                return (
                  <Fragment key={user.id}>
                    <tr>
                      <td>{user.name}</td>
                      <td>{user.email}</td>
                      <td>{formattedDate}</td>
                      <td>
                        <span className={userBadgeClasses[user.state]}>
                          {currentStatus.toUpperCase()}
                        </span>
                      </td>
                      <td className=" no-wrap">
                        {user.state === pendingstate && (
                          <Fragment>
                            <button
                              type="button"
                              className="btn  btn-sm text-uppercase approval-action-button"
                              onClick={() => {
                                changeState(
                                  user.id,
                                  "user.status.active",
                                  "Approved"
                                );
                              }}
                            >
                              <i
                                className="bi bi-check-circle-fill text-green-50 font-weight-light  font-size-16"
                                style={{
                                  fontWeight: "lighter  !important",
                                }}
                              ></i>{" "}
                              APPROVE
                            </button>
                            <button
                              type="button"
                              className="btn  btn-sm text-uppercase approval-action-button"
                              onClick={() => {
                                changeState(
                                  user.id,
                                  "user.status.rejected",
                                  "Rejected"
                                );
                              }}
                            >
                              <i
                                className="bi bi-x-circle-fill text-red font-size-16l  font-weight-light  font-size-16"
                                style={{
                                  fontWeight: "lighter  !important",
                                }}
                              ></i>{" "}
                              REJECT
                            </button>
                          </Fragment>
                        )}
                      </td>
                    </tr>
                  </Fragment>
                );
              })}
            </tbody>
          </table>
        </div>
      </div>
      <Pagination
        className="pagination-ui"
        count={totalPages}
        page={currentPage}
        onChange={handleChangePage}
        variant="outlined"
        shape="rounded"
      />
    </div>
  );
};

export default UserRegistration;
