import axios from 'axios'

const request = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL || '/api',  // ← 改这里：/api
    timeout: 10000  // ← 改成 10 秒
})

request.interceptors.response.use(
    response => response.data,
    error => {
        console.error('请求错误:', error)
        return Promise.reject(error)
    }
)

export default request