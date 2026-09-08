/**
 * AI功能状态管理
 * 管理AI摘要历史和相关操作
 */
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { aiApi } from '@/api/ai'

export const useAiStore = defineStore('ai', () => {
  /** AI摘要历史列表 */
  const summaryHistory = ref([])
  /** 加载状态 */
  const loading = ref(false)
  /** 错误信息 */
  const error = ref(null)

  /**
   * 生成AI摘要
   * @param {Object} data - 摘要请求数据
   * @returns {Promise} 生成的摘要
   */
  async function generateSummary(data) {
    loading.value = true
    error.value = null
    try {
      const response = await aiApi.generateSummary(data)
      if (response.code === 200) {
        const result = response.data || response
        if (result) {
          summaryHistory.value.unshift(result)
        }
        return result
      }
    } catch (e) {
      error.value = e.message || '生成摘要失败'
    } finally {
      loading.value = false
    }
    return null
  }

  /**
   * 获取AI摘要历史
   * @param {number} limit - 返回数量限制
   * @returns {Promise<Array>} 摘要历史列表
   */
  async function fetchSummaryHistory(limit = 10) {
    loading.value = true
    error.value = null
    try {
      const response = await aiApi.getSummaryHistory(limit)
      if (response.code === 200) {
        summaryHistory.value = response.data
        return response.data
      }
    } catch (e) {
      error.value = e.message || '获取摘要历史失败'
    } finally {
      loading.value = false
    }
    return []
  }

  /**
   * 删除AI摘要
   * @param {number} id - 摘要ID
   * @returns {Promise<boolean>} 是否删除成功
   */
  async function deleteSummary(id) {
    try {
      const response = await aiApi.deleteSummary(id)
      if (response.code === 200) {
        summaryHistory.value = summaryHistory.value.filter((s) => s.id !== id)
        return true
      }
    } catch (e) {
      error.value = e.message || '删除摘要失败'
    }
    return false
  }

  /**
   * 保存AI摘要到笔记
   * @param {number} id - 摘要ID
   * @returns {Promise<number|null>} 新笔记ID，失败返回null
   */
  async function saveToNote(id) {
    loading.value = true
    error.value = null
    try {
      const response = await aiApi.saveToNote(id)
      if (response.code === 200) {
        return response.data
      }
    } catch (e) {
      error.value = e.message || '保存到笔记失败'
    } finally {
      loading.value = false
    }
    return null
  }

  /**
   * 提取关键词
   * @param {string} content - 待提取的内容
   * @returns {Promise<string>} 提取的关键词
   */
  async function extractKeywords(content) {
    loading.value = true
    error.value = null
    try {
      const response = await aiApi.extractKeywords(content)
      if (response.code === 200) {
        return response.data
      }
    } catch (e) {
      error.value = e.message || '提取关键词失败'
    } finally {
      loading.value = false
    }
    return null
  }

  /**
   * 生成知识图谱
   * @param {string} content - 可选的内容，如果不传则从后端获取
   * @returns {Promise<string>} 知识图谱JSON数据
   */
  async function generateKnowledgeGraph(content) {
    loading.value = true
    error.value = null
    try {
      const response = await aiApi.generateKnowledgeGraph()
      if (response.code === 200) {
        let data = response.data
        if (typeof data === 'string') {
          try {
            data = JSON.parse(data)
          } catch (e) {
            console.error('解析知识图谱数据失败:', e)
            return null
          }
        }
        return data
      }
    } catch (e) {
      error.value = e.message || '生成知识图谱失败'
    } finally {
      loading.value = false
    }
    return null
  }

  return {
    summaryHistory,
    loading,
    error,
    generateSummary,
    fetchSummaryHistory,
    deleteSummary,
    saveToNote,
    extractKeywords,
    generateKnowledgeGraph,
  }
})
