/**
 * 笔记状态管理
 * 管理笔记列表、当前笔记、草稿和相关操作
 */
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { noteApi } from '@/api/note'

export const useNoteStore = defineStore('note', () => {
  /** 笔记列表 */
  const notes = ref([])
  /** 当前笔记 */
  const currentNote = ref(null)
  /** 加载状态 */
  const loading = ref(false)
  /** AI摘要生成中状态 */
  const generatingSummary = ref(false)
  /** 错误信息 */
  const error = ref(null)

  /**
   * 获取笔记列表
   * @param {string} category - 分类（可选）
   * @returns {Promise<void>}
   */
  async function fetchNotes(category) {
    loading.value = true
    error.value = null
    try {
      const response = await noteApi.list(category)
      if (response.code === 200) {
        notes.value = response.data
      }
    } catch (e) {
      error.value = e.message || '获取笔记列表失败'
    } finally {
      loading.value = false
    }
  }

  /**
   * 获取笔记详情
   * @param {number} id - 笔记ID
   * @returns {Promise<Object|null>} 笔记详情
   */
  async function fetchNote(id) {
    loading.value = true
    error.value = null
    try {
      const response = await noteApi.getById(id)
      if (response.code === 200) {
        currentNote.value = response.data
        return currentNote.value
      }
    } catch (e) {
      error.value = e.message || '获取笔记详情失败'
    } finally {
      loading.value = false
    }
    return null
  }

  /**
   * 创建笔记
   * @param {Object} data - 笔记数据
   * @returns {Promise<Object|null>} 创建的笔记
   */
  async function createNote(data) {
    loading.value = true
    error.value = null
    try {
      const response = await noteApi.create(data)
      if (response.code === 200) {
        notes.value.unshift(response.data)
        return response.data
      }
    } catch (e) {
      error.value = e.message || '创建笔记失败'
    } finally {
      loading.value = false
    }
    return null
  }

  /**
   * 更新笔记
   * @param {number} id - 笔记ID
   * @param {Object} data - 笔记数据
   * @returns {Promise<Object|null>} 更新后的笔记
   */
  async function updateNote(id, data) {
    loading.value = true
    error.value = null
    try {
      const response = await noteApi.update(id, data)
      if (response.code === 200) {
        const index = notes.value.findIndex((n) => n.id === id)
        if (index !== -1) {
          notes.value[index] = response.data
        }
        currentNote.value = response.data
        return response.data
      }
    } catch (e) {
      error.value = e.message || '更新笔记失败'
    } finally {
      loading.value = false
    }
    return null
  }

  /**
   * 删除笔记
   * @param {number} id - 笔记ID
   * @returns {Promise<boolean>} 是否删除成功
   */
  async function deleteNote(id) {
    loading.value = true
    error.value = null
    try {
      const response = await noteApi.delete(id)
      if (response.code === 200) {
        notes.value = notes.value.filter((n) => n.id !== id)
        return true
      }
    } catch (e) {
      error.value = e.message || '删除笔记失败'
    } finally {
      loading.value = false
    }
    return false
  }

  /**
   * 搜索笔记
   * @param {string} keyword - 搜索关键词
   * @returns {Promise<Array>} 搜索结果列表
   */
  async function searchNotes(keyword) {
    loading.value = true
    error.value = null
    try {
      const response = await noteApi.search(keyword)
      if (response.code === 200) {
        return response.data
      }
    } catch (e) {
      error.value = e.message || '搜索笔记失败'
    } finally {
      loading.value = false
    }
    return []
  }

  /**
   * 生成AI摘要
   * @param {number} id - 笔记ID
   * @param {Object} options - 摘要选项
   * @param {string} options.style - 摘要风格
   * @param {string} options.length - 摘要长度
   * @returns {Promise<Object|null>} 生成的摘要
   */
  async function generateSummary(id, options = {}) {
    if (generatingSummary.value) {
      console.log('AI摘要正在生成中，请勿重复调用')
      return null
    }

    generatingSummary.value = true
    sessionStorage.setItem('generatingSummaryNoteId', id)
    sessionStorage.setItem('generatingSummaryTime', Date.now().toString())
    error.value = null
    try {
      const response = await noteApi.generateSummary(id, options)
      if (response.code === 200) {
        if (currentNote.value && currentNote.value.id === id) {
          currentNote.value = { ...response.data }
        }
        sessionStorage.removeItem('generatingSummaryNoteId')
        sessionStorage.removeItem('generatingSummaryTime')
        return response.data
      }
    } catch (e) {
      error.value = e.message || '生成摘要失败'
    } finally {
      generatingSummary.value = false
      sessionStorage.removeItem('generatingSummaryNoteId')
      sessionStorage.removeItem('generatingSummaryTime')
    }
    return null
  }

  function checkGeneratingStatus(noteId) {
    const savedNoteId = sessionStorage.getItem('generatingSummaryNoteId')
    const savedTime = sessionStorage.getItem('generatingSummaryTime')
    if (savedNoteId && savedNoteId === String(noteId) && savedTime) {
      const elapsed = Date.now() - parseInt(savedTime)
      if (elapsed < 60000) {
        return true
      } else {
        sessionStorage.removeItem('generatingSummaryNoteId')
        sessionStorage.removeItem('generatingSummaryTime')
      }
    }
    return false
  }

  function setGeneratingStatus(status) {
    generatingSummary.value = status
  }

  /**
   * 清除当前笔记
   */
  function clearCurrentNote() {
    currentNote.value = null
  }

  /**
   * 更新笔记的知识库关联
   * @param {number} id - 笔记ID
   * @param {number} knowledgeId - 知识库ID
   * @returns {Promise<boolean>} 是否更新成功
   */
  async function updateKnowledgeId(id, knowledgeId) {
    try {
      const response = await noteApi.updateKnowledgeId(id, knowledgeId)
      if (response.code === 200) {
        const note = notes.value.find((n) => n.id === id)
        if (note) {
          note.knowledgeId = knowledgeId
        }
        if (currentNote.value && currentNote.value.id === id) {
          currentNote.value.knowledgeId = knowledgeId
        }
        return true
      }
    } catch (e) {
      error.value = e.message || '更新知识库关联失败'
    }
    return false
  }

  /**
   * 获取笔记数量
   * @returns {Promise<number>} 笔记数量
   */
  async function getNoteCount() {
    try {
      const response = await noteApi.count()
      if (response.code === 200) {
        return response.data
      }
    } catch (e) {
      console.error('获取笔记数量失败:', e)
    }
    return 0
  }

  /**
   * 添加图表到笔记
   * @param {number} noteId - 笔记ID
   * @param {Object} diagramData - 图表数据
   * @returns {Promise<Object|null>} 添加的图表
   */
  async function addDiagram(noteId, diagramData) {
    try {
      const response = await noteApi.addDiagram(noteId, diagramData)
      if (response.code === 200) {
        if (currentNote.value && currentNote.value.id === noteId) {
          if (!currentNote.value.diagrams) {
            currentNote.value.diagrams = []
          }
          currentNote.value.diagrams.push(response.data)
        }
        return response.data
      }
    } catch (e) {
      error.value = e.message || '添加图表失败'
    }
    return null
  }

  /**
   * 删除笔记中的图表
   * @param {number} noteId - 笔记ID
   * @param {number} diagramId - 图表ID
   * @returns {Promise<boolean>} 是否删除成功
   */
  async function deleteDiagram(noteId, diagramId) {
    try {
      const response = await noteApi.deleteDiagram(noteId, diagramId)
      if (response.code === 200) {
        if (currentNote.value && currentNote.value.id === noteId) {
          currentNote.value.diagrams = currentNote.value.diagrams.filter(d => d.id !== diagramId)
        }
        return true
      }
    } catch (e) {
      error.value = e.message || '删除图表失败'
    }
    return false
  }

  return {
    notes,
    currentNote,
    loading,
    generatingSummary,
    error,
    fetchNotes,
    fetchNote,
    createNote,
    updateNote,
    deleteNote,
    searchNotes,
    generateSummary,
    checkGeneratingStatus,
    setGeneratingStatus,
    clearCurrentNote,
    updateKnowledgeId,
    getNoteCount,
    addDiagram,
    deleteDiagram,
  }
})
