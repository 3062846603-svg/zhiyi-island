<!--
  笔记列表页面
  展示用户的所有笔记，支持搜索、筛选和分类
-->
<script setup>
  import { ref, computed, onMounted, onUnmounted } from 'vue'
  import { useRouter } from 'vue-router'
  import { Icon } from '@iconify/vue'
  import { useNoteStore } from '@/stores/note'
  import Pagination from '@/components/Pagination.vue'

  const router = useRouter()
  const noteStore = useNoteStore()

  /** 搜索关键词 */
  const searchQuery = ref('')
  /** 选中的分类 */
  const selectedCategory = ref('')
  /** 视图模式：grid/list */
  const viewMode = ref('grid')
  /** 分类下拉框显示状态 */
  const showCategoryDropdown = ref(false)
  /** 分类筛选器引用 */
  const categoryFilterRef = ref(null)
  /** 当前页码 */
  const currentPage = ref(1)
  /** 每页显示数量 */
  const pageSize = ref(12)

  /** 笔记列表 */
  const notes = computed(() => noteStore.notes)
  /** 加载状态 */
  const loading = computed(() => noteStore.loading)

  /**
   * 所有分类列表
   * 从笔记中提取唯一分类
   */
  const categories = computed(() => {
    const cats = new Set()
    notes.value.forEach((note) => {
      if (note.category) {
        cats.add(note.category)
      }
    })
    return ['全部', ...Array.from(cats).sort()]
  })

  /**
   * 过滤后的笔记列表
   * 根据搜索关键词和分类进行筛选
   */
  const filteredNotes = computed(() => {
    let result = notes.value

    if (searchQuery.value) {
      const query = searchQuery.value.toLowerCase()
      result = result.filter(
        (note) =>
          note.title?.toLowerCase().includes(query) ||
          note.content?.toLowerCase().includes(query)
      )
    }

    if (selectedCategory.value && selectedCategory.value !== '全部') {
      result = result.filter((note) => note.category === selectedCategory.value)
    }

    return result.sort((a, b) => {
      return new Date(b.createTime) - new Date(a.createTime)
    })
  })

  /** 总页数 */
  const totalPages = computed(() => Math.ceil(filteredNotes.value.length / pageSize.value))

  /** 当前页的笔记列表 */
  const paginatedNotes = computed(() => {
    const start = (currentPage.value - 1) * pageSize.value
    const end = start + pageSize.value
    return filteredNotes.value.slice(start, end)
  })

  /**
   * 处理页码变化
   * @param {number} page - 新页码
   */
  const handlePageChange = (page) => {
    currentPage.value = page
  }

  /**
   * 选择分类
   * @param {string} cat - 分类名称
   */
  const selectCategory = (cat) => {
    selectedCategory.value = cat
    showCategoryDropdown.value = false
    currentPage.value = 1
  }

  /**
   * 获取分类样式配置
   * @param {string} category - 分类名称
   * @returns {Object} 样式配置对象
   */
  const getCategoryStyle = (category) => {
    const styles = {
      '学习笔记': {
        icon: 'ri:book-open-line',
        gradient: 'linear-gradient(135deg, #4F7CFF 0%, #3B5FD9 100%)',
        color: '#4F7CFF',
        bg: '#EEF2FF',
        shadow: 'rgba(79, 124, 255, 0.25)'
      },
      '工作记录': {
        icon: 'ri:briefcase-line',
        gradient: 'linear-gradient(135deg, #6366F1 0%, #4F46E5 100%)',
        color: '#6366F1',
        bg: '#EEF2FF',
        shadow: 'rgba(99, 102, 241, 0.25)'
      },
      '生活随笔': {
        icon: 'ri:heart-line',
        gradient: 'linear-gradient(135deg, #F472B6 0%, #EC4899 100%)',
        color: '#EC4899',
        bg: '#FDF2F8',
        shadow: 'rgba(236, 72, 153, 0.25)'
      },
      '技术文档': {
        icon: 'ri:code-s-slash-line',
        gradient: 'linear-gradient(135deg, #22C55E 0%, #16A34A 100%)',
        color: '#22C55E',
        bg: '#F0FDF4',
        shadow: 'rgba(34, 197, 94, 0.25)'
      },
      '读书笔记': {
        icon: 'ri:book-2-line',
        gradient: 'linear-gradient(135deg, #FB923C 0%, #F97316 100%)',
        color: '#F97316',
        bg: '#FFF7ED',
        shadow: 'rgba(249, 115, 22, 0.25)'
      },
      '项目总结': {
        icon: 'ri:folder-chart-line',
        gradient: 'linear-gradient(135deg, #38BDF8 0%, #0EA5E9 100%)',
        color: '#0EA5E9',
        bg: '#F0F9FF',
        shadow: 'rgba(14, 165, 233, 0.25)'
      },
      '会议记录': {
        icon: 'ri:team-line',
        gradient: 'linear-gradient(135deg, #A78BFA 0%, #8B5CF6 100%)',
        color: '#8B5CF6',
        bg: '#F5F3FF',
        shadow: 'rgba(139, 92, 246, 0.25)'
      },
      '其他': {
        icon: 'ri:file-text-line',
        gradient: 'linear-gradient(135deg, #94A3B8 0%, #64748B 100%)',
        color: '#64748B',
        bg: '#F8FAFC',
        shadow: 'rgba(100, 116, 139, 0.25)'
      },
    }
    return styles[category] || {
      icon: 'ri:file-text-line',
      gradient: 'linear-gradient(135deg, #94A3B8 0%, #64748B 100%)',
      color: '#64748B',
      bg: '#F8FAFC',
      shadow: 'rgba(100, 116, 139, 0.25)'
    }
  }

  const getCategoryColor = (category) => {
    return getCategoryStyle(category)
  }

  /**
   * 格式化日期显示
   * @param {string} dateStr - 日期字符串
   * @returns {string} 格式化后的日期
   */
  const formatDate = (dateStr) => {
    if (!dateStr) return ''
    const date = new Date(dateStr)
    const now = new Date()
    const diff = now - date
    const days = Math.floor(diff / (1000 * 60 * 60 * 24))

    if (days === 0) {
      return '今天'
    } else if (days === 1) {
      return '昨天'
    } else if (days < 7) {
      return `${days}天前`
    } else {
      return date.toLocaleDateString('zh-CN', { month: 'short', day: 'numeric' })
    }
  }

  /**
   * 跳转到笔记详情
   * @param {number} id - 笔记ID
   */
  const goToDetail = (id) => {
    router.push(`/notes/${id}`)
  }

  /**
   * 创建新笔记
   */
  const createNote = () => {
    router.push('/home/add-note')
  }

  /**
   * 点击外部关闭下拉框
   * @param {Event} event - 点击事件
   */
  const handleClickOutside = (event) => {
    if (categoryFilterRef.value && !categoryFilterRef.value.contains(event.target)) {
      showCategoryDropdown.value = false
    }
  }

  onMounted(async () => {
    await noteStore.fetchNotes()
    document.addEventListener('click', handleClickOutside)
  })

  onUnmounted(() => {
    document.removeEventListener('click', handleClickOutside)
  })
