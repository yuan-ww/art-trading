import { createApp } from 'vue';
import App from './App.vue';
import router from './router'; // 确保路径正确
import axios from 'axios';
import VueAxios from 'vue-axios';
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import store from './store';
import { API_BASE_URL } from './config/api';

const app = createApp(App);
app.use(store); // 注册 store
app.use(ElementPlus);
app.use(router); // 使用路由
app.config.globalProperties.$axios = axios;
axios.defaults.baseURL = API_BASE_URL;
axios.defaults.withCredentials = true;
app.use(VueAxios, axios);
app.mount('#app');
  
