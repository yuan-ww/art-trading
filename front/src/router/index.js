import { createRouter, createWebHistory } from 'vue-router'
import LoginRegister from '../views/loginRegister.vue'
import HomePage from '../views/HomePage.vue'
import StartPage from '../views/StartPage.vue'
import SalePage from '../views/SalePage.vue'
import AuctionPage from '../views/AuctionPage.vue'
import AllArtworkPage from '../views/AllArtworkPage.vue'
import DetailsWithDeal from '../views/DetailsWithDeal.vue'
import PersonalCenter from '../views/PersonalCenter.vue'
import AuctionEnter from '../views/AuctionEnter.vue'
import ServicePage from '../views/ServicePage.vue'
import StoryPage from '../views/StoryPage.vue'
const routes = [
  {
    path: '/',
    name: 'StartPage',
    component: StartPage, 
  },
  {
    path: '/salepage',
    name: 'SalePage',
    component: SalePage, 
  },
  {
    path: '/auctionpage',
    name: 'AuctionPage',
    component: AuctionPage, 
  },
  {
    path: '/servicepage',
    name: 'ServicePage',
    component: ServicePage, 
  },
  {
    path: '/storypage',
    name: 'StoryPage',
    component: StoryPage, 
  },
  {
    path: '/allartworkpage',
    name: 'AllArtworkPage',
    component: AllArtworkPage, 
  },
  {
    path: '/detailsWithdeal/:artworkId',
    name: 'DetailsWithDeal',
    component: DetailsWithDeal, 
  },
  {
    path: '/login-register',
    name: 'LoginRegister',
    component: LoginRegister,
    meta: { hideHeaderAndFooter: true } 
  },
  {
    path: '/homepage',
    name: 'HomePage',
    component: HomePage,
    meta: { hideHeaderAndFooter: true }  
  },
  {
    path: '/personalcenter',
    name: 'PersonalCenter',
    component: PersonalCenter,
    meta: { hideHeaderAndFooter: true }  
  },
  {
    path: '/auctionenter',
    name: 'AuctionEnter',
    component: AuctionEnter,
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      // 如果有保存的位置（如浏览器后退），滚动到保存的位置
      return savedPosition;
    } else {
      // 否则，滚动到顶部
      return { top: 0 };
    }
  },
})

// 可选的全局导航守卫
router.beforeEach((to, from, next) => {
  if (to.meta.requiresAuth && !isAuthenticated()) { // isAuthenticated() 是一个示例函数
    next('/login-register') // 重定向到登录注册页面
  } else {
    next()
  }
})

function isAuthenticated() {
  // 在这里放置身份验证逻辑
  return true // 替换为登录状态的判断
}

export default router
