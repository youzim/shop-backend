<script setup>
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()

const isLogin = computed(() => userStore.isLogin)
const userInfo = computed(() => userStore.userInfo)

const fileInput = ref(null)

const goLogin = () => router.push('/login')

const doLogout = () => {
  if (confirm('确定要退出登录吗？')) {
    userStore.logout()
    router.push('/')
  }
}

// 点击头像
const triggerUpload = () => {
  if (!isLogin.value) {
    goLogin()
    return
  }
  fileInput.value?.click()
}

// 选择图片后处理
const handleFileChange = (e) => {
  const file = e.target.files[0]
  if (!file) return

  // 限制 2MB
  if (file.size > 2 * 1024 * 1024) {
    alert('图片大小不能超过 2MB')
    e.target.value = ''
    return
  }

  const reader = new FileReader()
  reader.onload = (event) => {
    userStore.updateAvatar(event.target.result)
  }
  reader.readAsDataURL(file)
  e.target.value = ''
}

const orderMenu = [
  { label: '待付款', icon: '💳', count: 0 },
  { label: '待发货', icon: '📦', count: 0 },
  { label: '待收货', icon: '🚚', count: 0 },
  { label: '待评价', icon: '⭐', count: 0 },
  { label: '退款/售后', icon: '🔧', count: 0 }
]

const tools = [
  { name: '我的收藏', icon: '❤️', count: 0 },
  { name: '收货地址', icon: '📍', count: 0 },
  { name: '我的优惠券', icon: '🎫', count: 0 },
  { name: '我的足迹', icon: '👣', count: 0 },
  { name: '账户安全', icon: '🔒', count: 0 },
  { name: '联系客服', icon: '🎧', count: 0 }
]
</script>

