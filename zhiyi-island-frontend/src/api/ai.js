/**
 * AI 相关 API
 * 包含摘要生成、关键词提取和知识图谱等接口
 */
import api from '@/utils/request'

const AI_TIMEOUT = 120000

export const aiApi = {
  /**
   * 生成AI摘要
   * @param {Object} data - 摘要请求数据
   * @param {string} data.title - 标题
   * @param {string} data.content - 原始内容
   * @param {string} data.style - 摘要风格：keypoints/paragraph
   * @param {string} data.length - 摘要长度：short/medium/long
   * @returns {Promise} 生成的摘要
   */
  generateSummary(data) {
    return api.post('/ai/summary', data, { timeout: AI_TIMEOUT })
  },

  /**
   * 获取AI摘要历史
   * @param {number} limit - 返回数量限制
   * @returns {Promise} 摘要历史列表
   */
  getSummaryHistory(limit = 10) {
    return api.get('/ai/summary/history', { params: { limit } })
  },

  /**
   * 删除AI摘要
   * @param {number} id - 摘要ID
   * @returns {Promise} 删除结果
   */
  deleteSummary(id) {
    return api.delete(`/ai/summary/${id}`)
  },

  /**
   * 保存AI摘要到笔记
   * @param {number} id - 摘要ID
   * @returns {Promise} 保存结果
   */
  saveToNote(id) {
    return api.post(`/ai/summary/${id}/save-to-note`)
  },

  /**
   * 提取关键词
   * @param {string} content - 待提取的内容
   * @returns {Promise} 提取的关键词
   */
  extractKeywords(content) {
    return api.post('/ai/keywords', content, {
      headers: { 'Content-Type': 'text/plain' },
      timeout: AI_TIMEOUT,
    })
  },

  /**
   * 生成知识图谱
   * @returns {Promise} 知识图谱数据（JSON格式）
   */
  generateKnowledgeGraph() {
    return api.get('/ai/knowledge-graph', { timeout: AI_TIMEOUT })
  },

  /**
   * AI问答对话
   * @param {Object} data - 问答请求数据
   * @param {string} data.question - 用户问题
   * @param {string} data.context - 上下文内容
   * @param {Array} data.history - 对话历史
   * @returns {Promise} AI回答
   */
  aiChat(data) {
    return api.post('/ai/chat', data, { timeout: AI_TIMEOUT })
  },
}
