import api from "./configs/axiosConfigs";
import { paramSerialize } from "../utils/utils";
export const ComponentAPI = {
  createComponent: function (data) {
    return api
      .request({
        url: `/component`,
        method: "POST",
        data,
      })
      .then((response) => response.data);
  },

  updateComponent: function (data) {
    return api
      .request({
        url: `/component`,
        method: "PUT",
        data,
      })
      .then((response) => response.data);
  },

  getComponents: async function (params) {
   
      
    const response = await api.request({
      url: `/component/search`,
      method: "GET",
      params,
      paramsSerializer: (params) => {
        return paramSerialize(params);
      },
    });
    return response.data;
    
  },
  getComponentById: async function (componentId) {
   
    const response = await api.request({
      url: `/component/${componentId}`,
      method: "GET",
    });
    return response.data;
    
  },
  changeState: async function (componentId, changeState) {
   
    const response = await api.request({
      url: `/component/state/${componentId}/${changeState}`,
      method: "PATCH",
    });
    return response;
    
  },
  changeRank: async function (componentId, changeRank) {
   
    const response = await api.request({
      url: `/component/rank/${componentId}/${changeRank}`,
      method: "PATCH",
    });
    return response;
    
  },
  validateConfiguration: function(refObjectUri, refId){
    return api.request({
      url: `/component/configuration/validate`,
      method: "GET"
    });
  }
};
