/**
 * 用户相关 API
 * 包含用户认证、信息管理和账户操作等接口
 */
import api from '@/utils/request'

/**
 * 认证相关 API
 */
export const authApi = {
  /**
   * 用户登录
   * @param {Object} data - 登录数据
   * @param {string} data.username - 用户名或邮箱
   * @param {string} data.password - 密码
   * @returns {Promise} 登录结果，包含 token 和用户信息
   */
  login(data) {
    return api.post('/user/login', data)
  },

  /**
   * 验证码登录
   * @param {Object} data - 登录数据
   * @param {string} data.email - 邮箱
   * @param {string} data.code - 验证码
   * @returns {Promise} 登录结果，包含 token 和用户信息
   */
  loginWithCode(data) {
    return api.post('/user/login/code', data)
  },

  /**
   * 用户注册
   * @param {Object} data - 注册数据
   * @param {string} data.username - 用户名
   * @param {string} data.password - 密码
   * @param {string} data.email - 邮箱（可选）
   * @returns {Promise} 注册结果
   */
  register(data) {
    return api.post('/user/register', data)
  },

  /**
   * 用户登出
   * @returns {Promise} 登出结果
   */
  logout() {
    return api.post('/user/logout')
  },

  /**
   * 获取当前用户信息
   * @returns {Promise} 用户信息
   */
  getUserInfo() {
    return api.get('/user/info')
  },

  /**
   * 重置密码（忘记密码）
   * @param {Object} data - 重置密码数据
   * @param {string} data.email - 邮箱
   * @param {string} data.code - 验证码
   * @param {string} data.newPassword - 新密码
   * @returns {Promise} 重置结果
   */
  resetPassword(data) {
    return api.post('/user/password/reset', data)
  },
}

/**
 * 用户操作相关 API
 */
export const userApi = {
  /**
   * 检查用户名是否已存在
   * 用于注册时实时验证用户名可用性
   * @param {string} username - 用户名
   * @returns {Promise<boolean>} 用户名是否已存在
   */
  checkUsername(username) {
    return api.get('/user/check-username', { params: { username } })
  },

  /**
   * 修改密码
   * 需要提供旧密码进行验证
   * @param {Object} data - 修改密码数据
   * @param {string} data.oldPassword - 旧密码
   * @param {string} data.newPassword - 新密码
   * @returns {Promise} 修改结果
   */
  updatePassword(data) {
    return api.put('/user/password', data)
  },

  /**
   * 通过验证码修改密码
   * 用于忘记密码场景，需要邮箱验证码验证身份
   * @param {Object} data - 修改密码数据
   * @param {string} data.email - 邮箱
   * @param {string} data.code - 验证码
   * @param {string} data.newPassword - 新密码
   * @returns {Promise} 修改结果
   */
  updatePasswordByCode(data) {
    return api.put('/user/password/by-code', data)
  },

  /**
   * 验证密码
   * 用于敏感操作前的身份验证
   * @param {Object} data - 验证数据
   * @param {string} data.password - 当前密码
   * @returns {Promise} 验证结果
   */
  verifyPassword(data) {
    return api.post('/user/password/verify', data)
  },

  /**
   * 发送邮箱验证码
   * @param {Object} data - 发送数据
   * @param {string} data.email - 邮箱地址
   * @returns {Promise} 发送结果
   */
  sendCode(data) {
    return api.post('/user/email/code', data)
  },

  /**
   * 验证邮箱验证码
   * 用于换绑邮箱等需要验证邮箱的场景
   * @param {Object} data - 验证数据
   * @param {string} data.email - 邮箱地址
   * @param {string} data.code - 验证码
   * @returns {Promise} 验证结果
   */
  verifyEmailCode(data) {
    return api.post('/user/email/verify', data)
  },

  /**
   * 更新用户资料
   * @param {Object} data - 用户资料
   * @param {string} data.nickname - 昵称（可选）
   * @param {string} data.avatar - 头像URL（可选）
   * @param {string} data.bio - 个人简介（可选）
   * @returns {Promise} 更新结果
   */
  updateProfile(data) {
    return api.put('/user/info', data)
  },

  /**
   * 更新邮箱
   * @param {Object} data - 邮箱数据
   * @param {string} data.email - 新邮箱
   * @param {string} data.code - 验证码
   * @returns {Promise} 更新结果
   */
  updateEmail(data) {
    return api.put('/user/email', data)
  },

  /**
   * 发送邮箱验证码
   * @param {string} email - 邮箱地址
   * @returns {Promise} 发送结果
   */
  sendEmailCode(email) {
    return api.post('/user/email/code', { email })
  },

  /**
   * 上传头像
   * @param {File} file - 头像文件
   * @returns {Promise} 上传结果，包含头像 URL
   */
  uploadAvatar(file) {
    const formData = new FormData()
    formData.append('file', file)
    return api.post('/user/avatar', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    })
  },

  /**
   * 删除账户
   * 危险操作，删除后数据不可恢复
   * @returns {Promise} 删除结果
   */
  deleteAccount() {
    return api.delete('/user/account')
  },
}
