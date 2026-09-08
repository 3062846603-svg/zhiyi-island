<script setup>
/**
 * 分页组件
 * 提供通用的分页功能，支持页码显示、跳转和信息展示
 */
import { Icon } from '@iconify/vue'

/**
 * 组件属性
 * @property {number} currentPage - 当前页码（必填）
 * @property {number} totalPages - 总页数（必填）
 * @property {number} totalItems - 总条目数
 * @property {number} pageSize - 每页条目数
 */
const props = defineProps({
  currentPage: {
    type: Number,
    required: true
  },
  totalPages: {
    type: Number,
    required: true
  },
  totalItems: {
    type: Number,
    default: 0
  },
  pageSize: {
    type: Number,
    default: 10
  }
})

const emit = defineEmits(['update:currentPage', 'pageChange'])

/**
 * 跳转到指定页
 * @param {number} page - 目标页码
 */
const goToPage = (page) => {
  if (page >= 1 && page <= props.totalPages && page !== props.currentPage) {
    emit('update:currentPage', page)
    emit('pageChange', page)
  }
}

/**
 * 获取要显示的页码数组
 * 根据当前页和总页数智能计算显示哪些页码
 * 当页数较多时显示省略号
 * @returns {Array<number|string>} 页码数组，可能包含省略号字符串
 */
const getPageNumbers = () => {
  const pages = []
  const { currentPage, totalPages } = props
  
  if (totalPages <= 7) {
    for (let i = 1; i <= totalPages; i++) {
      pages.push(i)
    }
  } else {
    if (currentPage <= 3) {
      for (let i = 1; i <= 4; i++) pages.push(i)
      pages.push('...')
      pages.push(totalPages)
    } else if (currentPage >= totalPages - 2) {
      pages.push(1)
      pages.push('...')
      for (let i = totalPages - 3; i <= totalPages; i++) pages.push(i)
    } else {
      pages.push(1)
      pages.push('...')
      for (let i = currentPage - 1; i <= currentPage + 1; i++) pages.push(i)
      pages.push('...')
      pages.push(totalPages)
    }
  }
  
  return pages
}
</script>

<template>
  <div class="pagination-container" v-if="totalPages > 1">
    <div class="pagination-info">
      共 {{ totalItems }} 条，每页 {{ pageSize }} 条
    </div>
    <div class="pagination">
      <button 
        class="page-btn prev-btn" 
        :disabled="currentPage === 1"
        @click="goToPage(currentPage - 1)"
      >
        <Icon icon="ri:arrow-left-s-line" />
      </button>
      
      <button
        v-for="(page, index) in getPageNumbers()"
        :key="index"
        class="page-btn"
        :class="{ active: page === currentPage, ellipsis: page === '...' }"
        :disabled="page === '...'"
        @click="page !== '...' && goToPage(page)"
      >
        {{ page }}
      </button>
      
      <button 
        class="page-btn next-btn" 
        :disabled="currentPage === totalPages"
        @click="goToPage(currentPage + 1)"
      >
        <Icon icon="ri:arrow-right-s-line" />
      </button>
    </div>
  </div>
</template>

<style scoped>
.pagination-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 0;
  margin-top: 20px;
}

.pagination-info {
  font-size: 13px;
  color: #6b7280;
}

.pagination {
  display: flex;
  align-items: center;
  gap: 4px;
}

.page-btn {
  min-width: 32px;
  height: 32px;
  padding: 0 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  font-size: 13px;
  color: #374151;
  cursor: pointer;
  transition: all 0.2s;
}

.page-btn:hover:not(:disabled):not(.active) {
  background: #f3f4f6;
  border-color: #d1d5db;
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-btn.active {
  background: #122E8A;
  border-color: #122E8A;
  color: white;
}

.page-btn.ellipsis {
  border: none;
  background: transparent;
  cursor: default;
}

.page-btn.ellipsis:hover {
  background: transparent;
}

.prev-btn,
.next-btn {
  font-size: 16px;
}
</style>
