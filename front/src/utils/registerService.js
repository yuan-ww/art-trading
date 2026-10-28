import axios from 'axios';
import { API_BASE_URL } from '../config/api';

const API_URL = `${API_BASE_URL}/api/auctionRegistrations/register`;
const URL = `${API_BASE_URL}/api/auctionRegistrations`;
// 报名
export const registerForAuction = async (auctionId, userId, depositAmount) => {
    try {
        const params = new URLSearchParams({
            auctionId: auctionId.toString(),
            userId: userId.toString(),
            depositAmount: depositAmount.toString() 
        });
        // 发送 POST 请求
        const response = await axios.post(API_URL, null, { params });
        return response.data;
    } catch (error) {
        // 记录错误并抛出
        console.error(`Error registering for auction:`, error);
        throw error;
    }
};
//检查是否已报名
export const checkUserRegistrationStatus = async (auctionId,userId) => {
  try {
    const response = await axios.get(`${URL}/isUserRegistered/${auctionId}/user/${userId}`);
    return response.data;
  } catch (error) {
    console.error('获取信息失败:', error);
    throw error; // 处理错误
  }
};
export const getRegistrationsForUser = async (userId) => {
  try {
    const response = await axios.get(`${URL}/user/${userId}`);
    return response.data;
  } catch (error) {
    console.error('获取信息失败:', error);
    throw error; // 处理错误
  }
};
//获取参加的人数
export const countRegistrationsForAuction = async (auctionId) => {
  try {
    const response = await axios.get(`${URL}/auction/${auctionId}/count`);
    return response.data;
  } catch (error) {
    console.error('获取信息失败:', error);
    throw error; // 处理错误
  }
};
//结算
export const  settleDeposit= async (auctionId) => {
  try {
    const response = await axios.post(`${URL}/settleDeposit/${auctionId}`);
    return response.data;  // 返回结算成功的信息
  } catch (error) {
    console.error('结算拍卖失败:', error.response ? error.response.data : error.message);
    throw error;  // 将错误抛出，供调用者处理
  }
};
