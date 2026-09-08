/**
 * 文件导入相关 API
 * 包含文件导入、导入历史记录管理等接口
 */
import api from '@/utils/request'

export const importApi = {
  /**
   * 导入文件
   * AI自动解析内容、智能分段、生成标题和分类、创建知识库
   *
   * @param {File} file - 要导入的文件（支持 PDF/DOCX/TXT/MD/EPUB 格式）
   * @param {Object} options - 导入选项
   * @param {boolean} options.aiSummary - 是否生成AI摘要（可选）
   * @returns {Promise} 导入结果
   */
  importFile(file, options = {}) {
    const formData = new FormData()
    formData.append('file', file)

    if (options.aiSummary !== undefined) {
      formData.append('aiSummary', options.aiSummary)
    }

    return api.post('/import', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
      timeout: 600000,
    })
  },

  /**
   * 获取导入历史记录
   * @returns {Promise} 导入历史列表
   */
  getHistory() {
    return api.get('/import/history')
  },

  /**
   * 删除导入记录
   * @param {number} id - 导入记录ID
   * @returns {Promise} 删除结果
   */
  deleteRecord(id) {
    return api.delete(`/import/${id}`)
  },
}
