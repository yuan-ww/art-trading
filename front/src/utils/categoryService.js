import axios from 'axios';
import { API_BASE_URL } from '../config/api';

// 设置基础URL
const API_URL = `${API_BASE_URL}/api/categories`;

// 获取所有分类
export const fetchcategories = () => {
    return axios.get(`${API_URL}/getAllCategories`);
  };
// 根据ID获取单个分类
export function getCategoryById(id) {
  return axios.get(`${API_URL}/${id}/getCategoryById`);
}
//添加分类
export const addCategory = async(CatecoryData) => {
    try {
      const response = await axios.post(`${API_URL}/createCategory`, {
        name:CatecoryData.name,
        description: CatecoryData.description,
      });
      return response.data;
    } catch (error) {
      console.error('更新失败:', error);
      return { success: false, message: '更新失败，请稍后重试' };
  }
  };
  // 检查分类是否已存在
  export const checkCategorynameAvailability = async (name,Id) => {
    try {
        const response = await axios.get(`${API_URL}/checkCategoryName`, { 
          params: { // 发送查询参数
            name,
            Id
          }
        });
        return response.data; // 返回检查结果
    } catch (error) {
        console.error('检查用户名时出错:', error);
        throw error;
    }
  };
// 更新分类（通过 URL 传递 id，请求体不包含 id）
export const updateCategoryInfo = async (CatecoryId, CatecoryInfo) => {
    try {
      const response = await axios.put(`${API_URL}/${CatecoryId}/updateCategory`, CatecoryInfo);
      return response.data; // 返回更新后的用户信息
    } catch (error) {
      console.error('更新用户信息失败:', error);
      throw error; // 处理错误
    }
  };

// 删除分类
export const batchDeleteCategories = async(CatecoryIds) => {
    try{
      const response = await axios.delete(`${API_URL}/deleteCategory`, {
        data: CatecoryIds,
      });
      return response;
      
    }
    catch (error) {
      console.error('删除失败:', error);
      return { success: false, message: '删除失败，请稍后重试' };
  }
  };
