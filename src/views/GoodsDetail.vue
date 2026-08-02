<script setup>
import { ref, onMounted, watch, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useCartStore } from '../stores/cart'
import { useOrderStore } from '../stores/order'
import { getGoodsDetail, addCartItem } from '../api/api'

const route = useRoute()
const router = useRouter()
const cartStore = useCartStore()
const orderStore = useOrderStore()

const goods = ref({
  id: 0,
  title: '',
  subtitle: '',
  price: 0,
  oldPrice: 0,
  sales: 0,
  stock: 0,
  image: ''
})

const images = ref([])
const currentImage = ref(0)

const skuList = ref([])
const selectedSku = ref({})
const quantity = ref(1)

const activeTab = ref('detail')
const tabs = [
  { key: 'detail', label: '商品介绍' },
  { key: 'params', label: '规格参数' },
  { key: 'reviews', label: '累计评价' }
]

const params = ref([])
const reviews = ref([])

// 商品详情图（根据商品ID动态）
const detailImages = computed(() => {
  const map = {
    1: ['https://img.yzcdn.cn/vant/apple-1.jpg', 'https://img.yzcdn.cn/vant/apple-2.jpg', 'https://img.yzcdn.cn/vant/apple-3.jpg'],
    2: ['https://img.yzcdn.cn/vant/apple-2.jpg', 'https://img.yzcdn.cn/vant/apple-3.jpg', 'https://img.yzcdn.cn/vant/ipad.jpeg'],
    3: ['https://img.yzcdn.cn/vant/apple-3.jpg', 'https://img.yzcdn.cn/vant/apple-1.jpg'],
    4: ['https://img.yzcdn.cn/vant/ipad.jpeg', 'https://img.yzcdn.cn/vant/apple-1.jpg'],
    5: ['https://img.yzcdn.cn/vant/cat.jpeg', 'https://img.yzcdn.cn/vant/apple-2.jpg'],
    6: ['https://img.yzcdn.cn/vant/empty-image-error.png', 'https://img.yzcdn.cn/vant/apple-3.jpg']
  }
  return map[goods.value.id] || ['https://img.yzcdn.cn/vant/apple-1.jpg']
})

