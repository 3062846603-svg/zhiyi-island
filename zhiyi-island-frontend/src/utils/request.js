/**
 * HTTP 请求配置
 * 配置 Axios 实例，请求/响应拦截器
 */
import axios from 'axios'

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'

const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
})

/** 是否正在重定向标志，防止多次跳转 */
let isRedirecting = false

/**
 * 请求拦截器
 * 自动在请求头中添加 Token
 */
api.interceptors.request.use(
  (config) => {
    const authState = sessionStorage.getItem('auth_state')
    if (authState) {
      const { token } = JSON.parse(authState)
      if (token) {
        config.headers.Authorization = `Bearer ${token}`
      }
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  },
)

/**
 * 响应拦截器
 * 处理响应数据和错误状态码
 */
api.interceptors.response.use(
  (response) => {
    return response.data
  },
  (error) => {
    if (error.response) {
      const { status, data } = error.response

      if (status === 401 || status === 403) {
        sessionStorage.removeItem('auth_state')
        if (!isRedirecting && window.location.pathname !== '/login') {
          isRedirecting = true
          sessionStorage.setItem('token_expired', 'true')
          window.location.href = '/login'
        }
      }

      return Promise.reject(data || { message: '请求失败' })
    }

    if (
      error.code === 'ERR_NETWORK' ||
      error.code === 'ECONNABORTED' ||
      error.message?.includes('Network Error')
    ) {
      return Promise.reject({ message: '服务暂不可用，请稍后重试' })
    }

    if (error.code === 'ECONNABORTED' || error.message?.includes('timeout')) {
      return Promise.reject({ message: '请求超时，请稍后重试' })
    }

    return Promise.reject({ message: '网络连接异常，请检查网络' })
  },
)

/**
 * 重置重定向标志
 * 用于登录成功后重置状态
 */
export function resetRedirectFlag() {
  isRedirecting = false
}

/**
 * 清除认证状态
 * 从 sessionStorage 中移除用户认证信息
 */
export function clearAuthState() {
  sessionStorage.removeItem('auth_state')
}

export default api
