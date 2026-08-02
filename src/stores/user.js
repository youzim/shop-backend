import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || 'null'))

  const isLogin = computed(() => !!token.value)

  const login = (data) => {
    token.value = 'fake-token-' + Date.now()
    // 过滤掉敏感字段再存
    const safeData = { ...data }
    delete safeData.password
    userInfo.value = safeData
    localStorage.setItem('token', token.value)
    localStorage.setItem('userInfo', JSON.stringify(safeData))
  }

  const logout = () => {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }

  const updateAvatar = (avatarUrl) => {
    if (userInfo.value) {
      userInfo.value.avatar = avatarUrl
      localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
    }
  }

  return { token, userInfo, isLogin, login, logout, updateAvatar }
})