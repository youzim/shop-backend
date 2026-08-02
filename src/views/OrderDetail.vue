<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getOrderDetail } from '../api/api'
import request from '@/utils/request'

const route = useRoute()
const router = useRouter()

const order = ref(null)
const loading = ref(true)

onMounted(async () => {
  const orderNo = route.params.orderNo
  if (!orderNo) {
    alert('订单号不存在')
    router.push('/orders')
    return
  }
  try {
    const res = await getOrderDetail(orderNo)
    if (res.code === 200 && res.data) {
      order.value = res.data
    } else {
      alert('订单不存在或已删除')
      router.push('/orders')
    }
  } catch (err) {
    alert('加载订单失败')
    router.push('/orders')
  } finally {
    loading.value = false
  }
})

const statusInfo = computed(() => {
  if (!order.value) return { label: '', color: '', icon: '' }
  const map = {
    0: { label: '待付款', color: '#ff6b6b', icon: '💳', next: '去支付' },
    1: { label: '待发货', color: '#1890ff', icon: '📦', next: '等待发货' },
    2: { label: '待收货', color: '#faad14', icon: '🚚', next: '确认收货' },
    3: { label: '已完成', color: '#52c41a', icon: '✅', next: '已完成' }
  }
  return map[order.value.status] || { label: '未知', color: '#999', icon: '❓', next: '' }
})

const goodsTotal = computed(() => {
  if (!order.value?.items) return 0
  return order.value.items.reduce((sum, item) => sum + item.price * item.num, 0)
})

const handlePay = async () => {
  if (!order.value) return
  try {
    const res = await request.get('/pay/create?orderNo=' + order.value.orderNo)
    if (res.code === 200 && res.data) {
      window.location.href = res.data
    } else {
      alert('创建支付失败')
    }
  } catch (err) {
    alert('支付请求失败')
  }
}

const handleConfirm = () => {
  alert('确认收货功能开发中~')
}

const goBack = () => router.push('/orders')
</script>

<template>
  <div class="order-detail-page">
    <div class="container" v-if="!loading && order">
      <!-- 头部状态 -->
      <div class="status-bar" :style="{ background: statusInfo.color }">
        <div class="status-icon">{{ statusInfo.icon }}</div>
        <div class="status-text">
          <h2>{{ statusInfo.label }}</h2>
          <p>订单号：{{ order.orderNo }}</p>
        </div>
      </div>

      <!-- 收货地址 -->
      <div class="section address-section">
        <h3 class="section-title">📍 收货信息</h3>
        <div class="addr-content">
          <p class="addr-name">
            <strong>{{ order.addressName }}</strong>
            <span>{{ order.addressPhone }}</span>
          </p>
          <p class="addr-detail">{{ order.addressDetail }}</p>
        </div>
      </div>

      <!-- 商品清单 -->
      <div class="section goods-section">
        <h3 class="section-title">🛍️ 商品信息</h3>
        <div class="goods-list">
          <div class="goods-item" v-for="item in order.items" :key="item.id">
            <img :src="item.image" class="goods-img" />
            <div class="goods-info">
              <p class="goods-title">{{ item.title }}</p>
              <p class="goods-spec">{{ item.spec }}</p>
              <div class="goods-bottom">
                <span class="goods-price">¥{{ item.price }}</span>
                <span class="goods-num">×{{ item.num }}</span>
              </div>
            </div>
            <div class="goods-subtotal">
              ¥{{ (item.price * item.num).toFixed(2) }}
            </div>
          </div>
        </div>
      </div>

      <!-- 金额明细 -->
      <div class="section amount-section">
        <h3 class="section-title">💰 金额明细</h3>
        <div class="amount-rows">
          <div class="amount-row">
            <span>商品总额</span>
            <span>¥{{ goodsTotal.toFixed(2) }}</span>
          </div>
          <div class="amount-row">
            <span>运费</span>
            <span>{{ order.freight > 0 ? '¥' + order.freight : '免运费' }}</span>
          </div>
          <div class="amount-row">
            <span>优惠</span>
            <span class="discount">- ¥{{ (goodsTotal + order.freight - order.payAmount).toFixed(2) }}</span>
          </div>
          <div class="amount-row total">
            <span>实付金额</span>
            <span class="total-price">¥{{ order.payAmount }}</span>
          </div>
        </div>
      </div>

      <!-- 订单信息 -->
      <div class="section info-section">
        <h3 class="section-title">📋 订单信息</h3>
        <div class="info-rows">
          <div class="info-row">
            <span>订单编号</span>
            <span>{{ order.orderNo }}</span>
          </div>
          <div class="info-row">
            <span>下单时间</span>
            <span>{{ order.createTime }}</span>
          </div>
          <div class="info-row">
            <span>支付方式</span>
            <span>Stripe 在线支付</span>
          </div>
        </div>
      </div>

      <!-- 底部操作 -->
      <div class="action-bar">
        <button class="btn-back" @click="goBack">返回列表</button>
        <button v-if="order.status === 0" class="btn-primary" @click="handlePay">
          {{ statusInfo.next }}
        </button>
        <button v-if="order.status === 2" class="btn-primary" @click="handleConfirm">
          {{ statusInfo.next }}
        </button>
      </div>
    </div>

    <!-- 加载中 -->
    <div v-else-if="loading" class="loading-state">
      <p>加载中...</p>
    </div>
  </div>