</script>

<template>
  <div class="notes-page">
    <div class="toolbar">


      <div class="search-box">
        <Icon icon="ri:search-line" class="search-icon" />
        <input v-model="searchQuery" type="text" placeholder="搜索笔记..." class="search-input" />
      </div>

      <div class="filters">
        <div class="category-filter" ref="categoryFilterRef">
          <div class="custom-select" @click="showCategoryDropdown = !showCategoryDropdown">
            <div class="select-display">
              <Icon icon="ri:folder-line" class="select-icon" />
              <span class="select-text">{{ selectedCategory || '全部分类' }}</span>
              <Icon icon="ri:arrow-down-s-line" class="arrow-icon" :class="{ rotated: showCategoryDropdown }" />
            </div>
            <Transition name="dropdown">
              <div class="select-dropdown" v-if="showCategoryDropdown">
                <div class="dropdown-item" :class="{ active: selectedCategory === '' }"
                  @click.stop="selectCategory('')">
                  <Icon icon="ri:apps-line" class="item-icon" />
                  <span>全部分类</span>
                </div>
                <div v-for="cat in categories.filter(c => c !== '全部')" :key="cat" class="dropdown-item"
                  :class="{ active: selectedCategory === cat }" @click.stop="selectCategory(cat)">
                  <Icon :icon="getCategoryStyle(cat).icon" class="item-icon" />
                  <span>{{ cat }}</span>
                </div>
              </div>
            </Transition>
          </div>
        </div>

        <div class="view-toggle">
          <button class="toggle-btn" :class="{ active: viewMode === 'grid' }" @click="viewMode = 'grid'">
            <Icon icon="ri:grid-fill" />
          </button>
          <button class="toggle-btn" :class="{ active: viewMode === 'list' }" @click="viewMode = 'list'">
            <Icon icon="ri:list-check" />
          </button>
        </div>

        <button class="btn btn-primary" @click="createNote">
          <Icon icon="ri:add-line" />
          新建笔记
        </button>
      </div>
    </div>

    <div class="notes-container" v-if="!loading">
      <div class="notes-grid" v-if="filteredNotes.length > 0 && viewMode === 'grid'">
        <div v-for="(note, index) in paginatedNotes" :key="note.id" class="note-card" :style="{
            '--hover-color': getCategoryStyle(note.category).color,
            '--shadow-color': getCategoryStyle(note.category).shadow,
            '--delay': `${index * 0.05}s`
          }" @click="goToDetail(note.id)">
          <div class="card-header" :style="{ background: getCategoryColor(note.category).gradient }">
            <div class="header-pattern"></div>
            <div class="header-content">
              <div class="category-badge">
                <Icon :icon="getCategoryStyle(note.category).icon" />
                <span>{{ note.category || '未分类' }}</span>
              </div>
            </div>
          </div>

          <div class="card-body">
            <h4 class="note-title">{{ note.title }}</h4>
            <div class="note-content" v-html="note.content"></div>
          </div>

          <div class="card-footer">
            <span class="date">
              <Icon icon="ri:calendar-line" />
              {{ formatDate(note.createTime) }}
            </span>
            <span class="ai-badge" v-if="note.aiSummary">
              <Icon icon="ri:robot-line" />
              AI摘要
            </span>
          </div>
        </div>
      </div>

      <div class="notes-list" v-else-if="filteredNotes.length > 0 && viewMode === 'list'">
        <div v-for="note in paginatedNotes" :key="note.id" class="list-item"
          :style="{ '--hover-color': getCategoryStyle(note.category).color }" @click="goToDetail(note.id)">
          <div class="item-content">
            <div class="item-header">
              <h4 class="item-title">{{ note.title }}</h4>
              <div class="item-meta">
                <span class="category"
                  :style="{ background: getCategoryStyle(note.category).bg, color: getCategoryStyle(note.category).color }">
                  <Icon :icon="getCategoryStyle(note.category).icon" />
                  {{ note.category || '未分类' }}
                </span>
                <span class="date">{{ formatDate(note.createTime) }}</span>
              </div>
            </div>
            <div class="item-preview" v-html="note.content"></div>
            <div class="item-footer">
              <div class="item-actions">
                <span class="ai-badge" v-if="note.aiSummary">
                  <Icon icon="ri:robot-line" />
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="empty-state" v-else-if="filteredNotes.length === 0">
        <div class="empty-icon">
          <Icon icon="ri:file-list-3-line" />
        </div>
        <h3>暂无笔记</h3>
        <p v-if="searchQuery || selectedCategory">没有找到符合条件的笔记</p>
        <p v-else>点击上方按钮创建你的第一篇笔记</p>
      </div>

      <Pagination v-if="filteredNotes.length > 0" :current-page="currentPage" :total-pages="totalPages"
        :total-items="filteredNotes.length" :page-size="pageSize" @page-change="handlePageChange" />
    </div>

    <div class="loading-state" v-else>
      <Icon icon="ri:loader-4-line" class="loading-icon" />
      <p>加载中...</p>
    </div>
  </div>
