<template>
    <div > 
            <el-row class="toolbar" :gutter="20" style="margin-bottom: 20px;">
              <el-col :span="2">
                  <el-button  @click="handleStatusChange('')" >显示全部</el-button>
                </el-col>
               <el-col :span="2">
                  <el-button  @click="handleStatusChange('Delivered')">查看已完成</el-button>
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
                <div class="statistics">共 {{ filteredShipping.length }} 条记录</div>
              </el-col>
            </el-row>
            <div class="table-container">
            <el-table :data="filteredShipping" class="my-table" height="550"  @selection-change="handleSelectionChange">
              <el-table-column type="selection" width="55" />
              <el-table-column  prop="shippingId"
                  label="序号"
                  width="80"
                  sortable
                  :sort-method="sortById"/>
                  <el-table-column prop="orderId" label="订单" width="80" />
                  <el-table-column prop="sender.username" label="卖家" width="80" />
                  <el-table-column prop="receiver.username" label="买家" width="80" />
                  <el-table-column prop="sender.address" label="发货地址" width="220" />
                  <el-table-column prop="receiver.address" label="收货地址" width="220" />
                  <el-table-column prop="carrier" label="物流" width="100" />
                  <el-table-column prop="shippingStatus" label="物流状态" width="100" />
                  <el-table-column label="商品信息" width="100">
                      <template #default="{ row }">
                          <el-button @click="handleViewDetails(row)" type="info" >查看</el-button>
                      </template>
                  </el-table-column>
              <el-table-column  label="操作" width="200">
                <template #default="{ row }">
                    <el-button type="primary" @click="openUpdateDialog(row)">送达</el-button>
                    <el-popconfirm 
                            title="确定删除物流?"
                            @confirm="() => handleDelete(row)"
                        >
                            <template #reference>
                                <el-button type="danger">删除</el-button>
                            </template>
                        </el-popconfirm>
                </template>
              </el-table-column>
            </el-table>
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
                      <span>{{ selectedArtworkInFo?.artworkId }}</span>
                      </el-form-item>
                      <el-form-item label="艺术品名称 :">
                      <span>{{ selectedArtworkInFo?.name }}</span>
                      </el-form-item>
                      <el-form-item label="艺术品作者 :">
                      <span>{{ selectedArtworkInFo?.author }}</span>
                      </el-form-item>
                      <el-form-item label="作品发布者 :">
                      <span>{{ selectedArtworkInFo?.user?.username }}</span>
                      </el-form-item>
                      <el-form-item label="艺术品描述 :">
                      <span>{{ selectedArtworkInFo?.description }}</span>
                      </el-form-item>
                  </el-col>
                  <el-col :span="12">
                      <el-form-item label="艺术分类 :">
                      <span>{{ selectedArtworkInFo?.category?.name || '未分类' }}</span>
                      </el-form-item>
                      <el-form-item label="作品定价 :">
                      <span>{{ selectedArtworkInFo?.fixedPrice }}</span>
                      </el-form-item>
                      <el-form-item label="销售形式 :">
                      <span>{{ selectedArtworkInFo?.saleType }}</span>
                      </el-form-item>
                      <el-form-item label="当前状态 :">
                      <span>{{ selectedArtworkInFo?.status }}</span>
                      </el-form-item>
                      <el-form-item label="创建时间 :">
                      <span>{{ selectedArtworkInFo?.createdAt }}</span>
                      </el-form-item>
                      <el-form-item label="更新时间 :">
                      <span>{{ selectedArtworkInFo?.updatedAt }}</span>
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
  import {getOrderById} from '../utils/orderServices';
  import { getAllShipping,deleteShipping,updateShippingStatus} from '../utils/shippingService';
  import { getUserById} from '../utils/api';
  export default {
    data() {
      return {
        selectedImageUrl: "",
        imageView: false,
        imageUrls:[],
        shippings: [],
        searchQuery: '',
        selectedStatus: '', 
        selectedCategories: [] ,
        infodialogVisible: false, 
        selectedArtworkInFo: {},
      };
    },
    computed:{
        filteredShipping() {
            if (!this.shippings || this.shippings.length === 0) {
            return []; 
            }
            let filtered = this.shippings
            if (this.selectedStatus) {
            filtered = filtered.filter(shipping => shipping.shippingStatus === this.selectedStatus);
            }
            return filtered;
            }
    },
        async mounted() {
        await this.fetchOrderList();
    },
    methods: {
      handleStatusChange(status) {
      this.selectedStatus = status;
    },
    resetEditForm() {
      this.selectedImageUrl = ""; 
      this.imageView = false,
      this.ImageUrls=[];
    },
    async openUpdateDialog(shipping) {
        try {
            const response = await updateShippingStatus(shipping.shippingId, 'Delivered');
            if (response) {
                this.$message.success("状态已更新");
                this.fetchOrderList();
            }
        } catch (error) {
            this.$message.error("更新失败，请重试");
        }
    },
    handleSelectionChange(val) {
        this.selectedCategories = val;
    },
    sortById(a, b) {
      return a.auctionId - b.auctionId; 
    },
    async fetchOrderList() {
      try {
            const response = await getAllShipping(); 
            this.shippings = await Promise.all(response.map(async shipping => {
            shipping.order = await getOrderById(shipping.orderId);
            shipping.receiver =await getUserById(shipping.receiverId);
            shipping.sender =await getUserById(shipping.senderId);
            return shipping;
            }));
        this.shippings = response;
      } catch (error) {
        console.error('获取数据失败:', error);
      }
    },
     async handleViewDetails(shipping){
        this.selectedArtworkInFo = await getArtworkById(shipping.order.artworkId)
        this.fetchImages(this.selectedArtworkInFo.artworkId); 
        this.infodialogVisible = true;
    },
    handleDelete(row) {
            const orderId = row.orderId;
            deleteShipping(orderId).then((response) => {
                this.$message.success(response.data);
                this.fetchOrderList();
            }).catch((error) => {
                console.error('删除失败:', error);
                this.$message.error('删除失败');
            });
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
  
  <style scoped src="@/assets/css/HomePage.css"></style>