<template>
  <div v-if="shippingId">
    <h4>物流轨迹</h4>
    <el-timeline><el-timeline-item v-for="event in events" :key="event.id" :timestamp="event.occurredAt">{{ event.description }}</el-timeline-item></el-timeline>
    <template v-if="editable"><el-input v-model="description" placeholder="输入物流节点" maxlength="500" /><el-button @click="add">添加节点</el-button></template>
  </div>
</template>
<script>
import axios from 'axios';
import { API_BASE_URL } from '../config/api';
export default {
  props: { shippingId: Number, editable: Boolean },
  data: () => ({ events: [], description: '' }),
  watch: { shippingId: { immediate: true, handler() { this.load(); } } },
  methods: {
    async load() { if (!this.shippingId) return; try { this.events = (await axios.get(`${API_BASE_URL}/api/shipping-events/${this.shippingId}`)).data; } catch { this.$message.error('加载物流轨迹失败'); } },
    async add() { if (!this.description.trim()) return; try { await axios.post(`${API_BASE_URL}/api/shipping-events/${this.shippingId}`, null, { params: { description: this.description } }); this.description = ''; await this.load(); } catch { this.$message.error('添加物流节点失败'); } }
  }
};
</script>
