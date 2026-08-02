<script setup>
import { computed, onMounted } from 'vue'
import { useCartStore } from '../stores/cart'
import { getCartList } from '../api/api'

const cartStore = useCartStore()

const cartList = computed(() => cartStore.list)
const isAllChecked = computed({
  get: () => cartStore.isAllChecked,
  set: (val) => { cartStore.isAllChecked = val }
})
const selectedCount = computed(() => cartStore.selectedCount)
const totalPrice = computed(() => cartStore.totalPrice)

onMounted(async () => {
  try {
    const userId = 1
    const res = await getCartList(userId)
    if (res.code === 200 && res.data && res.data.length > 0) {
      cartStore.list = res.data.map(item => ({
        cartId: item.id,
        id: item.goodsId,
        title: item.title,
        spec: item.spec,
        price: item.price,
        num: item.num,
        image: item.image,
        checked: item.checked === 1,
        shop: 'Apple官方旗舰店'
      }))
    }
  } catch (err) {
    console.error('加载购物车失败:', err)
  }
})
</script>

<template>
  <div class="cart-page">
    <div class="container">
      <div class="page-header">
        <h2>🛒 我的购物车</h2>
        <span class="cart-count">共 {{ cartList.length }} 件商品</span>
      </div>

      <div v-if="cartList.length === 0" class="empty-cart">
        <div class="empty-icon">🛒</div>
        <p>购物车还是空的，快去挑选心仪的商品吧~</p>
        <button class="btn-go" @click="$router.push('/home')">去逛逛</button>
      </div>

      <div v-else class="cart-main">
        <div class="cart-header">
          <div class="col-check">
            <input type="checkbox" v-model="isAllChecked" />
            <span>全选</span>
          </div>
          <div class="col-info">商品信息</div>
          <div class="col-price">单价</div>
          <div class="col-num">数量</div>
          <div class="col-total">小计</div>
          <div class="col-action">操作</div>
        </div>

        <div class="cart-item" v-for="item in cartList" :key="item.cartId">
          <div class="col-check">
            <input type="checkbox" v-model="item.checked" />
          </div>
          <div class="col-info">
            <img :src="item.image" class="item-img" />
            <div class="item-detail">
              <p class="item-title">{{ item.title }}</p>
              <p class="item-spec">{{ item.spec }}</p>
            </div>
          </div>
          <div class="col-price">
            <span class="price">¥{{ item.price }}</span>
          </div>
          <div class="col-num">
            <div class="num-box">
              <button @click="cartStore.changeNum(item.cartId, -1)">−</button>
              <input type="text" v-model.number="item.num" />
              <button @click="cartStore.changeNum(item.cartId, 1)">+</button>
            </div>
          </div>
          <div class="col-total">
            <span class="total-price">¥{{ item.price * item.num }}</span>
          </div>
          <div class="col-action">
            <span class="btn-remove" @click="cartStore.remove(item.cartId)">删除</span>
          </div>
        </div>
      </div>
    </div>

    <div v-if="cartList.length > 0" class="cart-footer">
      <div class="container footer-flex">
        <div class="footer-left">
          <label class="check-all">
            <input type="checkbox" v-model="isAllChecked" />
            <span>全选</span>
          </label>
          <span class="remove-batch" @click="cartStore.removeBatch()">
            删除选中商品
          </span>
        </div>
        <div class="footer-right">
          <div class="settle-info">
            <span class="selected">
              已选 <strong>{{ selectedCount }}</strong> 件商品
            </span>
            <span class="total">
              合计：<strong class="total-num">¥{{ totalPrice }}</strong>
            </span>
          </div>
          <button class="btn-settle" :disabled="selectedCount === 0" 
            @click="$router.push('/order-confirm')">
            去结算
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.cart-page { background: #f4f4f4; min-height: 100vh; padding-bottom: 100px; }
.container { max-width: 1200px; margin: 0 auto; padding: 0 20px; }

.page-header { display: flex; align-items: baseline; gap: 12px; padding: 24px 0 16px; }
.page-header h2 { font-size: 24px; color: #333; font-weight: bold; }
.cart-count { font-size: 14px; color: #999; }

.empty-cart { text-align: center; padding: 80px 0; background: #fff; border-radius: 8px; }
.empty-icon { font-size: 80px; margin-bottom: 16px; }
.empty-cart p { font-size: 16px; color: #999; margin-bottom: 24px; }
.btn-go { padding: 10px 40px; background: #ff6b6b; color: #fff; border: none; border-radius: 4px; font-size: 15px; cursor: pointer; }
.btn-go:hover { background: #ff5252; }

.cart-main { background: #fff; border-radius: 8px; overflow: hidden; }
.cart-header { display: flex; align-items: center; padding: 16px 20px; background: #f9f9f9; border-bottom: 1px solid #eee; font-size: 14px; color: #666; }
.cart-item { display: flex; align-items: center; padding: 20px; border-bottom: 1px solid #f0f0f0; transition: background 0.2s; }
.cart-item:hover { background: #fafafa; }

.col-check { width: 80px; display: flex; align-items: center; gap: 6px; }
.col-check input { width: 18px; height: 18px; cursor: pointer; }
.col-info { flex: 1; display: flex; gap: 16px; align-items: center; }
.col-price { width: 120px; text-align: center; }
.col-num { width: 140px; text-align: center; }
.col-total { width: 120px; text-align: center; }
.col-action { width: 80px; text-align: center; }

.item-img { width: 80px; height: 80px; object-fit: cover; border-radius: 4px; border: 1px solid #eee; }
.item-detail { flex: 1; }
.item-title { font-size: 14px; color: #333; line-height: 1.5; display: -webkit-box; -webkit-line-clamp: 2; line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; margin-bottom: 6px; }
.item-spec { font-size: 12px; color: #999; background: #f5f5f5; display: inline-block; padding: 2px 8px; border-radius: 4px; }
.price { font-size: 14px; color: #333; }

.num-box { display: inline-flex; border: 1px solid #ddd; border-radius: 4px; overflow: hidden; }
.num-box button { width: 32px; height: 32px; background: #f5f5f5; border: none; font-size: 16px; cursor: pointer; color: #666; }
.num-box button:hover { background: #eee; }
.num-box input { width: 50px; height: 32px; border: none; text-align: center; font-size: 14px; outline: none; }

.total-price { font-size: 16px; color: #ff6b6b; font-weight: bold; }
.btn-remove { font-size: 13px; color: #999; cursor: pointer; }
.btn-remove:hover { color: #ff6b6b; }

.cart-footer { position: fixed; bottom: 0; left: 0; right: 0; background: #fff; border-top: 1px solid #eee; box-shadow: 0 -2px 10px rgba(0,0,0,0.05); z-index: 100; }
.footer-flex { display: flex; justify-content: space-between; align-items: center; height: 60px; }
.footer-left { display: flex; align-items: center; gap: 20px; }
.check-all { display: flex; align-items: center; gap: 6px; font-size: 14px; color: #333; cursor: pointer; }
.check-all input { width: 18px; height: 18px; cursor: pointer; }
.remove-batch { font-size: 13px; color: #999; cursor: pointer; }
.remove-batch:hover { color: #ff6b6b; }
.footer-right { display: flex; align-items: center; gap: 20px; }
.settle-info { display: flex; align-items: center; gap: 24px; font-size: 14px; color: #666; }
.selected strong { color: #ff6b6b; font-size: 16px; }
.total-num { font-size: 22px; color: #ff6b6b; }
.btn-settle { padding: 0 40px; height: 44px; background: #ff6b6b; color: #fff; border: none; border-radius: 4px; font-size: 16px; cursor: pointer; transition: background 0.2s; }
.btn-settle:hover:not(:disabled) { background: #ff5252; }
.btn-settle:disabled { background: #ccc; cursor: not-allowed; }
</style>