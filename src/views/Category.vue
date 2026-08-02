<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getGoodsList } from '../api/api'

const router = useRouter()
const route = useRoute()

// ========== 分类数据（前端静态配置，等后端有分类表后可接接口替换） ==========
const categories = ref([
  {
    id: 1,
    name: '手机数码',
    icon: '📱',
    children: [
      { id: 11, name: '手机' },
      { id: 12, name: '平板电脑' },
      { id: 13, name: '智能手表' },
      { id: 14, name: '耳机音箱' },
      { id: 15, name: '充电器' },
      { id: 16, name: '数据线' }
    ]
  },
  {
    id: 2,
    name: '电脑办公',
    icon: '💻',
    children: [
      { id: 21, name: '笔记本电脑' },
      { id: 22, name: '台式机' },
      { id: 23, name: '显示器' },
      { id: 24, name: '键盘鼠标' },
      { id: 25, name: 'U盘硬盘' },
      { id: 26, name: '办公耗材' }
    ]
  },
  {
    id: 3,
    name: '家用电器',
    icon: '📺',
    children: [
      { id: 31, name: '电视' },
      { id: 32, name: '空调' },
      { id: 33, name: '冰箱' },
      { id: 34, name: '洗衣机' },
      { id: 35, name: '吸尘器' },
      { id: 36, name: '电饭煲' }
    ]
  },
  {
    id: 4,
    name: '服装鞋包',
    icon: '👜',
    children: [
      { id: 41, name: '男装' },
      { id: 42, name: '女装' },
      { id: 43, name: '童装' },
      { id: 44, name: '运动鞋' },
      { id: 45, name: '箱包' },
      { id: 46, name: '配饰' }
    ]
  },
  {
    id: 5,
    name: '食品生鲜',
    icon: '🍎',
    children: [
      { id: 51, name: '水果' },
      { id: 52, name: '零食' },
      { id: 53, name: '饮料' },
      { id: 54, name: '粮油' },
      { id: 55, name: '肉类' },
      { id: 56, name: '海鲜' }
    ]
  },
  {
    id: 6,
    name: '美妆护肤',
    icon: '💄',
    children: [
      { id: 61, name: '面膜' },
      { id: 62, name: '口红' },
      { id: 63, name: '护肤套装' },
      { id: 64, name: '香水' },
      { id: 65, name: '彩妆' },
      { id: 66, name: '洗护' }
    ]
  },
  {
    id: 7,
    name: '家居家装',
    icon: '🏠',
    children: [
      { id: 71, name: '家具' },
      { id: 72, name: '灯具' },
      { id: 73, name: '家纺' },
      { id: 74, name: '厨具' },
      { id: 75, name: '卫浴' },
      { id: 76, name: '五金' }
    ]
  },
  {
    id: 8,
    name: '运动户外',
    icon: '⚽',
    children: [
      { id: 81, name: '健身器材' },
      { id: 82, name: '户外装备' },
      { id: 83, name: '运动鞋服' },
      { id: 84, name: '球类' },
      { id: 85, name: '骑行' },
      { id: 86, name: '游泳' }
    ]
  },
  {
    id: 9,
    name: '图书文具',
    icon: '📚',
    children: [
      { id: 91, name: '文学小说' },
      { id: 92, name: '少儿读物' },
      { id: 93, name: '考试用书' },
      { id: 94, name: '文具用品' }
    ]
  },
  {
    id: 10,
    name: '母婴用品',
    icon: '🍼',
    children: [
      { id: 101, name: '奶粉' },
      { id: 102, name: '尿裤' },
      { id: 103, name: '玩具' },
      { id: 104, name: '童装' }
    ]
  }
])

// ========== 状态 ==========
const activeCatId = ref(1)
const activeSubId = ref(null)
const goodsList = ref([])
const loading = ref(false)
const sortType = ref('default')

// ========== 计算属性 ==========
const currentCategory = computed(() =>
  categories.value.find(c => c.id === activeCatId.value) || categories.value[0]
)

const breadcrumb = computed(() => {
  const items = [{ name: '首页', path: '/home' }]
  if (currentCategory.value) {
    items.push({ name: currentCategory.value.name })
    if (activeSubId.value) {
      const sub = currentCategory.value.children.find(c => c.id === activeSubId.value)
      if (sub) items.push({ name: sub.name })
    }
  }
  return items
})

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

// ========== 方法 ==========
const loadGoods = async () => {
  loading.value = true
  try {
    const params = {}
    if (activeSubId.value) {
      params.subCategoryId = activeSubId.value
    } else if (activeCatId.value) {
      params.categoryId = activeCatId.value
    }
    const res = await getGoodsList(params)
    if (res.code === 200 && res.data) {
      goodsList.value = res.data
    }
  } catch (err) {
    console.error('加载商品失败:', err)
  } finally {
    loading.value = false
  }
}

const switchCategory = (id) => {
  activeCatId.value = id
  activeSubId.value = null
  loadGoods()
}

const switchSubCategory = (id) => {
  activeSubId.value = id
  loadGoods()
}

const setSort = (type) => {
  sortType.value = type
}

const goDetail = (id) => {
  router.push(`/goods/${id}`)
}

watch(() => route.query.cat, (val) => {
  if (val) {
    const catId = Number(val)
    const found = categories.value.find(c => c.id === catId)
    if (found) {
      activeCatId.value = catId
      activeSubId.value = null
      loadGoods()
    }
  }
}, { immediate: true })

onMounted(() => {
  if (!route.query.cat) {
    loadGoods()
  }
})
</script>

