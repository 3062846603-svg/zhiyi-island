/**
 * 笔记相关 API
 * 包含笔记的增删改查、草稿管理和AI摘要等接口
 */
import api from '@/utils/request'

export const noteApi = {
  /**
   * 创建笔记
   * @param {Object} data - 笔记数据
   * @param {string} data.title - 标题
   * @param {string} data.content - 内容
   * @param {string} data.category - 分类（可选）
   * @param {number} data.knowledgeId - 关联知识库ID（可选）
   * @returns {Promise} 创建结果
   */
  create(data) {
    return api.post('/note', data)
  },

  /**
   * 更新笔记
   * @param {number} id - 笔记ID
   * @param {Object} data - 笔记数据
   * @returns {Promise} 更新结果
   */
  update(id, data) {
    return api.put(`/note/${id}`, data)
  },

  /**
   * 删除笔记
   * @param {number} id - 笔记ID
   * @returns {Promise} 删除结果
   */
  delete(id) {
    return api.delete(`/note/${id}`)
  },

  /**
   * 根据ID获取笔记详情
   * @param {number} id - 笔记ID
   * @returns {Promise} 笔记详情
   */
  getById(id) {
    return api.get(`/note/${id}`)
  },

  /**
   * 获取笔记列表
   * @param {string} category - 分类（可选）
   * @returns {Promise} 笔记列表
   */
  list(category) {
    const params = category ? { category } : {}
    return api.get('/note/list', { params })
  },

  /**
   * 获取知识库下的所有笔记
   * @param {number} knowledgeId - 知识库ID
   * @returns {Promise} 笔记列表
   */
  getByKnowledgeId(knowledgeId) {
    return api.get(`/note/knowledge/${knowledgeId}`)
  },

  /**
   * 搜索笔记
   * @param {string} keyword - 搜索关键词
   * @returns {Promise} 搜索结果列表
   */
  search(keyword) {
    return api.get('/note/search', { params: { keyword } })
  },

  /**
   * 更新笔记的知识库关联
   * @param {number} id - 笔记ID
   * @param {number} knowledgeId - 知识库ID（null表示取消关联）
   * @returns {Promise} 更新结果
   */
  updateKnowledgeId(id, knowledgeId) {
    return api.put(`/note/${id}/knowledge`, { knowledgeId })
  },

  /**
   * 获取用户笔记数量
   * @returns {Promise} 笔记数量
   */
  count() {
    return api.get('/note/count')
  },

  /**
   * 生成AI摘要
   * @param {number} id - 笔记ID
   * @param {Object} options - 摘要选项
   * @param {string} options.style - 摘要风格：keypoints/paragraph/outline
   * @param {string} options.length - 摘要长度：short/medium/long
   * @returns {Promise} 生成的摘要
   */
  generateSummary(id, options = {}) {
    return api.post(`/note/${id}/summary`, options, { timeout: 120000 })
  },

  /**
   * 获取草稿列表
   * @returns {Promise} 草稿列表
   */
  getDrafts() {
    return api.get('/note/drafts')
  },

  /**
   * 获取草稿数量
   * @returns {Promise} 草稿数量
   */
  countDrafts() {
    return api.get('/note/drafts/count')
  },

  /**
   * 发布草稿
   * @param {number} id - 草稿ID
   * @returns {Promise} 发布结果
   */
  publishDraft(id) {
    return api.put(`/note/${id}/publish`)
  },

  /**
   * 获取笔记分类列表
   * @returns {Promise} 分类列表
   */
  getCategories() {
    return api.get('/note/categories')
  },
}
