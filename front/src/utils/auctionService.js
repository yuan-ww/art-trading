import axios from 'axios';

const API_URL = 'http://localhost:10202/api/auctions';

// 获取所有拍卖
export const getAllAuctions = async () => {
  try {
    const response = await axios.get(`${API_URL}/getAuction`);
    return response; 
  } catch (error) {
    console.error('Error fetching auctions:', error);
    throw error;  
  }
};
//根据艺术品id获取拍卖
export const getAuctionsByArtworkId = async (artworkId) => {
    try {
      const response = await axios.get(`${API_URL}/getAuctionsByArtworkId/${artworkId}`);
      return response;  
    } catch (error) {
      console.error(`Error fetching auctions for artwork ${artworkId}:`, error);
      throw error;
    }
  };
// 根据拍卖ID获取某个拍卖
export const getAuctionById = async (auctionId) => {
  try {
    const response = await axios.get(`${API_URL}/getAuctionById/${auctionId}`);
    return response.data; 
  } catch (error) {
    console.error('Error fetching auction:', error);
    throw error;
  }
};
export const getAuctionsByArtworkIds = async (artworkIds) => {
  try {
    const response = await axios.post(`${API_URL}/getAuctionsByArtworkIds`, {artworkIds });
    return response.data; 
  } catch (error) {
    console.error('Error fetching auction:', error);
    throw error;
  }
};
// 创建新的拍卖
export const createAuction = async (auctionData) => {
  try {
    const response = await axios.post(`${API_URL}/createAuction`, auctionData);
    return response.data;  
  } catch (error) {
    console.error('Error creating auction:', error);
    throw error;
  }
};

// 更新拍卖
export const updateAuction = async (auctionId, auctionData) => {
  try {
    const response = await axios.put(`${API_URL}/updateAuction/${auctionId}`, auctionData);
    return response;  // 返回更新后的拍卖
  } catch (error) {
    console.error('Error updating auction:', error);
    throw error;
  }
};

// 删除拍卖
export const deleteAuction = async (auctionId) => {
  try {
    await axios.delete(`${API_URL}/deleteAuction/${auctionId}`);
    return true;  // 如果删除成功，返回 true
  } catch (error) {
    console.error('Error deleting auction:', error);
    throw error;
  }
};

export const rejectAuction = async (auctionId) => {
  try {
    const response = await axios.put(`${API_URL}/rejectAuction/${auctionId}`);
    return response; 
  } catch (error) {
    console.error('结算拍卖失败:', error);
    throw error; 
  }
}; 
export const settleAuction = async (auctionId) => {
  try {
    const response = await axios.put(`${API_URL}/settleAuction/${auctionId}`);
    return response.data; 
  } catch (error) {
    console.error('结算拍卖失败:', error);
    throw error; 
  }
}; 

