<template>
  <div>
    <h2>钱包流水</h2>
    <p>仅显示启用流水功能后的记录；历史余额不会自动生成流水。</p>
    <el-button @click="load">刷新</el-button>
    <el-table :data="transactions" v-loading="loading">
      <el-table-column prop="createdAt" label="时间" />
      <el-table-column prop="note" label="业务" />
      <el-table-column prop="orderId" label="订单号" />
      <el-table-column prop="amount" label="金额变动" />
      <el-table-column prop="balanceAfter" label="变动后余额" />
    </el-table>
    <el-pagination layout="prev, pager, next" :total="total" :page-size="20" :current-page="page + 1" @current-change="changePage" />
  </div>
</template>
<script>
import axios from 'axios';
import { API_BASE_URL } from '../config/api';
export default {
  data: () => ({ transactions: [], total: 0, page: 0, loading: false }),
  async mounted() { await this.load(); },
  methods: {
    async load() {
      this.loading = true;
      try {
        const id = this.$store.state.user.id;
        const response = await axios.get(`${API_BASE_URL}/api/wallet/${id}/transactions/page`, { params: { page: this.page, size: 20 } });
        this.transactions = response.data.content;
        this.total = response.data.totalElements;
      } catch (error) { this.$message.error('获取钱包流水失败'); }
      finally { this.loading = false; }
    },
    changePage(page) { this.page = page - 1; this.load(); }
  }
};
</script>
