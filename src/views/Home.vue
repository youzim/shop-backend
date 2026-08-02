<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { getGoodsList } from '../api/api'

const router = useRouter()
const userStore = useUserStore()

// ========== 轮播图：真实广告Banner ==========
const banners = ref([
  'https://kimi-web-img.moonshot.cn/img/img.magnific.com/25472f7d6a4dfb6dda1aa12aad3cf1c4009134a7.jpg',
  'https://kimi-web-img.moonshot.cn/img/img.magnific.com/6e2ac132aa1730138a11458023d9050bfdb2d1c6.jpg',
  'https://kimi-web-img.moonshot.cn/img/t3.ftcdn.net/22fd4c31c714cc4b1df5f98288f0c518f2fdbd18.jpg'
])

const categories = ref([
  { name: '手机数码', icon: '📱' },
  { name: '电脑办公', icon: '💻' },
  { name: '家用电器', icon: '📺' },
  { name: '服装鞋包', icon: '👜' },
  { name: '食品生鲜', icon: '🍎' },
  { name: '美妆护肤', icon: '💄' },
  { name: '家居家装', icon: '🏠' },
  { name: '运动户外', icon: '⚽' },
  { name: '图书文具', icon: '📚' },
  { name: '母婴用品', icon: '🍼' }
])

const sideMenu = ref([
  '手机 / 运营商 / 数码',
  '电脑 / 办公',
  '家居 / 家具 / 家装',
  '男装 / 女装 / 童装',
  '美妆 / 个护清洁',
  '女鞋 / 箱包 / 钟表',
  '房产 / 汽车 / 汽车用品',
  '母婴 / 玩具乐器',
  '食品 / 酒类 / 生鲜',
  '医药保健 / 计生情趣'
])

const goodsList = ref([])
const currentBanner = ref(0)

// 秒杀列表：取接口前 4 个真实商品
const seckillList = computed(() => goodsList.value.slice(0, 4))

// ========== 秒杀倒计时（真实倒计时） ==========
const seckillTime = ref({ h: '02', m: '00', s: '00' })
let countdownTimer = null

const updateCountdown = () => {
  const now = new Date()
  // 目标：下一个整点（如 18:32 -> 20:00）
  const target = new Date(now)
  target.setHours(target.getHours() + (now.getMinutes() > 0 || now.getSeconds() > 0 ? 1 : 0))
  target.setMinutes(0)
  target.setSeconds(0)
  
  // 如果距离太近（<30分钟），跳到再下一个整点
  let diff = target - now
  if (diff < 30 * 60 * 1000) {
    target.setHours(target.getHours() + 1)
    diff = target - now
  }

  if (diff <= 0) {
    seckillTime.value = { h: '00', m: '00', s: '00' }
    return
  }

  const hours = Math.floor(diff / 1000 / 60 / 60)
  const minutes = Math.floor((diff / 1000 / 60) % 60)
  const seconds = Math.floor((diff / 1000) % 60)

  seckillTime.value = {
    h: String(hours).padStart(2, '0'),
    m: String(minutes).padStart(2, '0'),
    s: String(seconds).padStart(2, '0')
  }
}

let bannerTimer = null
const startBanner = () => {
  bannerTimer = setInterval(() => {
    currentBanner.value = (currentBanner.value + 1) % banners.value.length
  }, 4000)
}
const stopBanner = () => {
  if (bannerTimer) clearInterval(bannerTimer)
}

onMounted(async () => {
  const res = await getGoodsList()
  if (res.code === 200) {
    goodsList.value = res.data
  }
  startBanner()
  updateCountdown()
  countdownTimer = setInterval(updateCountdown, 1000)
})

const goToDetail = (id) => {
  router.push(`/goods/${id}`)
}

const goLogin = () => router.push('/login')
const goUser = () => router.push('/user')
const goOrders = () => router.push('/orders')
const goCart = () => router.push('/cart')
</script>

