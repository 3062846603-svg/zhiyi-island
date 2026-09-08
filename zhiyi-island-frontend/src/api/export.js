/**
 * 数据导出相关 API
 * 包含数据导出、导出历史记录管理等接口
 */
import api from '@/utils/request'

export const exportApi = {
  /**
   * 导出数据
   * @param {Object} data - 导出配置
   * @param {boolean} data.notes - 是否导出笔记
   * @param {boolean} data.knowledge - 是否导出知识库
   * @param {boolean} data.aiSummaries - 是否导出AI摘要
   * @param {boolean} data.settings - 是否导出设置
   * @param {string} data.format - 导出格式：json/markdown
   * @returns {Promise} 导出的文件数据（Blob）
   */
  exportData(data) {
    return api.post('/export', data, {
      responseType: 'blob',
    })
  },

  /**
   * 获取导出历史记录
   * @returns {Promise} 导出历史列表
   */
  getHistory() {
    return api.get('/export/history')
  },

  /**
   * 下载导出文件
   * @param {Object} data - 导出配置
   * @param {string} fileName - 下载文件名
   * @returns {Promise} 下载完成
   */
  downloadExport(data, fileName) {
    return this.exportData(data).then((response) => {
      const blob = new Blob([response], { type: 'application/octet-stream' })
      const url = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = fileName
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      window.URL.revokeObjectURL(url)
    })
  },
}
