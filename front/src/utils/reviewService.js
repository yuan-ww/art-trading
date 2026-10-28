import axios from 'axios';
import { API_BASE_URL } from '../config/api';

const REVIEWS_API_URL = `${API_BASE_URL}/api/reviews`;
export const getAllReviews = async () => {
    try {
        const response = await axios.get(REVIEWS_API_URL);
        return response.data;
    } catch (error) {
        console.error('获取所有评论失败:', error);
        throw error;
    }
};

export const getReviewById = async (id) => {
    try {
        const response = await axios.get(`${REVIEWS_API_URL}/getReviewById/${id}`);
        return response.data;
    } catch (error) {
        console.error(`获取评论 (ID: ${id}) 失败:`, error);
        throw error;
    }
};

export const getReviewsByUserId = async (userId) => {
    try {
        const response = await axios.get(`${REVIEWS_API_URL}/getReviewsByUserId/${userId}`);
        return response.data;
    } catch (error) {
        console.error(`获取用户 (ID: ${userId}) 的评论失败:`, error);
        throw error;
    }
};

export const getReviewsByOrderId = async (orderId) => {
    try {
        const response = await axios.get(`${REVIEWS_API_URL}/getReviewsByOrderId/${orderId}`);
        return response.data;
    } catch (error) {
        console.error(`获取订单 (ID: ${orderId}) 的评论失败:`, error);
        throw error;
    }
};

export const createReview = async (reviewData) => {
    try {
        const response = await axios.post(`${REVIEWS_API_URL}/createReview`, reviewData);
        return response.data;
    } catch (error) {
        console.error('创建评论失败:', error);
        throw error;
    }
};

export const deleteReview = async (id) => {
    try {
        const response = await axios.delete(`${REVIEWS_API_URL}/deleteReview/${id}`);
        return response.data;
    } catch (error) {
        console.error(`删除评论 (ID: ${id}) 失败:`, error);
        throw error;
    }
};
