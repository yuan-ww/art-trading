<template>
    <div class="root-container">
      <div class="Topmenu">
        <div class="homelogo">
        <img src="@/assets/images/logo.png" alt="logo" />
      </div>
      <span style="color: aliceblue;font-size: large;">个人中心</span>
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
              <el-dropdown-item v-if="role==='Admin'" command="manage">后台管理</el-dropdown-item>
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
              <img src="@/assets/images/icons/home.png" alt="个人信息" class="menu-icon" />
              <span>个人信息</span>
            </el-menu-item>
            <el-menu-item index="product-1">
              <img src="@/assets/images/icons/product.png" alt="个人信息" class="menu-icon" />
              <span>我的发布</span>
            </el-menu-item>
            <el-menu-item index="product-2">
              <img src="@/assets/images/icons/audit.png" alt="个人信息" class="menu-icon" />
              <span>拍卖管理</span>
            </el-menu-item>
            <el-sub-menu index="user">
              <template #title>
                <img src="@/assets/images/icons/deal.png" alt="系统首页" class="menu-icon" />
                <span>我的交易</span>
              </template>
               <el-menu-item index="deal-1" >我卖出的</el-menu-item>
                <el-menu-item index="deal-2" >我买到的</el-menu-item>
                <el-menu-item index="deal-3" >我出价的</el-menu-item>
            </el-sub-menu>
            <el-menu-item index="favoriet">
              <img src="@/assets/images/icons/favoriet.png" alt="退出" class="menu-icon" />
              <span>我的收藏</span>
            </el-menu-item>
            <el-menu-item index="wallet">钱包流水</el-menu-item>
            <el-menu-item index="notifications">消息通知</el-menu-item>
            <el-menu-item index="logout">
              <img src="@/assets/images/icons/out.png" alt="退出" class="menu-icon" />
              <span>退出</span>
            </el-menu-item>
          </el-menu>
        </el-col>
        <el-col :span="6"> 
          <div class="data-management">
            <div v-if="activeContent === 'system'"> 
                <UserDetails />
            </div>
            <div v-if="activeContent === 'product-1'"> 
              <PLSale ref="plSaleRef"/>
            </div>
            <div v-if="activeContent === 'product-6'"> 
            </div>
            <div v-if="activeContent === 'product-2'">
              <PLAuction/>
            </div>
            <div v-if="activeContent === 'deal-1'"> 
              <PLSeller/>
            </div>
            <div v-if="activeContent === 'deal-2'"> 
              <PLBuyer/>
            </div>
            <div v-if="activeContent === 'deal-3'"> 
              <PLBids/>
            </div>
            <div v-if="activeContent === 'favoriet'">
              <PLFavorite/>
            </div>
            <div v-if="activeContent === 'wallet'"><WalletHistory /></div>
            <div v-if="activeContent === 'notifications'"><NotificationsPanel /></div>
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
  import PLSale from '@/components/PLSale.vue';
  import PLAuction from '@/components/PLAuction.vue';
  import PLSeller from '@/components/PLSeller.vue';
  import PLBuyer from '@/components/PLBuyer.vue';
  import PLBids from '@/components/PLBids.vue';
  import PLFavorite from '@/components/PLFavorite.vue';
  import WalletHistory from '@/components/WalletHistory.vue';
  import NotificationsPanel from '@/components/NotificationsPanel.vue';
  export default {
    components: {
      UserDetails,
      ChangePassword,
      PLSale,
      PLAuction,
      PLSeller,
      PLBuyer,
      PLBids,
      PLFavorite,
      WalletHistory,
      NotificationsPanel,
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
        return this.getUserRole;
      },
      ...mapState({
        username: state => state.user.username, 
        userId: state => state.user.id, 
        avatarUrl: state => state.user.avatarUrl 
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
          this.logout();
          this.$router.push('/'); 
          this.$message.success("已退出系统")
        }else if(command === "manage"){
          this.$router.push('/homepage'); 
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
    },
    mounted() {
      const query = this.$route.query
      if (query.activate) {
        this.activeContent = query.activate
      }
      this.$nextTick(() => {
        if (query.activate === 'product-1' && query.showEditDialog === 'true') {
          this.$refs.plSaleRef?.handleAddArtwork()
        }
      })
    }
  };
  </script>
  <style scoped src="@/assets/css/HomePage.css"></style>
