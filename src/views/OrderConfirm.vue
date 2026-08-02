<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useCartStore } from '../stores/cart'
import { useOrderStore } from '../stores/order'
import { createOrder } from '../api/api'

const router = useRouter()
const cartStore = useCartStore()
const orderStore = useOrderStore()

const selectedGoods = computed(() => {
  if (orderStore.buyNowGoods) {
    return [orderStore.buyNowGoods]
  }
  return cartStore.list.filter(item => item.checked)
})

// ========== 收货地址（响应式） ==========
const address = ref({
  name: '',
  phone: '',
  full: ''
})
const hasAddress = computed(() => !!address.value.name && !!address.value.phone && !!address.value.full)

// ========== 弹窗相关 ==========
const showAddressModal = ref(false)
const addressForm = ref({
  name: '',
  phone: '',
  detail: ''
})

const openAddressModal = () => {
  addressForm.value = {
    name: address.value.name || '',
    phone: address.value.phone || '',
    detail: address.value.full || ''
  }
  showAddressModal.value = true
}

const closeAddressModal = () => {
  showAddressModal.value = false
}

const saveAddress = () => {
  if (!addressForm.value.name.trim()) {
    alert('请输入收货人姓名')
    return
  }
  if (!addressForm.value.phone.trim()) {
    alert('请输入手机号')
    return
  }
  if (!addressForm.value.detail.trim()) {
    alert('请输入详细地址')
    return
  }
  address.value = {
    name: addressForm.value.name.trim(),
    phone: addressForm.value.phone.trim(),
    full: addressForm.value.detail.trim()
  }
  showAddressModal.value = false
}

const coupon = ref(200)
const freight = ref(0)

const goodsTotal = computed(() => selectedGoods.value.reduce((sum, item) => sum + item.price * item.num, 0))
const payTotal = computed(() => Math.max(0, goodsTotal.value - coupon.value + freight.value))

const submitOrder = async () => {
  if (selectedGoods.value.length === 0) {
    alert('请先选择要购买的商品')
    return
  }
  if (!hasAddress.value) {
    openAddressModal()
    return
  }

  const orderData = {
    userId: 1,
    totalAmount: goodsTotal.value,
    freight: freight.value,
    payAmount: payTotal.value,
    addressName: address.value.name,
    addressPhone: address.value.phone,
    addressDetail: address.value.full,
    items: selectedGoods.value.map(item => ({
      goodsId: item.id,
      title: item.title,
      spec: item.spec,
      price: item.price,
      num: item.num,
      image: item.image
    }))
  }

  const res = await createOrder(orderData)
  if (res.code === 200) {
    alert('订单提交成功！')
    if (orderStore.buyNowGoods) {
      orderStore.clearBuyNow()
    } else {
      cartStore.removeBatch()
    }
    router.push('/orders')
  } else {
    alert('订单提交失败：' + res.msg)
  }
}
</script>

