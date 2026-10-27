import axios from 'axios';


const API_URL = 'http://localhost:10202/api/carousel';


export const getAllCarousels = async () => {
  try {
    const response = await axios.get(`${API_URL}/getAllCarousels`);
    return response.data;
  } catch (error) {
    console.error('获取轮播图失败:', error);
  }
};


export const getCarouselById = async (id) => {
  try {
    const response = await axios.get(`${API_URL}/getCarouselById/${id}`);
    return response.data;
  } catch (error) {
    console.error(`获取轮播图 ID=${id} 失败:`, error);
  }
};


export const addCarousel = async (artworkId) => {
  try {
    const response = await axios.post(`${API_URL}/add/${artworkId}`);
    return response.data;
  } catch (error) {
    console.error('添加轮播图失败:', error);
  }
};


export const deleteCarousel = async (id) => {
  try {
    await axios.delete(`${API_URL}/deleteCarousel/${id}`);
  } catch (error) {
    console.error(`删除轮播图 ID=${id} 失败:`, error);
  }
};

export const deleteCarouselByArtworkId= async (artworkId)  => {
    try {
        const response =  axios.delete(`${API_URL}/deleteCarouselByArtworkId/${artworkId}`);
        return response;
    } catch (error) {
      console.error('删除失败:', error);
    }
  };