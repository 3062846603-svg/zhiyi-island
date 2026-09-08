/**
 * Toast消息状态管理
 * 封装Element Plus消息提示组件，提供统一的提示接口
 */
import { defineStore } from 'pinia'
import { ElMessage } from 'element-plus'

export const useToastStore = defineStore('toast', () => {
  /**
   * 显示成功消息
   * @param {string} message - 消息内容
   * @param {number} duration - 显示时长（毫秒）
   */
  function success(message, duration = 3000) {
    ElMessage({
      message,
      type: 'success',
      duration,
    })
  }

  /**
   * 显示错误消息
   * @param {string} message - 消息内容
   * @param {number} duration - 显示时长（毫秒）
   */
  function error(message, duration = 3000) {
    ElMessage({
      message,
      type: 'error',
      duration,
    })
  }

  /**
   * 显示警告消息
   * @param {string} message - 消息内容
   * @param {number} duration - 显示时长（毫秒）
   */
  function warning(message, duration = 3000) {
    ElMessage({
      message,
      type: 'warning',
      duration,
    })
  }

  /**
   * 显示信息消息
   * @param {string} message - 消息内容
   * @param {number} duration - 显示时长（毫秒）
   */
  function info(message, duration = 3000) {
    ElMessage({
      message,
      type: 'info',
      duration,
    })
  }

  return {
    success,
    error,
    warning,
    info,
  }
})