<template>
  <div class="order-confirm-page">
    <div class="container">
      <h2 class="page-title">确认订单</h2>

      <!-- 收货地址 -->
      <div class="section address-box">
        <h3 class="section-title">📍 收货地址</h3>
        
        <!-- 有地址 -->
        <div v-if="hasAddress" class="address-card" @click="openAddressModal">
          <div class="addr-info">
            <span class="addr-name">{{ address.name }}</span>
            <span class="addr-phone">{{ address.phone }}</span>
            <p class="addr-detail">{{ address.full }}</p>
          </div>
          <span class="addr-edit">修改 ›</span>
        </div>

        <!-- 无地址 -->
        <div v-else class="address-empty" @click="openAddressModal">
          <span class="add-icon">+</span>
          <span>添加收货地址</span>
        </div>
      </div>

      <!-- 商品清单 -->
      <div class="section goods-box">
        <h3 class="section-title">🛍️ 商品清单</h3>
        <div class="goods-table">
          <div class="table-header">
            <span class="col-name">商品信息</span>
            <span class="col-price">单价</span>
            <span class="col-num">数量</span>
            <span class="col-total">小计</span>
          </div>
          <div class="table-row" v-for="item in selectedGoods" :key="item.id + item.spec">
            <div class="col-name">
              <img :src="item.image" class="row-img" />
              <div class="row-info">
                <p class="row-title">{{ item.title }}</p>
                <p class="row-spec">{{ item.spec }}</p>
              </div>
            </div>
            <span class="col-price">¥{{ item.price }}</span>
            <span class="col-num">{{ item.num }}</span>
            <span class="col-total">¥{{ (item.price * item.num).toFixed(2) }}</span>
          </div>
        </div>
      </div>

      <!-- 优惠券 & 运费 -->
      <div class="section coupon-box">
        <div class="coupon-row">
          <span>优惠券</span>
          <span class="coupon-val">- ¥{{ coupon }}</span>
        </div>
        <div class="coupon-row">
          <span>运费</span>
          <span class="freight-val">{{ freight === 0 ? '免运费' : '¥' + freight }}</span>
        </div>
      </div>

      <!-- 底部结算 -->
      <div class="settle-bar">
        <div class="container settle-flex">
          <div class="settle-left">
            <span>共 <strong>{{ selectedGoods.length }}</strong> 件商品</span>
          </div>
          <div class="settle-right">
            <div class="pay-line">
              <span>商品总额：¥{{ goodsTotal.toFixed(2) }}</span>
              <span>优惠：- ¥{{ coupon }}</span>
              <span class="pay-total">
                实付金额：<strong>¥{{ payTotal.toFixed(2) }}</strong>
              </span>
            </div>
            <button class="btn-submit" @click="submitOrder">提交订单</button>
          </div>
        </div>
      </div>
    </div>

    <!-- ========== 地址弹窗 ========== -->
    <div v-if="showAddressModal" class="modal-overlay" @click.self="closeAddressModal">
      <div class="modal-card">
        <div class="modal-header">
          <h3>{{ hasAddress ? '修改收货地址' : '添加收货地址' }}</h3>
          <span class="modal-close" @click="closeAddressModal">✕</span>
        </div>
        
        <div class="modal-body">
          <div class="form-row">
            <label>收货人</label>
            <input v-model="addressForm.name" type="text" placeholder="请输入收货人姓名" />
          </div>
          <div class="form-row">
            <label>手机号</label>
            <input v-model="addressForm.phone" type="text" placeholder="请输入手机号" maxlength="11" />
          </div>
          <div class="form-row">
            <label>详细地址</label>
            <textarea v-model="addressForm.detail" rows="3" placeholder="请输入省市区及详细街道、门牌号"></textarea>
          </div>
        </div>

        <div class="modal-footer">
          <button class="btn-cancel" @click="closeAddressModal">取消</button>
          <button class="btn-save" @click="saveAddress">保存</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.order-confirm-page {
  background: #f4f4f4;
  min-height: 100vh;
  padding: 20px 0 100px;
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

/* 通用区块 */
.section {
  background: #fff;
  border-radius: 8px;
  padding: 24px;
  margin-bottom: 16px;
}
.section-title {
  font-size: 16px;
  color: #333;
  margin-bottom: 16px;
  font-weight: bold;
}

/* 地址 - 有地址 */
.address-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border: 1px solid #ffe0e0;
  background: #fff8f8;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}
.address-card:hover {
  background: #fff0f0;
  border-color: #ff6b6b;
}
.addr-name {
  font-size: 16px;
  font-weight: bold;
  color: #333;
  margin-right: 12px;
}
.addr-phone {
  font-size: 14px;
  color: #666;
}
.addr-detail {
  font-size: 14px;
  color: #666;
  margin-top: 6px;
}
.addr-edit {
  color: #ff6b6b;
  font-size: 14px;
  cursor: pointer;
}

/* 地址 - 无地址 */
.address-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 30px;
  border: 2px dashed #ddd;
  border-radius: 8px;
  color: #999;
  font-size: 15px;
  cursor: pointer;
  transition: all 0.2s;
}
.address-empty:hover {
  border-color: #ff6b6b;
  color: #ff6b6b;
  background: #fff5f5;
}
.add-icon {
  font-size: 24px;
  font-weight: 300;
}

