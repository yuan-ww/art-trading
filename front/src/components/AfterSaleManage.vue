<template>
  <div><h2>售后处理</h2><el-button @click="load">刷新</el-button>
    <el-table :data="requests">
      <el-table-column prop="requestId" label="申请号" /><el-table-column prop="orderId" label="订单号" />
      <el-table-column prop="buyerId" label="买家" /><el-table-column prop="reason" label="原因" />
      <el-table-column prop="status" label="状态" />
      <el-table-column label="操作"><template #default="{ row }">
        <el-button v-if="row.status !== 'Refunded'" @click="refund(row)">确认退货并退款</el-button>
      </template></el-table-column>
    </el-table><el-pagination layout="prev, pager, next" :total="total" :page-size="20" :current-page="page + 1" @current-change="changePage" />
    <p>请在确认退货或核实争议后再执行退款。退款需卖家余额足够。</p>
  </div>
</template>
<script>
import axios from 'axios';
import { API_BASE_URL } from '../config/api';
const base = `${API_BASE_URL}/api/after-sales`;
export default { data: () => ({ requests: [], total: 0, page: 0 }), async mounted() { await this.load(); }, methods: {
  async load() { try { const { data } = await axios.get(`${base}/page`, { params: { page: this.page, size: 20 } }); this.requests = data.content; this.total = data.totalElements; } catch { this.$message.error('获取售后申请失败'); } },
  changePage(page) { this.page = page - 1; this.load(); },
  async refund(row) { try { await this.$confirm('确认已核实退货并执行退款？', '管理员处理'); await axios.put(`${base}/${row.requestId}/admin-refund`); this.$message.success('已退款'); await this.load(); } catch (error) { if (error !== 'cancel' && error !== 'close') this.$message.error(error.response?.data || '退款失败'); } }
} };
</script>
