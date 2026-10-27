import axios from 'axios';

// 设置基础URL
const API_URL = 'http://localhost:10202/api/categories';
const URL = 'http://localhost:10202/api/artworks';
const IMAGE_URL = 'http://localhost:10202/api/artworkImages';

export const fetchCategories = () => {
    return axios.get(`${API_URL}/getAllCategories`);
  };

export const fetchArtworks = async() => {
    try {
        const response = await axios.get(`${URL}/getAllArtworks`); 
        return response.data;
      } catch (error) {
        console.error("Error fetching artworks:", error);
        throw error; 
      }
};
export const fetchArtworksByUserId = async(userid) => {
  try {
      const response = await axios.get(`${URL}/getArtworksByUserId/${userid}`); 
      return response.data;
    } catch (error) {
      console.error("Error fetching artworks:", error);
      throw error;
    }
};
export const getArtworkById = async(artworkId) => {
  try {
      const response = await axios.get(`${URL}/${artworkId}/getArtworkById`); 
      return response.data;
    } catch (error) {
      console.error("Error fetching artworks:", error);
      throw error;
    }
};
export const checkArtworkExists = async (artworkId) => {
  try {
    const response =  await axios.get(`${URL}/checkArtworkExists`, { params: { artworkId } });
    return response;
  } catch (error) {
    throw new Error('Failed to check artwork existence');
  }
};
export function getCategoryById(id) {
  return axios.get(`${API_URL}/${id}`);
}

export const addArtwork = async(FormData) => {
    try {
      const response = await axios.post(`${URL}/createArtwork`,FormData, {  
        headers: {
            'Content-Type': 'multipart/form-data', 
          }
      });
      return response.data;
    } catch (error) {
      console.error('添加失败:',error);
      return { success: false, message: '更新失败，请稍后重试' };
  }
  };
  export const createArtworkWithAuction = async(formData) => {
    try{
        const response=await axios.post(`${URL}/createWithAuction`, formData, {
        headers: {
            'Content-Type': 'multipart/form-data'  ,
        }
    });
    return response;
  }
    catch(error){
        console.error('错误:',error);
        throw error;  
    }
};

  export const updateArtworkInfo = async (ArtworkId, ArtworkInfo) => {
    try {
      const response = await axios.put(`${URL}/${ArtworkId}/updateArtwork`, ArtworkInfo);
      return response.data; 
    } catch (error) {
      console.error('更新用户信息失败:', error);
      throw error;
    }
  };
  export const updateArtworkStatus = async (artworkId, status) => {
    try {
      const response = await axios.put(`${URL}/updateArtworkStatus`, null, {
        params: {
          artworkId, 
          status,   
        },
      });
      return response.data; 
    } catch (error) {
      console.error('更新艺术品状态失败:', error);
      throw error; 
    }
  };  

export const batchDeleteArtworks = async(ArtworkIds) => {
    try{
      const response = await axios.delete(`${URL}/deleteArtwork`, {
        data: ArtworkIds,
      });
      return response;
      
    }
    catch (error) {
      console.error('删除失败:', error);
      return { success: false, message: '删除失败，请稍后重试' };
  }
  };

  export const fetchArtworkImages = async(artworkId) => {
    try{
      const response = axios.get(`${IMAGE_URL}/getImage/${artworkId}`,);
      return response;
    }catch(error){
      console.error("Error fetching artworks:", error);
      throw error; 
    }
  };

  export const deleteArtworkImage = async (imageId) => {
    try{
      const response=axios.delete(`${IMAGE_URL}/deleteImage/${imageId}`,);
      return response;
    }catch(error){
      console.error("Error fetching artworks:", error);
      throw error; 
    }
  };
  