/* 商品表格 */
.goods-table {
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  overflow: hidden;
}
.table-header {
  display: flex;
  align-items: center;
  padding: 14px 20px;
  background: #f9f9f9;
  font-size: 14px;
  color: #666;
}
.table-row {
  display: flex;
  align-items: center;
  padding: 20px;
  border-top: 1px solid #f0f0f0;
}
.col-name {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 16px;
}
.col-price {
  width: 120px;
  text-align: center;
  font-size: 14px;
  color: #333;
}
.col-num {
  width: 100px;
  text-align: center;
  font-size: 14px;
  color: #333;
}
.col-total {
  width: 120px;
  text-align: center;
  font-size: 16px;
  color: #ff6b6b;
  font-weight: bold;
}
.row-img {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 4px;
  border: 1px solid #eee;
}
.row-title {
  font-size: 14px;
  color: #333;
  margin-bottom: 6px;
}
.row-spec {
  font-size: 12px;
  color: #999;
  background: #f5f5f5;
  display: inline-block;
  padding: 2px 8px;
  border-radius: 4px;
}

/* 优惠券 */
.coupon-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  font-size: 14px;
  color: #333;
  border-bottom: 1px solid #f5f5f5;
}
.coupon-row:last-child {
  border-bottom: none;
}
.coupon-val {
  color: #ff6b6b;
  font-weight: bold;
}
.freight-val {
  color: #52c41a;
}

/* 底部结算 */
.settle-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #fff;
  border-top: 1px solid #eee;
  box-shadow: 0 -2px 10px rgba(0,0,0,0.05);
  z-index: 100;
}
.settle-flex {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 70px;
}
.settle-left {
  font-size: 14px;
  color: #666;
}
.settle-left strong {
  color: #ff6b6b;
}
.settle-right {
  display: flex;
  align-items: center;
  gap: 24px;
}
.pay-line {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4px;
  font-size: 14px;
  color: #666;
}
.pay-total {
  font-size: 16px;
}
.pay-total strong {
  font-size: 24px;
  color: #ff6b6b;
}
.btn-submit {
  padding: 0 50px;
  height: 48px;
  background: #ff6b6b;
  color: #fff;
  border: none;
  border-radius: 4px;
  font-size: 16px;
  cursor: pointer;
  transition: background 0.2s;
}
.btn-submit:hover {
  background: #ff5252;
}

/* ========== 弹窗 ========== */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  animation: fadeIn 0.2s ease;
}

.modal-card {
  width: 480px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 20px 60px rgba(0,0,0,0.2);
  animation: slideUp 0.25s ease;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #f0f0f0;
}
.modal-header h3 {
  font-size: 18px;
  color: #333;
  font-weight: 600;
}
.modal-close {
  font-size: 20px;
  color: #999;
  cursor: pointer;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: all 0.2s;
}
.modal-close:hover {
  background: #f5f5f5;
  color: #333;
}

.modal-body {
  padding: 24px;
}
.form-row {
  margin-bottom: 16px;
}
.form-row:last-child {
  margin-bottom: 0;
}
.form-row label {
  display: block;
  font-size: 14px;
  color: #333;
  margin-bottom: 8px;
  font-weight: 500;
}
.form-row input,
.form-row textarea {
  width: 100%;
  padding: 12px 16px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 14px;
  outline: none;
  transition: border-color 0.2s;
  resize: none;
}
.form-row input:focus,
.form-row textarea:focus {
  border-color: #ff6b6b;
}
.form-row input::placeholder,
.form-row textarea::placeholder {
  color: #bbb;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 16px 24px 24px;
}
.btn-cancel {
  padding: 10px 28px;
  border: 1px solid #ddd;
  background: #fff;
  color: #666;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}
.btn-cancel:hover {
  border-color: #999;
  color: #333;
}
.btn-save {
  padding: 10px 28px;
  border: none;
  background: #ff6b6b;
  color: #fff;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
  transition: background 0.2s;
}
.btn-save:hover {
  background: #ff5252;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}
@keyframes slideUp {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>