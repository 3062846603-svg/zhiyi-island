<script setup>
  import { ref, computed, onMounted } from 'vue'
  import { useRouter, useRoute } from 'vue-router'
  import { Icon } from '@iconify/vue'
  import { ElTooltip } from 'element-plus'
  import { useNoteStore } from '@/stores/note'
  import { useToastStore } from '@/stores/toast'
  import RichTextEditor from '@/components/RichTextEditor.vue'
  import MultiImageUploader from '@/components/MultiImageUploader.vue'
  import { noteApi } from '@/api/note'
  import { ValidationRules } from '@/utils/validation'

  const router = useRouter()
  const route = useRoute()
  const noteStore = useNoteStore()
  const toast = useToastStore()

  const isEdit = computed(() => !!route.query.id)
  const editNoteId = computed(() => route.query.id)

  const noteForm = ref({
    title: '',
    content: '',
    category: null,
    images: [],
    isDraft: false
  })

  const saving = ref(false)
  const categories = ref([])

  /** 失焦状态 */
  const titleBlurred = ref(false)

  /** 验证 */
  const titleInvalid = computed(() => {
    return titleBlurred.value && noteForm.value.title && noteForm.value.title.length > ValidationRules.note.title.maxLength
  })

  const categoryIcons = {
    '学习笔记': 'ri:book-open-line',
    '工作记录': 'ri:briefcase-line',
    '生活随笔': 'ri:heart-line',
    '技术文档': 'ri:code-s-slash-line',
    '读书笔记': 'ri:book-2-line',
    '项目总结': 'ri:folder-chart-line',
    '会议记录': 'ri:team-line',
    '其他': 'ri:file-text-line'
  }

  const loadCategories = async () => {
    try {
      const result = await noteApi.getCategories()
      if (result.code === 200) {
        categories.value = result.data.map(c => ({
          code: c.code,
          description: c.description,
          icon: categoryIcons[c.description] || 'ri:folder-line'
        }))
      }
    } catch (e) {
      console.error('获取分类失败:', e)
    }
  }

  const goBack = () => {
    router.back()
  }

  const saveNote = async (asDraft = false) => {
    if (!noteForm.value.title.trim()) {
      toast.warning(ValidationRules.note.title.messages.required)
      return
    }
    if (noteForm.value.title.length > ValidationRules.note.title.maxLength) {
      toast.warning(ValidationRules.note.title.messages.maxLength)
      return
    }
    if (!noteForm.value.content.trim()) {
      toast.warning(ValidationRules.note.content.messages.required)
      return
    }

    saving.value = true
    try {
      const noteData = {
        title: noteForm.value.title,
        content: noteForm.value.content,
        category: noteForm.value.category,
        images: noteForm.value.images,
        status: asDraft ? 0 : 1
      }

      if (isEdit.value) {
        const result = await noteStore.updateNote(editNoteId.value, noteData)
        if (result) {
          toast.success('更新成功')
          router.push('/notes')
        } else {
          toast.error('更新失败')
        }
      } else {
        const result = await noteStore.createNote(noteData)
        if (result) {
          toast.success(asDraft ? '已保存为草稿' : '保存成功')
          router.push(asDraft ? '/drafts' : '/notes')
        } else {
          toast.error('保存失败')
        }
      }
    } finally {
      saving.value = false
    }
  }

  const loadNoteForEdit = async () => {
    if (isEdit.value) {
      await noteStore.fetchNote(editNoteId.value)
      const note = noteStore.currentNote
      if (note) {
        const matchedCategory = categories.value.find(c => c.description === note.category)
        noteForm.value = {
          title: note.title || '',
          content: note.content || '',
          category: matchedCategory ? matchedCategory.code : null,
          images: note.images || [],
          isDraft: note.status === 0
        }
      }
    }
  }

  const handleImageError = (message) => {
    toast.error(message)
  }

  onMounted(async () => {
    await loadCategories()
    if (isEdit.value) {
      loadNoteForEdit()
    }
  })
</script>

