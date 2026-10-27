<template>
  <div class="details-page" v-if="artwork">
    <div class="image-section">
      <el-carousel
        height="560px"
        arrow="hover"
        indicator-position="outside"
        class="carousel-box"
        v-if="artwork.images && artwork.images.length"
      >
        <el-carousel-item
          v-for="(img, index) in artwork.images"
          :key="index"
        >
          <div class="img-wrapper">
            <el-image
            :src="img.imageUrl"
            :zoom-rate="1.2"
            :max-scale="7"
            :min-scale="0.2"
            
            preview-teleported="true"
            fit="cover"
            :preview-src-list="artwork.images.map(item => item.imageUrl)"
          ></el-image>
          </div>
        </el-carousel-item>
      </el-carousel>
    </div>
    <div class="right-section">
      <transition  name="slide-fade" mode="out-in">
        <div class="messages" v-show="!isBidding" key="info">
          <div class="text-section">
            <div class="top-container">
             <h1>{{ artwork.name }}</h1>
             <button class="icon-button" @click="toggleFavorite" :disabled="isLoading">
                  <svg
                    :class="['icon', { favorite: isFavorite, loading: isLoading }]"
                    viewBox="0 0 1024 1024"
                    version="1.1"
                    xmlns="http://www.w3.org/2000/svg"
                    width="40"
                    height="40"
                  >
                    <path
                      :fill="isFavorite ? '#F1494F' : '#CCCCCC'"
                      d="M707.584 93.184c-77.312 0-148.992 38.912-196.608 102.912-47.104-64-119.296-102.912-196.608-102.912-139.264 0-252.416 123.904-252.416 275.968 0 90.624 40.448 154.624 73.216 205.824C229.888 723.968 468.48 908.8 478.72 916.48c9.728 7.68 20.992 11.264 32.256 11.264s22.528-3.584 32.256-11.264c10.24-7.68 248.32-193.024 343.552-341.504 32.768-51.2 73.216-115.2 73.216-205.824 0-152.064-113.152-275.968-252.416-275.968z"
                    />
                  </svg>
              </button>
            </div>
            <p><strong>作者:</strong> {{ artwork.author }}</p>
            <p><strong>类别:</strong> {{ artwork.category.name }}</p>
            <p><strong>描述:</strong> {{ artwork.description }}</p>
            <p><strong>发布者:</strong> {{ artwork.user.username }}</p>
            <p><strong>发布时间:</strong> {{ formatDate(artwork.createdAt) }}</p>
          </div>
          <div class="purchase-section">
            <div v-if="artwork.saleType === '拍卖'" class="auction-info flex-container">
              <div class="auction-times flex-item left">
                <p><strong>开始时间:</strong> {{ formatDate(auctionInfo.startTime) }}</p>
                <p><strong>结束时间:</strong> {{ formatDate(auctionInfo.endTime) }}</p>
              </div>
              <div class="auction-actions flex-item right">
                <p><strong>起拍价:</strong> ￥{{ auctionInfo.startingPrice }}</p>
                <div v-if="auctionInfo.status ==='已结束'">
                  <el-tag class="custom-tag">{{ auctionInfo.status }}</el-tag>
                </div>
                <div v-else>
                  <button v-if="!UserRegistrationStatus && artwork.status ==='在售'" @click="signUpAuction" class="buy-button">报名参与</button>
                  <button v-else @click="toBiding" class="buy-button">去出价</button>
                </div>
              </div>
            </div>
            <div v-else class="flex-container vertical-layout">
              <div class="flex-item full-width">
                <p><strong>定价:</strong> ￥{{ artwork.fixedPrice }}</p>
              </div>
              <div class="flex-container buy-button-container">
              <div v-if="artwork.status ==='已售'">
                  <el-tag class="custom-tag">{{ artwork.status }}</el-tag>
                </div>
                <div v-else>
                  <button v-if="artwork.saleType !== '拍卖' && artwork.status ==='在售' " @click="buyArtwork" class="buy-button flex-item full-width">购买</button>
                </div>
            </div>
            </div>
          </div>
        </div>
      </transition>
        <transition  name="slide-fade" mode="out-in">
        <div class="biding" v-if="isBidding" key="bid">
          <BidForAuction :auctionId="auctionInfo.auctionId"/>
          <button  @click="outBiding" class="buy-button ">收起</button>
        </div>
        </transition>
    </div>
  </div>
  <div v-else class="loading-message">
    <p>正在加载艺术品信息...</p>
  </div>
  <el-drawer v-model="drawer">
      <template #header>
        <h2>拍卖会参与责任声明及协议</h2>
      </template>
      <template #default>
        <div>
            <p>尊敬的参与者：</p>
            <p>感谢您报名参加本次拍卖会。为确保拍卖会的顺利进行，并明确各方的权利与义务，请您仔细阅读以下责任声明及协议内容。报名即视为您已充分理解并同意以下条款。</p>
            
            <h3>一、参与者责任声明</h3>
            
            <h4>身份确认与资质</h4>
            <p>参与者需确保所提供的个人信息真实、准确、完整。</p>
            <p>参与者需具备完全民事行为能力，若为机构代表，需提供合法授权文件。</p>
            
            <h4>拍卖规则遵守</h4>
            <p>参与者需严格遵守拍卖会的规则和流程，包括但不限于竞价规则、付款方式、交割时间等。</p>
            <p>参与者不得恶意竞价或干扰拍卖秩序，否则主办方有权取消其参与资格。</p>
            
            <h4>风险自担</h4>
            <p>参与者需自行承担拍卖过程中可能产生的风险，包括但不限于拍品真伪、品质、价值等。</p>
            <p>主办方不对拍品的真实性、完整性或适用性作任何明示或暗示的担保。</p>
            
            <h4>法律责任</h4>
            <p>参与者在拍卖会中的行为需符合法律法规，若因违反法律或本协议导致损失或纠纷，参与者需自行承担全部责任。</p>
            <p>若参与者行为损害主办方或其他参与者的合法权益，主办方有权追究其法律责任。</p>
            
            <h3>二、拍卖会规则协议</h3>
            
            <h4>竞价规则</h4>
            <p>参与者需按照拍卖师的指示进行竞价，每次加价不得低于规定的最低加价幅度。</p>
            <p>竞价成功后，参与者需当场确认，并在规定时间内完成付款及交割手续。</p>
            
            <h4>付款与交割</h4>
            <p>竞拍成功后，参与者需在规定时间内支付全款，逾期未支付者视为违约，主办方有权取消交易并追究违约责任。</p>
            <p>拍品的交割方式及时间以主办方通知为准，参与者需自行承担拍品的运输及保险费用。</p>
            
            <h4>拍品描述与瑕疵</h4>
            <p>主办方将尽可能提供拍品的详细描述及图片，但不对拍品的瑕疵或缺陷承担任何责任。</p>
            <p>参与者应在拍卖前充分了解拍品情况，竞拍成功后不得以拍品存在瑕疵为由拒绝付款或交割。</p>
            
            <h4>违约处理</h4>
            <p>若参与者未按约定支付款项或完成交割，主办方有权没收其已支付的保证金，并保留追究其法律责任的权利。</p>
            <p>若主办方因不可抗力或其他合理原因取消拍卖会，将退还参与者已支付的费用，但不承担其他赔偿责任。</p>
            
            <h3>三、免责条款</h3>
            
            <h4>不可抗力</h4>
            <p>如因自然灾害、战争、疫情等不可抗力因素导致拍卖会无法正常进行，主办方不承担任何责任。</p>
            
            <h4>网络拍卖风险</h4>
            <p>若为线上拍卖会，参与者需自行确保网络环境稳定，因网络问题导致的竞价失败或交易中断，主办方不承担任何责任。</p>
            
            <h4>拍品风险</h4>
            <p>参与者需自行承担拍品在运输、保管及使用过程中可能产生的风险，主办方不对拍品的损坏、丢失或贬值负责。</p>
            
            <h3>四、其他条款</h3>
            
            <h4>协议修改</h4>
            <p>主办方有权根据实际情况对本协议进行修改，修改后的协议将在公告后生效。</p>
            
            <h4>争议解决</h4>
            <p>如因本协议产生争议，双方应友好协商解决；协商不成的，任何一方可向主办方所在地有管辖权的人民法院提起诉讼。</p>
            
            <h4>协议生效</h4>
            <p>本协议自参与者报名成功之日起生效，具有法律效力。</p>
        </div>
      </template>
      <template #footer>
        <div style="flex: auto;margin-bottom: 8px;">
          <el-checkbox v-model="checked">我已阅读并理解上述责任声明及协议内容，自愿报名参加本次拍卖</el-checkbox>
          <div style="display: flex; align-items: center; justify-content: space-between">
          <p style="margin: 0;margin-top: 20px;"><strong>押金:</strong> ￥{{ auctionInfo.depositAmount }}</p>
          <div>
            <el-button class="buy-button" @click="cancelClick">取消</el-button>
            <el-button class="buy-button" type="primary" @click="confirmClick">确认</el-button>
          </div>
        </div>
        </div>
      </template>
    </el-drawer>
    <el-drawer v-model="drawer2">
      <template #header>
        <h3>支付押金</h3>
      </template>
      <template #default>
      <p>您正在参与《{{ artwork.name }}》的拍卖</p>
      <p>为了防止恶意竞拍，保障拍卖过程的顺利进行</p>
      <p>您需要支付{{ auctionInfo.depositPercentage }}%的押金(拍卖结束后返回原账户)</p>
      <h3>选择支付方式</h3>
        <div style="flex: auto;margin-bottom: 8px;">
          <div class="radio-group-container">
          <el-radio-group v-model="selectedPayment">
            <el-radio :value="1" size="large" border  class="radio">余额</el-radio>
            <img src="@/assets/images/icons/qb.png" alt="余额" class="payicon">
            <el-radio :value="2" size="large"  border class="radio">支付宝</el-radio>
            <img src="@/assets/images/icons/zfb.png" alt="支付宝" class="payicon">
            <el-radio :value="3" size="large" border  class="radio">微信</el-radio>
            <img src="@/assets/images/icons/wx.png" alt="微信" class="payicon">
            <el-radio :value="4" size="large" border  class="radio">银行卡</el-radio>
            <img src="@/assets/images/icons/yhk.png" alt="银行卡" class="payicon">
          </el-radio-group>
        </div>
        </div>
      </template>
      <template #footer>
        <div style="display: flex; align-items: center; justify-content: space-between;margin-bottom: 8px;">
          <p style="margin: 0;margin-top: 20px;"><strong>支付金额:</strong> ￥{{ auctionInfo.depositAmount }}</p>
          <div>
            <el-button class="buy-button" @click="cancelpay">取消</el-button>
            <el-button class="buy-button" type="primary" @click="payClick">支付</el-button>
          </div>
        </div>
      </template>
    </el-drawer>
    <el-drawer v-model="drawer3" v-if="artwork">
      <template #header>
        <h2>确认订单</h2>
      </template>
      <template #default>
        <div>
          <p>您正在参与《{{ artwork.name }}》的直购</p>
          <p><strong>作者:</strong> {{ artwork.author }}</p>
          <div style="margin-top: 50px;">
          <h3>确认您的信息</h3>
            <div class="form-group">
              <label>邮箱:</label>
              <input class="input" v-model="user.email" disabled /> 
            </div>
            <div class="form-group">
              <label>电话:</label>
              <input class="input" v-model="user.phone" @input="filterPhoneInput" />
            </div>
            <div class="form-group">
              <label>地址:</label>
              <input class="input" v-model="user.address" />
            </div>
          </div>
        </div>
      </template>
      <template #footer>
        <div style="flex: auto;margin-bottom: 8px;" >
          <div style="display: flex; align-items: center; justify-content: space-between">
          <p style="margin: 0;margin-top: 20px;"><strong>价格:</strong> ￥{{ artwork.fixedPrice }}</p>
          <div>
            <el-button class="buy-button" @click="cancelClick">取消</el-button>
            <el-button class="buy-button" type="primary" @click="generateOrder">确认</el-button>
          </div>
        </div>
        </div>
      </template>
    </el-drawer>
    <el-drawer v-model="drawer4" v-if="artwork">
      <template #header>
        <h3>订单支付</h3>
      </template>
      <template #default>
      <p>您正在参与《{{ artwork.name }}》的直购</p>
      <h3>选择支付方式</h3>
        <div style="flex: auto;margin-bottom: 8px;">
          <div class="radio-group-container">
          <el-radio-group v-model="selectedPayment">
            <el-radio :value="1" size="large" border  class="radio">余额</el-radio>
            <img src="@/assets/images/icons/qb.png" alt="余额" class="payicon">
            <el-radio :value="2" size="large"  border class="radio">支付宝</el-radio>
            <img src="@/assets/images/icons/zfb.png" alt="支付宝" class="payicon">
            <el-radio :value="3" size="large" border  class="radio">微信</el-radio>
            <img src="@/assets/images/icons/wx.png" alt="微信" class="payicon">
            <el-radio :value="4" size="large" border  class="radio">银行卡</el-radio>
            <img src="@/assets/images/icons/yhk.png" alt="银行卡" class="payicon">
          </el-radio-group>
        </div>
        </div>
      </template>
      <template #footer>
        <div style="display: flex; align-items: center; justify-content: space-between;margin-bottom: 8px;">
          <p style="margin: 0;margin-top: 20px;"><strong>支付金额:</strong> ￥{{ artwork.fixedPrice }}</p>
          <div>
            <el-button class="buy-button" @click="cancelpay">取消</el-button>
            <el-button class="buy-button" type="primary" @click="paySaleClick(artwork)">支付</el-button>
          </div>
        </div>
      </template>
    </el-drawer>
