<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { searchGoods } from '../api/api'

const router = useRouter()
const route = useRoute()

const keyword = ref('')
const goodsList = ref([])
const loading = ref(false)
const sortType = ref('default')

const breadcrumb = computed(() => [
  { name: '首页', path: '/home' },
  { name: `搜索结果：${keyword.value || '...'}` }
])

const filteredGoods = computed(() => {
  let list = [...goodsList.value]
  if (sortType.value === 'sales') {
    list.sort((a, b) => (b.sales || 0) - (a.sales || 0))
  } else if (sortType.value === 'price_asc') {
    list.sort((a, b) => a.price - b.price)
  } else if (sortType.value === 'price_desc') {
    list.sort((a, b) => b.price - a.price)
  }
  return list
})

const loadSearch = async () => {
  if (!keyword.value) return
  loading.value = true
  try {
    const res = await searchGoods(keyword.value)
    if (res.code === 200 && res.data) {
      goodsList.value = res.data
    }
  } catch (err) {
    console.error('搜索失败:', err)
  } finally {
    loading.value = false
  }
}

const setSort = (type) => {
  sortType.value = type
}

const goDetail = (id) => {
  router.push(`/goods/${id}`)
}

watch(() => route.query.keyword, (val) => {
  if (val) {
    keyword.value = decodeURIComponent(val)
    loadSearch()
  }
}, { immediate: true })
</script>

<template>
  <div class="search-page">
    <div class="container">
      <!-- 面包屑 -->
      <div class="breadcrumb">
        <span v-for="(item, idx) in breadcrumb" :key="idx" class="bc-item">
          <a v-if="item.path" @click="router.push(item.path)">{{ item.name }}</a>
          <span v-else :class="{ active: idx === breadcrumb.length - 1 }">{{ item.name }}</span>
          <span v-if="idx < breadcrumb.length - 1" class="bc-sep">›</span>
        </span>
      </div>

      <!-- 搜索统计 -->
      <div class="search-header">
        <h2 class="search-title">
          "{{ keyword }}"
          <span class="search-count">共找到 <strong>{{ filteredGoods.length }}</strong> 件商品</span>
        </h2>
      </div>

      <!-- 排序栏 -->
      <div class="sort-bar">
        <div class="sort-left">
          <span
            class="sort-item"
            :class="{ active: sortType === 'default' }"
            @click="setSort('default')"
          >综合排序</span>
          <span
            class="sort-item"
            :class="{ active: sortType === 'sales' }"
            @click="setSort('sales')"
          >销量优先</span>
          <span
            class="sort-item"
            :class="{ active: sortType === 'price_asc' || sortType === 'price_desc' }"
            @click="setSort(sortType === 'price_asc' ? 'price_desc' : 'price_asc')"
          >
            价格
            <span class="sort-arrow">
              {{ sortType === 'price_asc' ? '↑' : sortType === 'price_desc' ? '↓' : '↕' }}
            </span>
          </span>
        </div>
      </div>

      <!-- 加载中 -->
      <div v-if="loading" class="loading-state">
        <div class="loading-spinner"></div>
        <p>正在搜索...</p>
      </div>

      <!-- 空状态 -->
      <div v-else-if="filteredGoods.length === 0" class="empty-state">
        <div class="empty-icon">🔍</div>
        <p class="empty-title">未找到相关商品</p>
        <p class="empty-desc">换个关键词试试，或去首页逛逛吧~</p>
        <button class="btn-go" @click="router.push('/home')">去首页</button>
      </div>

      <!-- 商品列表 -->
      <div v-else class="goods-list">
        <div
          class="goods-card"
          v-for="item in filteredGoods"
          :key="item.id"
          @click="goDetail(item.id)"
        >
          <div class="card-img-wrap">
            <img :src="item.image" class="goods-img" loading="lazy" />
            <span v-if="item.tag" class="card-tag">{{ item.tag }}</span>
          </div>
          <div class="goods-info">
            <p class="goods-title">{{ item.title }}</p>
            <div class="goods-meta">
              <span class="goods-sales">已售 {{ item.sales || 0 }}</span>
            </div>
            <div class="goods-price-row">
              <span class="price-now">¥{{ item.price }}</span>
              <span v-if="item.oldPrice" class="price-old">¥{{ item.oldPrice }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.search-page {
  background: #f4f4f4;
  min-height: 100vh;
  padding: 20px 0 60px;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

/* 面包屑 */
.breadcrumb {
  font-size: 13px;
  color: #999;
  margin-bottom: 16px;
}

.bc-item a {
  color: #666;
  cursor: pointer;
  transition: color 0.2s;
}

.bc-item a:hover {
  color: #ff6b6b;
}

.bc-item span.active {
  color: #333;
  font-weight: 500;
}

.bc-sep {
  margin: 0 6px;
  color: #ccc;
}

/* 搜索标题 */
.search-header {
  margin-bottom: 16px;
}

.search-title {
  font-size: 22px;
  color: #333;
  font-weight: 700;
}

.search-title .search-count {
  font-size: 14px;
  color: #999;
  font-weight: 400;
  margin-left: 12px;
}

.search-count strong {
  color: #ff6b6b;
}

/* 排序栏 */
.sort-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fff;
  padding: 12px 20px;
  border-radius: 8px;
  margin-bottom: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

.sort-left {
  display: flex;
  gap: 4px;
  align-items: center;
}

.sort-item {
  padding: 6px 14px;
  border-radius: 4px;
  font-size: 13px;
  color: #666;
  cursor: pointer;
  transition: all 0.2s;
  user-select: none;
  display: flex;
  align-items: center;
  gap: 4px;
}

.sort-item:hover {
  color: #ff6b6b;
}

.sort-item.active {
  background: #fff5f5;
  color: #ff6b6b;
  font-weight: 500;
}

.sort-arrow {
  font-size: 11px;
  color: #ccc;
}

.sort-item.active .sort-arrow {
  color: #ff6b6b;
}

/* 商品列表 */
.goods-list {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.goods-card {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.goods-card:hover {
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
  transform: translateY(-4px);
}

.card-img-wrap {
  position: relative;
  width: 100%;
  padding-top: 100%;
  overflow: hidden;
  background: #f9f9f9;
}

.goods-img {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.goods-card:hover .goods-img {
  transform: scale(1.05);
}

.card-tag {
  position: absolute;
  top: 10px;
  left: 10px;
  background: #ff6b6b;
  color: #fff;
  font-size: 12px;
  padding: 2px 10px;
  border-radius: 12px;
  z-index: 1;
}

.goods-info {
  padding: 14px;
}

.goods-title {
  font-size: 14px;
  color: #333;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 42px;
  margin-bottom: 6px;
}

.goods-meta {
  margin-bottom: 8px;
}

.goods-sales {
  font-size: 12px;
  color: #999;
}

.goods-price-row {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.price-now {
  font-size: 20px;
  color: #ff6b6b;
  font-weight: bold;
}

.price-old {
  font-size: 13px;
  color: #ccc;
  text-decoration: line-through;
}

/* 加载 / 空状态 */
.loading-state {
  text-align: center;
  padding: 80px 20px;
  background: #fff;
  border-radius: 8px;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 3px solid #f0f0f0;
  border-top-color: #ff6b6b;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  margin: 0 auto 16px;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.loading-state p {
  font-size: 14px;
  color: #999;
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
  transition: background 0.2s;
}

.btn-go:hover {
  background: #ff5252;
}

/* 响应式 */
@media (max-width: 1024px) {
  .goods-list {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 768px) {
  .goods-list {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }
}
</style>