<template>
  <div class="category-page">
    <div class="container category-flex">
      <!-- 左侧分类树 -->
      <aside class="category-sidebar">
        <div class="sidebar-header">
          <span class="sidebar-icon">📂</span>
          <span>全部分类</span>
        </div>
        <div class="cat-list">
          <div
            v-for="cat in categories"
            :key="cat.id"
            class="cat-item"
            :class="{ active: activeCatId === cat.id }"
            @click="switchCategory(cat.id)"
          >
            <span class="cat-icon">{{ cat.icon }}</span>
            <span class="cat-name">{{ cat.name }}</span>
            <span v-if="activeCatId === cat.id" class="cat-arrow">›</span>
          </div>
        </div>
      </aside>

      <!-- 右侧主内容 -->
      <main class="category-main">
        <!-- 面包屑 + 标题 -->
        <div class="main-header">
          <div class="breadcrumb">
            <span
              v-for="(item, idx) in breadcrumb"
              :key="idx"
              class="bc-item"
            >
              <a v-if="item.path" @click="router.push(item.path)">{{ item.name }}</a>
              <span v-else :class="{ active: idx === breadcrumb.length - 1 }">{{ item.name }}</span>
              <span v-if="idx < breadcrumb.length - 1" class="bc-sep">›</span>
            </span>
          </div>
          <h2 class="cat-title">
            {{ currentCategory?.name }}
            <span v-if="activeSubId" class="sub-title">
              — {{ currentCategory?.children.find(c => c.id === activeSubId)?.name }}
            </span>
          </h2>
        </div>

        <!-- 子分类快捷入口 -->
        <div class="sub-cat-bar">
          <div
            class="sub-cat"
            :class="{ active: activeSubId === null }"
            @click="switchSubCategory(null)"
          >
            全部
          </div>
          <div
            v-for="sub in currentCategory?.children"
            :key="sub.id"
            class="sub-cat"
            :class="{ active: activeSubId === sub.id }"
            @click="switchSubCategory(sub.id)"
          >
            {{ sub.name }}
          </div>
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
          <div class="sort-right">
            共 <strong>{{ filteredGoods.length }}</strong> 件商品
          </div>
        </div>

        <!-- 加载中 -->
        <div v-if="loading && filteredGoods.length === 0" class="loading-state">
          <div class="loading-spinner"></div>
          <p>商品加载中...</p>
        </div>

        <!-- 空状态 -->
        <div v-else-if="filteredGoods.length === 0" class="empty-state">
          <div class="empty-icon">📦</div>
          <p class="empty-title">该分类暂无商品</p>
          <p class="empty-desc">去看看其他分类吧~</p>
          <button class="btn-go" @click="switchCategory(1)">去首页逛逛</button>
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
      </main>
    </div>
  </div>
</template>

<style scoped>
.category-page {
  background: #f4f4f4;
  min-height: 100vh;
  padding: 20px 0 60px;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.category-flex {
  display: flex;
  gap: 20px;
  align-items: flex-start;
}

/* ========== 左侧分类树 ========== */
.category-sidebar {
  width: 220px;
  background: #fff;
  border-radius: 12px;
  padding: 16px 0;
  flex-shrink: 0;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  position: sticky;
  top: 20px;
}

.sidebar-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 0 20px 14px;
  margin-bottom: 8px;
  border-bottom: 1px solid #f0f0f0;
  font-size: 15px;
  font-weight: 600;
  color: #333;
}

.sidebar-icon {
  font-size: 18px;
}

.cat-list {
  padding: 4px 0;
}

.cat-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 20px;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 14px;
  color: #666;
  position: relative;
}

.cat-item:hover {
  background: #f9f9f9;
  color: #ff6b6b;
}

.cat-item.active {
  background: #fff5f5;
  color: #ff6b6b;
  font-weight: 600;
}

.cat-item.active::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 20px;
  background: #ff6b6b;
  border-radius: 0 3px 3px 0;
}

.cat-icon {
  font-size: 18px;
  width: 24px;
  text-align: center;
}

.cat-name {
  flex: 1;
}

.cat-arrow {
  font-size: 14px;
  color: #ff6b6b;
}

/* ========== 右侧主内容 ========== */
.category-main {
  flex: 1;
  min-width: 0;
}

.main-header {
  margin-bottom: 16px;
}

.breadcrumb {
  font-size: 13px;
  color: #999;
  margin-bottom: 10px;
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

.cat-title {
  font-size: 22px;
  color: #333;
  font-weight: 700;
}

.sub-title {
  font-size: 16px;
  color: #999;
  font-weight: 400;
  margin-left: 8px;
}

/* ========== 子分类 ========== */
.sub-cat-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  background: #fff;
  padding: 16px 20px;
  border-radius: 8px;
  margin-bottom: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

.sub-cat {
  padding: 6px 16px;
  background: #f5f5f5;
  border-radius: 6px;
  font-size: 13px;
  color: #666;
  cursor: pointer;
  transition: all 0.2s;
  user-select: none;
}

.sub-cat:hover {
  background: #ff6b6b;
  color: #fff;
}

.sub-cat.active {
  background: #ff6b6b;
  color: #fff;
  font-weight: 500;
}

/* ========== 排序栏 ========== */
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

.sort-right {
  font-size: 13px;
  color: #999;
}

.sort-right strong {
  color: #ff6b6b;
}

/* ========== 商品列表 ========== */
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

/* ========== 加载 / 空状态 ========== */
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

/* ========== 响应式 ========== */
@media (max-width: 1024px) {
  .goods-list {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 768px) {
  .category-sidebar {
    display: none;
  }

  .goods-list {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }

  .sub-cat-bar {
    padding: 12px;
    gap: 8px;
  }

  .sub-cat {
    padding: 5px 12px;
    font-size: 12px;
  }
}
</style>