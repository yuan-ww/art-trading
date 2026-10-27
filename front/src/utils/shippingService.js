import axios from 'axios';

const API_BASE_URL = 'http://localhost:10202/api/shipping'; 

export const getShippingByOrderId = async (orderId) => {
  try {
        const response = await axios.get(`${API_BASE_URL}/getShippingByOrderId/${orderId}`);
        return response.data;
    } catch (error) {
        console.error("Error fetching shipping details:", error);
        throw error;
    }
};
export const getAllShipping = async () => {
    try {
      const response = await axios.get(`${API_BASE_URL}/getAllShipping`);
      return response.data; 
    } catch (error) {
      console.error('获取物流信息失败:', error);
      throw error;
    }
  };
export const createShipping = async (shippingData) => {
  try {
        const response = await axios.post(`${API_BASE_URL}/createShipping`, shippingData);
        return response.data;
    } catch (error) {
        console.error("Error creating shipping:", error);
        throw error;
    }
};

// 更新物流信息
export const updateShipping = async (shippingId, shippingDetails) => {
  try {
        const response = await axios.put(`${API_BASE_URL}/updateShipping/${shippingId}`, shippingDetails);
        return response.data;
    } catch (error) {
        console.error("Error updating shipping:", error);
        throw error;
    }
};
export const updateShippingStatus = async (shippingId, newStatus) => {
    try {
        const response = await axios.put(`${API_BASE_URL}/updateStatus`, null, {
            params: {
                shippingId: shippingId,
                shippingStatus: newStatus
            }
        });
        return response.data;
    } catch (error) {
        console.error(`更新物流状态失败:`, error);
        throw error;
    }
};
export const deleteShipping = async (shippingId) => {
    try {
      const response = await axios.delete(`${API_BASE_URL}/deleteShipping/${shippingId}`);
      return response.data; 
    } catch (error) {
      console.error('删除物流信息失败:', error);
      throw error; 
    }
  };