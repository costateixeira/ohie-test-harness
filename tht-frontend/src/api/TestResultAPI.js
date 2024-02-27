import api from "./configs/axiosConfigs";
import { paramSerialize } from "../utils/utils";
//Api's for Fetching Manual Test Case Questions and The Options.
export const TestResultAPI = {
	getTestCases: async function (trid) {
		try {
			const response = await api.request({
				url: `/testcase-result`,
				method: "GET",
				params: {
					testRequestId: trid,
					manual: true,
					size: "1000",
					sort: "rank",
				},
			});
			// console.log(response);
			return response.data;
		} catch (error) {
			throw error;
		}
	},
	getTestcaseResultStatus: async function (testcaseResultId, params) {
		if (!!params.manual) {
			params.manual = true;
		}
		if (!!params.automated) {
			params.automated = true;
		}
		if (!!params.required) {
			params.required = true;
		}
		if (!!params.recommended) {
			params.recommended = true;
		}
		if (!!params.workflow) {
			params.workflow = true;
		}
		if (!!params.functional) {
			params.functional = true;
		}
		try {
			const response = await api.request({
				url: `/testcase-result/status/${testcaseResultId}`,
				method: "GET",
				params,
			});
			return response.data;
		} catch (error) {
			throw error;
		}
	},
	getQuestions: async function (parentTestcaseResultId, currentPage) {
		try {
			const response = await api.request({
				url: `/testcase-result`,
				method: "GET",
				params: {
					parentTestcaseResultId: parentTestcaseResultId,
					sort: "rank",
					manual: true,
					size: 1,
					page: currentPage,
				},
			});
			return response.data;
		} catch (error) {
			throw error;
		}
	},

	getTestCaseOptions: async function (testcaseId) {
		try {
			const response = await api.request({
				url: "/testcase-option",
				method: "GET",
				params: {
					testcaseId: testcaseId,
				},
			});
			return response.data;
		} catch (error) {
			throw error;
		}
	},
	saveOptions: async function (testcaseResultId, selectedTestcaseOptionIds) {
		try {
			const params = {};
			if(selectedTestcaseOptionIds && selectedTestcaseOptionIds.length > 0){
				params.selectedTestcaseOptionId = selectedTestcaseOptionIds;
			}
			const response = await api.request({
				url: `testcase-result/submit/${testcaseResultId}`,
				method: "PATCH",
				params,
				paramsSerializer: params => {
				  return paramSerialize(params);
				}
			});
			return response.data;
		} catch (error) {
			throw error;
		}
	},
	fetchCasesForProgressBar: async function (params) {
		params.size = 1000;
		try {
			const response = await api.request({
				url: "/testcase-result",
				method: "GET",
				params: params,
			});
			return response;
		} catch (error) {
			throw error;
		}
	},
	startTests: async function (params) {
		try {
			const response = await api.request({
				url: `/test-request/start-testing-process/${params.testRequestId}`,
				method: "PUT",
				params
			});
		} catch (error) {
			throw error;
		}
	},
	getTestCaseResultByTestRequestId: async function (testRequestId, manual, automated) {
		try {
			const params = {
				testRequestId: testRequestId,
				sort: "rank",
				size: 1000,
			};

			if (!!manual) {
				params.manual = true;
			}

			if (!!automated) {
				params.automated = true;
			}

			const response = await api.request({
				url: "/testcase-result",
				method: "GET",
				params: params,
			});

			return response.data;
		} catch (error) {
			throw error;
		}
	},
	getTestCaseResultById: async function(testResultId){
		const response = await api.request({
			url: `/testcase-result/${testResultId}`,
			method: "GET"
		});

		return response.data;
	},
	patchTestCaseResult: async function(testcaseResultId, patchData){
		const response = await api.request({
			url: `/testcase-result/${testcaseResultId}`,
			method: "PATCH",
			headers: {
				"Content-Type":"application/json-patch+json"
			},
			data : patchData
		});
		
		return response.data;
	}


};
