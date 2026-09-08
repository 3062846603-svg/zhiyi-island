<!--
  知识库列表页面
  展示用户的知识库列表，支持分类筛选、搜索和操作
-->
<script setup>
  import { ref, computed, onMounted, onUnmounted } from 'vue'
  import { useRouter } from 'vue-router'
  import { Icon } from '@iconify/vue'
  import { ElMessageBox } from 'element-plus'
  import { useKnowledgeStore } from '@/stores/knowledge'
  import { useToastStore } from '@/stores/toast'
  import Pagination from '@/components/Pagination.vue'

  const router = useRouter()
  const knowledgeStore = useKnowledgeStore()
  const toast = useToastStore()

  const viewMode = ref('grid')
  const searchQuery = ref('')
  const activeCategory = ref('')
  const activeMenu = ref(null)
  const showCategoryDropdown = ref(false)
  const categoryFilterRef = ref(null)
  const currentPage = ref(1)
  const pageSize = ref(12)

  const categoryStyles = {
    '技术': { icon: 'ri:code-s-slash-line', color: '#22C55E', bg: '#F0FDF4', shadow: 'rgba(34, 197, 94, 0.2)' },
    '学习': { icon: 'ri:book-open-line', color: '#4F7CFF', bg: '#EEF2FF', shadow: 'rgba(79, 124, 255, 0.2)' },
    '工作': { icon: 'ri:briefcase-line', color: '#6366F1', bg: '#EEF2FF', shadow: 'rgba(99, 102, 241, 0.2)' },
    '生活': { icon: 'ri:heart-line', color: '#EC4899', bg: '#FDF2F8', shadow: 'rgba(236, 72, 153, 0.2)' },
    '健康': { icon: 'ri:heart-pulse-line', color: '#10B981', bg: '#ECFDF5', shadow: 'rgba(16, 185, 129, 0.2)' },
    '财经': { icon: 'ri:money-cny-circle-line', color: '#F59E0B', bg: '#FFFBEB', shadow: 'rgba(245, 158, 11, 0.2)' },
    '文化': { icon: 'ri:palette-line', color: '#8B5CF6', bg: '#F5F3FF', shadow: 'rgba(139, 92, 246, 0.2)' },
    '其他': { icon: 'ri:file-text-line', color: '#6B7280', bg: '#F9FAFB', shadow: 'rgba(107, 114, 128, 0.2)' }
  }

  const getCategoryStyle = (category) => {
    return categoryStyles[category] || categoryStyles['其他']
  }

  const categories = computed(() => {
    const cats = new Set()
    knowledgeStore.knowledgeList.forEach((item) => {
      if (item.category) cats.add(item.category)
    })
    return ['全部', ...Array.from(cats).sort()]
  })

  const filteredItems = computed(() => {
    let result = knowledgeStore.knowledgeList

    if (activeCategory.value && activeCategory.value !== '全部') {
      result = result.filter((item) => item.category === activeCategory.value)
    }

    if (searchQuery.value.trim()) {
      const query = searchQuery.value.toLowerCase()
      result = result.filter(
        (item) =>
          item.title?.toLowerCase().includes(query) ||
          item.description?.toLowerCase().includes(query),
      )
    }

    return result
  })

  const totalPages = computed(() => Math.ceil(filteredItems.value.length / pageSize.value))

  const paginatedItems = computed(() => {
    const start = (currentPage.value - 1) * pageSize.value
    const end = start + pageSize.value
    return filteredItems.value.slice(start, end)
  })

  const handlePageChange = (page) => {
    currentPage.value = page
  }

  const selectCategory = (cat) => {
    activeCategory.value = cat
    showCategoryDropdown.value = false
    currentPage.value = 1
  }

  const goToDetail = (id) => {
    router.push(`/knowledge/${id}`)
  }

  const goToKnowledgeGraph = () => {
    router.push('/home/knowledge-graph')
  }

  const toggleMenu = (id, event) => {
    event.stopPropagation()
    activeMenu.value = activeMenu.value === id ? null : id
  }

  const closeMenu = () => {
    activeMenu.value = null
  }

  const handleEdit = (item, event) => {
    event.stopPropagation()
    activeMenu.value = null
    router.push(`/knowledge/${item.id}/edit`)
  }

  const handleDelete = async (item, event) => {
    event.stopPropagation()
    activeMenu.value = null
    try {
      await ElMessageBox.confirm(`确定要删除 "${item.title}" 吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      })
      await knowledgeStore.deleteKnowledge(item.id)
      toast.success('删除成功')
    } catch (e) {
      if (e !== 'cancel') {
        toast.error('删除失败: ' + (e.message || '未知错误'))
      }
    }
  }

  const handleClickOutside = (event) => {
    if (categoryFilterRef.value && !categoryFilterRef.value.contains(event.target)) {
      showCategoryDropdown.value = false
    }
  }

  onMounted(() => {
    knowledgeStore.fetchKnowledgeList()
    document.addEventListener('click', handleClickOutside)
  })

  onUnmounted(() => {
    document.removeEventListener('click', handleClickOutside)
  })
</script>

<template>
  <div class="knowledge-page" @click="closeMenu">
    <div class="page-toolbar">
      <div class="toolbar-left">
        <div class="view-toggle">
          <button class="toggle-btn" :class="{ active: viewMode === 'grid' }" @click="viewMode = 'grid'">
            <Icon icon="ri:grid-line" />
          </button>
          <button class="toggle-btn" :class="{ active: viewMode === 'list' }" @click="viewMode = 'list'">
            <Icon icon="ri:list-unordered" />
          </button>
        </div>
      </div>
      <div class="toolbar-right">
        <button class="btn btn-primary" @click="goToKnowledgeGraph">
          <Icon icon="ri:bubble-chart-line" />
          <span>知识图谱</span>
        </button>
      </div>
    </div>

    <div class="filter-section">
      <div class="filter-left">
        <div class="category-filter" ref="categoryFilterRef">
          <div class="select-trigger" @click.stop="showCategoryDropdown = !showCategoryDropdown">
            <Icon
              :icon="activeCategory && activeCategory !== '全部' ? getCategoryStyle(activeCategory).icon : 'ri:apps-line'"
              class="trigger-icon" />
            <span class="select-text">{{ activeCategory || '全部分类' }}</span>
            <Icon icon="ri:arrow-down-s-line" class="arrow-icon" :class="{ rotated: showCategoryDropdown }" />
          </div>
          <Transition name="dropdown">
            <div class="select-dropdown" v-if="showCategoryDropdown">
              <div class="dropdown-item" :class="{ active: !activeCategory || activeCategory === '全部' }"
                @click.stop="selectCategory('')">
                <Icon icon="ri:apps-line" class="item-icon" />
                <span>全部分类</span>
              </div>
              <div v-for="cat in categories.filter(c => c !== '全部')" :key="cat" class="dropdown-item"
                :class="{ active: activeCategory === cat }" @click.stop="selectCategory(cat)">
                <Icon :icon="getCategoryStyle(cat).icon" class="item-icon"
                  :style="{ color: getCategoryStyle(cat).color }" />
                <span>{{ cat }}</span>
              </div>
            </div>
          </Transition>
        </div>
      </div>
      <div class="search-box">
        <Icon icon="ri:search-line" class="search-icon" />
        <input v-model="searchQuery" type="text" placeholder="搜索知识库..." class="search-input" />
      </div>
    </div>

    <div class="knowledge-grid" :class="{ 'list-view': viewMode === 'list' }" v-if="filteredItems.length > 0">
      <div v-for="item in paginatedItems" :key="item.id" class="knowledge-card"
        :style="{
          '--hover-color': getCategoryStyle(item.category).color,
          '--shadow-color': getCategoryStyle(item.category).shadow
        }"
        @click="goToDetail(item.id)">
        <div class="card-icon"
          :style="{ background: getCategoryStyle(item.category).bg, color: getCategoryStyle(item.category).color }">
          <Icon :icon="getCategoryStyle(item.category).icon" />
        </div>
        <div class="card-content">
          <div class="card-header">
            <h4 class="card-title">{{ item.title }}</h4>
            <span class="card-category"
              :style="{ background: getCategoryStyle(item.category).bg, color: getCategoryStyle(item.category).color }">
              {{ item.category || '未分类' }}
            </span>
          </div>
          <p class="card-desc">{{ item.description || '暂无描述' }}</p>
          <div class="card-footer">
            <span class="item-count">
              <Icon icon="ri:file-text-line" />
              {{ item.itemCount || 0 }} 条知识
            </span>
          </div>
        </div>
      </div>
    </div>

    <div class="empty-state" v-else>
      <Icon icon="ri:brain-line" class="empty-icon" />
      <p>没有找到相关知识库</p>
    </div>

    <Pagination v-if="filteredItems.length > 0" :current-page="currentPage" :total-pages="totalPages"
      :total-items="filteredItems.length" :page-size="pageSize" @page-change="handlePageChange" />
  </div>
</template>

<style scoped>
  .knowledge-page {
    width: 100%;
  }

  .page-toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;
  }

  .toolbar-left,
  .toolbar-right {
    display: flex;
    align-items: center;
    gap: 12px;
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

  .filter-section {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;
    flex-wrap: wrap;
    gap: 16px;
  }

  .filter-left {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  .category-filter {
    position: relative;
    min-width: 140px;
  }

  .select-trigger {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 10px 14px;
    background: white;
    border: 1px solid #e5e7eb;
    border-radius: 10px;
    cursor: pointer;
    transition: all 0.2s;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  }

  .select-trigger:hover {
    border-color: #d1d5db;
    box-shadow: 0 2px 6px rgba(0, 0, 0, 0.08);
  }

  .trigger-icon {
    font-size: 18px;
    color: #6b7280;
  }

  .select-text {
    font-size: 14px;
    color: #374151;
    flex: 1;
  }

  .arrow-icon {
    font-size: 18px;
    color: #9ca3af;
    transition: transform 0.2s;
  }

  .arrow-icon.rotated {
    transform: rotate(180deg);
  }

  .select-dropdown {
    position: absolute;
    top: calc(100% + 4px);
    left: 0;
    min-width: 180px;
    background: white;
    border: 1px solid #e5e7eb;
    border-radius: 10px;
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
    z-index: 100;
    overflow: hidden;
  }

  .dropdown-item {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 10px 14px;
    cursor: pointer;
    transition: all 0.15s;
    font-size: 14px;
    color: #374151;
  }

  .dropdown-item:hover {
    background: #f9fafb;
  }

  .dropdown-item.active {
    background: #EEF2FF;
    color: var(--primary-color);
  }

  .item-icon {
    font-size: 18px;
    color: #6b7280;
  }

  .search-box {
    display: flex;
    align-items: center;
    background: var(--card-bg, white);
    border: 1px solid var(--border-color, #e5e7eb);
    border-radius: 10px;
    padding: 0 14px;
    width: 240px;
  }

  .search-icon {
    color: var(--text-secondary, #9ca3af);
    font-size: 18px;
  }

  .search-input {
    flex: 1;
    border: none;
    outline: none;
    padding: 10px 12px;
    font-size: 14px;
    color: var(--text-color, #374151);
    background: transparent;
  }

  .search-input::placeholder {
    color: var(--text-secondary, #9ca3af);
  }

  .knowledge-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
    gap: 20px;
  }

  .knowledge-grid.list-view {
    grid-template-columns: 1fr;
  }

  .knowledge-card {
    background: var(--card-bg, white);
    border-radius: 14px;
    padding: 16px;
    display: flex;
    gap: 14px;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
    border: 1px solid #e5e7eb;
    transition:
      transform 0.2s,
      box-shadow 0.2s;
    cursor: pointer;
  }

  .knowledge-card:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 24px var(--shadow-color, rgba(0, 0, 0, 0.12));
    border-color: var(--hover-color);
  }

  .card-icon {
    width: 48px;
    height: 48px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 22px;
    flex-shrink: 0;
  }

  .card-content {
    flex: 1;
    min-width: 0;
    display: flex;
    flex-direction: column;
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 4px;
  }

  .card-title {
    font-size: var(--font-size-card-title);
    font-weight: 600;
    color: var(--text-color, #1f2937);
  }

  .card-category {
    font-size: var(--font-size-xs);
    padding: 3px 10px;
    border-radius: 20px;
    white-space: nowrap;
  }

  .card-desc {
    font-size: var(--font-size-small);
    color: var(--text-secondary, #9ca3af);
    margin-bottom: 10px;
    line-height: 18px;
    height: 36px;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }

  .card-footer {
    display: flex;
    justify-content: flex-end;
    align-items: center;
  }

  .item-count {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: var(--font-size-small);
    color: var(--text-secondary, #6b7280);
  }

  .list-view .knowledge-card {
    align-items: center;
  }

  .empty-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 80px 20px;
    background: var(--card-bg, white);
    border-radius: 16px;
    border: 1px solid #e5e7eb;
    height: 60vh;
  }

  .empty-icon {
    font-size: 64px;
    color: var(--border-color, #d1d5db);
    margin-bottom: 16px;
  }

  .empty-state p {
    font-size: 16px;
    color: var(--text-secondary, #9ca3af);
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
</style>