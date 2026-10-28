 
import axios from 'axios';
import { API_BASE_URL } from '../config/api';

// Base URL for the API
const API_URL = `${API_BASE_URL}/api/bids`;
const URL = `${API_BASE_URL}/api/bids/createBid`;
 
// 出价
export const  createBid= async (auctionId, userId, bidAmount) => {
  try {
    const params = new URLSearchParams({
        auctionId: auctionId.toString(),
        userId: userId.toString(),
        bidAmount: bidAmount.toString() 
    });
    // 发送 POST 请求
    const response = await axios.post(URL, null, { params });
    return response;
  } catch (error) {
    console.error('Error:', error);
    throw error;
  }
};
// 获取所有出价2记录
export const getAllBids = async() =>  {
  try {
    const response = await axios.get(`${API_URL}/getAllBid`);
    return response.data;
  } catch (error) {
    console.log(error);
  }
};
 
// 根据ID获取出价记录
export const getBidById = async(id)=> {
  try {
    const response = await axios.get(`${API_URL}/getBid/${id}`);
    return response.data;
  } catch (error) {
    if (error.response && error.response.status === 404) {
      return null; // Bid not found
    }
    console.log(error);
  }
};
 
// 删除记录
export const deleteBid = async(id)=> {
  try {
    const response = await axios.delete(`${API_URL}/deleteBid/${id}`);
    return response.status === 200; // Return true if deletion was successful
  } catch (error) {
    if (error.response && error.response.status === 404) {
      return false; // Bid not found
    }
    console.log(error);
  }
};
 
// 根据auction ID获取记录
export const getBidsByAuction = async(auctionId)=> {
  try {
    const response = await axios.get(`${API_URL}/getForAuction/${auctionId}`);
    return response.data;
  } catch (error) {
    console.log(error);
  }
};
 
// 根据用户id获取记录
export const getBidsByUser= async(userId)=> {
  try {
    const response = await axios.get(`${API_URL}/getUserBid/${userId}`);
    return response.data;
  } catch (error) {
    console.log(error);
  }
};
