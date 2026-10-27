
import axios from 'axios';


const API_URL = 'http://localhost:10202/api'; // 后端地址进行

// 获取用户信息
export const getUserById = async (userId) => {
  try {
    const response = await axios.get(`${API_URL}/user/${userId}`);
    return response.data; // 返回用户信息
  } catch (error) {
    console.error('获取用户信息失败:', error);
    throw error; // 处理错误
  }
};
//更新信息
export const updateUserInfo = async (userId, userInfo) => {
  try {
    const response = await axios.put(`${API_URL}/user/${userId}/updateUser`, userInfo);
    return response.data; // 返回更新后的用户信息
  } catch (error) {
    console.error('更新用户信息失败:', error);
    throw error; // 处理错误
  }
};
export const updateBalance = async (userId, amount) => {
  try {
    const response = await axios.post(`${API_URL}/user/${userId}/updateBalance`, {
      amount: amount  // 将amount包装成一个对象
    });
    return response.data; 
  } catch (error) {
    console.error('更新用户信息失败:', error);
    throw error; // 处理错误
  }
};
//更新头像
export const uploadAvatar = async (userId, file) => {
  const formData = new FormData();
  formData.append('file', file);
  try {
    const response = await axios.post(`${API_URL}/user/${userId}/uploadAvatar`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    });
    return response.data; // 返回上传结果
  } catch (error) {
    console.error('上传头像失败:', error);
    throw error; // 处理错误
  }
};
// 检查用户名是否已存在
export const checkUsernameAvailability = async (username,userId) => {
  try {
      const response = await axios.get(`${API_URL}/user/checkUsername`, { 
        params: { // 发送查询参数
          username,
          userId
        }
      });
      return response.data; // 返回检查结果
  } catch (error) {
      console.error('检查用户名时出错:', error);
      throw error;
  }
};
export const checkEmailAvailability = async (email,userId) => {
  try {
      const response = await axios.get(`${API_URL}/user/checkEmail`, { 
        params:{
            email,
            userId
        }
       });
      return response.data; // 返回检查结果
  } catch (error) {
      console.error('检查用户名时出错:', error);
      throw error;
  }
};
//获取实名信息
export const getRealNameInfo = async (userId) => {
  try {
      const response = await axios.get(`${API_URL}/user/${userId}/getrealname`);
      return response.data; // 返回实名信息
  } catch (error) {
      console.error('获取实名信息失败:', error);
      throw error; // 抛出错误以便后续处理
  }
};
export const recharge = async (userId, amount) => {
  try {
    // 向后端发送请求
    const response = await axios.post(`${API_URL}/user/recharge`, {
      id: userId,    // 用户ID
      amount: amount // 充值金额
    }); 
    return response;
  } catch (error) {
    console.error('充值请求失败:', error);
  }
};
// 改密码
export const updatePassword = async (userId, currentPassword, newPassword) => {
  try {
    const response = await axios.post(`${API_URL}/user/changePassword`, {
      id:userId,
      currentPassword:currentPassword,
      newPassword:newPassword
    });
   return response.data;
  } catch (error) {
    console.error('更新失败:', error);
    return { success: false, message: '更新失败，请稍后重试' };
  }
};
//根据角色获取用户
export const fetchUsersByRole = (role) => {
  return axios.get(`${API_URL}/user/getUsersByRole`, {
    params: { role },  // 传递角色参数
  });
};
//添加用户
export const addUser = async(userData) => {
  try {
    const response = await axios.post(`${API_URL}/user/add`, {
      username:userData.username,
      realName: userData.realName,
      password:userData.password,
      email: userData.email,
      phone: userData.phone,
      role:userData.role,
      walletBalance: userData.walletBalance
    });
    console.log(response);
    return response.data;
    
  } catch (error) {
    console.error('更新失败:', error);
    return { success: false, message: '更新失败，请稍后重试' };
}
};
//删除用户
export const batchDeleteUsers = async(userIds) => {
  try{
    const response = await axios.delete(`${API_URL}/user/delete`, {
      data: userIds,
    });
    return response;
    
  }
  catch (error) {
    console.error('删除失败:', error);
    return { success: false, message: '删除失败，请稍后重试' };
}
};