<template>
  <div class="user-page">
    <div class="container">
      <!-- 用户信息卡片 -->
      <div class="user-card">
        <div class="user-left">
          <!-- 头像区域：点击上传 -->
          <div class="avatar-wrap" @click="triggerUpload">
            <img 
              :src="isLogin ? (userInfo?.avatar || 'https://img.yzcdn.cn/vant/cat.jpeg') : 'https://img.yzcdn.cn/vant/cat.jpeg'" 
              class="user-avatar" 
            />
            <div class="avatar-mask">
              <span class="mask-text">📷 更换头像</span>
            </div>
            <input 
              type="file" 
              ref="fileInput" 
              accept="image/*" 
              style="display: none" 
              @change="handleFileChange"
            />
          </div>
          <div class="user-meta">
            <h3 class="user-name">
              {{ isLogin ? (userInfo?.name || '用户') : 'Hi，欢迎来到商城' }}
            </h3>
            <span class="user-level">
              {{ isLogin ? '普通会员' : '登录后享受更多权益' }}
            </span>
          </div>
        </div>
        <div class="user-right">
          <button v-if="!isLogin" class="btn-primary" @click="goLogin">立即登录</button>
          <button v-else class="btn-ghost" @click="doLogout">退出登录</button>
        </div>
      </div>

      <!-- 资产/数据概览 -->
      <div class="stats-bar">
        <div class="stat-item">
          <span class="stat-num">0</span>
          <span class="stat-label">收藏商品</span>
        </div>
        <div class="stat-item">
          <span class="stat-num">0</span>
          <span class="stat-label">店铺关注</span>
        </div>
        <div class="stat-item">
          <span class="stat-num">0</span>
          <span class="stat-label">浏览记录</span>
        </div>
        <div class="stat-item">
          <span class="stat-num">0</span>
          <span class="stat-label">优惠券</span>
        </div>
      </div>

      <!-- 订单入口 -->
      <div class="panel">
        <div class="panel-header">
          <h4>我的订单</h4>
          <a class="panel-more" @click.prevent="router.push('/orders')">查看全部 ›</a>
        </div>
        <div class="order-menu">
          <div class="order-item" v-for="item in orderMenu" :key="item.label" @click="router.push('/orders')">
            <div class="order-icon">
              {{ item.icon }}
              <span v-if="item.count > 0" class="order-badge">{{ item.count }}</span>
            </div>
            <span class="order-label">{{ item.label }}</span>
          </div>
        </div>
      </div>

      <!-- 功能列表 -->
      <div class="panel">
        <div class="panel-header">
          <h4>更多服务</h4>
        </div>
        <div class="tool-list">
          <div class="tool-item" v-for="item in tools" :key="item.name">
            <span class="tool-icon">{{ item.icon }}</span>
            <span class="tool-name">{{ item.name }}</span>
            <span v-if="item.count > 0" class="tool-count">{{ item.count }}</span>
            <span class="tool-arrow">›</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.user-page {
  background: #f4f4f4;
  min-height: 100vh;
  padding: 20px 0 60px;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

/* ========== 用户信息卡片 ========== */
.user-card {
  background: linear-gradient(135deg, #ff4757, #ff6b81);
  border-radius: 16px;
  padding: 32px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #fff;
  margin-bottom: 16px;
  box-shadow: 0 8px 32px rgba(255, 71, 87, 0.25);
}

.user-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

/* 头像容器：关键改动 */
.avatar-wrap {
  position: relative;
  width: 76px;
  height: 76px;
  border-radius: 50%;
  padding: 3px;
  background: rgba(255, 255, 255, 0.35);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
  cursor: pointer;
  overflow: hidden;
  transition: transform 0.2s;
}

.avatar-wrap:hover {
  transform: scale(1.05);
}

.user-avatar {
  width: 70px;
  height: 70px;
  border-radius: 50%;
  object-fit: cover;
  display: block;
  border: 2px solid #fff;
}

/* 悬停遮罩 */
.avatar-mask {
  position: absolute;
  top: 3px;
  left: 3px;
  right: 3px;
  bottom: 3px;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.25s;
}

.avatar-wrap:hover .avatar-mask {
  opacity: 1;
}

.mask-text {
  color: #fff;
  font-size: 11px;
  font-weight: 600;
  text-align: center;
  line-height: 1.3;
}

.user-name {
  font-size: 24px;
  font-weight: 700;
  color: #fff;
  margin-bottom: 8px;
  letter-spacing: 0.5px;
  text-shadow: 0 2px 6px rgba(0, 0, 0, 0.25);
}

.user-level {
  display: inline-block;
  font-size: 12px;
  font-weight: 600;
  color: #ff4757;
  background: #fff;
  padding: 4px 12px;
  border-radius: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.user-right {
  display: flex;
  align-items: center;
}

.btn-primary {
  padding: 10px 28px;
  background: #fff;
  color: #ff4757;
  border: none;
  border-radius: 24px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.25s;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.15);
}

.btn-ghost {
  padding: 10px 28px;
  background: rgba(255, 255, 255, 0.15);
  color: #fff;
  border: 1px solid rgba(255, 255, 255, 0.4);
  border-radius: 24px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.25s;
}

.btn-ghost:hover {
  background: rgba(255, 255, 255, 0.3);
}

/* ========== 资产概览 ========== */
.stats-bar {
  display: flex;
  justify-content: space-around;
  background: #fff;
  border-radius: 12px;
  padding: 24px 0;
  margin-bottom: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  transition: transform 0.2s;
}

.stat-item:hover {
  transform: translateY(-2px);
}

.stat-num {
  font-size: 22px;
  font-weight: 700;
  color: #333;
}

.stat-label {
  font-size: 13px;
  color: #999;
}

/* ========== 通用面板 ========== */
.panel {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.panel-header h4 {
  font-size: 16px;
  color: #333;
  font-weight: 600;
}

.panel-more {
  font-size: 13px;
  color: #999;
  cursor: pointer;
  transition: color 0.2s;
}

.panel-more:hover {
  color: #ff4757;
}

/* ========== 订单入口 ========== */
.order-menu {
  display: flex;
  justify-content: space-around;
}

.order-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 8px 16px;
  border-radius: 8px;
  transition: all 0.2s;
}

.order-item:hover {
  background: #fff5f5;
  transform: translateY(-2px);
}

.order-icon {
  font-size: 28px;
  position: relative;
}

.order-badge {
  position: absolute;
  top: -8px;
  right: -10px;
  background: #ff4757;
  color: #fff;
  font-size: 11px;
  padding: 0 6px;
  height: 18px;
  line-height: 18px;
  border-radius: 9px;
  min-width: 18px;
  text-align: center;
  font-weight: 600;
  box-shadow: 0 2px 6px rgba(255, 71, 87, 0.3);
}

.order-label {
  font-size: 13px;
  color: #666;
}

/* ========== 功能列表 ========== */
.tool-list {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
}

.tool-item {
  display: flex;
  align-items: center;
  padding: 16px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.tool-item:hover {
  background: #fafafa;
}

.tool-icon {
  font-size: 22px;
  margin-right: 12px;
}

.tool-name {
  flex: 1;
  font-size: 14px;
  color: #333;
}

.tool-count {
  font-size: 13px;
  color: #ff4757;
  font-weight: 600;
  margin-right: 8px;
}

.tool-arrow {
  color: #ccc;
  font-size: 16px;
}
</style>