// 根据商品ID动态设置规格、参数、评价
const loadGoodsConfig = (goodsId) => {
  const numId = Number(goodsId)

  // 规格
  const skuMap = {
    1: [
      { name: '颜色', options: ['黑色钛金属', '白色钛金属', '原色钛金属', '沙漠色钛金属'] },
      { name: '容量', options: ['256GB', '512GB', '1TB'] }
    ],
    2: [
      { name: '颜色', options: ['深空黑色', '银色'] },
      { name: '内存', options: ['18GB', '36GB'] }
    ],
    3: [
      { name: '颜色', options: ['银色', '黑色', '午夜蓝'] }
    ],
    4: [
      { name: '版本', options: ['标准版', '全能版'] }
    ],
    5: [
      { name: '尺码', options: ['40', '41', '42', '43', '44'] },
      { name: '颜色', options: ['纯白', '纯黑'] }
    ],
    6: [
      { name: '规格', options: ['230ml', '330ml'] }
    ]
  }
  skuList.value = skuMap[numId] || [{ name: '规格', options: ['默认'] }]
  selectedSku.value = {}

  // 参数
  const paramMap = {
    1: [
      { name: '品牌', value: 'Apple/苹果' },
      { name: '型号', value: 'iPhone 16 Pro Max' },
      { name: '机身颜色', value: '钛金属' },
      { name: '存储容量', value: '256GB' },
      { name: '屏幕尺寸', value: '6.9英寸' },
      { name: '分辨率', value: '2868 x 1320' },
      { name: 'CPU型号', value: 'A18 Pro' },
      { name: '电池容量', value: '4685mAh' },
      { name: '充电功率', value: '27W' },
      { name: '5G网络', value: '支持' }
    ],
    2: [
      { name: '品牌', value: 'Apple/苹果' },
      { name: '型号', value: 'MacBook Pro 14英寸' },
      { name: '芯片', value: 'Apple M3' },
      { name: '内存', value: '18GB' },
      { name: '硬盘', value: '512GB SSD' },
      { name: '屏幕', value: '14.2英寸 Liquid Retina XDR' },
      { name: '续航', value: '最长18小时' },
      { name: '重量', value: '1.61kg' }
    ],
    3: [
      { name: '品牌', value: 'Sony/索尼' },
      { name: '型号', value: 'WH-1000XM5' },
      { name: '类型', value: '头戴式无线降噪耳机' },
      { name: '续航', value: '30小时' },
      { name: '降噪', value: '主动降噪' },
      { name: '连接', value: '蓝牙5.2' }
    ],
    4: [
      { name: '品牌', value: 'Dyson/戴森' },
      { name: '型号', value: 'V15 Detect Fluffy' },
      { name: '类型', value: '无绳吸尘器' },
      { name: '吸力', value: '230AW' },
      { name: '续航', value: '60分钟' },
      { name: '重量', value: '2.17kg' }
    ],
    5: [
      { name: '品牌', value: 'Nike/耐克' },
      { name: '型号', value: 'Air Force 1 \'07' },
      { name: '鞋面', value: '头层牛皮' },
      { name: '鞋底', value: '橡胶' },
      { name: '闭合方式', value: '系带' },
      { name: '适用场景', value: '休闲' }
    ],
    6: [
      { name: '品牌', value: 'SK-II' },
      { name: '品名', value: '护肤精华露（神仙水）' },
      { name: '规格', value: '230ml' },
      { name: '功效', value: '补水、保湿、控油平衡' },
      { name: '适用肤质', value: '任何肤质' }
    ]
  }
  params.value = paramMap[numId] || []

  // 评价
  const reviewMap = {
    1: [
      { user: '张***', avatar: '👤', rating: 5, content: '手机非常好用，物流很快，第二天就到了！', date: '2026-07-20', images: [] },
      { user: '李***', avatar: '👤', rating: 5, content: '拍照效果太惊艳了，钛金属手感也很棒，值得入手。', date: '2026-07-18', images: ['https://img.yzcdn.cn/vant/apple-1.jpg'] },
      { user: '王***', avatar: '👤', rating: 4, content: '整体不错，就是价格有点贵，等活动买的。', date: '2026-07-15', images: [] }
    ],
    2: [
      { user: '陈***', avatar: '👤', rating: 5, content: 'M3芯片速度飞快，屏幕显示效果一流，生产力工具！', date: '2026-07-19', images: [] },
      { user: '刘***', avatar: '👤', rating: 5, content: '深空黑色非常有质感，续航确实能撑一整天。', date: '2026-07-17', images: ['https://img.yzcdn.cn/vant/apple-2.jpg'] }
    ],
    3: [
      { user: '赵***', avatar: '👤', rating: 5, content: '降噪效果真的绝了，坐地铁完全听不到噪音。', date: '2026-07-20', images: [] },
      { user: '孙***', avatar: '👤', rating: 4, content: '音质很好，就是夏天戴有点热。', date: '2026-07-16', images: [] }
    ],
    4: [
      { user: '周***', avatar: '👤', rating: 5, content: '激光探测功能很实用，灰尘无所遁形！', date: '2026-07-18', images: [] },
      { user: '吴***', avatar: '👤', rating: 5, content: '吸力很强，猫毛吸得干干净净。', date: '2026-07-14', images: [] }
    ],
    5: [
      { user: '郑***', avatar: '👤', rating: 5, content: '经典纯白，百搭好看，穿着很舒服。', date: '2026-07-19', images: [] },
      { user: '钱***', avatar: '👤', rating: 4, content: '尺码标准，就是鞋底有点硬，穿穿就好了。', date: '2026-07-15', images: [] }
    ],
    6: [
      { user: '冯***', avatar: '👤', rating: 5, content: '用了一周皮肤明显变好了，会回购！', date: '2026-07-20', images: [] },
      { user: '褚***', avatar: '👤', rating: 5, content: '正品保证，和专柜买的一样。', date: '2026-07-17', images: [] }
    ]
  }
  reviews.value = reviewMap[numId] || []
}

const loadGoods = async (id) => {
  if (!id) return
  try {
    const res = await getGoodsDetail(id)
    if (res.code === 200 && res.data) {
      const data = res.data
      goods.value = {
        ...data,
        tags: ['自营', '次日达', '7天无理由'],
        coupons: data.price > 5000 ? ['满5000减200', '新用户立减100'] : ['新用户立减10']
      }
      images.value = data.image ? [data.image] : []
      currentImage.value = 0
      quantity.value = 1
      loadGoodsConfig(id)
    } else {
      alert('商品不存在或已下架')
      router.replace('/')
    }
  } catch (err) {
    console.error('加载商品失败:', err)
    alert('加载商品失败，请稍后重试')
  }
}

