<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getOrderList } from '../api/api'
import request from '@/utils/request'

const router = useRouter()
const route = useRoute()

const activeTab = ref('all')
const tabs = [
  { key: 'all', label: '全部订单' },
  { key: 'unpay', label: '待付款' },
  { key: 'unsend', label: '待发货' },
  { key: 'unreceive', label: '待收货' },
  { key: 'done', label: '已完成' }
]

const orders = ref([])

onMounted(async () => {
  // ========== 关键：从 Stripe 支付成功跳回来后，确认支付状态 ==========
  if (route.query.success === 'true' && route.query.session_id) {
    try {
      const res = await request.get('/pay/confirm?sessionId=' + route.query.session_id)
      if (res.code === 200 && res.data === 'PAID') {
        alert('🎉 支付成功！订单已确认')
      } else {
        alert('支付状态确认中，请稍后刷新页面查看')
      }
    } catch (err) {
      alert('支付确认失败，请手动刷新查看订单状态')
    }
    // 清空 URL 参数，避免刷新重复触发
    router.replace('/orders')
  }

  await loadOrders()
})

const loadOrders = async () => {
  const userId = 1
  const res = await getOrderList(userId)
  if (res.code === 200 && res.data) {
    orders.value = res.data.map(order => ({
      id: order.orderNo,
      status: mapStatus(order.status),
      statusCode: mapStatusCode(order.status),
      date: order.createTime,
      goods: order.items ? order.items.map(item => ({
        image: item.image,
        title: item.title,
        price: item.price,
        num: item.num
      })) : [],
      total: order.payAmount
    }))
  }
}

const filteredOrders = computed(() => {
  if (activeTab.value === 'all') return orders.value
  return orders.value.filter(o => o.statusCode === activeTab.value)
})

function mapStatus(status) {
  const map = { 0: '待付款', 1: '待发货', 2: '待收货', 3: '已完成' }
  return map[status] || '未知'
}

function mapStatusCode(status) {
  const map = { 0: 'unpay', 1: 'unsend', 2: 'unreceive', 3: 'done' }
  return map[status] || 'all'
}

// 点击"立即付款"：创建 Stripe 收银台并跳转
const handlePay = async (orderNo) => {
  try {
    const res = await request.get('/pay/create?orderNo=' + orderNo)
    if (res.code === 200 && res.data) {
      window.location.href = res.data
    } else {
      alert('创建支付失败：' + (res.msg || '未知错误'))
    }
  } catch (err) {
    alert('支付请求失败，请检查后端是否启动')
  }
}
</script>

<template>
  <div class="order-page">
    <div class="container">
      <h2 class="page-title">我的订单</h2>

      <div class="order-tabs">
        <div
          v-for="tab in tabs"
          :key="tab.key"
          class="tab-item"
          :class="{ active: activeTab === tab.key }"
          @click="activeTab = tab.key"
        >
          {{ tab.label }}
        </div>
      </div>

      <div class="order-list">
        <div v-if="filteredOrders.length === 0" class="empty-state">
          <div class="empty-icon">📦</div>
          <p class="empty-title">暂无相关订单</p>
          <p class="empty-desc">您还没有该状态的订单，快去选购商品吧~</p>
          <button class="btn-go" @click="router.push('/home')">去逛逛</button>
        </div>

        <div class="order-card" v-for="order in filteredOrders" :key="order.id">
          <div class="order-header">
            <span class="order-id">订单号：{{ order.id }}</span>
            <span class="order-date">{{ order.date }}</span>
            <span class="order-status">{{ order.status }}</span>
          </div>

          <div class="order-goods">
            <div class="goods-row" v-for="g in order.goods" :key="g.title + g.num">
              <img :src="g.image" class="goods-img" />
              <div class="goods-info">
                <p class="goods-title">{{ g.title }}</p>
                <p class="goods-price">¥{{ g.price }} × {{ g.num }}</p>
              </div>
            </div>
          </div>

          <div class="order-footer">
            <span class="order-total">实付金额：<strong>¥{{ order.total }}</strong></span>
            <div class="order-btns">
              <button v-if="order.statusCode === 'unpay'" class="btn-pay" @click="handlePay(order.id)">
                立即付款
              </button>
              <button v-if="order.statusCode === 'unreceive'" class="btn-confirm">确认收货</button>
           <button class="btn-detail" @click="router.push('/order-detail/' + order.id)">查看详情</button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.order-page {
  background: #f4f4f4;
  min-height: 100vh;
  padding: 20px 0;
}
.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}
.page-title {
  font-size: 24px;
  color: #333;
  margin-bottom: 20px;
}

.order-tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 20px;
}
.tab-item {
  padding: 10px 24px;
  background: #fff;
  border-radius: 4px;
  font-size: 14px;
  color: #666;
  cursor: pointer;
  transition: all 0.2s;
}
.tab-item:hover {
  color: #ff6b6b;
}
.tab-item.active {
  background: #ff6b6b;
  color: #fff;
}

.order-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.order-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
}
.order-header {
  display: flex;
  align-items: center;
  gap: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
  font-size: 14px;
}
.order-id {
  color: #333;
  font-weight: bold;
}
.order-date {
  color: #999;
}
.order-status {
  margin-left: auto;
  color: #ff6b6b;
  font-weight: bold;
}

.order-goods {
  padding: 16px 0;
}
.goods-row {
  display: flex;
  gap: 16px;
  margin-bottom: 12px;
}
.goods-row:last-child {
  margin-bottom: 0;
}
.goods-img {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 4px;
  border: 1px solid #eee;
}
.goods-info {
  flex: 1;
}
.goods-title {
  font-size: 14px;
  color: #333;
  margin-bottom: 6px;
}
.goods-price {
  font-size: 14px;
  color: #999;
}

.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}
.order-total {
  font-size: 14px;
  color: #333;
}
.order-total strong {
  font-size: 20px;
  color: #ff6b6b;
}
.order-btns {
  display: flex;
  gap: 10px;
}
.order-btns button {
  padding: 6px 20px;
  border-radius: 4px;
  font-size: 13px;
  cursor: pointer;
}
.btn-pay {
  background: #ff6b6b;
  color: #fff;
  border: none;
}
.btn-confirm {
  background: #52c41a;
  color: #fff;
  border: none;
}
.btn-detail {
  background: #fff;
  color: #666;
  border: 1px solid #ddd;
}

.empty-state {
  text-align: center;
  padding: 80px 20px;
  background: #fff;
  border-radius: 8px;
}
.empty-icon {
  font-size: 80px;
  margin-bottom: 16px;
}
.empty-title {
  font-size: 18px;
  color: #333;
  margin-bottom: 8px;
}
.empty-desc {
  font-size: 14px;
  color: #999;
  margin-bottom: 24px;
}
.btn-go {
  padding: 10px 40px;
  background: #ff6b6b;
  color: #fff;
  border: none;
  border-radius: 4px;
  font-size: 15px;
  cursor: pointer;
}
.btn-go:hover {
  background: #ff5252;
}
</style>