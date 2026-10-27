<template>
  <el-carousel :interval="4000" type="card" height="620px">
    <div v-if="loading" class="loading-overlay">
      <div class="loading-spinner"></div>
      <p>数据加载中...</p>
    </div>
    <el-carousel-item v-else v-for="item in carousels" :key="item.carouselId">
      <router-link :to="{ name: 'DetailsWithDeal', params: { artworkId: item.artwork.artworkId } }" @click="setArtworkInVuex(item.artwork)">
        <div class="image-container">
          <img
            v-if="item.artwork?.imageUrls?.length > 0"
            :src="item.artwork?.imageUrls[0]"
            alt="Artwork Image"
            class="carousel-image"
          />
          <div v-else class="no-image">暂无图片</div>
        </div>
        <div class="text-overlay">
          <p class="artwork-name">{{ item.artwork.name }}</p>
          <p class="artwork-author">{{ item.artwork.author }}</p>
          <p class="artwork-description">{{ item.artwork.description }}</p>
        </div>
      </router-link>
      </el-carousel-item>
  </el-carousel>
</template>
  
  <script>
import { fetchArtworkImages} from '../utils/artworkService';
import { getAllCarousels} from '../utils/carouselService';

export default {
  data() {
    return {
      carousels: [], 
      loading: true, 
    };
  },
  async created() {
    await this.fetchCarousels(); 
  },
  methods: {
     setArtworkInVuex(artwork) {
        this.$store.dispatch('updateArtwork', artwork);
      },
    async fetchCarousels() {
      this.loading = true; 
      try {
        const carousels = await getAllCarousels();
        const carouselsWithImages = await Promise.all(
          carousels.map(async (carousel) => {
            try {
              const response = await fetchArtworkImages(carousel.artwork.artworkId);
              carousel.artwork.imageUrls = response.data.map(image => image.imageUrl);
            } catch (error) {
              console.error(`获取艺术品 ${carousel.artwork.artworkId} 图片失败:`, error);
              carousel.artwork.imageUrls = [];
            }
            return carousel;
          })
        );
        this.carousels = carouselsWithImages;
      } catch (error) {
        console.error('获取轮播图失败:', error);
      } finally {
        this.loading = false; 
      }
    },
  },
};
</script>
  <style scoped>
  .text-overlay {
  backdrop-filter: blur(10px);
}
</style>
  <style scoped src="@/assets/css/SideShow.css"></style>
