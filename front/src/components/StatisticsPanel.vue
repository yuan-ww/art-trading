<template>
  <div><h2>运营统计</h2><el-button @click="load">刷新</el-button>
    <el-descriptions :column="1" border>
      <el-descriptions-item label="用户数">{{ stats.users }}</el-descriptions-item>
      <el-descriptions-item label="作品数">{{ stats.artworks }}</el-descriptions-item>
      <el-descriptions-item label="拍卖数">{{ stats.auctions }}</el-descriptions-item>
      <el-descriptions-item label="订单数">{{ stats.orders }}</el-descriptions-item>
      <el-descriptions-item label="当前已支付订单">{{ stats.paidOrders }}</el-descriptions-item>
    </el-descriptions>
  </div>
</template>
<script>
import axios from 'axios';
import { API_BASE_URL } from '../config/api';
export default { data: () => ({ stats: {} }), async mounted() { await this.load(); }, methods: {
  async load() { try { this.stats = (await axios.get(`${API_BASE_URL}/api/admin/statistics`)).data; } catch { this.$message.error('加载统计失败'); } }
} };
</script>
