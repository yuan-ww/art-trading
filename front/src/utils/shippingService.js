import axios from 'axios';
import { API_BASE_URL } from '../config/api';

const SHIPPING_API_URL = `${API_BASE_URL}/api/shipping`;

export const getShippingByOrderId = async (orderId) => {
  try {
        const response = await axios.get(`${SHIPPING_API_URL}/getShippingByOrderId/${orderId}`);
        return response.data;
    } catch (error) {
        console.error("Error fetching shipping details:", error);
        throw error;
    }
};
export const getAllShipping = async () => {
    try {
      const response = await axios.get(`${SHIPPING_API_URL}/getAllShipping`);
      return response.data; 
    } catch (error) {
      console.error('获取物流信息失败:', error);
      throw error;
    }
  };
export const createShipping = async (shippingData) => {
  const storageKey = `shipment:${shippingData.orderId}:${shippingData.carrier}:${shippingData.trackingNumber}`;
  const requestKey = sessionStorage.getItem(storageKey) || crypto.randomUUID();
  sessionStorage.setItem(storageKey, requestKey);
  try {
        const response = await axios.post(`${SHIPPING_API_URL}/createShipping`, shippingData, {
          headers: { 'Idempotency-Key': requestKey }
        });
        sessionStorage.removeItem(storageKey);
        return response.data;
    } catch (error) {
        console.error("Error creating shipping:", error);
        throw error;
    }
};

// 更新物流信息
export const updateShipping = async (shippingId, shippingDetails) => {
  try {
        const response = await axios.put(`${SHIPPING_API_URL}/updateShipping/${shippingId}`, shippingDetails);
        return response.data;
    } catch (error) {
        console.error("Error updating shipping:", error);
        throw error;
    }
};
export const updateShippingStatus = async (shippingId, newStatus) => {
    try {
        const response = await axios.put(`${SHIPPING_API_URL}/updateStatus`, null, {
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
      const response = await axios.delete(`${SHIPPING_API_URL}/deleteShipping/${shippingId}`);
      return response.data; 
    } catch (error) {
      console.error('删除物流信息失败:', error);
      throw error; 
    }
  };
