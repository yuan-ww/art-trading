<template>
    <div class="body">
        <div class="sideshow">
          <SideShow/>
        </div>
     <div class="link-container">
      <router-link to="/allartworkpage" class="link-item">
        <div class="link-content">
          <span class="large-text">浏览全部艺术作品  ></span>
          <span class="small-text">寻找心仪杰作</span>
      </div>
      </router-link>
      <div class="link-item" @click="handleClick">
        <div class="link-content">
          <span class="large-text">上架最伟大的藏品  ></span>
          <span class="small-text">不可不知的艺术</span>
        </div>
      </div>
     </div>
     <div class="auction-list">
      <div class="banner">
        <div class="banner-right">
          <h1>竞拍，成就非凡</h1>
          <p>一锤定音，珍稀藏品触手可得</p>
        </div>
        <div class="banner-left">
          <img src="@/assets/images/art1.jpg" alt="Auction Banner" class="banner-image">
        </div>
      </div>
        <div class="header-2">
          <h2>一锤定音</h2>
          <router-link to="/auctionpage" class="view-all-link">
            <span>浏览全部拍卖</span>
            <img src="@/assets/images/icons/go.png">
          </router-link>
        </div>
        <el-row :gutter="20" class="auction-items" v-if="auctionArtworks.length > 0">
          <el-col :span="12" v-for="item in auctionArtworks" :key="item.artworkId" >
            <router-link :to="{ name: 'DetailsWithDeal', params: { artworkId: item.artworkId } }" @click="setArtworkInVuex(item)" class="auction-placeholder">
              <div class="image-container">
              <img :src="item.images[0].imageUrl" alt="Auction Artwork" />
            </div>
            <div class="text-container">
              <p><img src="@/assets/images/icons/work.png" alt="Icon" class="icon" /> 《{{ item.name }}》</p>
              <p><img src="@/assets/images/icons/author.png" alt="Icon" class="icon" />{{item.author}}</p>
            </div>
            </router-link>
          </el-col>
        </el-row>
      </div>
      <div class="auction-list">
        <div class="banner">
          <div class="banner-right">
          <h1>立即拥有心仪之物</h1>
          <p>不要让等待成为遗憾</p>
        </div>
        <div class="banner-left">
          <img src="@/assets/images/art2.jpg" alt="Auction Banner" class="banner-image">
        </div>
      </div>
        <div class="header-2">
          <h2>即刻购买</h2>
          <router-link to="/salepage" class="view-all-link">
            <span>浏览全部直购</span>
            <img src="@/assets/images/icons/go.png">
          </router-link>
        </div>
        <el-row :gutter="20" class="auction-items" v-if="directArtworks.length > 0">
          <el-col :span="12" v-for="item in directArtworks" :key="item.artworkId">
            <router-link :to="{ name: 'DetailsWithDeal', params: { artworkId: item.artworkId } }" @click="setArtworkInVuex(item)" class="auction-placeholder">
              <div class="image-container">
              <img :src="item.images[0].imageUrl" alt="Auction Artwork" />
            </div>
            <div class="text-container">
              <p><img src="@/assets/images/icons/work.png" alt="Icon" class="icon" />《{{ item.name }}》</p>
              <p><img src="@/assets/images/icons/author.png" alt="Icon" class="icon" />{{item.author}}</p>
            </div>
            </router-link>
          </el-col>
        </el-row>
      </div>
      <div class="auction-list">
        <div class="banner">
          <div class="banner-right">
          <h1>世间美好</h1>
          <p>与你环环相扣</p>
        </div>
        <div class="banner-left">
          <img src="@/assets/images/art5.jpg" alt="Auction Banner" class="banner-image">
        </div>
      </div>
        <div class="header-2">
          <h2>与您有关</h2>
          <router-link to="/personalcenter" class="view-all-link" v-if="isLoggedIn">
            <span>个人中心</span>
            <img src="@/assets/images/icons/go.png">
          </router-link>
        </div>
        <el-row :gutter="20" class="order-status-section" v-if="isLoggedIn">
          <el-col :span="8">
            <div class="order-card">
              <h3 class="order-title">我的收藏</h3>
              <ul v-if="myFavorite.length" class="order-list">
                <li v-for="item in myFavorite" :key="item.id" class="order-item">
                  <router-link :to="{ name: 'DetailsWithDeal', params: { artworkId: item.artworkId } }" @click="setArtworkInVuex(item)" class="order-card-link">
                    <div class="order-info">
                    《{{ item.name }}》 - {{ item.author }}
                  </div>
                  <div class="order-price">￥{{ item.fixedPrice }}</div>
                  </router-link>
                </li>
              </ul>
              <div v-else class="no-data">暂无</div>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="order-card" @click="goToOrder">
              <h3 class="order-title">待支付订单</h3>
              <ul v-if="pendingOrders.length" class="order-list">
                <li v-for="item in pendingOrders" :key="item.id" class="order-item">
                  <div class="order-info">
                    《{{ item.artwork.name }}》 - {{ item.artwork.author }}
                  </div>
                  <div class="order-price">￥{{ item.finalPrice }}</div>
                </li>
              </ul>
              <div v-else class="no-data">暂无</div>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="order-card" @click="goToOrder">
              <h3 class="order-title">已完成订单</h3>
              <ul v-if="completedOrders.length" class="order-list">
                <li v-for="item in completedOrders" :key="item.id" class="order-item">
                  <div class="order-info">
                    《{{ item.artwork.name }}》 - {{ item.artwork.author }}
                  </div>
                  <div class="order-price">￥{{ item.finalPrice }}</div>
                </li>
              </ul>
              <div v-else class="no-data">暂无</div>
            </div>
          </el-col>
        </el-row>
        <el-row v-else justify="center" style="margin-top: 30px;">
          <div style="text-align: center;">
            <p style="font-size: 18px; font-weight: bold; color: #303133; font-family: 'Segoe UI', 'Helvetica Neue', Arial, sans-serif;">
              您还未登录，
              <router-link to="/login-register" style="color: #409EFF; text-decoration: none; font-weight: 300;">
                点击这里登录
              </router-link>
            </p>
          </div>
        </el-row>
      </div>
    </div>
  </template>
  
  <script>
