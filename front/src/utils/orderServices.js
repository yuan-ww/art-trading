import axios from 'axios';
import { API_BASE_URL } from '../config/api';

const BASE_URL = `${API_BASE_URL}/api/orders`;
export const getParticipantOrdersPage = async (params) => (await axios.get(`${BASE_URL}/participant-page`, { params })).data;

// 获取所有订单
export const getAllOrders = async () => {
    try {
        const response = await axios.get(`${BASE_URL}/getAllOrders`);
        return response.data;
    } catch (error) {
        console.error('获取订单列表失败:', error);
        throw error;
    }
};

// 根据订单 ID 获取单个订单
export const getOrderById = async (orderId) => {
    try {
        const response = await axios.get(`${BASE_URL}/getOrderById/${orderId}`);
        return response.data;
    } catch (error) {
        console.error('获取单个订单失败:', error);
        throw error;
    }
};
// 根据艺术品 ID 获取单个订单
export const getOrdersByArtworkId = async (artworkId) => {
    try {
        const response = await axios.get(`${BASE_URL}/getOrdersByArtworkId/${artworkId}`);
        return response.data;
    } catch (error) {
        console.error('获取单个订单失败:', error);
        throw error;
    }
};
// 根据买家 ID 获取单个订单
export const getOrdersByBuyerId = async (userId) => {
    try {
        const response = await axios.get(`${BASE_URL}/getOrdersByBuyerId/${userId}`);
        return response.data;
    } catch (error) {
        console.error('获取单个订单失败:', error);
        throw error;
    }
};
// 根据卖家 ID 获取订单
export const getOrdersBySellerId = async (userId) => {
    try {
        const response = await axios.get(`${BASE_URL}/getOrdersBySellerId/${userId}`);
        return response.data;
    } catch (error) {
        console.error('获取单个订单失败:', error);
        throw error;
    }
};
// 创建新订单
export const createOrder = async (orderData) => {
    try {
        const response = await axios.post(`${BASE_URL}/createOrder`, orderData);
        return response.data;
    } catch (error) {
        console.error('创建订单失败:', error);
        throw error;
    }
};

// 更新订单信息
export const updateOrder = async (orderId, orderData) => {
    try {
        const response = await axios.put(`${BASE_URL}/updateOrder/${orderId}`, orderData);
        return response.data;
    } catch (error) {
        console.error('更新订单失败:', error);
        throw error;
    }
};
export const handleExpiredOrder = async (orderId) => {
    try {
        const response = await axios.put(`${BASE_URL}/handleExpiredOrder/${orderId}`);
        return response.data;
    } catch (error) {
        console.error('更新订单失败:', error);
        throw error;
    }
};
export const updateOrderStatus = async (userId, artworkId, status) => {
    const storageKey = `payment:${userId}:${artworkId}:${status}`;
    const requestKey = sessionStorage.getItem(storageKey) || crypto.randomUUID();
    sessionStorage.setItem(storageKey, requestKey);
    try {
        const response = await axios.put(`${BASE_URL}/updateOrderStatus`, null, {
          params: {
              userId,
              artworkId,
              status,
            },
            headers: { 'Idempotency-Key': requestKey },
          });
        sessionStorage.removeItem(storageKey);
        return response.data;
    } catch (error) {
        // 记录错误并抛出
        console.error(`Error registering for auction:`, error);
        throw error;
    }
};
// 删除订单
export const deleteOrder = async (orderId) => {
    try {
        await axios.delete(`${BASE_URL}/deleteOrder/${orderId}`);
        return true;
    } catch (error) {
        console.error('删除订单失败:', error);
        throw error;
    }
};