<template>
  <div class="add-note-page">
    <div class="page-header">
      <button class="btn btn-secondary" @click="goBack">
        <Icon icon="ri:arrow-left-line" />
        返回
      </button>
      <div class="header-title">
        <h1>{{ isEdit ? '编辑笔记' : '新建笔记' }}</h1>
        <p>{{ isEdit ? '修改你的笔记内容' : '记录你的想法和收获' }}</p>
      </div>
      <div class="header-actions">
        <button v-if="!isEdit" class="btn btn-secondary" @click="saveNote(true)" :disabled="saving">
          <Icon icon="ri:draft-line" />
          存草稿
        </button>
        <button class="btn btn-primary" @click="saveNote(false)" :disabled="saving">
          <Icon :icon="saving ? 'ri:loader-4-line' : 'ri:save-line'" :class="{ spin: saving }" />
          {{ saving ? '保存中...' : '发布' }}
        </button>
      </div>
    </div>

    <div class="form-container">
      <div class="form-main">
        <div class="form-card">
          <div class="form-group title-group">
            <ElTooltip :visible="titleInvalid" :content="ValidationRules.note.title.messages.maxLength" effect="light"
              placement="top">
              <template #default>
                <input v-model="noteForm.title" type="text" class="title-input" :class="{ 'input-error': titleInvalid }"
                  placeholder="输入笔记标题（最多200个字符）" @blur="titleBlurred = true" />
              </template>
            </ElTooltip>
          </div>

          <div class="form-group images-group">
            <label class="section-label">
              <Icon icon="ri:image-line" />
              笔记图片（最多9张）
            </label>
            <MultiImageUploader v-model="noteForm.images" :max-count="9" @error="handleImageError" />
          </div>

          <div class="form-group content-group">
            <label class="section-label">
              <Icon icon="ri:file-text-line" />
              笔记内容
            </label>
            <RichTextEditor v-model="noteForm.content" placeholder="开始记录你的想法..." height="600px" />
          </div>
        </div>
      </div>

      <div class="form-sidebar">
        <div class="sidebar-card">
          <h3 class="sidebar-title">
            <Icon icon="ri:settings-4-line" />
            笔记设置
          </h3>

          <div class="setting-item">
            <label class="setting-label">分类</label>
            <div class="category-grid">
              <button v-for="cat in categories" :key="cat.code" class="category-btn"
                :class="{ active: noteForm.category === cat.code }" @click="noteForm.category = cat.code">
                <Icon :icon="cat.icon" />
                {{ cat.description }}
              </button>
            </div>
          </div>
        </div>

        <div class="sidebar-card tips-card">
          <h3 class="sidebar-title">
            <Icon icon="ri:lightbulb-line" />
            写作提示
          </h3>
          <ul class="tips-list">
            <li>记录核心概念和关键定义</li>
            <li>写下自己的理解和思考</li>
            <li>关联其他知识点</li>
            <li>提出疑问待后续探索</li>
          </ul>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
  .add-note-page {
    max-width: 1400px;
    margin: 0 auto;
    height: calc(100vh - 108px);
    display: flex;
    flex-direction: column;
  }

  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;
    padding-bottom: 16px;
    border-bottom: 1px solid #e5e7eb;
  }

  .header-title {
    text-align: center;
  }

  .header-title h1 {
    font-size: 20px;
    font-weight: 600;
    color: #1f2937;
    margin-bottom: 4px;
  }

  .header-title p {
    font-size: 13px;
    color: #6b7280;
  }

  .header-actions {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  .spin {
    animation: spin 1s linear infinite;
  }

  @keyframes spin {
    from {
      transform: rotate(0deg);
    }

    to {
      transform: rotate(360deg);
    }
  }

  .form-container {
    flex: 1;
    display: grid;
    grid-template-columns: 1fr 280px;
    gap: 24px;
    overflow: hidden;
  }

  .form-main {
    overflow: hidden;
  }

  .form-card {
    background: white;
    border-radius: 16px;
    padding: 24px;
    height: 100%;
    display: flex;
    flex-direction: column;
    overflow-y: auto;
  }

  .form-group {
    margin-bottom: 16px;
  }

  .title-group {
    margin-bottom: 20px;
  }

  .title-input {
    width: 100%;
    padding: 16px 0;
    border: none;
    border-bottom: 2px solid #e5e7eb;
    font-size: 24px;
    font-weight: 600;
    color: #1f2937;
    background: transparent;
    transition: border-color 0.2s;
  }

  .title-input:focus {
    outline: none;
    border-color: var(--primary-color);
  }

  .title-input.input-error {
    border-color: #ef4444;
  }

  .title-input::placeholder {
    color: #9ca3af;
    font-weight: 400;
  }

  .section-label {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 14px;
    font-weight: 500;
    color: #374151;
    margin-bottom: 12px;
  }

  .images-group {
    margin-bottom: 20px;
  }

  .content-group {
    flex: 1;
    display: flex;
    flex-direction: column;
    margin-bottom: 0;
    min-height: 600px;
  }

  .form-sidebar {
    display: flex;
    flex-direction: column;
    gap: 16px;
  }

  .sidebar-card {
    background: white;
    border-radius: 16px;
    padding: 20px;
  }

  .sidebar-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 15px;
    font-weight: 600;
    color: #1f2937;
    margin-bottom: 16px;
  }

  .setting-item {
    margin-bottom: 20px;
  }

  .setting-item:last-child {
    margin-bottom: 0;
  }

  .setting-label {
    display: block;
    font-size: 13px;
    font-weight: 500;
    color: #6b7280;
    margin-bottom: 12px;
  }

  .category-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 8px;
  }

  .category-btn {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 10px 12px;
    background: #f9fafb;
    border: 1px solid #e5e7eb;
    border-radius: 8px;
    color: #374151;
    font-size: 13px;
    cursor: pointer;
    transition: all 0.2s;
  }

  .category-btn:hover {
    background: #f3f4f6;
    border-color: #d1d5db;
  }

  .category-btn.active {
    background: linear-gradient(135deg, #E8EDFA 0%, #D4DFF5 100%);
    border-color: var(--primary-color);
    color: var(--primary-color);
  }

  .tips-card {
    background: linear-gradient(135deg, #E8EDFA 0%, #D4DFF5 100%);
    border: none;
  }

  .tips-card .sidebar-title {
    color: var(--primary-color);
  }

  .tips-list {
    padding-left: 20px;
    margin: 0;
  }

  .tips-list li {
    font-size: 13px;
    color: #0D1F5C;
    margin-bottom: 8px;
    line-height: 1.5;
  }

  .tips-list li:last-child {
    margin-bottom: 0;
  }

  @media (max-width: 1024px) {
    .form-container {
      grid-template-columns: 1fr;
    }

    .form-sidebar {
      flex-direction: row;
      flex-wrap: wrap;
    }

    .sidebar-card {
      flex: 1;
      min-width: 280px;
    }
  }

  @media (max-width: 768px) {
    .page-header {
      flex-wrap: wrap;
      gap: 12px;
    }

    .header-title {
      order: -1;
      width: 100%;
    }

    .header-actions {
      width: 100%;
      justify-content: space-between;
    }

    .back-btn,
    .save-btn {
      flex: 1;
      justify-content: center;
    }

    .form-sidebar {
      flex-direction: column;
    }

    .sidebar-card {
      min-width: auto;
    }

    .category-grid {
      grid-template-columns: 1fr;
    }
  }
</style>