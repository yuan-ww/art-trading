<template>
  <div class="auction-bidding">
    <div class="auction-info">
      <h2>《{{ auctionData?.artwork?.name }}》</h2>
      <div class="stats">
        <div class="stat-item">
          <span class="label">参与人数: {{ countRegistration }}</span>
          <img src="@/assets/images/icons/people.png" alt="参与人数图标" class="stat-icon label">
          <span class="value"></span>
        </div>
        <div class="stat-item">
          <span class="label value">最高价:￥{{ auctionData?.currentHighestBid}}</span>
        </div>
        <div class="stat-item">
          <span class="label">出价者: {{ auctionData?.highestBidder?.username}}</span>
        </div>
        <div class="stat-item">  
          <p v-if="countdown > 0" class="label value">剩余时间:{{ formattedCountdown }}<img src="@/assets/images/icons/time.png" alt="倒计时" class="stat-icon label"></p>
          <p v-else class="label value">拍卖已结束！</p>
        </div>
      </div>
    </div>
    <div class="bidding-history" >
      <h3>最新出价记录</h3>
      <div class="bidding-records-list">
        <div v-for="(bid, index) in sortedBiddingRecordsByAmount.slice(0, 5)" :key="index" class="bidding-record">
            <img :src="bid.user.avatar || require('@/assets/images/icons/people2.png')" alt="头像" class="stat-icon">
            <span class="bid-info">{{ bid.user.username }} for:￥{{ bid.bidAmount }}</span>
          <span class="bid-time">{{ bid.bidTime}}</span>
        </div>
      </div>
    </div>
    <div v-if="countdown > 0" class="bidding-action">
      <el-button class="quickBid" @click="replace">重置</el-button>
      <el-input-number clsss="inputnumber" v-model="bid.bidAmount" :step="100" :min="0" />
          <el-button class="quickBid" @click="setQuickBid(200)">+200</el-button>
          <el-button class="quickBid" @click="setQuickBid(500)">+500</el-button>
          <el-button  class="quickBid" @click="setQuickBid(1000)">+1000</el-button>
        <el-button 
          class="place-bid-btn"
          type="warning"
          @click="open"
          :loading="isBidding"
        >
        出价
        </el-button>
    </div>
  </div>
</template>

<script>
import { ElMessageBox } from 'element-plus'
import {getAuctionById,settleAuction} from '../utils/auctionService';
import {createBid,getBidsByAuction} from '../utils/bidService';
import {countRegistrationsForAuction,settleDeposit} from '../utils/registerService';
import { mapState } from 'vuex';
export default {
  props: ['auctionId'],
  data() {
    return {
        auctionData: null,
        isBidding: false,
        registrationsforauction:null,
        countRegistration:null,
        biddingRecords: [],
        deadline: null,
        countdown: 0,
        timer: null,
        bid:{
            user:{},
            auctionId: null,
            userId: null,
            bidAmount:''
        } 
    }
  },
  computed: {
    ...mapState({
        userId: state => state.user.id,
        }),
     sortedBiddingRecordsByAmount() {
      return this.biddingRecords.slice().sort((a, b) => {
        return b.bidAmount - a.bidAmount;
      });
    },
     formattedCountdown() {
      const hours = String(Math.floor(this.countdown / 3600)).padStart(2, "0");
      const minutes = String(Math.floor((this.countdown % 3600) / 60)).padStart(2, "0");
      const seconds = String(this.countdown % 60).padStart(2, "0");
      return `${hours}:${minutes}:${seconds}`;
    },
  },
  mounted() {
    this.startCountdown(); 
    this.fetchData()
        getAuctionById(this.auctionId).then(response => {
        this.auctionData = response;
      })
      .catch(error => {
        console.error('获取拍卖数据失败:', error);
      });
     countRegistrationsForAuction(this.auctionId).then(response => {
        this.registrationsforauction = response;
        this.countRegistration=response.length;
      })
      .catch(error => {
        console.error('获取拍卖数据失败:', error);
      });
  },
  beforeUnmount() {
    clearInterval(this.timer);
  },
  methods: {
    calculateCountdown() {
      if (!this.deadline) return 0;
      const now = new Date().getTime();
      return Math.max(Math.floor((this.deadline - now) / 1000), 0);
    }, 
    async sendUpdateRequest() {
      settleAuction(this.auctionData.auctionId) 
        .then((data) => {
          console.log("拍卖结算结果:", data); 
        })
        .catch((error) => {
          console.error("结算失败:", error); 
        });
        settleDeposit(this.auctionData.auctionId) 
        .then((data) => {
          console.log("押金退还结果:", data); 
        })
        .catch((error) => {
          console.error("退还失败:", error); 
        });
    },

    startCountdown() {
      if (!this.deadline) return; 
      this.countdown = this.calculateCountdown();
      this.timer = setInterval(() => {
        this.countdown = this.calculateCountdown();
        if (this.countdown <= 0) {
          clearInterval(this.timer);
          this.sendUpdateRequest();
        }
      }, 1000);
    },
    async fetchData() {
      try {
        const [bidsResponse, auctionResponse, registrationsResponse] = await Promise.all([
          getBidsByAuction(this.auctionId),
          getAuctionById(this.auctionId),
          countRegistrationsForAuction(this.auctionId)
        ]);
        this.biddingRecords = bidsResponse;
        this.auctionData = auctionResponse;
        this.registrationsforauction = registrationsResponse;
        this.countRegistration = registrationsResponse.length;
      } catch (error) {
        console.error('获取数据失败:', error);
      }
    },
    async placeBid() {
    try {
        if (this.bid.bidAmount > this.auctionData.currentHighestBid && this.bid.bidAmount >this.auctionData?.artwork?.fixedPrice) {
            const response = await createBid(this.auctionData.auctionId, this.userId, this.bid.bidAmount);
            if (response.status === 200) {
                this.auctionData.currentHighestBid = this.bid.bidAmount;
                this.$message.success("出价成功");
                await this.fetchData();
            }
        } else {
            this.$message.error("出价失败,不得低于当前最高价");
        }
    } catch (error) {
        console.error('出价出错:', error);
        this.$message.error("出价失败");
    }
  },
  open() {
  if (this.auctionData.highestBidder.userId === this.userId) {
    ElMessageBox.confirm(
      '你当前是最高出价者，是否仍要继续出价？',
      '重复出价确认',
      {
        confirmButtonText: '继续出价',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
      .then(() => {
        this.placeBid()
      })
      .catch(() => {
        this.$message.info('已取消出价')
      })
  } else {
    this.placeBid()
  }
},
  setQuickBid(amount) {
        const currentBidAmount = parseFloat(this.bid.bidAmount) || 0;
        this.bid.bidAmount = currentBidAmount + amount;
    },
    replace(){
      this.bid.bidAmount=0;
    }
  },
  watch: {
    "auctionData.endTime": {
      immediate: true, 
      handler(newEndTime) {
        if (newEndTime) {
          this.deadline = new Date(newEndTime).getTime();
          this.startCountdown(); 
        }
      },
    },
  },
}
</script>

<style scoped src="@/assets/css/Bid.css"></style>