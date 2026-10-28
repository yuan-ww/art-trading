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
                                <p v-if="order.paymentStatus!=='Paid'" class="deadline">
                                  订单剩余时间: {{ getRemainingTime(order) }}
                                </p>
                                <p style="margin-top: 20px;" v-if="order.shippingStatus==='Delivered'">订单已完成</p>
                            </div>
                        </div>
                        <div class="buttonContainer">
                            <el-button v-if="order.paymentStatus ==='Pending'" class="shipButton" @click="handlePay(order)">支付</el-button>
                            <el-button :disabled="order.shippingStatus === 'Pending'" v-else class="shipButton" @click="checkShipping(order)">物流</el-button>
                            <el-button class="viewButton" @click="handleViewDetails(order.artworkId)">信息</el-button>
                            <el-button v-if="order.paymentStatus === 'Paid' && order.shippingStatus !== 'Pending'" @click="requestAfterSale(order)">申请退货</el-button>
                            <el-tag v-if="order.shippingStatus !=='Delivered'" class="custom-tag" style="margin-top: 20px;" :type="getStatusType(order.paymentStatus)">
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
                                        <el-rate
                                        v-model="reviewData.rating"
                                        size="large"
                                        :colors="['#99A9BF', '#F7BA2A', '#FF9900']"
                                        show-text
                                        :disabled="reviewData.rating !== 0"
                                        text-color="#F7BA2A"
                                        :texts="['差', '一般', '不错', '很好', '极好']"
                                        />
                                    </div>
                                    </div>
                                    <div class="review-section">
                                    <div class="section-row">
                                        <p class="section-title">内容</p>
                                        <el-input
                                        v-model="reviewData.comment"
                                        type="textarea"
                                        placeholder="请输入您的评价内容"
                                        maxlength="200"
                                        show-word-limit
                                        resize="none"
                                        />
                                    </div>
                                    </div>
                                <div class="submit-button">
                                <el-button type="primary" @click="submitReview(order)">提交评价</el-button>
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
                            <el-tag v-if="order.paymentStatus==='Paid' && order.shippingStatus !=='Delivered'" style="margin-top: 10px;" class="custom-tag" :type="getStatusType(order.shippingStatus)">
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
          <el-drawer v-model="drawer">
            <template #header>
                <h3>订单支付</h3>
            </template>
            <template #default>
            <p>您正在参与《{{ order.artwork.name }}》的直购</p>
            <h3>选择支付方式</h3>
                <div style="flex: auto;margin-bottom: 8px;">
                <div class="radio-group-container">
                <el-radio-group v-model="selectedPayment">
                    <el-radio :value="1" size="large" border  class="radio">余额</el-radio>
                    <img src="@/assets/images/icons/qb.png" alt="余额" class="payicon">
                </el-radio-group>
                </div>
                </div>
            </template>
            <template #footer>
                <div style="display: flex; align-items: center; justify-content: space-between;margin-bottom: 8px;">
                <p style="margin: 0;margin-top: 20px;"><strong>支付金额:</strong> ￥{{ order.finalPrice}}</p>
                <div>
                    <el-button class="buy-button" @click="cancelpay">取消</el-button>
                    <el-button class="buy-button" type="primary" @click="paySaleClick(order.artwork)">支付</el-button>
                </div>
                </div>
            </template>
            </el-drawer>
            <el-drawer v-model="drawer1" @close="handleDrawerClose">
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
                        <ShippingTimeline :shipping-id="shippingInfo.shippingId" />
                        </div>
                </div>
            </template>
            <template #footer>
                <div style="display: flex; align-items: center; justify-content: space-between;margin-bottom: 8px;">
                <div>
                    <el-button class="buy-button" style=" margin-left: 200px;" @click="cancelpay">收起</el-button>
                    <el-button class="buy-button" type="primary" @click="confirmReceipt(shippingInfo)">确定收货</el-button>
                </div>
                </div>
            </template>
            </el-drawer>
          </div>
  </template>

  <script>
  import {fetchArtworkImages,getArtworkById} from '../utils/artworkService';
  import { getShippingByOrderId,updateShippingStatus} from "@/utils/shippingService";
  import {getParticipantOrdersPage,updateOrderStatus,handleExpiredOrder,deleteOrder} from '../utils/orderServices';
  import { getUserById} from '../utils/api';
  import { createReview,getReviewsByOrderId} from '../utils/reviewService';
  import { mapState } from 'vuex';
  import { requestReturn } from '../utils/afterSaleService';
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
        now: Date.now(),
        selectedPayment: 1,
        order:'',
        searchQuery: '',
        selectedPaymentStatus: null,
        selectedShippingStatus: null,
        selectedCategories: [] ,
        editDialogVisible: false,
        infodialogVisible: false,
        selectedArtworkInfo: {},
        shippingInfo:{},
        reviewData:{
            order:{},
            user:{},
            rating:"",
            comment:"",
        },
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
        this.timer = setInterval(() => {
            this.now = Date.now();
            }, 1000);
        await this.fetchOrderList();
    },
    beforeUnmount() {
        clearInterval(this.timer);
    },
    methods: {
        scheduleSearch() { clearTimeout(this.searchTimer); this.searchTimer = setTimeout(() => this.resetPage(), 300); },
        resetPage() { this.page = 0; this.fetchOrderList(); },
        changePage(page) { this.page = page - 1; this.fetchOrderList(); },
        async requestAfterSale(order) {
            try {
                const { value } = await this.$prompt('请输入退货原因', '申请退货', { inputValidator: value => !!value?.trim() || '原因不能为空' });
                await requestReturn(order.orderId, value);
                this.$message.success('申请已提交，请联系卖家确认退回');
            } catch (error) {
                if (error !== 'cancel' && error !== 'close') this.$message.error(error.response?.data || '申请失败');
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
        async confirmReceipt(shippingInfo){
            try {
            const response = await updateShippingStatus(shippingInfo.shippingId, 'Delivered');
            if (response) {
                this.$message.success("已确认收货");
                this.fetchOrderList();
            }
        } catch (error) {
            this.$message.error("更新失败，请重试");
        }
        },
        async checkReview(order) {
            const response = await getReviewsByOrderId(order.orderId);
            if (response.length > 0) {
                this.reviewData = response[0];
            } else {
                this.reviewData = { rating: 0, comment: '' };
            }
        },
        async submitReview(order) {
            const response = await getReviewsByOrderId(order.orderId);
            if (response.length > 0) {
                this.$message.warning('您已经评价过了');
                return;
            } else {
                    if (this.reviewData.rating === 0) {
                    this.$message.warning('请先评分');
                    return;
                    }
                    if (this.reviewData.comment.trim() === '') {
                        this.$message.warning('请输入评论内容');
                        return;
                    }
                    try {
                        this.reviewData.order=order
                        this.reviewData.user=await getUserById(this.userId)
                        const result = await createReview(this.reviewData);
                        this.$message.success('评论提交成功');
                        console.log('评论创建结果:', result);
                    } catch (error) {
                        this.$message.error('评论提交失败');
                        console.error('提交失败:', error);
                    }
                }
            },
        handleDrawerClose(){
            this.shippingInfo = {
                carrier: '',
                trackingNumber: '',
                senderAddress: '',
                receiverAddress: '',
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
        handlePay(order){
            this.order=order
            this.drawer=true
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
        async paySaleClick(artwork){
        const loading = this.$loading({
        lock: true,
        text: '支付处理中...',
        background: 'rgba(0, 0, 0, 0.7)',
        });
        await new Promise(resolve => setTimeout(resolve, 2000));
        try {
            this.isPaying = true;
              const orderResponse =await updateOrderStatus(this.userId,artwork.artworkId,'Paid');
              if(orderResponse){
                this.$message.success("支付成功")
              }else{
                this.$message.error("支付失败")
              }
      } catch (error) {
        this.$message.error(error.response?.data || '支付失败，请重试');
      } finally {
        this.drawer =false;
        this.isPaying = false;
        this.fetchOrderList();
        loading.close();
      }
    },
        resetEditForm() {
        this.selectedImageUrl = "";
        this.imageView = false,
        this.ImageUrls=[];
        },
        handleSelectionChange(val) {
            this.selectedCategories = val;
        },
        async fetchOrderList() {
        const sequence = ++this.requestSequence;
        this.isLoading = true;
        try {
            const params = { buyerId: this.userId, page: this.page, size: 20 };
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
                const localDate = new Date(date);
                localDate.setHours(localDate.getHours() - 0);
                const options = { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit', second: '2-digit' };
                return localDate.toLocaleDateString('zh-CN', options);
            },
        getDeadline(date) {
            const deadline = new Date(date);
            deadline.setDate(deadline.getDate() + 1);
            deadline.setHours(deadline.getHours() - 0);
            return this.formatDate(deadline);
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
            return '已取消';
            default:
            return status;
        }
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
    getRemainingTime(order) {
      const deadline = new Date(order.createdAt).getTime() + 24 * 3600 * 1000;
      const remaining = deadline - this.now;

      if (remaining <= 0) {
        if(order.paymentStatus==='Pending'){
            this.handleTimeout(order);
        }
        return "00:00:00";
      }
      return this.formatTime(remaining);
    },
    formatTime(ms) {
      const hours = Math.floor(ms / 3600000);
      const minutes = Math.floor((ms % 3600000) / 60000);
      const seconds = Math.floor((ms % 60000) / 1000);
      return `${hours.toString().padStart(2, "0")}:${minutes.toString().padStart(2, "0")}:${seconds.toString().padStart(2, "0")}`;
    },
    async handleTimeout(order) {
        if (order.timeoutHandling) {
          return;
        }
        order.timeoutHandling = true;
        try {
          await handleExpiredOrder(order.orderId);
          await this.fetchOrderList();
        } catch (error) {
          console.error('处理超时订单失败:', error);
        } finally {
          order.timeoutHandling = false;
        }
    },
    },
  };
  </script>
    <style scoped src="@/assets/css/OrderItem.css"></style>
  <style scoped src="@/assets/css/HomePage.css"></style>
