import { useEffect } from "react";
import { useDispatch } from "react-redux";
import { login_success } from "../../../reducers/authReducer";
import { useLoader } from "../../loader/LoaderContext";
import { useNavigate } from "react-router-dom";
import { setAuthToken } from "../../../api/configs/axiosConfigs";
import { message, notification } from "antd";
import { UserAPI } from "../../../api/UserAPI";
import { userinfo_success } from "../../../reducers/UserInfoReducer";

export default function GoogleAuth() {
	const { showLoader, hideLoader } = useLoader();
	const dispatch = useDispatch();
	const navigate = useNavigate();

	useEffect(() => {
		showLoader();
		const urlParams = new URLSearchParams(window.location.search);
		let param = urlParams.get("result");
		param = atob(param);
		// setResult(JSON.parse(param));
		const result = JSON.parse(param);
		if (result.access_token) {
			hideLoader();
			dispatch(login_success(result));
			setAuthToken(result.access_token);
			UserAPI.viewUser().then((user) => {
				dispatch(userinfo_success(user));
				navigate("/dashboard");
			});

		}
		else if(result?.isUserCreatedWithOauth){
			hideLoader();
			navigate(`/CongratulationsPage/${result.email}/${result.isUserCreatedWithOauth}`);
		} else if (result.message) {
			hideLoader();
			navigate("/login");
			notification.error({
				className:"notificationError",
				message: `${result.message}`,
				placement:"bottomRight",
			});
		} else {
			hideLoader();
		}
	},[]);
}
