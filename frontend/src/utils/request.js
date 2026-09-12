import axios from 'axios'
import { ElMessage } from 'element-plus'

// 创建 axios 实例，基础路径 /api（由 Vite 代理转发到后端 8080）
const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

// 响应拦截器：统一处理后端返回的 {code, message, data} 格式
request.interceptors.response.use(
  response => {
    const res = response.data
    // 后端约定 code=200 为成功，直接返回 data
    if (res.code === 200) {
      return res.data
    } else {
      // 业务失败，弹出错误提示
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message))
    }
  },
  error => {
    ElMessage.error('网络错误：' + error.message)
    return Promise.reject(error)
  }
)

export default request
