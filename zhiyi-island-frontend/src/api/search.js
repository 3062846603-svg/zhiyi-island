/**
 * 搜索相关 API
 * 包含普通搜索、AI搜索、智能搜索、语义问答和搜索历史等接口
 */
import api from '@/utils/request'

export const searchApi = {
  /**
   * 普通搜索
   * 根据关键词搜索笔记和知识库
   * @param {string} query - 搜索关键词
   * @param {string} type - 搜索类型：all/note/knowledge
   * @returns {Promise} 搜索结果列表
   */
  search(query, type = 'all') {
    return api.get('/search', { params: { query, type } })
  },

  /**
   * AI搜索
   * 使用AI扩展搜索词，提供更智能的搜索结果
   * @param {string} query - 搜索关键词
   * @returns {Promise} AI搜索响应
   */
  aiSearch(query) {
    return api.post('/search/ai', { query }, { timeout: 120000 })
  },

  /**
   * 智能搜索
   * 结合AI语义理解，提供更精准的搜索结果
   * @param {string} query - 搜索关键词
   * @returns {Promise} 智能搜索响应
   */
  intelligentSearch(query) {
    return api.post('/search/intelligent', { query }, { timeout: 120000 })
  },

  /**
   * 语义问答
   * 基于用户笔记和知识库进行语义理解和问答
   * @param {string} question - 用户问题
   * @param {string} context - 上下文内容（可选）
   * @param {string} searchType - 搜索类型：all/note/knowledge
   * @param {boolean} returnRelatedContent - 是否返回相关内容
   * @returns {Promise} 语义问答响应
   */
  semanticChat(question, context = '', searchType = 'all', returnRelatedContent = true) {
    return api.post('/search/semantic-chat', {
      question,
      context,
      searchType,
      returnRelatedContent
    }, { timeout: 120000 })
  },

  /**
   * 获取搜索历史
   * @param {number} limit - 返回数量限制
   * @returns {Promise} 搜索历史列表
   */
  getHistory(limit = 10) {
    return api.get('/search/history', { params: { limit } })
  },

  /**
   * 删除单条搜索历史
   * @param {number} id - 搜索历史ID
   * @returns {Promise} 删除结果
   */
  deleteHistory(id) {
    return api.delete(`/search/history/${id}`)
  },

  /**
   * 清空搜索历史
   * @returns {Promise} 清空结果
   */
  clearHistory() {
    return api.delete('/search/history')
  },
}
