import axios from 'axios';


const URL = 'http://localhost:10202/api/artworks';
//获取所有艺术品
export const getAllArtworksWithImages = async() => {
    try {
        const response = await axios.get(`${URL}/getAllArtworksWithImages`); 
        return response; // 返回艺术品数据
      } catch (error) {
        console.error("Error fetching artworks:", error);
        throw error; // 如果请求失败，抛出错误
      }
};
