/**
 * 通知状态管理
 * 管理系统通知的未读数量、通知列表和相关操作
 */
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { notificationApi } from '@/api/notification'

export const useNotificationStore = defineStore('notification', () => {
  /** 未读通知数量 */
  const unreadCount = ref(0)
  /** 通知列表 */
  const notifications = ref([])
  /** 加载状态 */
  const loading = ref(false)

  /**
   * 获取未读通知数量
   * @returns {Promise<void>}
   */
  const fetchUnreadCount = async () => {
    try {
      const result = await notificationApi.getUnreadCount()
      if (result.code === 200) {
        unreadCount.value = result.data.count
      }
    } catch (e) {
      console.error('获取未读数量失败:', e)
    }
  }

  /**
   * 获取通知列表
   * @param {number} limit - 返回数量限制
   * @returns {Promise<void>}
   */
  const fetchNotifications = async (limit = 100) => {
    loading.value = true
    try {
      const result = await notificationApi.getList(limit)
      if (result.code === 200) {
        notifications.value = result.data.map((item) => ({
          id: item.id,
          title: item.title,
          content: item.content,
          type: item.type,
          createTime: item.createTime,
          read: item.read === true,
        }))
        unreadCount.value = notifications.value.filter((n) => !n.read).length
      }
    } catch (e) {
      console.error('获取通知失败:', e)
    } finally {
      loading.value = false
    }
  }

  /**
   * 标记单条通知为已读
   * @param {number} id - 通知ID
   * @returns {Promise<Object>} 操作结果
   */
  const markAsRead = async (id) => {
    try {
      const result = await notificationApi.markAsRead(id)
      if (result.code === 200) {
        const notification = notifications.value.find((n) => n.id === id)
        if (notification) {
          if (!notification.read) {
            notification.read = true
            unreadCount.value = Math.max(0, unreadCount.value - 1)
          }
        } else {
          await fetchUnreadCount()
        }
      }
      return result
    } catch (e) {
      console.error('标记已读失败:', e)
      throw e
    }
  }

  /**
   * 标记所有通知为已读
   * @returns {Promise<Object>} 操作结果
   */
  const markAllAsRead = async () => {
    try {
      const result = await notificationApi.markAllAsRead()
      if (result.code === 200) {
        notifications.value.forEach((n) => (n.read = true))
        unreadCount.value = 0
      }
      return result
    } catch (e) {
      console.error('全部标记已读失败:', e)
      throw e
    }
  }

  /**
   * 删除单条通知
   * @param {number} id - 通知ID
   * @returns {Promise<Object>} 操作结果
   */
  const deleteNotification = async (id) => {
    try {
      const result = await notificationApi.delete(id)
      if (result.code === 200) {
        const notification = notifications.value.find((n) => n.id === id)
        if (notification) {
          if (!notification.read) {
            unreadCount.value = Math.max(0, unreadCount.value - 1)
          }
          notifications.value = notifications.value.filter((n) => n.id !== id)
        } else {
          await fetchUnreadCount()
        }
      }
      return result
    } catch (e) {
      console.error('删除通知失败:', e)
      throw e
    }
  }

  return {
    unreadCount,
    notifications,
    loading,
    fetchUnreadCount,
    fetchNotifications,
    markAsRead,
    markAllAsRead,
    deleteNotification,
  }
})
