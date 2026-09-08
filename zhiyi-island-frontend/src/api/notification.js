/**
 * 通知相关 API
 */
import api from '@/utils/request'

export const notificationApi = {
  /**
   * 获取通知列表
   * @param {number} limit - 返回数量限制
   * @returns {Promise} 通知列表
   */
  getList(limit = 10) {
    return api.get('/notification/list', { params: { limit } })
  },

  /**
   * 获取未读通知数量
   * @returns {Promise} 未读数量
   */
  getUnreadCount() {
    return api.get('/notification/unread-count')
  },

  /**
   * 标记通知已读
   * @param {number} id - 通知ID
   * @returns {Promise} 操作结果
   */
  markAsRead(id) {
    return api.put(`/notification/${id}/read`)
  },

  /**
   * 标记所有通知已读
   * @returns {Promise} 操作结果
   */
  markAllAsRead() {
    return api.put('/notification/read-all')
  },

  /**
   * 删除通知
   * @param {number} id - 通知ID
   * @returns {Promise} 删除结果
   */
  delete(id) {
    return api.delete(`/notification/${id}`)
  },
}
