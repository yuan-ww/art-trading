<template>
    <div > 
            <el-row class="toolbar" :gutter="20" style="margin-bottom: 20px;">
              <el-col :span="2">
                  <el-button  @click="handleStatusChange('')" >显示全部</el-button>
                </el-col>
               <el-col :span="2">
                  <el-button  @click="handleStatusChange('进行中')">查看进行中</el-button>
                </el-col>
                <el-col :span="2">
                  <el-button  @click="handleStatusChange('已结束')">查看已结束</el-button>
                </el-col>
                <el-col :span="8">
                <el-input
                  v-model="searchQuery"
                   placeholder="请输入艺术品名搜索"
                   clearable
                />
              </el-col>
              <el-col :span="2">  
              </el-col>
              <el-col :span="8">
                <div class="statistics">共 {{ filteredRegistrations.length }} 条记录</div>
              </el-col>
            </el-row>
            <div class="table-container">
              <div v-if="isLoading" class="loading-overlay">
              <div class="loading-spinner"></div>
              </div>
              <transition-group
              name="fade-in"
              tag="div"
              :class="['product-list', { 'filter-enabled': showFilter }]"
              v-if="filteredRegistrations.length > 0 && !isLoading"
              >
              <div v-for="registration in filteredRegistrations" :key="registration.registrationId" class="product-item">
                <div class="productContent">
                  <img :src="registration?.imageUrl" alt="商品图片" class="product-image" />
                  <div class="textAndButtons">
                        <div class="productDetails">
                            <div class="nameAuthor">
                                <p class="product-name">《{{ registration?.auction.artwork.name }}》</p>
                                <p class="product-author">{{ registration?.auction.artwork.author }}</p>
                                <p class="starting-price">￥{{ registration?.auction.startingPrice }}</p>
                            </div>
                            <div class="orderTime">
                                <p>开始时间: {{ formatDate(registration?.auction.startTime) }}</p>
                                <p class="deadline">结束时间: {{ formatDate(registration?.auction.endTime)}}</p>
                                <p v-if="registration?.auction.status==='进行中'" class="deadline">当前最高价:￥{{registration?.auction.currentHighestBid}}</p>
                                <div v-else>
                                    <p style="margin-top: 20px;" >拍卖已结束,最高价:￥{{registration?.auction.currentHighestBid}}</p>
                                    <p style="margin-top: 20px;">退还押金:￥{{registration?.auction.depositAmount}}</p>
                                </div>
                            </div>
                        </div>
                        <div class="buttonContainer">
                            <router-link 
                            :to="{ name: 'DetailsWithDeal', params: { artworkId: registration.auction.artwork.artworkId } }" 
                            @click="setArtworkInVuex(registration.auction.artwork)"
                            >
                            <el-button class="shipButton">去出价</el-button>
                            </router-link>
                            <el-button class="viewButton" style="margin-left: 12px;" @click="handleViewDetails(registration.auction.artworkId)">详细信息</el-button>
                            <el-tag class="custom-tag" style="margin-top: 20px;" :type="getStatusType(registration?.auction.status)">
                                    {{ registration?.auction.status }}
                                </el-tag>
                        </div>
                  </div>
                </div>
              </div>
              </transition-group>
              <div v-else-if="!isLoading" class="nodata">
              <p>没有数据~</p>
              </div>
          </div>
            <el-dialog
              v-model="infodialogVisible"
              title="艺术品详情"
              width="50%"
              @close="resetEditForm"
              >
              <div v-if="!imageView">
              <el-form label-width="120px">
                  <el-row :gutter="20">
                  <el-col :span="12">
                      <el-form-item label="艺术品编号 :">
                      <span>{{ selectedArtworkInfo.artworkId }}</span>
                      </el-form-item>
                      <el-form-item label="艺术品名称 :">
                      <span>{{ selectedArtworkInfo.name }}</span>
                      </el-form-item>
                      <el-form-item label="艺术品作者 :">
                      <span>{{ selectedArtworkInfo.author }}</span>
                      </el-form-item>
                      <el-form-item label="作品发布者 :">
                      <span>{{ selectedArtworkInfo.user?.username }}</span>
                      </el-form-item>
                      <el-form-item label="艺术品描述 :">
                      <span>{{ selectedArtworkInfo.description }}</span>
                      </el-form-item>
                  </el-col>
                  <el-col :span="12">
                      <el-form-item label="艺术分类 :">
                      <span>{{ selectedArtworkInfo.category?.name || '未分类' }}</span>
                      </el-form-item>
                      <el-form-item label="作品定价 :">
                      <span>{{ selectedArtworkInfo.fixedPrice }}</span>
                      </el-form-item>
                      <el-form-item label="销售形式 :">
                      <span>{{ selectedArtworkInfo.saleType }}</span>
                      </el-form-item>
                      <el-form-item label="当前状态 :">
                      <span>{{ selectedArtworkInfo.status }}</span>
                      </el-form-item>
                      <el-form-item label="创建时间 :">
                      <span>{{ selectedArtworkInfo.createdAt }}</span>
                      </el-form-item>
                      <el-form-item label="更新时间 :">
                      <span>{{ selectedArtworkInfo.updatedAt }}</span>
                      </el-form-item>
                  </el-col>
                  <el-col>
                    <div v-if="imageUrls && imageUrls.length > 0" class="image-preview-container">
                        <div v-for="(image, index) in imageUrls" :key="index" class="image-preview-wrapper">
                            <img 
                            :src="image.imageUrl"
                            alt="Artwork Image" 
                            class="image-preview"
                            @click="viewImage(image.imageUrl)"
                            />
                        </div>
                    </div>
  
                  </el-col>
                  </el-row>
              </el-form>
            </div>
            <div v-else class="full-image">
              <img :src="selectedImageUrl" alt="Selected Artwork Image" class="full-image" @click="closeImage" title="点击缩小"/>
            </div>
          </el-dialog>
          </div>
  </template>
  
  <script>
  import {fetchArtworkImages,getArtworkById} from '../utils/artworkService';
  import {getRegistrationsForUser} from '../utils/registerService';
  import { mapState } from 'vuex';


  export default {
    data() {
      return {
        selectedImageUrl: "",
        imageView: false,
        drawer:false,
        drawer1:false,
        imageUrls:[],
        registrationsList:[],
        registration:'',
        searchQuery: '', 
        selectedStatus: null, 
        selectedCategories: [] ,
        editDialogVisible: false,
        infodialogVisible: false, 
        selectedArtworkInfo: {},
      };
    },
    computed:{
      ...mapState({
        userId: state => state.user.id,
        }),
        filteredRegistrations() {
        return this.registrationsList.filter(registration => {
        const nameMatch = registration.auction.artwork?.name?.toLowerCase().includes(this.searchQuery.toLowerCase());
        const paymentMatch = this.selectedStatus
        ? registration.auction.status === this.selectedStatus
        : true;
        console.log('Name Match:', nameMatch, 'Payment Match:', paymentMatch);
         return nameMatch && paymentMatch;
      });
    }
    },
        async mounted() {
        await this.fetchRegistrationsList();
    },
    methods: {
        setArtworkInVuex(artwork) {
            this.$store.dispatch('updateArtwork', artwork);
        },
        handleStatusChange(status) {
            this.selectedStatus = status; 
        },
        cancelpay(){
            this.drawer = this.drawer === true ? false : this.drawer;
            this.drawer1 = this.drawer1 === true ? false : this.drawer1;
        },
        resetEditForm() {
        this.selectedImageUrl = ""; 
        this.imageView = false,
        this.ImageUrls=[];
        },
        handleSelectionChange(val) {
            this.selectedCategories = val;
        },
        async fetchRegistrationsList() {
        this.isLoading = true;
        try {
            const registrations = await getRegistrationsForUser(this.userId);
            console.log(registrations)
            this.registrationsList = await Promise.all(registrations.map(async (registration) => {
                const images = await fetchArtworkImages(registration.auction.artworkId); 
                registration.imageUrl = images.data.length > 0 ? images.data[0].imageUrl : ''; 
                return registration; 
            }));
        } catch (error) {
            console.error('获取数据失败:', error);
            this.$message.error('获取数据失败，请稍后重试');
        }
        this.isLoading = false;
        },
        async handleViewDetails(artworkId){
            const response=await getArtworkById (artworkId)
            this.selectedArtworkInfo = response;
            this.fetchImages(this.selectedArtworkInfo.artworkId); 
            this.infodialogVisible = true;
        },
        fetchImages(artworkId) {
            fetchArtworkImages(artworkId)
            .then((response) => {
                this.imageUrls = response.data;
            })
            .catch((error) => {
                console.error('Error fetching images:', error)
            })
        },
        viewImage(imageUrl) {
            this.selectedImageUrl = imageUrl; 
            this.imageView = true;
        },
        closeImage(){
            this.selectedImageUrl = ""; 
            this.imageView = false;
        },
        formatDate(date) {
        const options = { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit', second: '2-digit' };
        return new Date(date).toLocaleDateString('zh-CN', options);
        },
        getDeadline(date) {
        const deadline = new Date(date);
        deadline.setDate(deadline.getDate() + 7);
        return this.formatDate(deadline);
        },
        getStatusType(status) {
            switch (status) {
                case "已结束":
                return "warning";
                case "进行中":
                return "success";
                case "审核中":
                return "primary";
                default:
                return "info";
            }
        },
    },
  };
  </script>
    <style scoped src="@/assets/css/OrderItem.css"></style>
  <style scoped src="@/assets/css/HomePage.css"></style>