</template>
  <script>
import {getAuctionsByArtworkId} from '../utils/auctionService';
import {registerForAuction,checkUserRegistrationStatus} from '../utils/registerService';
import {addFavorite,checkIsFavorite,removeFavorite} from '../utils/favoriteService';
import {createOrder,updateOrderStatus} from '../utils/orderServices';
import BidForAuction from '@/components/BidForAuction.vue';
import { getUserById,updateUserInfo,updateBalance} from '@/utils/api';
import { mapGetters } from 'vuex';
import { mapState } from 'vuex';
export default {
    name: 'DetailsWithDeal',
    components: {
      BidForAuction
  },
    data() {
    return {
      isFavorite: false,
      isLoading: false,
      checked: false,
      drawer:false,
      drawer2:false,
      drawer3:false,
      drawer4:false,
      isPaying:false,
      isBidding:false,
      UserRegistrationStatus:false,
      selectedPayment: 1,
      auctionInfo: {},
      user:{},
      orderData:{
        artworkId: "",
        buyerId: "",
        sellerId: "",
        finalPrice: "",
        paymentStatus: "Pending",
        shippingStatus: "Pending",
      },
    }; 
  },
  computed: {
    ...mapState({
        userId: state => state.user.id,
        }),
    ...mapGetters(['isLoggedIn', 'user','getUsername']),
    artworkId() {
      return this.$route.params.artworkId;
    },
    artwork() {
      return this.$store.getters.getArtwork;
    },
  },
  async mounted() {
    if (!this.artwork) {
      await this.$store.dispatch('loadArtworkFromStorage');
    }

    if (!this.artwork) {
      this.$message.error("加载艺术品信息失败，请返回重新进入");
      return;
    }

    if (this.artwork.saleType === '拍卖') {
      await this.fetchAuctionInfo();
      await this.fetchUserRegistrationStatus();
    }
    this.checkFavoriteStatus();
  },
  methods: {
    toBiding(){
      const now = new Date();
      const startTime = new Date(this.auctionInfo.startTime).getTime();
      const endTime = new Date(this.auctionInfo.endTime).getTime() ;
      console.log(endTime)
      if (now < startTime) {
        this.$notify({
          message: '拍卖未开始',
          type: 'warning',
          position: 'bottom-right',
          duration: 2000
        });
        return; 
      }
      if (now > endTime) {
        this.$notify({
          message: '拍卖已结束',
          type: 'warning',
          position: 'bottom-right',
          duration: 2000
        });
        return; 
      }
      this.isBidding = !this.isBidding;
    },
    outBiding(){
      this.isBidding = !this.isBidding;
    },
    cancelClick(){
      this.drawer=false
      this.drawer3=false
    },
    cancelpay(){
      if(this.drawer2){
        this.drawer2=false;
        this.drawer=true
      }
      if(this.drawer4){
        this.drawer4=false;
        this.drawer3=true
      }
    },
    confirmClick(){
      if (!this.checked) {
            this.$notify({
              message: '请先阅读并勾选上述协议',
              type: 'warning',
              position: 'bottom-right',
              duration: 2000
            });
        return;
      }
      this.drawer =false;
      this.drawer2=true;
    },
    filterPhoneInput(event) {
      const input = event.target.value;
      const filteredInput = input.replace(/\D/g, '');
      this.user.phone = filteredInput;
    },
    async generateOrder(){
      const phone = this.user.phone? this.user.phone.trim() : '';
      const address = this.user.address? this.user.address.trim() : '';
      if(phone!== '' && address!== ''){
        try {
            const loading = this.$loading({
            lock: true,
            text: '订单生成中...',
            background: 'rgba(0, 0, 0, 0.7)',
          });
          await new Promise(resolve => setTimeout(resolve, 2000));
          this.orderData.artworkId=this.artwork.artworkId;
          this.orderData.buyerId=this.userId;
          this.orderData.sellerId =this.artwork.user.userId;
          this.orderData.finalPrice =this.artwork.fixedPrice;
          const updatedUser = await updateUserInfo(this.userId, this.user);
          const response = await  createOrder(this.orderData);
          if(response && updatedUser){
            this.$message.success("已生成订单")
            this.drawer3 =false;
            this.drawer4=true;
            loading.close();
          }
        } catch (error) {
          this.$message.error("订单生成失败")
        }
      }else{
        this.$notify({
          message: '请将个人信息填写完整',
          type: 'warning',
          position: 'bottom-right',
          duration: 2000
        });
      }
    },
    async buyArtwork(){
      if (!this.$store.getters.isLoggedIn) {
        this.$message.warning("请先登录")
        this.$router.push('/login-register');
      } else {
        this.drawer3=true;
        const userData =await  getUserById(this.userId);
        this.user = userData;
      }
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
            if(this.selectedPayment==2){
            this.$message.success("支付宝成功")
          }else if(this.selectedPayment==3){
            this.$message.success("微信成功")
          }else if(this.selectedPayment==4){
            this.$message.success("银行卡成功")
          }else{
            const userData = await getUserById(this.userId);
            let finalPrice;
            if(artwork.saleType=='直售'){
                finalPrice = artwork.fixedPrice
            }else{
                const response =await getAuctionsByArtworkId(artwork.artworkId)
                finalPrice =  response.data.currentHighestBid
            }
            if(userData.walletBalance >= finalPrice){
              const orderResponse =await updateOrderStatus(this.userId,artwork.artworkId,'Paid');
              const userResponse  =await updateBalance(this.userId,-finalPrice)
              if(orderResponse &&userResponse){
                this.$router.push({ name: 'SalePage' });
                this.$message.success("支付成功")
              }else{
                this.$message.error("支付失败")
              }
            }else{
              this.$message.error("余额不足")
            }
        }
      } catch (error) {
        this.$message.error(error.message || '支付失败，请重试');
      } finally {
        this.drawer2 =false;
        this.drawer4 =false;
        this.isPaying = false;
        loading.close();
      }
    },
     async payClick(){
      const loading = this.$loading({
        lock: true,
        text: '支付处理中...',
        background: 'rgba(0, 0, 0, 0.7)',
      });
      await new Promise(resolve => setTimeout(resolve, 2000));
      try {
          this.isPaying = true;
          if(this.selectedPayment==2){
          this.$message.success("支付宝成功")
        }else if(this.selectedPayment==3){
          this.$message.success("微信成功")
        }else if(this.selectedPayment==4){
          this.$message.success("银行卡成功")
        }else{
          const userData = await getUserById(this.userId);
              if(userData.walletBalance >= this.auctionInfo.depositAmount){
              const response =await registerForAuction(this.auctionInfo.auctionId,this.userId,this.auctionInfo.depositAmount);
              if(response){
                this.$message.success("报名成功")
              }else{
                this.$message.error("报名失败")
              }
            }else{
              this.$message.error("余额不足")
            }
        }
      } catch (error) {
        this.$message.error(error.message || '支付失败，请重试');
      } finally {
        this.drawer2 =false;
        this.drawer4 =false;
        this.isPaying = false;
        this.fetchUserRegistrationStatus();
        loading.close();
      }
    },
    signUpAuction(){
      if (!this.$store.getters.isLoggedIn) {
        this.$message.warning("请先登录")
        this.$router.push('/login-register');
      } else {
        this.drawer=true;
      }
    },
    async fetchUserRegistrationStatus() {
      try {
        const response = await checkUserRegistrationStatus(this.auctionInfo.auctionId,this.userId);
        this.UserRegistrationStatus = response;
      } catch (err) {
        this.error = 'Error checking user registration status.';
        console.error(err);
      } finally {
        this.loading = false;
      }
    },
    formatDate(dateString) {
      if (!dateString) {
      return '无日期信息'; 
      }
      const date = new Date(dateString);
      if (isNaN(date.getTime())) {
        return '无效日期';
      }
      const adjustedDate = new Date(date.getTime() -0);
      return adjustedDate.toLocaleString();
      },
    async fetchAuctionInfo() {
      try {
        const response = await getAuctionsByArtworkId(this.artworkId);
        this.auctionInfo = response.data;
      } catch (error) {
        console.error('获取拍卖信息失败:', error);
      }
    },
    async toggleFavorite() {
      if (!this.$store.getters.isLoggedIn) {
        this.$message.warning("请先登录");
        this.$router.push('/login-register');
      } else {
        this.isLoading = true; 
        try {
          if (this.isFavorite) {
            const response = await removeFavorite(this.userId, this.artwork.artworkId); 
            if (response) {
              await new Promise((resolve) => setTimeout(resolve, 1000)); 
              await this.checkFavoriteStatus();
            } else {
              this.$message.error("取消收藏失败");
            }
          } else {
            const response = await addFavorite(this.userId, this.artwork.artworkId);
            if (response) {
              await new Promise((resolve) => setTimeout(resolve, 1000)); 
              await this.checkFavoriteStatus();
              this.$message.success("收藏成功");
            } else {
              this.$message.error("收藏失败");
            }
          }
        } catch (error) {
          console.error("操作失败:", error);
          this.$message.error("操作失败，请重试");
        } finally {
          this.isLoading = false;
        }
      }
    },
    async checkFavoriteStatus() {
      try {
        const response = await checkIsFavorite(this.userId,this.artwork.artworkId)   
        this.isFavorite = response; 
      } catch (error) {
        console.error('检查收藏状态失败', error);
      }
    },
  }
};
  </script>
  
  <style scoped src="@/assets/css/Deal.css"></style>
  