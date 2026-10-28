<template>
    <div > 
            <el-row class="toolbar" :gutter="20" style="margin-bottom: 20px;">
              <el-col :span="2">
                  <el-button  @click="handleStatusChange('')" >显示全部</el-button>
                </el-col>
               <el-col :span="2">
                  <el-button  @click="handleStatusChange('审核中')">查看未审核</el-button>
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
                <div class="statistics">共 {{ filteredAuctions.length }} 条记录</div>
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
              v-if="filteredAuctions.length > 0 && !isLoading"
              >
              <div v-for="auction in filteredAuctions" :key="auction.auctionId" class="product-item">
                <div class="product-content">
                  <img :src="auction.artwork.images[0].imageUrl" alt="商品图片" class="product-image" />
                  <div class="text-and-buttons">
                    <div class="product-details">
                      <div class="name-author">
                        <p class="product-name">《{{ auction.artwork.name }}》</p>
                        <p class="product-author">{{ auction.artwork.author }}</p>
                      </div>
                      <div class="price-status">
                        <p class="starting-price">￥{{ auction.artwork.fixedPrice }}</p>
                        <p class="auntion-status">{{ auction.status }}</p>
                      </div>
                    </div>
                    <div class="stat-item">
                      <span v-if="auction?.currentHighestBid > 0" class="starting-price">最高价:￥{{ auction?.currentHighestBid }}</span>
                      <span v-if="auction?.currentHighestBid > 0" class="product-author">出价者: {{ auction?.highestBidder?.username }}</span>
                      <span v-else class="no-bid-message">未有人出价</span>
                    </div>
                    <div class="action-buttons">
                      <el-button @click="handleViewDetails(auction)">查看</el-button>
                      <el-button v-if="auction.status === '已取消'" @click="handleResubmit(auction)">重新提交</el-button>
                      <el-popconfirm 
                        title="取消拍卖"
                        @confirm="() => handleDelete(auction)"
                      >
                        <template #reference>
                          <el-button>取消</el-button>
                        </template>
                      </el-popconfirm>
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
                      <span>{{ selectedAuctionInFo.artworkId }}</span>
                      </el-form-item>
                      <el-form-item label="艺术品名称 :">
                      <span>{{ selectedAuctionInFo.artwork.name }}</span>
                      </el-form-item>
                      <el-form-item label="艺术品作者 :">
                      <span>{{ selectedAuctionInFo.artwork.author }}</span>
                      </el-form-item>
                      <el-form-item label="作品发布者 :">
                      <span>{{ selectedAuctionInFo.artwork.user?.username }}</span>
                      </el-form-item>
                      <el-form-item label="艺术品描述 :">
                      <span>{{ selectedAuctionInFo.artwork.description }}</span>
                      </el-form-item>
                  </el-col>
                  <el-col :span="12">
                      <el-form-item label="艺术分类 :">
                      <span>{{ selectedAuctionInFo.artwork.category?.name || '未分类' }}</span>
                      </el-form-item>
                      <el-form-item label="作品定价 :">
                      <span>{{ selectedAuctionInFo.artwork.fixedPrice }}</span>
                      </el-form-item>
                      <el-form-item label="销售形式 :">
                      <span>{{ selectedAuctionInFo.artwork.saleType }}</span>
                      </el-form-item>
                      <el-form-item label="当前状态 :">
                      <span>{{ selectedAuctionInFo.artwork.status }}</span>
                      </el-form-item>
                      <el-form-item label="创建时间 :">
                      <span>{{ selectedAuctionInFo.artwork.createdAt }}</span>
                      </el-form-item>
                      <el-form-item label="更新时间 :">
                      <span>{{ selectedAuctionInFo.artwork.updatedAt }}</span>
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
  import {updateAuction,getAuctionsByArtworkIds,rejectAuction,resubmitAuction} from '../utils/auctionService';
  import {fetchArtworkImages,fetchArtworksByUserId} from '../utils/artworkService';
  import { mapState } from 'vuex';
  export default {
    data() {
      return {
        selectedImageUrl: "",
        imageView: false,
        imageUrls:[],
        auctions: [],
        searchQuery: '',
        selectedStatus: '', 
        selectedCategories: [] ,
        editDialogVisible: false,
        infodialogVisible: false, 
        selectedAuctionInFo: {},
        editForm: {
            auctionId:'',
            artworkId:'',
            startingPrice:'',
            status:'', 
            depositAmount:'',
            depositPercentage: '',   
        },
      };
    },
    computed:{
      ...mapState({
        userId: state => state.user.id,
        }),
        filteredAuctions() {
            if (!this.auctions || this.auctions.length === 0) {
            return []; 
            }
            let filtered = this.auctions.filter(auction => {
          // 使用可选链操作符
            const artworkName = auction.artwork?.name;
            if (artworkName) {
                return artworkName.toLowerCase().includes(this.searchQuery.toLowerCase());
            }
          });
            // 根据状态过滤
            if (this.selectedStatus) {
              filtered = filtered.filter(auction => auction.status === this.selectedStatus);
            }
            return filtered;
      }
    },
        async mounted() {
        await this.fetchAuctionsList();
    },
    methods: {
      async handleResubmit(auction) {
        try {
          const start = await this.$prompt('请输入新的开始时间，例如 2026-11-01T10:00:00', '重新提交拍卖', { inputValue: auction.startTime });
          const end = await this.$prompt('请输入新的结束时间，例如 2026-11-02T10:00:00', '重新提交拍卖', { inputValue: auction.endTime });
          await resubmitAuction(auction.auctionId, { ...auction, startTime: start.value, endTime: end.value });
          this.$message.success('已重新提交审核');
          await this.fetchAuctionsList();
        } catch (error) {
          if (error !== 'cancel' && error !== 'close') this.$message.error('重新提交失败，请检查时间');
        }
      },
      async handleDelete(auction) {
        const now = new Date();
        const startTime = new Date(auction.startTime);
        const endtTime = new Date(auction.endTime);
          if (now >= startTime && now <= endtTime) {
            this.$message.warning('拍卖已开始，无法取消');
            return;
          }
        try {
          const response = await rejectAuction(auction.auctionId);
          if (response) {
            this.fetchAuctionsList();
            this.$message.success('已取消');
          }
        } catch (error) {
          this.$message.error('取消失败');
        }
      },
      handleStatusChange(status) {
      this.selectedStatus = status;  // 改变选择的状态
    },
      handleEdit(row) {
      this.editForm = { ...row };
      this.editDialogVisible = true;
    },
    async saveEdit() {
        if(!this.editForm.depositAmount || !this.editForm.depositPercentage){
            this.$message.error('输入不能为空');
            return;
        } 
        try {
            await updateAuction(this.editForm.auctionId, this.editForm);
            await this.fetchAuctionsList();
            this.$message.success("更新成功")
        } catch (error) {
            alert('更新失败')
        }
      this.editDialogVisible = false; 
    },
    resetEditForm() {
      this.editForm = {
          auctionId:'',
          artworkId:'',
          startingPrice:'', 
          depositAmount:'',
          depositPercentage: '',  
      };
      this.selectedImageUrl = ""; 
      this.imageView = false,
      this.ImageUrls=[];
    },
    async handleAddCategory() {
      this.resetEditForm();
      this.editDialogVisible = true; 
    },
    handleSelectionChange(val) {
        this.selectedCategories = val;
    },
    sortById(a, b) {
      return a.auctionId - b.auctionId; 
    },
    async fetchAuctionsList() {
      try {
        const artworkList = await fetchArtworksByUserId(this.userId);
        const artworkIds = artworkList.map(artwork => artwork.artworkId);
        const response = await getAuctionsByArtworkIds(artworkIds); 
        this.auctions = response;
      } catch (error) {
        console.error('获取数据失败:', error);
      }
    },
    handleViewDetails(auction){
        this.selectedAuctionInFo = auction;
        this.fetchImages(this.selectedAuctionInFo.artworkId); 
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
    },
  };
  </script>
    <style scoped src="@/assets/css/ProductItem.css"></style>
  <style scoped src="@/assets/css/HomePage.css"></style>
