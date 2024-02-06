import { Navigate, createBrowserRouter } from "react-router-dom";
import Login from "../components/CommonFiles/Login/Login";
import Dashboard from "../components/CommonFiles/Dashboard";
import { useSelector } from "react-redux";
import WaitingPage from "../components/UserFiles/WaitingPage";
import SignUp from "../components/UserFiles/SignUp";
import CongratulationsPage from "../components/UserFiles/CongratulationsPage";
import ApplicationReport from "../components/AdminFiles/ApplicationReport/ApplicationReport";
import ChooseTest from "../components/AdminFiles/ChooseTest/ChooseTest";
import Landing from "../components/CommonFiles/Landing";
import FunctionalTesting from "../components/AdminFiles/ManualTesting/ManualTesting";
import AutomatedTesting from "../components/AdminFiles/AutomatedTesting/AutomatedTesting";
import GoogleAuth from "../components/UserFiles/GoogleAuth";
import ForgotPassword from "../components/CommonFiles/ForgotPassword";
import UpdatePassword from "../components/UserFiles/UpdatePassword";
import Assessee from "../components/AdminFiles/Assessee/Assessee";
import Applications from "../components/AdminFiles/Applications/Applications";
import TestingRequests from "../components/CommonFiles/TestingRequests/TestingRequests";
import RegisterApplication from "../components/CommonFiles/RegisterApplication/RegisterApplication";
import EmailVerified from "../components/UserFiles/EmailVerified";
import UpdateAdminUser from "../components/AdminFiles/UpdateAdminUser/UpdateAdminUser";
import AdminUsers from "../components/AdminFiles/AdminUsers/AdminUsers";
import AddAdminUser from "../components/AdminFiles/AddAdminUsers/AddAdminUser";
import ManualTesting from "../components/AdminFiles/ManualTesting/ManualTesting";
const PrivateRoute = () => {
	const token = useSelector((state) => state.authSlice.access_token);

	const isAuthenticated = !!token;
	return isAuthenticated ? <Dashboard /> : <Navigate to="/login" />;
};

const routes = createBrowserRouter([
	{ path: "/waiting", element: <WaitingPage /> },
	{ path: "/login", element: <Login /> },
	{ path: "/SignUp", element: <SignUp /> },
	{ path: "/CongratulationsPage/:email", element: <CongratulationsPage /> },
	{ path: "application-report/:testRequestId", element: <ApplicationReport /> },
	{ path: "/google/success", element: <GoogleAuth /> },
	{ path: "/forgotpassword", element: <ForgotPassword /> },
	{ path: "/reset/cred/:base64UserEmail/:base64TokenId", element: <UpdatePassword /> },
	{path:"/email/verify/:base64UserEmail/:base64TokenId",element:<EmailVerified/>},
	{
		path: "/dashboard",
		element: <PrivateRoute />,
		children: [
			{ path: "", element: <Landing /> },
			{ path: "testing-requests", element: <TestingRequests /> },
			{ path: "applications", element: <Applications /> },
			{ path: "choose-test/:testRequestId", element: <ChooseTest />, },
			{ path: "manual-testing/:testRequestId", element: <ManualTesting /> },
			{ path: "automated-testing/:testRequestId", element: <AutomatedTesting /> },
			{ path: "assessee", element: <Assessee /> },
			{ path: "register-application", element: <RegisterApplication />},
      { path: "admin-users", element: <AdminUsers /> },
      { path: "admin-users/add-admin-user", element: <AddAdminUser /> },
      { path: "admin-users/update-admin-user", element: <UpdateAdminUser /> },
      // { path: "admin-users/update-admin-user", element: <AdminUsers /> },
		],
	},
	{ path: "/", element: <Navigate to="/login" /> },
]);

export default routes;
