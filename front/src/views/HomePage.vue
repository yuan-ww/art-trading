<template>
  <div class="root-container">
    <div class="Topmenu">
      <div class="homelogo">
      <img src="@/assets/images/logo.png" alt="logo" />
    </div>
    <span style="color: aliceblue;font-size: large;">后台管理</span>
    <div class="user-section">
      <img :src="avatarUrl || require('@/assets/images/avatars/avatar.png')" alt="头像" class="avatar"/>
      <template v-if="isLoggedIn">      
      <el-dropdown @command="handleCommand" v-if="isLoggedIn">
        <span class="el-dropdown-link">
          {{ username }} <el-icon class="el-icon--right"></el-icon>
        </span>
        <template v-slot:dropdown>
          <el-dropdown-menu>
            <el-dropdown-item @click="showUserProfile = true" command="profile">个人信息</el-dropdown-item>
            <el-dropdown-item index="settings" command="settings">修改密码</el-dropdown-item>
            <el-dropdown-item command="logout" >退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </template>
    <template v-else>
      <span class="login-link" @click="goToLogin">
        请登录
      </span>
    </template>
    </div>
  </div>
    <el-row class="navbar">
      <el-col :span="18">
        <el-menu
          :default-active="activeMenu" 
          class="el-menu-vertical-demo"
          @select="handleMenuSelect"
        >
          <el-menu-item index="system">
            <img src="@/assets/images/icons/home.png" alt="系统首页" class="menu-icon" />
            <span>系统首页</span>
          </el-menu-item>
          <el-sub-menu index="auction">
            <template #title>
              <img src="@/assets/images/icons/audit.png" alt="上架审核" class="menu-icon" />
              <span>审核上架</span>
            </template>
            <el-menu-item index="product-6" >直售审核</el-menu-item>
              <el-menu-item index="product-1" >拍卖审核</el-menu-item>
          </el-sub-menu>
          <el-sub-menu index="product">
            <template #title>
              <img src="@/assets/images/icons/product.png" alt="商品管理" class="menu-icon" />
              <span>商品管理</span>
            </template>
              <el-menu-item index="product-2" >商品列表</el-menu-item>
              <el-menu-item index="product-3" >商品分类</el-menu-item>
              <el-menu-item index="product-4" >订单列表</el-menu-item>
              <el-menu-item index="product-5" >物流列表</el-menu-item>
          </el-sub-menu>
          <el-sub-menu index="user">
            <template #title>
              <img src="@/assets/images/icons/user.png" alt="系统首页" class="menu-icon" />
              <span>用户管理</span>
            </template>
             <el-menu-item index="user-1" >管理员</el-menu-item>
              <el-menu-item index="user-2" >普通用户</el-menu-item>
          </el-sub-menu>
          <el-menu-item index="carousel">
            <img src="@/assets/images/icons/carousel.png" alt="轮播" class="menu-icon" />
            <span>轮播管理</span>
          </el-menu-item>
          <el-menu-item index="logout">
            <img src="@/assets/images/icons/out.png" alt="系统首页" class="menu-icon" />
            <span>退出系统</span>
          </el-menu-item>
        </el-menu>
      </el-col>
      <el-col :span="6"> 
        <div class="data-management">
          <div v-if="activeContent === 'system'"> 
            <ManageHome/>
          </div>
          <div v-if="activeContent === 'product-1'"> 
            <AuctionAudit/>
          </div>
          <div v-if="activeContent === 'product-6'"> 
            <SaleAudit/>
          </div>
          <div v-if="activeContent === 'product-2'">
            <ArtworksManage/>
          </div>
          <div v-if="activeContent === 'product-3'"> 
            <CategoryManage/>
          </div>
          <div v-if="activeContent === 'product-4'"> 
            <OrdersManage/>
          </div>
          <div v-if="activeContent === 'product-5'"> 
            <ShippingManage/>
          </div>
          <div v-if="activeContent === 'user-1'"> 
            <AdminManage/>
          </div>
          <div v-if="activeContent === 'user-2'"> 
            <UsersManage/>
          </div>
          <div v-if="activeContent === 'carousel'">
            <CarouselManage/>
          </div>
          <div v-if="activeContent === 'logout'">

          </div>
          <div v-if="activeContent === 'profile'">
            <UserDetails />
          </div>
          <div v-if="activeContent === 'settings'">
            <ChangePassword/>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>
<script>
import { uploadAvatar} from '../utils/api';
import { mapState, mapActions, mapGetters} from 'vuex'; 
import UserDetails from "@/components/UserDetails.vue";
import ChangePassword from '@/components/ChangePassword.vue';
import AdminManage from '@/components/AdminManage.vue';
import UsersManage from '@/components/UsersManage.vue';
import CategoryManage from '@/components/CategoryManage.vue';
import ArtworksManage from '@/components/ArtworksManage.vue';
import AuctionAudit from '@/components/AuctionAudit.vue';
import SaleAudit from '@/components/SaleAudit.vue';
import OrdersManage from '@/components/OrdersManage.vue';
import ShippingManage from '@/components/ShippingManage.vue';
import CarouselManage from '@/components/CarouselManage.vue';
import ManageHome from '@/components/ManageHome.vue';
export default {
  components: {
    UserDetails,
    ChangePassword,
    AdminManage,
    UsersManage,
    CategoryManage,
    ArtworksManage,
    AuctionAudit,
    SaleAudit,
    OrdersManage,
    ShippingManage,
    CarouselManage,
    ManageHome
  },
  data() {
    return {
      activeContent: 'system', // 默认显示的内容
      activeMenu: 'system',
      selectedFile: null,
      users: [],
    }; 
  },
  computed: {
    ...mapGetters(['getUsername', 'getAvatarUrl','isLoggedIn','getUserRole']),
    username() {
      return this.getUsername;
    },
    avatarUrl() {
      return this.getAvatarUrl;
    },
    role(){
      return this.getUserRole
    },
    ...mapState({
      username: state => state.user.username, 
      userId: state => state.user.id, 
      avatarUrl: state => state.user.avatarUrl,
      role:state => state.user.role
    }),
  },
  methods: {
    ...mapActions(['login', 'logout']), 
    handleMenuSelect(index) {
      this.activeContent = index;
      if (index === 'logout') {
        this.$router.push('/'); 
      }
    },
    handleCommand(command) {
      if (command === "profile") {
        this.activeContent = "profile"; 
      } else if (command === "settings") {
        this.activeContent = "settings";
      } else if (command === "logout") {
        this.$router.push('/'); 
        this.$store.dispatch('logout');
        this.$message.success("已退出登录")
      }
    } ,
    goToLogin() {
      this.$router.push({ name: 'LoginRegister' });
    },
    onFileSelected(event) {
      this.selectedFile = event.target.files[0];
    },
    async uploadAvatar() {
      if (!this.selectedFile) {
        alert('请选择要上传的文件');
        return;
      }
      try {
        const response = await uploadAvatar(this.userId, this.selectedFile);
        this.avatarUrl = response.data;
      } catch (error) {
        console.error('头像上传失败', error);
      }
    }
  }
};
</script>
<style scoped src="@/assets/css/HomePage.css"></style>
