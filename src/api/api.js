import request from '@/utils/request'

// ========== 商品 ==========
export const getGoodsList = (params) => request.get('/goods/list', { params })
export const getGoodsDetail = (id) => request.get(`/goods/detail/${id}`)

// ========== 用户 ==========
export const login = (data) => request.post('/user/login', data)
export const register = (data) => request.post('/user/register', data)
export const getUserInfo = (id) => request.get('/user/info', { params: { id } })

// ========== 购物车 ==========
export const getCartList = (userId) => request.get('/cart/list', { params: { userId } })
export const addCartItem = (data) => request.post('/cart/add', data)
export const updateCartItem = (data) => request.put('/cart/update', data)
export const deleteCartItem = (id) => request.delete('/cart/delete', { params: { id } })

// ========== 订单 ==========
export const getOrderList = (userId) => request.get('/order/list', { params: { userId } })
export const createOrder = (data) => request.post('/order/create', data)
export const getOrderDetail = (orderNo) => request.get(`/order/detail/${orderNo}`)

export const searchGoods = (keyword) => request.get('/goods/search', { params: { keyword } })