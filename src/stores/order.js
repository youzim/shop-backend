import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useOrderStore = defineStore('order', () => {
  const buyNowGoods = ref(null)

  const setBuyNow = (goods) => {
    buyNowGoods.value = goods
  }

  const clearBuyNow = () => {
    buyNowGoods.value = null
  }

  return { buyNowGoods, setBuyNow, clearBuyNow }
})