</template>

<style scoped>
.order-detail-page {
  background: #f4f4f4;
  min-height: 100vh;
  padding: 20px 0 80px;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

/* 状态头部 */
.status-bar {
  border-radius: 12px;
  padding: 32px;
  display: flex;
  align-items: center;
  gap: 16px;
  color: #fff;
  margin-bottom: 16px;
}

.status-icon {
  font-size: 48px;
}

.status-text h2 {
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 6px;
}

.status-text p {
  font-size: 14px;
  opacity: 0.9;
}

/* 通用区块 */
.section {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 16px;
}

.section-title {
  font-size: 16px;
  color: #333;
  font-weight: bold;
  margin-bottom: 16px;
}

/* 收货地址 */
.addr-content {
  padding-left: 8px;
}

.addr-name {
  font-size: 15px;
  color: #333;
  margin-bottom: 8px;
}

.addr-name strong {
  font-size: 16px;
  margin-right: 12px;
}

.addr-name span {
  color: #666;
}

.addr-detail {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
}

/* 商品列表 */
.goods-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.goods-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.goods-item:last-child {
  border-bottom: none;
  padding-bottom: 0;
}

.goods-img {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 8px;
  border: 1px solid #eee;
}

.goods-info {
  flex: 1;
}

.goods-title {
  font-size: 14px;
  color: #333;
  line-height: 1.5;
  margin-bottom: 6px;
}

.goods-spec {
  font-size: 12px;
  color: #999;
  background: #f5f5f5;
  display: inline-block;
  padding: 2px 8px;
  border-radius: 4px;
  margin-bottom: 8px;
}

.goods-bottom {
  display: flex;
  gap: 12px;
  align-items: center;
}

.goods-price {
  font-size: 14px;
  color: #ff6b6b;
  font-weight: bold;
}

.goods-num {
  font-size: 13px;
  color: #999;
}

.goods-subtotal {
  font-size: 16px;
  color: #333;
  font-weight: bold;
  min-width: 80px;
  text-align: right;
}

/* 金额明细 */
.amount-rows {
  padding: 0 8px;
}

.amount-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  font-size: 14px;
  color: #666;
  border-bottom: 1px solid #f5f5f5;
}

.amount-row:last-child {
  border-bottom: none;
}

.amount-row.total {
  padding-top: 16px;
  margin-top: 8px;
  border-top: 1px solid #eee;
  font-size: 16px;
  color: #333;
  font-weight: bold;
}

.total-price {
  font-size: 24px;
  color: #ff6b6b;
}

.discount {
  color: #ff6b6b;
}

/* 订单信息 */
.info-rows {
  padding: 0 8px;
}

.info-row {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  font-size: 14px;
  color: #666;
}

.info-row span:first-child {
  color: #999;
}

/* 底部操作栏 */
.action-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #fff;
  border-top: 1px solid #eee;
  box-shadow: 0 -2px 10px rgba(0,0,0,0.05);
  z-index: 100;
}

.action-bar .container {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 12px;
  height: 64px;
}

.btn-back {
  padding: 10px 28px;
  border: 1px solid #ddd;
  background: #fff;
  color: #666;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-back:hover {
  border-color: #999;
  color: #333;
}

.btn-primary {
  padding: 10px 32px;
  border: none;
  background: #ff6b6b;
  color: #fff;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
  transition: background 0.2s;
}

.btn-primary:hover {
  background: #ff5252;
}

/* 加载 */
.loading-state {
  text-align: center;
  padding: 120px 20px;
  color: #999;
  font-size: 16px;
}
</style>