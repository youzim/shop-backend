<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useCartStore } from './stores/cart'
import { useUserStore } from './stores/user'
import { ref } from 'vue'

const searchKeyword = ref('')

const doSearch = () => {
  const kw = searchKeyword.value.trim()
  if (!kw) return
  router.push(`/search?keyword=${encodeURIComponent(kw)}`)
  searchKeyword.value = ''
}
const route = useRoute()
const router = useRouter()
const cartStore = useCartStore()
const userStore = useUserStore()

// 判断是否是独立页面（不显示顶部和底部）
const isStandalonePage = computed(() => {
  return route.path === '/login'
})

const isActive = (path) => route.path === path

</script>

<template>
  <!-- 登录页：完全独立，没有任何导航和页脚 -->
  <template v-if="isStandalonePage">
    <router-view />
  </template>

  <!-- 其他页面：带顶部导航 + 页脚的商城布局 -->
  <div v-else class="pc-layout">
    <!-- 顶部通栏 -->
    <header class="top-bar">
      <div class="container">
        <div class="top-left">
          <span>欢迎来到商城</span>
          <template v-if="!userStore.isLogin">
            <a href="#" @click.prevent="router.push('/login')">请登录</a>
            <a href="#" @click.prevent="router.push('/login')">免费注册</a>
          </template>
          <template v-else>
            <span class="user-name">Hi，{{ userStore.userInfo?.name || '用户' }}</span>
            <a href="#" @click.prevent="userStore.logout(); router.push('/login')">退出</a>
          </template>
        </div>
       <div class="top-right">
  <a href="#" @click.prevent="userStore.isLogin ? router.push('/orders') : router.push('/login')">我的订单</a>
  <a href="#" @click.prevent="userStore.isLogin ? router.push('/user') : router.push('/login')">个人中心</a>
  <a href="#" @click.prevent="router.push('/cart')">我的购物车({{ cartStore.totalNum }})</a>
  <a href="#">我的收藏</a>
  <a href="#">客户服务</a>
</div>
      </div>
    </header>

    <!-- 搜索头部 -->
    <div class="header-main">
      <div class="container header-flex">
        <div class="logo" @click="router.push('/home')">商城</div>
        <div class="search-box">
  <input 
    v-model="searchKeyword" 
    type="text" 
    placeholder="搜索商品名称" 
    @keyup.enter="doSearch"
  />
  <button @click="doSearch">搜索</button>
</div>
        <div class="header-cart" @click="router.push('/cart')">
          <span class="cart-icon">🛒</span>
          <span>购物车</span>
          <span v-if="cartStore.totalNum > 0" class="cart-badge">{{ cartStore.totalNum }}</span>
        </div>
      </div>
    </div>

    <!-- 导航菜单 -->
    <nav class="main-nav">
      <div class="container">
        <div class="nav-all" :class="{ active: isActive('/home') }" @click="router.push('/home')">首页</div>
        <div class="nav-list">
          <a href="#" @click.prevent="router.push('/category')" :class="{ active: isActive('/category') }">全部商品分类</a>
          <a href="#">手机数码</a>
          <a href="#">电脑办公</a>
          <a href="#">家用电器</a>
          <a href="#">服装鞋包</a>
          <a href="#">食品生鲜</a>
        </div>
      </div>
    </nav>

    <!-- 页面内容 -->
    <main class="main-content">
      <router-view />
    </main>

    <!-- 页脚 -->
    <footer class="footer">
      <div class="container">
        <div class="footer-links">
          <a href="#">关于我们</a>
          <a href="#">联系我们</a>
          <a href="#">联系客服</a>
          <a href="#">合作招商</a>
          <a href="#">商家帮助</a>
          <a href="#">营销中心</a>
        </div>
        <p>© 2026 商城 版权所有</p>
      </div>
    </footer>
  </div>
</template>

<style>
* { margin: 0; padding: 0; box-sizing: border-box; }
body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif; background: #f4f4f4; color: #333; }
a { text-decoration: none; color: inherit; }
.container { max-width: 1200px; margin: 0 auto; padding: 0 20px; }

/* 顶部通栏 */
.top-bar { background: #e3e4e5; font-size: 12px; color: #999; border-bottom: 1px solid #ddd; }
.top-bar .container { display: flex; justify-content: space-between; height: 30px; align-items: center; }
.top-bar a { color: #999; margin-left: 16px; transition: color 0.2s; }
.top-bar a:hover { color: #ff6b6b; }
.user-name { color: #ff6b6b; margin-right: 12px; font-weight: 500; }

/* 搜索头部 */
.header-main { background: #fff; padding: 20px 0; border-bottom: 2px solid #ff6b6b; }
.header-flex { display: flex; align-items: center; justify-content: space-between; }
.logo { font-size: 32px; font-weight: bold; color: #ff6b6b; letter-spacing: 2px; cursor: pointer; user-select: none; }
.search-box { display: flex; width: 500px; height: 40px; border: 2px solid #ff6b6b; border-radius: 4px; overflow: hidden; }
.search-box input { flex: 1; border: none; padding: 0 16px; font-size: 14px; outline: none; }
.search-box button { width: 80px; background: #ff6b6b; color: #fff; border: none; font-size: 16px; cursor: pointer; transition: background 0.2s; }
.search-box button:hover { background: #ff5252; }
.header-cart { display: flex; align-items: center; gap: 6px; padding: 8px 16px; border: 1px solid #eee; background: #fff; font-size: 14px; cursor: pointer; position: relative; transition: all 0.2s; }
.header-cart:hover { border-color: #ff6b6b; color: #ff6b6b; }
.cart-icon { font-size: 18px; }
.cart-badge { position: absolute; top: -6px; right: -6px; background: #ff6b6b; color: #fff; font-size: 11px; padding: 0 6px; height: 18px; line-height: 18px; border-radius: 9px; min-width: 18px; text-align: center; }

/* 导航 */
.main-nav { background: #fff; }
.main-nav .container { display: flex; align-items: center; height: 44px; }
.nav-all { width: 200px; background: #ff6b6b; color: #fff; text-align: center; line-height: 44px; font-size: 15px; font-weight: bold; cursor: pointer; transition: background 0.2s; }
.nav-all:hover { background: #ff5252; }
.nav-all.active { background: #d9363e; }
.nav-list { display: flex; gap: 40px; margin-left: 30px; }
.nav-list a { font-size: 15px; color: #333; font-weight: 500; transition: color 0.2s; padding: 10px 0; }
.nav-list a:hover { color: #ff6b6b; }
.nav-list a.active { color: #ff6b6b; }

/* 页脚 */
.footer { background: #2e2e2e; color: #999; text-align: center; padding: 30px 0; margin-top: 40px; font-size: 13px; }
.footer-links { display: flex; justify-content: center; gap: 24px; margin-bottom: 16px; flex-wrap: wrap; }
.footer-links a { color: #999; transition: color 0.2s; }
.footer-links a:hover { color: #fff; }
</style>