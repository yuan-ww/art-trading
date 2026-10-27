<template>
    <div > 
            <el-row class="toolbar" :gutter="20" style="margin-bottom: 20px;">
              <el-col :span="2">
                  <el-button  @click="handleStatusChange('')" >显示全部</el-button>
                </el-col>
                <el-col :span="2">
                  <el-button  @click="togglePaymentStatus('Pending')">查看未付款</el-button>
                </el-col>
                <el-col :span="2">
                  <el-button   @click="toggleShippingStatus('Pending')">查看未发货</el-button>
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
                <div class="statistics">共 {{ filteredOrders.length }} 条记录</div>
              </el-col>
            </el-row>
            <div class="table-container">
            <el-table :data="filteredOrders" class="my-table" height="550"  @selection-change="handleSelectionChange">
              <el-table-column type="selection" width="55" />
              <el-table-column  prop="orderId"
                  label="序号"
                  width="80"
                  sortable
                  :sort-method="sortById"/>
                  <el-table-column prop="artwork.name" label="名称" width="100" />
                  <el-table-column prop="seller.username" label="卖家" width="100" />
                  <el-table-column prop="buyer.username" label="买家" width="100" />
                  <el-table-column prop="finalPrice" label="金额" width="100" />
                  <el-table-column prop="createdAt" label="下单时间" width="280" />
                  <el-table-column prop="paymentStatus" label="支付状态" width="100" />
                  <el-table-column prop="shippingStatus" label="物流状态" width="100" />
                  <el-table-column label="商品信息" width="100">
                      <template #default="{ row }">
                          <el-button @click="handleViewDetails(row)" type="info" >查看</el-button>
                      </template>
                  </el-table-column>
              <el-table-column  label="操作" width="200">
                <template #default="{ row }">
                    <el-popconfirm 
                            title="确定删除订单?"
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
  import {getAllOrders,deleteOrder} from '../utils/orderServices';
  import { getUserById} from '../utils/api';
  export default {
    data() {
      return {
        selectedImageUrl: "",
        imageView: false,
        imageUrls:[],
        Orders: [],
        searchQuery: '',
        selectedStatus: '', 
        selectedCategories: [] ,
        editDialogVisible: false,
        selectedPaymentStatus: null, 
        selectedShippingStatus: null, 
        infodialogVisible: false, 
        selectedArtworkInFo: {},
      };
    },
    computed:{
            filteredOrders() {
            return this.Orders.filter(order => {
            const nameMatch = order.artwork?.name?.toLowerCase().includes(this.searchQuery.toLowerCase());
            const paymentMatch = this.selectedPaymentStatus
            ? order.paymentStatus === this.selectedPaymentStatus
            : true;

        const shippingMatch = this.selectedShippingStatus
            ? order.shippingStatus === this.selectedShippingStatus
            : true;

        return nameMatch && paymentMatch && shippingMatch;
        });
        }
        },
        async mounted() {
        await this.fetchOrderList();
    },
    methods: {
      handleStatusChange() {
        this.selectedPaymentStatus = null;
            this.selectedShippingStatus = null;
    },
        togglePaymentStatus(status) {
            this.selectedPaymentStatus = status; 
            this.selectedShippingStatus = null; 
        },
        toggleShippingStatus(status) {
            this.selectedShippingStatus = status; 
            this.selectedPaymentStatus = null;
        },
    resetEditForm() {
      this.selectedImageUrl = ""; 
      this.imageView = false,
      this.ImageUrls=[];
    },
    handleSelectionChange(val) {
        this.selectedCategories = val;
    },
    sortById(a, b) {
      return a.auctionId - b.auctionId; 
    },
    async fetchOrderList() {
      try {
            const response = await getAllOrders(); 
            this.Orders = await Promise.all(response.map(async order => {
            order.artwork = await getArtworkById(order.artworkId);
            order.buyer =await getUserById(order.buyerId);
            order.seller =await getUserById(order.sellerId);
            return order;
            }));
        this.Orders = response;
      } catch (error) {
        console.error('获取数据失败:', error);
      }
    },
     handleViewDetails(order){
        this.selectedArtworkInFo =order.artwork
        this.fetchImages(this.selectedArtworkInFo.artworkId); 
        this.infodialogVisible = true;
    },
    handleDelete(row) {
            const orderId = row.orderId;
            deleteOrder(orderId).then((response) => {
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
      getPaymentStatus(status) {
      switch (status) {
        case 'Pending':
          return '待支付';
        case 'Paid':
          return '已支付';
        case 'Refunded':
          return '已退款';
        default:
          return status;
      }
    },
    getSgippedStatus(status) {
      switch (status) {
        case 'Pending':
          return '待发货';
        case 'Shipped':
          return '已发货';
        case 'Delivered':
          return '已送达';
        default:
          return status;
      }
    },
    },
  };
  </script>
  
  <style scoped src="@/assets/css/HomePage.css"></style>