</template>

<style scoped>
  .notes-page {
    width: 100%;
  }

  .toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: 16px;
    margin-bottom: 24px;
    padding: 12px 16px;
    background: white;
    border-radius: 12px;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
    border: 1px solid #e5e7eb;
  }

  .search-box {
    flex: 1;
    max-width: 400px;
    position: relative;
  }

  .search-icon {
    position: absolute;
    left: 14px;
    top: 50%;
    transform: translateY(-50%);
    color: #9ca3af;
    font-size: 18px;
  }

  .search-input {
    width: 100%;
    padding: 10px 14px 10px 42px;
    border: 1px solid #e5e7eb;
    border-radius: 8px;
    font-size: 14px;
    transition: all 0.2s;
  }

  .search-input:focus {
    outline: none;
    border-color: var(--primary-color);
    box-shadow: 0 0 0 3px rgba(18, 46, 138, 0.1);
  }

  .filters {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  .category-filter {
    position: relative;
  }

  .custom-select {
    position: relative;
    min-width: 160px;
  }

  .select-display {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 10px 14px;
    background: white;
    border: 1px solid #e5e7eb;
    border-radius: 10px;
    cursor: pointer;
    transition: all 0.2s ease;
    user-select: none;
  }

  .select-display:hover {
    border-color: #122E8A;
    box-shadow: 0 2px 8px rgba(18, 46, 138, 0.1);
  }

  .select-icon {
    font-size: 18px;
    color: #122E8A;
  }

  .select-text {
    flex: 1;
    font-size: 14px;
    color: #374151;
    font-weight: 500;
  }

  .arrow-icon {
    font-size: 18px;
    color: #9ca3af;
    transition: transform 0.2s ease;
  }

  .arrow-icon.rotated {
    transform: rotate(180deg);
  }

  .select-dropdown {
    position: absolute;
    top: calc(100% + 8px);
    left: 0;
    right: 0;
    background: white;
    border-radius: 12px;
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
    border: 1px solid #e5e7eb;
    padding: 8px;
    z-index: 100;
    max-height: 300px;
    overflow-y: auto;
  }

  .dropdown-item {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 10px 12px;
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.15s ease;
    font-size: 14px;
    color: #374151;
  }

  .dropdown-item:hover {
    background: linear-gradient(135deg, #E8EDFA 0%, #D4DFF5 100%);
    color: #122E8A;
  }

  .dropdown-item.active {
    background: linear-gradient(135deg, #122E8A 0%, #1e40af 100%);
    color: white;
  }

  .dropdown-item.active .item-icon {
    color: white;
  }

  .item-icon {
    font-size: 18px;
    color: #6b7280;
  }

  .dropdown-enter-active,
  .dropdown-leave-active {
    transition: all 0.2s ease;
  }

  .dropdown-enter-from,
  .dropdown-leave-to {
    opacity: 0;
    transform: translateY(-8px);
  }

  .view-toggle {
    display: flex;
    background: linear-gradient(180deg, #f9fafb 0%, #f3f4f6 100%);
    border-radius: 10px;
    padding: 4px;
    box-shadow:
      inset 0 1px 2px rgba(0, 0, 0, 0.06),
      0 1px 0 rgba(255, 255, 255, 0.8);
    border: 1px solid rgba(0, 0, 0, 0.06);
  }

  .toggle-btn {
    width: 36px;
    height: 32px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: transparent;
    border: none;
    border-radius: 8px;
    color: #6b7280;
    cursor: pointer;
    transition: all 0.2s ease;
    font-size: 18px;
  }

  .toggle-btn:hover {
    color: #374151;
    background: rgba(255, 255, 255, 0.5);
  }

  .toggle-btn.active {
    background: linear-gradient(180deg, #ffffff 0%, #fafbfc 100%);
    color: var(--primary-color);
    box-shadow:
      0 2px 4px rgba(0, 0, 0, 0.08),
      0 4px 8px rgba(0, 0, 0, 0.04),
      inset 0 1px 0 rgba(255, 255, 255, 1);
  }

  .notes-container {
    min-height: 400px;
  }

  .notes-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
    gap: 24px;
  }

  .note-card {
    background: white;
    border-radius: 16px;
    overflow: hidden;
    cursor: pointer;
    transition: all 0.3s ease;
    animation: fadeInUp 0.5s ease forwards;
    animation-delay: var(--delay);
    opacity: 0;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
    border: 1px solid #e5e7eb;
    display: flex;
    flex-direction: column;
  }

  @keyframes fadeInUp {
    from {
      opacity: 0;
      transform: translateY(20px);
    }

    to {
      opacity: 1;
      transform: translateY(0);
    }
  }

  .note-card:hover {
    transform: translateY(-4px);
    box-shadow: 0 12px 24px var(--shadow-color, rgba(0, 0, 0, 0.12));
    border-color: var(--hover-color);
  }

  .card-header {
    position: relative;
    padding: 20px;
    color: white;
    overflow: hidden;
  }

  .header-pattern {
    position: absolute;
    top: -50%;
    right: -50%;
    width: 100%;
    height: 200%;
    background: radial-gradient(circle, rgba(255, 255, 255, 0.1) 0%, transparent 70%);
  }

  .header-content {
    position: relative;
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .category-badge {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 6px 12px;
    background: rgba(255, 255, 255, 0.2);
    border-radius: 20px;
    font-size: 12px;
    font-weight: 500;
    backdrop-filter: blur(4px);
  }

  .card-body {
    padding: 20px;
    flex: 1;
    display: flex;
    flex-direction: column;
  }

  .note-title {
    font-size: 16px;
    font-weight: 600;
    color: #1f2937;
    margin-bottom: 12px;
    line-height: 1.4;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }

  .note-content {
    font-size: 14px;
    color: #6b7280;
    line-height: 1.6;
    margin-bottom: 16px;
    display: -webkit-box;
    -webkit-line-clamp: 3;
    line-clamp: 3;
    -webkit-box-orient: vertical;
    overflow: hidden;
    flex: 1;
  }

  .note-content :deep(p) {
    margin: 0;
  }

  .note-content :deep(ul),
  .note-content :deep(ol) {
    padding-left: 20px;
    margin: 0;
  }

  .note-content :deep(li) {
    list-style-position: inside;
  }

  .note-content :deep(code) {
    background: #f3f4f6;
    padding: 2px 4px;
    border-radius: 4px;
    font-size: 13px;
  }

  .note-content :deep(strong) {
    color: #374151;
    font-weight: 600;
  }

  .note-content :deep(blockquote) {
    border-left: 3px solid #d1d5db;
    padding-left: 12px;
    color: #9ca3af;
    margin: 0;
  }

  .note-content :deep(p) {
    margin: 0;
  }

  .card-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px 20px;
    border-top: 1px solid #f3f4f6;
  }

  .date {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 12px;
    color: #9ca3af;
  }

  .ai-badge {
    display: flex;
    align-items: center;
    gap: 4px;
    padding: 4px 10px;
    background: linear-gradient(135deg, #E8EDFA 0%, #D4DFF5 100%);
    border-radius: 12px;
    font-size: 11px;
    color: var(--primary-color);
    font-weight: 500;
  }

  .notes-list {
    display: flex;
    flex-direction: column;
    gap: 12px;
  }

  .list-item {
    display: flex;
    background: white;
    border-radius: 12px;
    overflow: hidden;
    cursor: pointer;
    transition: all 0.2s;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
    border: 1px solid #e5e7eb;
  }

  .list-item:hover {
    transform: translateX(4px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
    border-color: var(--hover-color);
  }

  .item-content {
    flex: 1;
    padding: 20px;
  }

  .item-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 8px;
  }

  .item-title {
    font-size: 16px;
    font-weight: 600;
    color: #1f2937;
    flex: 1;
    margin-right: 16px;
  }

  .item-meta {
    display: flex;
    align-items: center;
    gap: 12px;
    flex-shrink: 0;
  }

  .category {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    padding: 4px 10px;
    background: #f3f4f6;
    border-radius: 12px;
    font-size: 12px;
    color: #6b7280;
  }

  .date {
    font-size: 12px;
    color: #9ca3af;
  }

  .item-preview {
    font-size: 14px;
    color: #6b7280;
    line-height: 1.6;
    margin-bottom: 12px;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }

  .item-preview :deep(p) {
    margin: 0;
  }

  .item-preview :deep(ul),
  .item-preview :deep(ol) {
    padding-left: 20px;
    margin: 0;
  }

  .item-preview :deep(li) {
    list-style-position: inside;
  }

  .item-preview :deep(code) {
    background: #f3f4f6;
    padding: 2px 4px;
    border-radius: 4px;
    font-size: 13px;
  }

  .item-preview :deep(strong) {
    color: #374151;
    font-weight: 600;
  }

  .item-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .item-actions {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .item-actions .star-btn {
    width: 28px;
    height: 28px;
    background: #f3f4f6;
    color: #6b7280;
  }

  .item-actions .star-btn.active {
    color: #fbbf24;
  }

  .empty-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 80px 20px;
    text-align: center;
    background: white;
    border-radius: 16px;
    border: 1px solid #e5e7eb;
    height: 70vh;
  }

  .empty-icon {
    width: 80px;
    height: 80px;
    background: linear-gradient(135deg, #E8EDFA 0%, #D4DFF5 100%);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 36px;
    color: var(--primary-color);
    margin-bottom: 24px;
  }

  .empty-state h3 {
    font-size: 18px;
    font-weight: 600;
    color: #1f2937;
    margin-bottom: 8px;
  }

  .empty-state p {
    font-size: 14px;
    color: #6b7280;
    margin-bottom: 24px;
  }

  .loading-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 80px 20px;
  }

  .loading-icon {
    font-size: 48px;
    color: var(--primary-color);
    animation: spin 1s linear infinite;
    margin-bottom: 16px;
  }

  @keyframes spin {
    from {
      transform: rotate(0deg);
    }

    to {
      transform: rotate(360deg);
    }
  }

  .loading-state p {
    font-size: 14px;
    color: #6b7280;
  }

  @media (max-width: 768px) {
    .toolbar {
      flex-direction: column;
      align-items: stretch;
    }

    .search-box {
      max-width: none;
    }

    .filters {
      justify-content: space-between;
    }

    .notes-grid {
      grid-template-columns: 1fr;
    }

    .item-header {
      flex-direction: column;
      gap: 8px;
    }

    .item-meta {
      flex-wrap: wrap;
    }
  }
</style>