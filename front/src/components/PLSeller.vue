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
                  <el-button  @click="toggleShippingStatus('Pending')">查看未发货</el-button>
                </el-col>
                <el-col :span="8">
                <el-input
                  v-model="searchQuery"
                   @input="scheduleSearch"
                   placeholder="请输入艺术品名搜索"
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
              <div v-if="isLoading" class="loading-overlay">
              <div class="loading-spinner"></div>
              </div>
              <transition-group
              name="fade-in"
              tag="div"
              :class="['product-list', { 'filter-enabled': showFilter }]"
              v-if="filteredOrders.length > 0 && !isLoading"
              >
              <div v-for="order in filteredOrders" :key="order.orderId" class="product-item">
                <div class="productContent">
                  <img :src="order.imageUrl" alt="商品图片" class="product-image" />
                  <div class="textAndButtons">
                        <div class="productDetails">
                            <div class="nameAuthor">
                                <p class="product-name">《{{ order.artwork.name }}》</p>
                                <p class="product-author">{{ order.artwork.author }}</p>
                                <p class="starting-price">￥{{ order.finalPrice }}</p>
                            </div>
                            <div class="orderTime">
                                <p style="margin-bottom: 20px;">订单号:{{order.orderId}}</p>
                                <p>下单时间: {{ formatDate(order.createdAt) }}</p>
                                <p style="margin-top: 20px;" v-if="order.shippingStatus==='Delivered'">订单已完成</p>
                            </div>
                        </div>
                        <div class="buttonContainer">
                            <el-button v-if="order.shippingStatus ==='Pending' && order.paymentStatus === 'Paid'" class="shipButton" @click="handleShipping(order)">发货</el-button>
                            <el-button v-else-if="order.shippingStatus !== 'Pending'" class="shipButton" @click="checkShipping(order)">物流</el-button>
                            <el-button class="viewButton" @click="handleViewDetails(order.artworkId)">详细</el-button>
                            <el-button v-if="order.paymentStatus === 'Paid' && order.shippingStatus !== 'Pending'" @click="confirmAfterSale(order)">确认退货</el-button>
                            <el-button v-if="order.paymentStatus === 'Paid' && order.shippingStatus !== 'Pending'" @click="rejectAfterSale(order)">拒绝退货</el-button>
                            <el-tag v-if="order.shippingStatus!=='Delivered'" class="custom-tag" style="margin-top: 20px;" :type="getStatusType(order.paymentStatus)">
                                    {{ getPaymentStatus(order.paymentStatus) }}
                                </el-tag>
                                <el-popover v-else placement="right" :width="400" trigger="click">
                                    <template #reference>
                                        <el-button  class="shipButton" @click="checkReview(order)">评价</el-button>
                                    </template>
                                    <div class="review-container">
                                        <div class="review-section">
                                    <div class="section-row">
                                        <p class="section-title">评分</p>
                                        <p v-if="reviewData.rating===0" style="color: #d4d4d4;">用户未评分</p>
                                        <el-rate
                                        v-else
                                        v-model="reviewData.rating"
                                        size="large"
                                        :colors="['#99A9BF', '#F7BA2A', '#FF9900']"
                                        show-text
                                        disabled
                                        text-color="#F7BA2A"
                                        :texts="['差', '一般', '不错', '很好', '极好']"
                                        />
                                    </div>
                                    </div>
                                    <div class="review-section">
                                    <div class="section-row">
                                        <p class="section-title">内容</p>
                                        <p class="comment-text">{{ reviewData.comment }}</p>
                                    </div>
                                    </div>
                                    </div>
                                    </el-popover>
                                    <el-popconfirm
                                        v-if="order.shippingStatus ==='Delivered'"
                                        title="确定删除订单?"
                                        @confirm="() => handDeleteOrder(order)"
                                    >
                                        <template #reference>
                                        <el-button class="viewButton">删除</el-button>
                                        </template>
                                    </el-popconfirm>
                                <el-tag v-if="order.paymentStatus==='Paid' && order.shippingStatus!=='Delivered'" style="margin-top: 20px;" class="custom-tag" :type="getStatusType(order.shippingStatus)">
                                    {{ getSgippedStatus(order.shippingStatus) }}
                                </el-tag>
                        </div>
                  </div>
                </div>
              </div>
              </transition-group>
              <div v-else-if="!isLoading" class="nodata">
              <p>没有数据~</p>
              </div>
              <el-pagination v-if="total > 20" layout="prev, pager, next" :total="total" :page-size="20" :current-page="page + 1" @current-change="changePage" />
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
          <el-drawer v-model="drawer" @close="handleDrawerClose">
            <template #header>
                <h3>提交物流信息</h3>
            </template>
            <template #default>
                <div class="shipping-info-container">
                    <div class="shipping-form-item">
                    <label for="orderId">订单 ID</label>
                    <input
                        id="orderId"
                        v-model="order.orderId"
                        type="text"
                        disabled
                        placeholder="自动填充订单ID"
                    />
                    </div>
                    <div class="shipping-address-phone">
                        <div class="address-container">
                            <div class="address-item">
                                <label for="sellerAddress">发货地址</label>
                                <input
                                    id="sellerAddress"
                                    v-model="order.seller.address"
                                    type="text"
                                    placeholder="自动填充"
                                />
                            </div>
                            <div class="address-item">
                                <label for="buyerAddress">收货地址</label>
                                <input
                                    id="buyerAddress"
                                    v-model="order.buyer.address"
                                    type="text"
                                    disabled
                                    placeholder="自动填充"
                                />
                            </div>
                        </div>
                    </div>
                    <div class="shipping-address-phone">
                        <div class="address-container">
                            <div class="address-item">
                                <label for="sellerAddress">发货人电话</label>
                                <input
                                    id="sellerAddress"
                                    v-model="order.seller.phone"
                                    type="text"
                                    placeholder="自动填充"
                                />
                            </div>
                            <div class="address-item">
                                <label for="buyerAddress">收货人电话</label>
                                <input
                                    id="buyerAddress"
                                    v-model="order.buyer.phone"
                                    type="text"
                                    disabled
                                    placeholder="自动填充"
                                />
                            </div>
                        </div>
                    </div>
                    <div class="shipping-form-item">
                        <label for="trackingNumber">物流单号</label>
                        <input
                            id="trackingNumber"
                            v-model="shippingForm.trackingNumber"
                            type="text"
                            placeholder="请输入物流单号"
                            required
                        />
                        </div>

                        <div class="shipping-form-item">
                        <label for="carrier">物流公司</label>
                        <el-select
                            id="carrier"
                            v-model="shippingForm.carrier"
                            placeholder="请选择物流公司"
                            required
                        >
                            <el-option label="顺丰" value="SF Express"></el-option>
                            <el-option label="圆通" value="YTO Express"></el-option>
                            <el-option label="中通" value="ZTO Express"></el-option>
                            <el-option label="申通" value="STO Express"></el-option>
                            <el-option label="京东" value="JD Express"></el-option>
                        </el-select>
                        </div>

                        <div class="shipping-form-item">
                        <label>物流状态</label>
                        <el-radio-group v-model="shippingForm.shippingStatus">
                            <el-radio-button label="Pending">待发货</el-radio-button>
                            <el-radio-button label="Shipped">已发货</el-radio-button>
                            <el-radio-button label="Delivered">已送达</el-radio-button>
                        </el-radio-group>
                        </div>
                </div>
            </template>
            <template #footer>
                <div style="display: flex; align-items: center; justify-content: space-between;margin-bottom: 8px;">
                <div>
                    <el-button class="buy-button" style=" margin-left: 150px;" @click="cancelpay">取消</el-button>
                    <el-button class="buy-button" @click="resetForm">重置</el-button>
                    <el-button class="buy-button" type="primary" @click="paySaleClick(order)">提交</el-button>
                </div>
                </div>
            </template>
            </el-drawer>
            <el-drawer v-model="drawer1">
            <template #header>
                <h3>物流信息</h3>
            </template>
            <template #default>
                <div class="shipping-info-container">
                    <div class="shipping-details">
                        <el-descriptions title="订单物流详情" border :column="1" >
                            <el-descriptions-item label="物流公司">{{ getShippingCarrier(shippingInfo.carrier) }}</el-descriptions-item>
                            <el-descriptions-item label="运单号">{{ shippingInfo.trackingNumber }}</el-descriptions-item>
                            <el-descriptions-item label="发货时间">{{ formatDate(shippingInfo.shippingTime) }}</el-descriptions-item>
                            <el-descriptions-item label="物流状态">
                            <el-tag class="custom-tag" :type="getStatusType(shippingInfo.shippingStatus)">
                                {{ getSgippedStatus(shippingInfo.shippingStatus) }}
                            </el-tag>
                            </el-descriptions-item>
                        </el-descriptions>

                        <el-descriptions title="发货方信息" border :column="2">
                            <el-descriptions-item label="发货人ID">{{ shippingInfo.senderId }}</el-descriptions-item>
                            <el-descriptions-item label="发货电话">{{ shippingInfo.senderPhone }}</el-descriptions-item>
                            <el-descriptions-item label="发货地址">{{ shippingInfo.senderAddress }}</el-descriptions-item>
                        </el-descriptions>

                        <el-descriptions title="收货方信息" border :column="2">
                            <el-descriptions-item label="收货人ID">{{ shippingInfo.receiverId }}</el-descriptions-item>
                            <el-descriptions-item label="收货电话">{{ shippingInfo.receiverPhone }}</el-descriptions-item>
                            <el-descriptions-item label="收货地址">{{ shippingInfo.receiverAddress }}</el-descriptions-item>
                        </el-descriptions>
                        <ShippingTimeline :shipping-id="shippingInfo.shippingId" editable />
                        </div>
                </div>
            </template>
            <template #footer>
                <div style="display: flex; align-items: center; justify-content: space-between;margin-bottom: 8px;">
                <div>
                    <el-button class="buy-button" style=" margin-left: 320px;" @click="cancelpay">收起</el-button>
                </div>
                </div>
            </template>
            </el-drawer>
          </div>
  </template>

  <script>
  import {fetchArtworkImages,getArtworkById} from '../utils/artworkService';
  import { createShipping,getShippingByOrderId } from "@/utils/shippingService";
  import {getParticipantOrdersPage,deleteOrder} from '../utils/orderServices';
  import {getReviewsByOrderId} from '../utils/reviewService';
  import axios from 'axios';
  import { API_BASE_URL } from '../config/api';
  import { mapState } from 'vuex';
  import { getReturn, confirmReturn, rejectReturn } from '../utils/afterSaleService';
  import ShippingTimeline from './ShippingTimeline.vue';


  export default {
    components: { ShippingTimeline },
    data() {
      return {
        selectedImageUrl: "",
        imageView: false,
        drawer:false,
        drawer1:false,
        imageUrls:[],
        OrderList:[],
        page: 0, total: 0, searchTimer: null, requestSequence: 0,
        order:'',
        searchQuery: '',
        selectedPaymentStatus: null,
        selectedShippingStatus: null,
        selectedCategories: [] ,
        editDialogVisible: false,
        infodialogVisible: false,
        selectedArtworkInfo: {},
        shippingInfo:{},
        reviewData:{},
        shippingForm: {
            orderId: '',
            senderId: '',
            receiverId: '',
            senderAddress: "",
            receiverAddress: "",
            senderPhone: "",
            receiverPhone: "",
            trackingNumber: "",
            carrier: "",
            shippingStatus: "Shipped",
      }
      };
    },
    computed:{
      ...mapState({
        userId: state => state.user.id,
        }),
        filteredOrders() {
        return this.OrderList;
    }
    },
    async mounted() {
    await this.fetchOrderList();
    },
    methods: {
      scheduleSearch() { clearTimeout(this.searchTimer); this.searchTimer = setTimeout(() => this.resetPage(), 300); },
      resetPage() { this.page = 0; this.fetchOrderList(); },
      changePage(page) { this.page = page - 1; this.fetchOrderList(); },
      async rejectAfterSale(order) {
        try {
          const { data } = await getReturn(order.orderId);
          if (data.status !== 'Requested') { this.$message.info('没有待处理的退货申请'); return; }
          await this.$confirm(`拒绝该退货申请？原因：${data.reason}`, '拒绝退货');
          await rejectReturn(data.requestId);
          this.$message.success('已拒绝申请，买家可联系管理员');
        } catch (error) {
          if (error !== 'cancel' && error !== 'close') this.$message.error(error.response?.status === 404 ? '没有退货申请' : (error.response?.data || '处理失败'));
        }
      },
      async confirmAfterSale(order) {
        try {
          const { data } = await getReturn(order.orderId);
          if (data.status !== 'Requested') { this.$message.info('没有待处理的退货申请'); return; }
          await this.$confirm(`确认收到退货并退款？原因：${data.reason}`, '确认退货');
          await confirmReturn(data.requestId);
          this.$message.success('退款成功');
          await this.fetchOrderList();
        } catch (error) {
          if (error !== 'cancel' && error !== 'close') this.$message.error(error.response?.status === 404 ? '没有退货申请' : (error.response?.data || '处理失败'));
        }
      },
      async handDeleteOrder(order){
            try{
                const response =await deleteOrder(order.orderId);
                if (response) {
                this.$message.success("已删除订单");
                this.fetchOrderList();
            }
            }catch (error) {
            this.$message.error("删除失败，请重试");
        }
        },
        async checkReview(order){
            const response = await getReviewsByOrderId(order.orderId);
            if (response.length > 0) {
                this.reviewData = response[0];
                await this.fetchOrderList();
            } else {
                this.reviewData = { rating: 0, comment: '用户未评价' };
            }
        },
        handleDrawerClose(){
            this.shippingForm= {
                    senderAddress: "",
                    receiverAddress: "",
                    senderPhone: "",
                    receiverPhone: "",
                    trackingNumber: "",
                    carrier: "",
                    shippingStatus: "Shipped",
            }
        },
        cancelpay(){
            this.drawer = this.drawer === true ? false : this.drawer;
            this.drawer1 = this.drawer1 === true ? false : this.drawer1;
        },
        async checkShipping(order){
            this.drawer1=true;
            try {
                const response =await getShippingByOrderId(order.orderId)
                if(response){
                    this.shippingInfo = response;
                }
            } catch (error) {
                console.log(error)
            }
        },
        async paySaleClick(order){
            const loading = this.$loading({
            lock: true,
            text: '提交信息中...',
            background: 'rgba(0, 0, 0, 0.7)',
            });
            await new Promise(resolve => setTimeout(resolve, 2000));
            if (!this.shippingForm.carrier || !this.shippingForm.trackingNumber) {
            this.$message.warning('请将物流信息填写完整');
            return;
          }
            this.shippingForm.orderId = order.orderId;
            this.shippingForm.senderId=order.sellerId;
            this.shippingForm.receiverId=order.buyerId;
            this.shippingForm.senderAddress=order.seller.address;
            this.shippingForm.receiverAddress=order.buyer.address;
            this.shippingForm.senderPhone=order.seller.phone;
            this.shippingForm.receiverPhone=order.buyer.phone;
            try {
                const response = await createShipping(this.shippingForm);
                if(response){
                    this.$message.success('提交成功');
                    this.drawer=false
                    this.fetchOrderList();
                    loading.close();
                }
            } catch (error) {
                this.$message.error('提交失败');
            }
        },
        async handleShipping(order){
            try {
              const { data } = await axios.get(`${API_BASE_URL}/api/orders/${order.orderId}/shipping-contacts`);
              this.order = { ...order, seller: data.sender, buyer: data.receiver };
              this.drawer = true;
            } catch (error) { this.$message.error('获取发货信息失败'); }
        },
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
    resetForm() {
      this.$refs.shippingForm.resetFields();
    },
    handleSelectionChange(val) {
        this.selectedCategories = val;
    },
    async fetchOrderList() {
      const sequence = ++this.requestSequence;
      this.isLoading = true;
      try {
        const params = { sellerId: this.userId, page: this.page, size: 20 };
        if (this.searchQuery.trim()) params.keyword = this.searchQuery.trim();
        if (this.selectedPaymentStatus) params.paymentStatus = this.selectedPaymentStatus;
        if (this.selectedShippingStatus) params.shippingStatus = this.selectedShippingStatus;
        const result = await getParticipantOrdersPage(params);
        const orders = result.content.map(order => ({ ...order, artwork: {
          artworkId: order.artworkId, name: order.artworkName || '作品已不存在', author: order.artworkAuthor || ''
        }}));
        if (sequence !== this.requestSequence) return;
        this.OrderList = orders;
        this.total = result.totalElements;
      } catch (error) {
        console.error('获取订单失败:', error);
        this.$message.error('获取订单失败，请稍后重试');
      }
      if (sequence === this.requestSequence) this.isLoading = false;
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
            case "Pending":
            case "Refunded":
            return "warning";
            case "Shipped":
            case "Paid":
            return "success";
            case "Delivered":
            return "primary";
            default:
            return "info";
        }
    },
    getPaymentStatus(status) {
      switch (status) {
        case 'Pending':
          return '待支付';
        case 'Paid':
          return '已支付';
        case 'Refunded':
          return '已退款';
        case 'Cancel':
        return '订单已取消';
        default:
          return status;
      }
    },
    getShippingCarrier(carrier) {
      switch (carrier) {
        case 'SF Express':
          return '顺丰速递';
        case 'YTO Express':
          return '圆通快递';
        case 'ZTO Express':
          return '中通快递';
        case 'STO Express':
        return '申通快递';
        case 'JD Express':
        return '京东快递';
        default:
          return '其他';
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
    <style scoped src="@/assets/css/OrderItem.css"></style>
  <style scoped src="@/assets/css/HomePage.css"></style>
