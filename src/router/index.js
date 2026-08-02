import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  scrollBehavior() {
    return { top: 0 }
  },
  routes: [
    // 默认进入登录页
    { path: '/', redirect: '/login' },
    
    // 登录页（独立页面，不受Layout影响）
    { 
      path: '/login', 
      name: 'login', 
      component: () => import('../views/Login.vue'),
      meta: { public: true }
    },
    
    // 首页
    { path: '/home', name: 'home', component: () => import('../views/Home.vue') },
    
    // 其他页面
    { path: '/category', name: 'category', component: () => import('../views/Category.vue') },
    { path: '/cart', name: 'cart', component: () => import('../views/Cart.vue') },
    { path: '/user', name: 'user', component: () => import('../views/User.vue') },
    { path: '/goods/:id', name: 'goodsDetail', component: () => import('../views/GoodsDetail.vue') },
    { path: '/order-confirm', name: 'orderConfirm', component: () => import('../views/OrderConfirm.vue') },
    { path: '/orders', name: 'orders', component: () => import('../views/OrderList.vue') },
    { path: '/order-detail/:orderNo', name: 'orderDetail', component: () => import('../views/OrderDetail.vue') },
    { path: '/search', name: 'search', component: () => import('../views/Search.vue') }
  ]
})

export default router