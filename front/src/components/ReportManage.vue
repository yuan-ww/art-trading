<template>
  <div><h2>举报处理</h2><el-button @click="load">刷新</el-button>
    <el-table :data="reports">
      <el-table-column prop="id" label="编号" /><el-table-column prop="artworkId" label="作品编号" />
      <el-table-column prop="reporterId" label="举报人" /><el-table-column prop="reason" label="原因" />
      <el-table-column prop="status" label="状态" /><el-table-column label="操作"><template #default="{ row }">
        <el-button v-if="row.status === 'Pending'" @click="resolve(row)">标记已处理</el-button>
      </template></el-table-column>
    </el-table>
    <el-pagination layout="prev, pager, next" :total="total" :page-size="20" :current-page="page + 1" @current-change="changePage" />
  </div>
</template>
<script>
import axios from 'axios'; import { API_BASE_URL } from '../config/api';
export default { data: () => ({ reports: [], total: 0, page: 0 }), async mounted() { await this.load(); }, methods: {
  async load() { try { const { data } = await axios.get(`${API_BASE_URL}/api/reports`, { params: { page: this.page, size: 20 } }); this.reports = data.content; this.total = data.totalElements; } catch { this.$message.error('获取举报失败'); } },
  changePage(page) { this.page = page - 1; this.load(); },
  async resolve(row) { try { await axios.put(`${API_BASE_URL}/api/reports/${row.id}/resolve`); await this.load(); } catch { this.$message.error('处理举报失败'); } }
} };
</script>
