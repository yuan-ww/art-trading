import axios from 'axios';
import { API_BASE_URL } from '../config/api';

const API_URL = `${API_BASE_URL}/api/favorites`;
// 获取用户的收藏列表
export const getFavoritesByUser = async (userId) => {
  try {
    const response = await axios.get(`${API_URL}/getFavoritesByUser/${userId}`);
    return response.data; 
  } catch (error) {
    console.error('获取收藏列表失败:', error);
    throw error;
  }
};
// 添加收藏
export const addFavorite = async (userId, artworkId) => {
  try {
    const response = await axios.post(`${API_URL}/addFavorite`, null, {
      params: { userId, artworkId },
    });
    return response.data;
  } catch (error) {
    console.error('添加收藏失败:', error);
    throw error; 
  }
};
// 移除收藏
export const removeFavorite = async (userId, artworkId) => {
  try {
    const response = await axios.delete(`${API_URL}/removeFavorite`, {
      params: { userId, artworkId }, 
    });
    return response.data; 
  } catch (error) {
    console.error('移除收藏失败:', error);
    throw error; 
  }
};
export const checkIsFavorite = async (userId, artworkId) => {
    try {
      const response = await axios.get(`${API_URL}/isFavorite`, {
        params: { userId, artworkId },
      });
      return response.data;
    } catch (error) {
      console.error('检查收藏失败:', error);
      throw error; 
    }
  };
