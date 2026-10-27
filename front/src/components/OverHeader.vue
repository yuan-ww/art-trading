<template>
  <div v-if="!$route.meta.hideHeaderAndFooter" class="top-bar">
    <el-button @click="handleAction" size="small" >{{ isLoggedIn ? getUsername : '未登录' }}</el-button>
      <el-button v-if="isLoggedIn" @click="goToProfile" size="small" >个人中心</el-button>
      <el-button v-if="isLoggedIn" @click="handleLogout" size="small" >退出</el-button>
    </div>
  <header v-if="!$route.meta.hideHeaderAndFooter" class="header">
    <div class="logo">
      <img src="@/assets/images/logo.png" alt="Logo" />
    </div>
    <nav class="nav">
      <ul>
        <li><router-link to="/" exact-active-class="active">首页</router-link></li>
        <li><router-link to="/auctionpage" exact-active-class="active">拍卖</router-link></li>
        <li><router-link to="/salepage" exact-active-class="active">直售</router-link></li>
        <li><router-link to="/servicepage" exact-active-class="active">服务</router-link></li>
        <li><router-link to="/storypage" exact-active-class="active">故事</router-link></li>
      </ul>
    </nav>
    <div class="search">
      <el-input
        v-model="searchQuery"
        placeholder="Search..."
        clearable
        @clear="handleClear"
        @keyup.enter="handleSearch"
      >
        <template #append>
          <el-button @click="handleSearch">
            <img src="@/assets/images/icons/search.png" alt="Search" style="width: 18px; height: 18px;" />
          </el-button>
        </template>
      </el-input>
    </div>
  </header>
</template>
<script>
import { mapGetters } from 'vuex';
export default {
  data() {
    return {
      searchQuery: '', 
    };
  },
  mounted() {
    this.$store.dispatch('loadUserFromStorage');
  },
  computed: {
    ...mapGetters(['isLoggedIn', 'user','getUsername']),
  },
  methods: {
    handleSearch() {
      if (this.searchQuery.trim()) {
        this.$router.push({
          name: 'AllArtworkPage',
          query: { q: this.searchQuery.trim() }
        });
      }
    },
    handleClear() {
      console.log('Search cleared');
    },
    handleAction() {
      if (!this.$store.getters.isLoggedIn) {
        this.$router.push('/login-register');
      } else {
        this.$router.push('/personalcenter');
      }
    },
    goToProfile() {
        this.$router.push('/personalcenter');
    },
    handleLogout() {
      this.$router.push('/');
      this.$message.success("已退出系统")
      this.$store.dispatch('logout');

    },
  },
  watch: {
  isLoggedIn(newValue) {
    if (newValue) {
      console.log('用户已登录');
    } else {
      console.log('用户未登录');
    }
  },
},
};
</script>
  <style>
.top-bar {
  display: flex;
  justify-content: flex-end;
  padding: 0.2rem 0.5rem;
  background-color: #f7f7f7;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}
.top-bar .el-button {
  background-color: #333;
  color: #ffffff;
  font-size: 0.8rem;
  margin-left: 1rem;
}
.top-bar .el-button:hover {
  background-color: #e6e6e6 !important;  /* 加上 !important 强制生效 */
  color: #000000 !important;
}
.header {
  margin-bottom: 5px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0.5rem 1rem;
  background-color: #fff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.logo {
  display: flex;
  align-items: center;
}

.logo img {
  width: auto;
  height: 50px;
  margin-right: 0.5rem;
}

.logo span {
  font-size: 2.1rem;
  font-weight: bold;
  color: #333;
}
.nav{
  margin-right: 20%;
}
.nav ul {
  display: flex;
  list-style: none;
  margin: 0;
  padding: 0;
}

.nav li {
  margin: 0 1rem;
}

.nav a {
  text-decoration: none;
  font-size: 1rem;
  color: #555;
  transition: color 0.3s;
}

.nav a:hover {
  color: #007bff;
}
.nav a.active {
  position: relative;
  text-decoration: none;
}

.nav a.active::after {
  content: "";
  position: absolute;
  bottom: -24px;
  left: 0;
  width: 100%;
  height: 5px; 
  background-color: #555;
}

.search {
  display: flex;
  align-items: center;
  max-width: 300px;
  width: 100%;
}
  </style>
  