onMounted(() => {
  loadGoods(route.params.id)
})

watch(() => route.params.id, (newId, oldId) => {
  if (newId && newId !== oldId) {
    loadGoods(newId)
  }
})

const addToCart = async () => {
  for (const sku of skuList.value) {
    if (!selectedSku.value[sku.name]) {
      alert(`请选择${sku.name}`)
      return
    }
  }

  const spec = skuList.value.map(s => `${s.name}：${selectedSku.value[s.name]}`).join('；')

  const cartItem = {
    userId: 1,
    goodsId: goods.value.id,
    title: goods.value.title,
    spec: spec,
    price: goods.value.price,
    num: quantity.value,
    image: images.value[0] || goods.value.image,
    checked: 1
  }

  try {
    await addCartItem(cartItem)
    cartStore.addToCart({
      id: goods.value.id,
      title: goods.value.title,
      spec: spec,
      price: goods.value.price,
      num: quantity.value,
      image: images.value[0] || goods.value.image,
      shop: 'Apple官方旗舰店'
    })
    alert('已加入购物车')
  } catch (err) {
    alert('加入购物车失败，请检查网络或后端服务')
  }
}

const buyNow = () => {
  for (const sku of skuList.value) {
    if (!selectedSku.value[sku.name]) {
      alert(`请选择${sku.name}`)
      return
    }
  }

  const spec = skuList.value.map(s => `${s.name}：${selectedSku.value[s.name]}`).join('；')

  orderStore.setBuyNow({
    id: goods.value.id,
    title: goods.value.title,
    spec: spec,
    price: goods.value.price,
    num: quantity.value,
    image: images.value[0] || goods.value.image,
    checked: true
  })

  router.push('/order-confirm')
}
</script>
<template>
  <div class="goods-detail-page">
    <div class="container">
      <!-- 面包屑 -->
      <div class="breadcrumb">
        <a href="/">首页</a>
        <span>›</span>
        <a href="#">手机数码</a>
        <span>›</span>
        <a href="#">手机</a>
        <span>›</span>
        <span class="current">{{ goods.title }}</span>
      </div>

      <!-- 商品主区域 -->
      <div class="goods-main">
        <!-- 左侧图片 -->
        <div class="gallery">
          <div class="main-image">
            <img :src="images[currentImage]" />
          </div>
          <div class="thumb-list">
            <div
              v-for="(img, idx) in images"
              :key="idx"
              class="thumb-item"
              :class="{ active: currentImage === idx }"
              @click="currentImage = idx"
            >
              <img :src="img" />
            </div>
          </div>
        </div>

        <!-- 右侧信息 -->
        <div class="goods-info">
          <h1 class="goods-title">{{ goods.title }}</h1>
          <p class="goods-subtitle">{{ goods.subtitle }}</p>

          <div class="goods-tags">
            <span class="tag" v-for="tag in goods.tags" :key="tag">{{ tag }}</span>
          </div>

          <!-- 价格区 -->
          <div class="price-box">
            <div class="price-row">
              <span class="price-label">原价</span>
              <span class="price-old">¥{{ goods.oldPrice }}</span>
            </div>
            <div class="price-row">
              <span class="price-label">现价</span>
              <span class="price-now">¥{{ goods.price }}</span>
            </div>
            <div class="price-row">
              <span class="price-label">促销</span>
              <div class="coupons">
                <span class="coupon-tag" v-for="c in goods.coupons" :key="c">{{ c }}</span>
              </div>
            </div>
          </div>

          <!-- SKU 选择 -->
          <div class="sku-box">
            <div class="sku-row" v-for="sku in skuList" :key="sku.name">
              <span class="sku-label">{{ sku.name }}</span>
              <div class="sku-options">
                <span
                  v-for="opt in sku.options"
                  :key="opt"
                  class="sku-item"
                  :class="{ active: selectedSku[sku.name] === opt }"
                  @click="selectedSku[sku.name] = opt"
                >
                  {{ opt }}
                </span>
              </div>
            </div>

            <div class="sku-row">
              <span class="sku-label">数量</span>
              <div class="quantity-box">
                <button @click="quantity > 1 ? quantity-- : null">−</button>
                <input type="text" v-model.number="quantity" />
                <button @click="quantity++">+</button>
                <span class="stock">库存 {{ goods.stock }} 件</span>
              </div>
            </div>
          </div>

          <!-- 操作按钮 -->
          <div class="action-btns">
            <button class="btn-cart" @click="addToCart">加入购物车</button>
            <button class="btn-buy" @click="buyNow">立即购买</button>
          </div>

          <!-- 服务承诺 -->
          <div class="service-bar">
            <span>✅ 7天无理由退货</span>
            <span>✅ 正品保证</span>
            <span>✅ 极速退款</span>
            <span>✅ 免费包邮</span>
          </div>
        </div>
      </div>

      <!-- 底部标签内容 -->
      <div class="detail-tabs">
        <div class="tab-header">
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

        <div class="tab-content">
          <!-- 商品介绍 -->
          <div v-if="activeTab === 'detail'" class="detail-images">
           <img v-for="(img, idx) in detailImages" :key="idx" :src="img" />
          </div>

          <!-- 规格参数 -->
          <div v-if="activeTab === 'params'" class="params-table">
            <div class="param-row" v-for="p in params" :key="p.name">
              <div class="param-name">{{ p.name }}</div>
              <div class="param-value">{{ p.value }}</div>
            </div>
          </div>

          <!-- 累计评价 -->
          <div v-if="activeTab === 'reviews'" class="reviews-list">
            <div class="review-item" v-for="r in reviews" :key="r.date + r.user">
              <div class="review-user">
                <div class="review-avatar">{{ r.avatar }}</div>
                <div>
                  <div class="review-name">{{ r.user }}</div>
                  <div class="review-stars">⭐⭐⭐⭐⭐</div>
                </div>
                <span class="review-date">{{ r.date }}</span>
              </div>
              <p class="review-content">{{ r.content }}</p>
              <div class="review-images" v-if="r.images.length">
                <img v-for="img in r.images" :key="img" :src="img" />
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.goods-detail-page {
  background: #f4f4f4;
  min-height: 100vh;
  padding-bottom: 40px;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

/* 面包屑 */
.breadcrumb {
  padding: 16px 0;
  font-size: 13px;
  color: #666;
}
.breadcrumb a {
  color: #666;
}
.breadcrumb a:hover {
  color: #ff6b6b;
}
.breadcrumb span {
  margin: 0 8px;
  color: #ccc;
}
.breadcrumb .current {
  color: #999;
}

/* 主区域 */
.goods-main {
  display: flex;
  gap: 30px;
  background: #fff;
  border-radius: 8px;
  padding: 30px;
  margin-bottom: 20px;
}

/* 左侧图片 */
.gallery {
  width: 420px;
  flex-shrink: 0;
}
.main-image {
  width: 420px;
  height: 420px;
  border: 1px solid #eee;
  border-radius: 8px;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}
.main-image img {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
}
.thumb-list {
  display: flex;
  gap: 10px;
  margin-top: 12px;
}
.thumb-item {
  width: 72px;
  height: 72px;
  border: 2px solid #eee;
  border-radius: 4px;
  overflow: hidden;
  cursor: pointer;
  transition: border-color 0.2s;
}
.thumb-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.thumb-item.active,
.thumb-item:hover {
  border-color: #ff6b6b;
}

/* 右侧信息 */
.goods-info {
  flex: 1;
}
.goods-title {
  font-size: 20px;
  font-weight: bold;
  color: #333;
  line-height: 1.4;
  margin-bottom: 8px;
}
.goods-subtitle {
  font-size: 14px;
  color: #ff6b6b;
  margin-bottom: 12px;
}
.goods-tags {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
}
.tag {
  font-size: 12px;
  color: #ff6b6b;
  border: 1px solid #ff6b6b;
  padding: 2px 8px;
  border-radius: 4px;
}

/* 价格区 */
.price-box {
  background: #f9f9f9;
  padding: 16px 20px;
  border-radius: 8px;
  margin-bottom: 20px;
}
.price-row {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}
.price-row:last-child {
  margin-bottom: 0;
}
.price-label {
  width: 50px;
  font-size: 13px;
  color: #999;
}
.price-old {
  font-size: 14px;
  color: #999;
  text-decoration: line-through;
}
.price-now {
  font-size: 28px;
  color: #ff6b6b;
  font-weight: bold;
}
.coupons {
  display: flex;
  gap: 8px;
}
.coupon-tag {
  font-size: 12px;
  color: #fff;
  background: #ff6b6b;
  padding: 2px 8px;
  border-radius: 4px;
}

/* SKU */
.sku-box {
  margin-bottom: 24px;
}
.sku-row {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
}
.sku-label {
  width: 50px;
  font-size: 13px;
  color: #666;
  flex-shrink: 0;
}
.sku-options {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}
.sku-item {
  padding: 8px 16px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 13px;
  color: #333;
  cursor: pointer;
  transition: all 0.2s;
}
.sku-item:hover {
  border-color: #ff6b6b;
  color: #ff6b6b;
}
.sku-item.active {
  border-color: #ff6b6b;
  color: #ff6b6b;
  background: #fff5f5;
}

/* 数量 */
.quantity-box {
  display: flex;
  align-items: center;
  gap: 0;
}
.quantity-box button {
  width: 36px;
  height: 36px;
  border: 1px solid #ddd;
  background: #f5f5f5;
  font-size: 16px;
  cursor: pointer;
}
.quantity-box button:hover {
  background: #eee;
}
.quantity-box input {
  width: 50px;
  height: 36px;
  border: 1px solid #ddd;
  border-left: none;
  border-right: none;
  text-align: center;
  font-size: 14px;
  outline: none;
}
.stock {
  margin-left: 12px;
  font-size: 13px;
  color: #999;
}

/* 操作按钮 */
.action-btns {
  display: flex;
  gap: 16px;
  margin-bottom: 20px;
}
.btn-cart {
  flex: 1;
  height: 48px;
  border: 1px solid #ff6b6b;
  background: #fff;
  color: #ff6b6b;
  font-size: 16px;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;
}
.btn-cart:hover {
  background: #fff5f5;
}
.btn-buy {
  flex: 1;
  height: 48px;
  border: none;
  background: #ff6b6b;
  color: #fff;
  font-size: 16px;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;
}
.btn-buy:hover {
  background: #ff5252;
}

/* 服务 */
.service-bar {
  display: flex;
  gap: 24px;
  font-size: 13px;
  color: #666;
}

/* 底部标签 */
.detail-tabs {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
}
.tab-header {
  display: flex;
  border-bottom: 1px solid #eee;
}
.tab-item {
  padding: 16px 32px;
  font-size: 15px;
  color: #666;
  cursor: pointer;
  border-bottom: 2px solid transparent;
  transition: all 0.2s;
}
.tab-item:hover {
  color: #ff6b6b;
}
.tab-item.active {
  color: #ff6b6b;
  border-bottom-color: #ff6b6b;
  font-weight: bold;
}
.tab-content {
  padding: 30px;
  min-height: 300px;
}

/* 商品详情图 */
.detail-images {
  text-align: center;
}
.detail-images img {
  max-width: 100%;
  margin-bottom: 16px;
  border-radius: 4px;
}

/* 规格参数 */
.params-table {
  max-width: 800px;
}
.param-row {
  display: flex;
  border-bottom: 1px solid #f0f0f0;
  padding: 14px 0;
}
.param-name {
  width: 150px;
  font-size: 14px;
  color: #666;
  flex-shrink: 0;
}
.param-value {
  font-size: 14px;
  color: #333;
}

/* 评价 */
.reviews-list {
  max-width: 900px;
}
.review-item {
  border-bottom: 1px solid #f0f0f0;
  padding: 20px 0;
}
.review-user {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 10px;
}
.review-avatar {
  width: 40px;
  height: 40px;
  background: #f5f5f5;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
}
.review-name {
  font-size: 14px;
  color: #333;
}
.review-stars {
  color: #ffc107;
  font-size: 12px;
  margin-top: 2px;
}
.review-date {
  margin-left: auto;
  font-size: 13px;
  color: #999;
}
.review-content {
  font-size: 14px;
  color: #333;
  line-height: 1.6;
  padding-left: 52px;
}
.review-images {
  display: flex;
  gap: 8px;
  margin-top: 10px;
  padding-left: 52px;
}
.review-images img {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 4px;
}
</style>