<template>
  <div class="home-pc">
    <div class="container">
      <!-- 首屏 -->
      <div class="hero-section">
        <div class="side-menu">
          <div class="menu-item" v-for="(item, idx) in sideMenu" :key="idx">
            <span>{{ item }}</span>
            <span class="menu-arrow">›</span>
          </div>
        </div>

        <div class="banner-wrap" @mouseenter="stopBanner" @mouseleave="startBanner">
          <div class="banner">
            <img :src="banners[currentBanner]" class="banner-img" />
            <div class="banner-dots">
              <span
                v-for="(img, idx) in banners"
                :key="idx"
                :class="{ active: currentBanner === idx }"
                @click="currentBanner = idx"
              ></span>
            </div>
          </div>
          <div class="sub-banners">
            <img src="https://images.unsplash.com/photo-1607082349566-187342175e2f?w=600&h=280&fit=crop" />
            <img src="https://images.unsplash.com/photo-1556742049-0cfed4f6a45d?w=600&h=280&fit=crop" />
          </div>
        </div>

        <div class="side-extra">
          <!-- 用户卡片：根据登录态切换 -->
          <div class="user-card">
            <template v-if="!userStore.isLogin">
              <div class="avatar-placeholder">👤</div>
              <p class="welcome">Hi~ 欢迎逛商城</p>
              <p class="welcome-sub">登录后享专属优惠</p>
              <div class="user-btns">
                <button class="btn-login" @click="goLogin">登录</button>
                <button class="btn-register" @click="goLogin">注册</button>
              </div>
            </template>
            <template v-else>
              <img 
                :src="userStore.userInfo?.avatar || 'https://img.yzcdn.cn/vant/cat.jpeg'" 
                class="user-avatar" 
              />
              <p class="welcome">Hi，{{ userStore.userInfo?.name || '用户' }}</p>
              <span class="level-tag">🥇 普通会员</span>
              <div class="user-btns">
                <button class="btn-login" @click="goUser">个人中心</button>
                <button class="btn-register" @click="goOrders">我的订单</button>
              </div>
            </template>
          </div>

          <div class="notice-card">
            <h4>商城公告</h4>
            <ul>
              <li>🎉 暑期大促，全场满300减50</li>
              <li>📦 新用户首单免运费</li>
              <li>💳 开通会员享95折优惠</li>
              <li>🚚 自营商品次日达</li>
            </ul>
          </div>
        </div>
      </div>

      <!-- 快捷分类 -->
      <div class="quick-category">
        <div class="cat-item" v-for="item in categories" :key="item.name" @click="router.push('/category')">
          <div class="cat-icon">{{ item.icon }}</div>
          <div class="cat-name">{{ item.name }}</div>
        </div>
      </div>

      <!-- 秒杀 -->
      <div class="seckill-section">
        <div class="seckill-header">
          <div class="seckill-title">
            <span class="sk-icon">⚡</span>
            <span class="sk-text">限时秒杀</span>
            <span class="sk-time">
              <span class="t-num">{{ seckillTime.h }}</span> :
              <span class="t-num">{{ seckillTime.m }}</span> :
              <span class="t-num">{{ seckillTime.s }}</span>
            </span>
          </div>
          <a href="#" class="sk-more" @click.prevent="router.push('/category')">查看全部 ›</a>
        </div>
        <div class="seckill-list">
          <div 
            class="sk-item" 
            v-for="item in seckillList" 
            :key="item.id" 
            @click="goToDetail(item.id)"
          >
            <img :src="item.image" />
            <div class="sk-name">
              {{ item.title.length > 14 ? item.title.slice(0, 14) + '...' : item.title }}
            </div>
            <div class="sk-price">
              <span class="sk-now">¥{{ Math.floor(item.price * 0.7) }}</span>
              <span class="sk-old">¥{{ item.price }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 热门推荐 -->
      <div class="recommend-section">
        <div class="section-title">
          <h2>🔥 热门推荐</h2>
          <a href="#" @click.prevent="router.push('/category')">查看更多 ›</a>
        </div>
        <div class="goods-grid">
          <div class="goods-card" v-for="item in goodsList" :key="item.id" @click="goToDetail(item.id)">
            <div class="card-img-wrap">
              <img :src="item.image" class="card-img" />
              <span v-if="item.tag" class="card-tag">{{ item.tag }}</span>
            </div>
            <div class="card-info">
              <p class="card-title">{{ item.title }}</p>
              <div class="card-price">
                <span class="price-now">¥{{ item.price }}</span>
                <span class="price-old">¥{{ item.oldPrice }}</span>
              </div>
              <div class="card-bottom">
                <span class="sales">已售 {{ item.sales || 0 }}</span>
                <button class="buy-btn">查看详情</button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.home-pc { padding: 20px 0; }
.container { max-width: 1200px; margin: 0 auto; padding: 0 20px; }

.hero-section { display: flex; gap: 12px; height: 480px; }

.side-menu { width: 200px; background: #fff; border-radius: 8px; overflow: hidden; flex-shrink: 0; }
.menu-item { display: flex; justify-content: space-between; align-items: center; padding: 0 16px; height: 48px; font-size: 14px; color: #333; cursor: pointer; transition: background 0.2s; }
.menu-item:hover { background: #f5f5f5; color: #ff6b6b; }
.menu-arrow { color: #ccc; font-size: 16px; }

.banner-wrap { flex: 1; display: flex; flex-direction: column; gap: 12px; }
.banner { flex: 1; border-radius: 8px; overflow: hidden; position: relative; background: #ddd; }
.banner-img { width: 100%; height: 100%; object-fit: cover; display: block; }
.banner-dots { position: absolute; bottom: 16px; left: 50%; transform: translateX(-50%); display: flex; gap: 8px; }
.banner-dots span { width: 10px; height: 10px; border-radius: 50%; background: rgba(255,255,255,0.5); cursor: pointer; transition: all 0.3s; }
.banner-dots span.active { background: #fff; width: 24px; border-radius: 5px; }
.sub-banners { display: flex; gap: 12px; height: 140px; }
.sub-banners img { flex: 1; border-radius: 8px; object-fit: cover; }

.side-extra { width: 200px; flex-shrink: 0; display: flex; flex-direction: column; gap: 12px; }
.user-card { background: #fff; border-radius: 8px; padding: 20px; text-align: center; }
.avatar-placeholder { width: 60px; height: 60px; line-height: 60px; border-radius: 50%; background: #f5f5f5; margin: 0 auto 10px; font-size: 30px; }
.user-avatar { width: 60px; height: 60px; border-radius: 50%; object-fit: cover; margin: 0 auto 10px; display: block; border: 2px solid #ff6b6b; }
.welcome { font-size: 14px; color: #666; margin-bottom: 4px; }
.welcome-sub { font-size: 12px; color: #999; margin-bottom: 14px; }
.level-tag { display: inline-block; font-size: 12px; color: #ff6b6b; background: #fff5f5; padding: 2px 10px; border-radius: 12px; margin-bottom: 14px; }
.user-btns { display: flex; gap: 10px; }
.user-btns button { flex: 1; height: 32px; border-radius: 16px; border: none; font-size: 13px; cursor: pointer; transition: all 0.2s; }
.btn-login { background: #ff6b6b; color: #fff; }
.btn-login:hover { background: #ff5252; }
.btn-register { background: #fff; color: #ff6b6b; border: 1px solid #ff6b6b !important; }
.btn-register:hover { background: #fff5f5; }
.notice-card { flex: 1; background: #fff; border-radius: 8px; padding: 16px; }
.notice-card h4 { font-size: 15px; margin-bottom: 12px; color: #333; }
.notice-card ul { list-style: none; }
.notice-card li { font-size: 13px; color: #666; line-height: 2; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }

.quick-category { display: flex; justify-content: space-between; background: #fff; border-radius: 8px; padding: 24px 20px; margin-top: 20px; }
.cat-item { display: flex; flex-direction: column; align-items: center; gap: 8px; cursor: pointer; transition: transform 0.2s; }
.cat-item:hover { transform: translateY(-4px); }
.cat-icon { width: 56px; height: 56px; border-radius: 50%; background: linear-gradient(135deg, #ff6b6b, #ff8e8e); display: flex; align-items: center; justify-content: center; font-size: 28px; box-shadow: 0 4px 12px rgba(255,107,107,0.25); }
.cat-name { font-size: 13px; color: #666; }

.seckill-section { background: #fff; border-radius: 8px; margin-top: 20px; padding: 20px; }
.seckill-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.seckill-title { display: flex; align-items: center; gap: 10px; }
.sk-icon { font-size: 24px; }
.sk-text { font-size: 22px; font-weight: bold; color: #ff6b6b; }
.sk-time { font-size: 14px; color: #666; margin-left: 6px; }
.t-num { display: inline-block; width: 28px; height: 28px; line-height: 28px; text-align: center; background: #333; color: #fff; border-radius: 4px; font-size: 14px; margin: 0 2px; }
.sk-more { font-size: 14px; color: #999; }
.sk-more:hover { color: #ff6b6b; }
.seckill-list { display: flex; gap: 16px; }
.sk-item { flex: 1; cursor: pointer; transition: transform 0.2s; }
.sk-item:hover { transform: translateY(-4px); }
.sk-item img { width: 100%; height: 160px; object-fit: cover; border-radius: 8px; display: block; }
.sk-name { font-size: 13px; color: #333; margin-top: 8px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.sk-price { margin-top: 4px; }
.sk-now { font-size: 18px; color: #ff6b6b; font-weight: bold; }
.sk-old { font-size: 13px; color: #ccc; text-decoration: line-through; margin-left: 6px; }

.recommend-section { margin-top: 20px; }
.section-title { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.section-title h2 { font-size: 22px; color: #333; }
.section-title a { font-size: 14px; color: #999; }
.section-title a:hover { color: #ff6b6b; }
.goods-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; }
.goods-card { background: #fff; border-radius: 8px; overflow: hidden; transition: box-shadow 0.3s, transform 0.3s; cursor: pointer; }
.goods-card:hover { box-shadow: 0 8px 24px rgba(0,0,0,0.1); transform: translateY(-4px); }
.card-img-wrap { position: relative; }
.card-img { width: 100%; height: 220px; object-fit: cover; display: block; }
.card-tag { position: absolute; top: 10px; left: 10px; background: #ff6b6b; color: #fff; font-size: 12px; padding: 2px 10px; border-radius: 12px; }
.card-info { padding: 14px; }
.card-title { font-size: 14px; color: #333; line-height: 1.5; display: -webkit-box; -webkit-line-clamp: 2; line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; min-height: 42px; margin-bottom: 10px; }
.card-price { margin-bottom: 10px; }
.price-now { font-size: 20px; color: #ff6b6b; font-weight: bold; }
.price-old { font-size: 13px; color: #ccc; text-decoration: line-through; margin-left: 8px; }
.card-bottom { display: flex; justify-content: space-between; align-items: center; }
.sales { font-size: 12px; color: #999; }
.buy-btn { padding: 4px 14px; border: 1px solid #ff6b6b; background: #fff; color: #ff6b6b; border-radius: 4px; font-size: 13px; cursor: pointer; transition: all 0.2s; }
.buy-btn:hover { background: #ff6b6b; color: #fff; }
</style>