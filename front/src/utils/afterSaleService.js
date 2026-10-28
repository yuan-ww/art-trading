import axios from 'axios';
import { API_BASE_URL } from '../config/api';
const base = `${API_BASE_URL}/api/after-sales`;
export const requestReturn = (orderId, reason) => axios.post(`${base}/orders/${orderId}`, null, { params: { reason } });
export const getReturn = (orderId) => axios.get(`${base}/orders/${orderId}`);
export const confirmReturn = (requestId) => axios.put(`${base}/${requestId}/confirm-return`);
export const rejectReturn = (requestId) => axios.put(`${base}/${requestId}/reject`);