import SideShow from '@/components/SideShow.vue';
import { getAllArtworksWithImages} from '../utils/startService';
import {fetchArtworkImages,getArtworkById} from '../utils/artworkService';
import {getOrdersByBuyerId} from '../utils/orderServices';
import {getFavoritesByUser } from '../utils/favoriteService';
import { mapState } from 'vuex';
import { mapGetters } from 'vuex';
  export default {
    components: {
      SideShow 
    },
    data() {
    return {
      myFavorite:[],
      artworks: [], 
      auctionArtworks: [], 
      directArtworks: [],    
      OrderList:[],  
    };
  },
  methods: {
    handleClick() {
      if (this.isLoggedIn) {
        this.$router.push({
        path: '/personalcenter',
        query: {
          activate: 'product-1',
          showEditDialog: 'true'
        }
      })
      } else {
        this.$message.warning('请先登录后再进行操作')
        this.$router.push('/login-register') 
      }
    },
      goToOrder() {
        this.$router.push({
          path: '/personalcenter',
          query: { activate: 'deal-2'}
        })
      },
    setArtworkInVuex(artwork) {
      this.$store.dispatch('updateArtwork', artwork);
      localStorage.setItem('artwork', JSON.stringify(artwork));
    },
    async fetchCategorizedArtworks() {
      try {
        const response = await getAllArtworksWithImages();
        const artworks = response.data;  
        this.auctionArtworks = artworks.filter(item => item.saleType === '拍卖' && item.status==='在售').slice(0, 6);
        this.directArtworks = artworks.filter(item => item.saleType === '直售'&& item.status==='在售').slice(0, 6)
      } catch (error) {
        console.error("Error fetching categorized artworks:", error);
      }
    },
    async fetchOrderList() {
      if (!this.$store.getters.isLoggedIn) {
          return
      }
        try {
            const orders = await getOrdersByBuyerId(this.userId);
            this.OrderList = await Promise.all(orders.map(async order => {
            order.artwork = await getArtworkById(order.artworkId);
            const images = await fetchArtworkImages(order.artworkId);
            order.imageUrl = images.data.length > 0 ? images.data[0].imageUrl : ''; 
            return order;
            }));
        } catch (error) {
            console.error('获取订单失败:', error);
            this.$message.error('获取订单失败，请稍后重试');
        }
        },
        async fetchArtworksList() {
            try {
                const response = await getFavoritesByUser(this.userId);
                this.myFavorite = response.map(favorite => favorite.artwork);
            } catch (error) {
                console.error('获取艺术品数据失败:', error);
            }
        },
  },
  mounted() {
    this.fetchCategorizedArtworks();
    this.fetchOrderList(); 
    this.fetchArtworksList();
  },
  computed: {
    ...mapState({
      userId: (state) => state.user.id,
    }),
    ...mapGetters(['isLoggedIn', 'user','getUsername']),
    pendingOrders() {
      return this.OrderList.filter(
        item => item.paymentStatus === 'Penging'
      );
    },
    completedOrders() {
      return this.OrderList.filter(
        item => item.shippingStatus === 'Delivered'
      );
    }
  },
  };
  </script>
  
  <style scoped src="@/assets/css/StartPage.css"></style>
  