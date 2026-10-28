<template>
  <div>
    <h2>消息通知</h2>
    <el-button @click="load">刷新</el-button>
    <el-empty v-if="!notifications.length" description="暂无通知" />
    <el-table v-else :data="notifications">
      <el-table-column prop="createdAt" label="时间" />
      <el-table-column prop="message" label="消息" />
      <el-table-column label="状态"><template #default="{ row }">
        <span v-if="row.read">已读</span><el-button v-else @click="markRead(row)">标记已读</el-button>
      </template></el-table-column>
    </el-table>
    <el-pagination layout="prev, pager, next" :total="total" :page-size="20" :current-page="page + 1" @current-change="changePage" />
  </div>
</template>
<script>
import axios from 'axios';
import { API_BASE_URL } from '../config/api';
export default {
  data: () => ({ notifications: [], total: 0, page: 0 }),
  async mounted() { await this.load(); },
  methods: {
    async load() { try { const { data } = await axios.get(`${API_BASE_URL}/api/notifications/users/${this.$store.state.user.id}/page`, { params: { page: this.page, size: 20 } }); this.notifications = data.content; this.total = data.totalElements; } catch { this.$message.error('加载通知失败'); } },
    changePage(page) { this.page = page - 1; this.load(); },
    async markRead(row) { try { await axios.put(`${API_BASE_URL}/api/notifications/${row.id}/read`, null, { params: { userId: this.$store.state.user.id } }); await this.load(); } catch { this.$message.error('更新通知失败'); } }
  }
};
</script>
