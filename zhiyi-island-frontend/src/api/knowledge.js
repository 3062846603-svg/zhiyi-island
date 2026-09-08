/**
 * 知识库相关 API
 * 包含知识库和知识条目的增删改查等接口
 */
import api from '@/utils/request'

export const knowledgeApi = {
  /**
   * 创建知识库
   * @param {Object} data - 知识库数据
   * @param {string} data.title - 标题
   * @param {string} data.description - 描述（可选）
   * @param {string} data.category - 分类（可选）
   * @returns {Promise} 创建结果
   */
  create(data) {
    return api.post('/knowledge', data)
  },

  /**
   * 更新知识库
   * @param {number} id - 知识库ID
   * @param {Object} data - 知识库数据
   * @returns {Promise} 更新结果
   */
  update(id, data) {
    return api.put(`/knowledge/${id}`, data)
  },

  /**
   * 删除知识库
   * @param {number} id - 知识库ID
   * @returns {Promise} 删除结果
   */
  delete(id) {
    return api.delete(`/knowledge/${id}`)
  },

  /**
   * 根据ID获取知识库详情
   * @param {number} id - 知识库ID
   * @returns {Promise} 知识库详情（包含知识条目）
   */
  getById(id) {
    return api.get(`/knowledge/${id}`)
  },

  /**
   * 获取知识库列表
   * @param {string} category - 分类（可选）
   * @returns {Promise} 知识库列表
   */
  list(category) {
    const params = category ? { category } : {}
    return api.get('/knowledge/list', { params })
  },

  /**
   * 获取用户知识库数量
   * @returns {Promise} 知识库数量
   */
  count() {
    return api.get('/knowledge/count')
  },

  /**
   * 获取用户知识条目数量
   * @returns {Promise} 知识条目数量
   */
  itemCount() {
    return api.get('/knowledge/item-count')
  },

  /**
   * 创建知识条目
   * @param {number} knowledgeId - 知识库ID
   * @param {Object} data - 知识条目数据
   * @param {string} data.title - 标题
   * @param {string} data.content - 内容
   * @param {string} data.source - 来源（可选）
   * @param {number} data.noteId - 来源笔记ID（可选）
   * @returns {Promise} 创建结果
   */
  createItem(knowledgeId, data) {
    return api.post(`/knowledge/${knowledgeId}/item`, data)
  },

  /**
   * 更新知识条目
   * @param {number} itemId - 知识条目ID
   * @param {Object} data - 知识条目数据
   * @returns {Promise} 更新结果
   */
  updateItem(itemId, data) {
    return api.put(`/knowledge/item/${itemId}`, data)
  },

  /**
   * 删除知识条目
   * @param {number} itemId - 知识条目ID
   * @returns {Promise} 删除结果
   */
  deleteItem(itemId) {
    return api.delete(`/knowledge/item/${itemId}`)
  },

  /**
   * 获取知识库下的所有知识条目
   * @param {number} knowledgeId - 知识库ID
   * @returns {Promise} 知识条目列表
   */
  getItems(knowledgeId) {
    return api.get(`/knowledge/${knowledgeId}/items`)
  },

  /**
   * 获取来源于某笔记的所有知识条目
   * @param {number} noteId - 笔记ID
   * @returns {Promise} 知识条目列表
   */
  getItemsByNoteId(noteId) {
    return api.get(`/knowledge/note/${noteId}/items`)
  },
}
