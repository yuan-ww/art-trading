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
                   @input="scheduleSearch"
                   clearable
                />
              </el-col>
              <el-col :span="2">
              </el-col>
              <el-col :span="8">
                <div class="statistics">共 {{ total }} 条记录</div>
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
            <el-pagination layout="prev, pager, next" :total="total" :page-size="20" :current-page="page + 1" @current-change="changePage" />
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
  import {deleteOrder} from '../utils/orderServices';
  import axios from 'axios';
  import { API_BASE_URL } from '../config/api';
  export default {
    data() {
      return {
        selectedImageUrl: "",
        imageView: false,
        imageUrls:[],
        Orders: [],
        page: 0,
        total: 0,
        searchTimer: null,
        requestSequence: 0,
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
            return this.Orders;
        }
        },
        async mounted() {
        await this.fetchOrderList();
    },
    methods: {
      scheduleSearch() { clearTimeout(this.searchTimer); this.searchTimer = setTimeout(() => this.resetPage(), 300); },
      resetPage() { this.page = 0; this.fetchOrderList(); },
      changePage(page) { this.page = page - 1; this.fetchOrderList(); },
      handleStatusChange() {
        this.selectedPaymentStatus = null;
            this.selectedShippingStatus = null;
            this.resetPage();
    },
        togglePaymentStatus(status) {
            this.selectedPaymentStatus = status;
            this.selectedShippingStatus = null;
            this.resetPage();
        },
        toggleShippingStatus(status) {
            this.selectedShippingStatus = status;
            this.selectedPaymentStatus = null;
            this.resetPage();
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
      const sequence = ++this.requestSequence;
      try {
        const params = { page: this.page, size: 20 };
        if (this.searchQuery.trim()) params.keyword = this.searchQuery.trim();
        if (this.selectedPaymentStatus) params.paymentStatus = this.selectedPaymentStatus;
        if (this.selectedShippingStatus) params.shippingStatus = this.selectedShippingStatus;
        const { data } = await axios.get(`${API_BASE_URL}/api/orders/admin-page`, { params });
        if (sequence !== this.requestSequence) return;
        this.Orders = data.content.map(order => ({ ...order, artwork: { name: order.artworkName }, buyer: { username: order.buyerName }, seller: { username: order.sellerName } }));
        this.total = data.totalElements;
      } catch (error) {
        console.error('获取数据失败:', error);
      }
    },
     async handleViewDetails(order){
        try {
          this.selectedArtworkInFo = await getArtworkById(order.artworkId);
          this.fetchImages(order.artworkId);
          this.infodialogVisible = true;
        } catch (error) { this.$message.error('加载作品详情失败'); }
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
