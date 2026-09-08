/**
 * 知识库状态管理
 * 管理知识库列表、知识条目和相关操作
 */
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { knowledgeApi } from '@/api/knowledge'

export const useKnowledgeStore = defineStore('knowledge', () => {
  /** 知识库列表 */
  const knowledgeList = ref([])
  /** 当前知识库 */
  const currentKnowledge = ref(null)
  /** 当前知识条目列表 */
  const currentItems = ref([])
  /** 加载状态 */
  const loading = ref(false)
  /** 错误信息 */
  const error = ref(null)

  /**
   * 获取知识库列表
   * @param {string} category - 分类（可选）
   * @returns {Promise<void>}
   */
  async function fetchKnowledgeList(category) {
    loading.value = true
    error.value = null
    try {
      const response = await knowledgeApi.list(category)
      if (response.code === 200) {
        knowledgeList.value = response.data
      }
    } catch (e) {
      error.value = e.message || '获取知识库列表失败'
    } finally {
      loading.value = false
    }
  }

  /**
   * 获取知识库详情
   * @param {number} id - 知识库ID
   * @returns {Promise<Object|null>} 知识库详情
   */
  async function fetchKnowledge(id) {
    loading.value = true
    error.value = null
    try {
      const response = await knowledgeApi.getById(id)
      if (response.code === 200) {
        currentKnowledge.value = response.data
        return response.data
      }
    } catch (e) {
      error.value = e.message || '获取知识库详情失败'
    } finally {
      loading.value = false
    }
    return null
  }

  /**
   * 创建知识库
   * @param {Object} data - 知识库数据
   * @returns {Promise<Object|null>} 创建的知识库
   */
  async function createKnowledge(data) {
    loading.value = true
    error.value = null
    try {
      const response = await knowledgeApi.create(data)
      if (response.code === 200) {
        knowledgeList.value.unshift(response.data)
        return response.data
      }
    } catch (e) {
      error.value = e.message || '创建知识库失败'
    } finally {
      loading.value = false
    }
    return null
  }

  /**
   * 更新知识库
   * @param {number} id - 知识库ID
   * @param {Object} data - 知识库数据
   * @returns {Promise<Object|null>} 更新后的知识库
   */
  async function updateKnowledge(id, data) {
    loading.value = true
    error.value = null
    try {
      const response = await knowledgeApi.update(id, data)
      if (response.code === 200) {
        const index = knowledgeList.value.findIndex((k) => k.id === id)
        if (index !== -1) {
          knowledgeList.value[index] = response.data
        }
        currentKnowledge.value = response.data
        return response.data
      }
    } catch (e) {
      error.value = e.message || '更新知识库失败'
    } finally {
      loading.value = false
    }
    return null
  }

  /**
   * 删除知识库
   * @param {number} id - 知识库ID
   * @returns {Promise<boolean>} 是否删除成功
   */
  async function deleteKnowledge(id) {
    loading.value = true
    error.value = null
    try {
      const response = await knowledgeApi.delete(id)
      if (response.code === 200) {
        knowledgeList.value = knowledgeList.value.filter((k) => k.id !== id)
        return true
      }
    } catch (e) {
      error.value = e.message || '删除知识库失败'
    } finally {
      loading.value = false
    }
    return false
  }

  /**
   * 获取知识库下的知识条目
   * @param {number} knowledgeId - 知识库ID
   * @returns {Promise<Array>} 知识条目列表
   */
  async function fetchItems(knowledgeId) {
    loading.value = true
    error.value = null
    try {
      const response = await knowledgeApi.getItems(knowledgeId)
      if (response.code === 200) {
        currentItems.value = response.data
        return response.data
      }
    } catch (e) {
      error.value = e.message || '获取知识条目失败'
    } finally {
      loading.value = false
    }
    return []
  }

  /**
   * 获取笔记关联的知识条目
   * @param {number} noteId - 笔记ID
   * @returns {Promise<Array>} 知识条目列表
   */
  async function fetchItemsByNoteId(noteId) {
    loading.value = true
    error.value = null
    try {
      const response = await knowledgeApi.getItemsByNoteId(noteId)
      if (response.code === 200) {
        return response.data
      }
    } catch (e) {
      error.value = e.message || '获取笔记关联的知识条目失败'
    } finally {
      loading.value = false
    }
    return []
  }

  /**
   * 创建知识条目
   * @param {number} knowledgeId - 知识库ID
   * @param {Object} data - 知识条目数据
   * @returns {Promise<Object|null>} 创建的知识条目
   */
  async function createItem(knowledgeId, data) {
    loading.value = true
    error.value = null
    try {
      const response = await knowledgeApi.createItem(knowledgeId, data)
      if (response.code === 200) {
        currentItems.value.push(response.data)
        return response.data
      }
    } catch (e) {
      error.value = e.message || '创建知识条目失败'
    } finally {
      loading.value = false
    }
    return null
  }

  /**
   * 更新知识条目
   * @param {number} itemId - 知识条目ID
   * @param {Object} data - 知识条目数据
   * @returns {Promise<Object|null>} 更新后的知识条目
   */
  async function updateItem(itemId, data) {
    loading.value = true
    error.value = null
    try {
      const response = await knowledgeApi.updateItem(itemId, data)
      if (response.code === 200) {
        const index = currentItems.value.findIndex((i) => i.id === itemId)
        if (index !== -1) {
          currentItems.value[index] = response.data
        }
        return response.data
      }
    } catch (e) {
      error.value = e.message || '更新知识条目失败'
    } finally {
      loading.value = false
    }
    return null
  }

  /**
   * 删除知识条目
   * @param {number} itemId - 知识条目ID
   * @returns {Promise<boolean>} 是否删除成功
   */
  async function deleteItem(itemId) {
    loading.value = true
    error.value = null
    try {
      const response = await knowledgeApi.deleteItem(itemId)
      if (response.code === 200) {
        currentItems.value = currentItems.value.filter((i) => i.id !== itemId)
        return true
      }
    } catch (e) {
      error.value = e.message || '删除知识条目失败'
    } finally {
      loading.value = false
    }
    return false
  }

  /**
   * 获取知识库数量
   * @returns {Promise<number>} 知识库数量
   */
  async function getKnowledgeCount() {
    try {
      const response = await knowledgeApi.count()
      if (response.code === 200) {
        return response.data
      }
    } catch (e) {
      console.error('获取知识库数量失败:', e)
    }
    return 0
  }

  return {
    knowledgeList,
    currentKnowledge,
    currentItems,
    loading,
    error,
    fetchKnowledgeList,
    fetchKnowledge,
    createKnowledge,
    updateKnowledge,
    deleteKnowledge,
    fetchItems,
    fetchItemsByNoteId,
    createItem,
    updateItem,
    deleteItem,
    getKnowledgeCount,
  }
})
