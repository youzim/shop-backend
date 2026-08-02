import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { deleteCartItem, updateCartItem } from '../api/api'

export const useCartStore = defineStore('cart', () => {
  const list = ref([])

  const addToCart = (goods) => {
    const index = list.value.findIndex(
      item => item.id === goods.id && item.spec === goods.spec
    )
    if (index > -1) {
      list.value[index].num += goods.num
    } else {
      list.value.push({ ...goods, checked: true })
    }
  }

  const remove = async (cartId) => {
    if (cartId) {
      await deleteCartItem(cartId)
    }
    list.value = list.value.filter(item => item.cartId !== cartId)
  }

  const removeBatch = async () => {
    const checkedItems = list.value.filter(item => item.checked)
    for (const item of checkedItems) {
      if (item.cartId) {
        await deleteCartItem(item.cartId)
      }
    }
    list.value = list.value.filter(item => !item.checked)
  }

  const changeNum = async (cartId, delta) => {
    const item = list.value.find(i => i.cartId === cartId)
    if (item) {
      const newNum = item.num + delta
      if (newNum >= 1 && newNum <= 99) {
        item.num = newNum
        if (cartId) {
          await updateCartItem({
            id: cartId,
            userId: item.userId || 1,
            goodsId: item.id,
            title: item.title,
            spec: item.spec,
            price: item.price,
            num: newNum,
            image: item.image,
            checked: item.checked ? 1 : 0
          })
        }
      }
    }
  }

  const isAllChecked = computed({
    get: () => list.value.length > 0 && list.value.every(item => item.checked),
    set: (val) => list.value.forEach(item => (item.checked = val))
  })

  const selectedCount = computed(() =>
    list.value.filter(i => i.checked).reduce((sum, i) => sum + i.num, 0)
  )

  const totalPrice = computed(() =>
    list.value.filter(i => i.checked).reduce((sum, i) => sum + i.price * i.num, 0)
  )

  const totalNum = computed(() => list.value.reduce((sum, i) => sum + i.num, 0))

  return {
    list,
    addToCart,
    remove,
    removeBatch,
    changeNum,
    isAllChecked,
    selectedCount,
    totalPrice,
    totalNum
  }
})