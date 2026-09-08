/**
 * 用户认证状态管理
 * 管理用户登录状态、Token和用户信息
 */
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi } from '@/api/user'

export const useAuthStore = defineStore('auth', () => {
  /** 用户Token */
  const token = ref('')
  /** 用户信息 */
  const userInfo = ref(null)

  /** 是否已登录 */
  const isLoggedIn = computed(() => !!token.value)

  /**
   * 设置Token
   * @param {string} newToken - 新Token
   */
  function setToken(newToken) {
    token.value = newToken
  }

  /**
   * 设置用户信息
   * @param {Object} info - 用户信息对象
   */
  function setUserInfo(info) {
    userInfo.value = info
  }

  /**
   * 用户登录
   * @param {Object} credentials - 登录凭证（用户名和密码）
   * @returns {Promise} 登录结果
   */
  async function login(credentials) {
    const response = await authApi.login(credentials)

    if (response.code === 200) {
      setToken(response.data.token)
      setUserInfo({
        id: response.data.userId,
        username: response.data.username,
        nickname: response.data.nickname,
        avatar: response.data.avatar,
        email: response.data.email,
      })
    }

    return response
  }

  /**
   * 验证码登录
   * @param {Object} credentials - 登录凭证（邮箱和验证码）
   * @returns {Promise} 登录结果
   */
  async function loginWithCode(credentials) {
    const response = await authApi.loginWithCode(credentials)

    if (response.code === 200) {
      setToken(response.data.token)
      setUserInfo({
        id: response.data.userId,
        username: response.data.username,
        nickname: response.data.nickname,
        avatar: response.data.avatar,
        email: response.data.email,
      })
    }

    return response
  }

  /**
   * 用户注册
   * @param {Object} userData - 注册数据
   * @returns {Promise} 注册结果
   */
  async function register(userData) {
    const response = await authApi.register(userData)
    return response
  }

  /**
   * 用户登出
   * 清除本地存储的Token和用户信息
   */
  async function logout() {
    try {
      await authApi.logout()
    } catch (error) {
      console.error('登出请求失败:', error)
    } finally {
      setToken('')
      setUserInfo(null)
    }
  }

  /**
   * 获取用户信息
   * 从服务器获取最新的用户信息
   * @returns {Promise} 用户信息
   */
  async function fetchUserInfo() {
    if (!token.value) return null

    try {
      const response = await authApi.getUserInfo()
      if (response.code === 200) {
        setUserInfo(response.data)
        return response.data
      }
    } catch (error) {
      console.error('获取用户信息失败:', error)
    }

    return null
  }

  /**
   * 清除认证信息
   * 用于Token过期或强制登出时清除本地状态
   */
  function clearAuth() {
    setToken('')
    setUserInfo(null)
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    setToken,
    setUserInfo,
    login,
    loginWithCode,
    register,
    logout,
    fetchUserInfo,
    clearAuth,
  }
})
