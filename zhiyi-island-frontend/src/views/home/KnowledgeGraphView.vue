<!--
  知识图谱页面
  可视化展示知识点之间的关联关系
-->
<script setup>
  import { ref, computed, onMounted } from 'vue'
  import { useRouter } from 'vue-router'
  import { Icon } from '@iconify/vue'
  import { useAiStore } from '@/stores/ai'
  import { useNoteStore } from '@/stores/note'

  const router = useRouter()
  const aiStore = useAiStore()
  const noteStore = useNoteStore()

  /** 图谱节点数据 */
  const nodes = ref([])
  /** 图谱边数据 */
  const edges = ref([])
  /** 当前选中的节点 */
  const selectedNode = ref(null)
  /** 视图模式：graph-图谱，tree-树形 */
  const viewMode = ref('graph')
  /** 缩放比例 */
  const scale = ref(1)
  /** X轴平移 */
  const translateX = ref(0)
  /** Y轴平移 */
  const translateY = ref(0)
  /** 是否正在拖拽 */
  const isDragging = ref(false)
  /** 拖拽起始X坐标 */
  const dragStartX = ref(0)
  /** 拖拽起始Y坐标 */
  const dragStartY = ref(0)
  /** 加载状态 */
  const loading = ref(false)

  const minScale = 0.5
  const maxScale = 2

  const viewBox = computed(() => {
    const width = 1000
    const height = 700
    const scaledWidth = width / scale.value
    const scaledHeight = height / scale.value
    const x = (width - scaledWidth) / 2 - translateX.value
    const y = (height - scaledHeight) / 2 - translateY.value
    return `${x} ${y} ${scaledWidth} ${scaledHeight}`
  })

  const treeNodes = computed(() => {
    const levelGroups = {}
    nodes.value.forEach((node) => {
      if (!levelGroups[node.level]) {
        levelGroups[node.level] = []
      }
      levelGroups[node.level].push(node)
    })

    const treeData = []
    Object.keys(levelGroups)
      .sort((a, b) => a - b)
      .forEach((level, levelIndex) => {
        const group = levelGroups[level]
        const y = 120 + levelIndex * 180
        group.forEach((node, nodeIndex) => {
          const totalWidth = group.length * 180
          const startX = (1000 - totalWidth) / 2
          treeData.push({
            ...node,
            treeX: startX + nodeIndex * 180 + 90,
            treeY: y,
          })
        })
      })
    return treeData
  })

  const selectNode = (node) => {
    selectedNode.value = node
  }

  const goBack = () => {
    router.push('/home')
  }

  const zoomIn = () => {
    if (scale.value < maxScale) {
      scale.value = Math.min(scale.value + 0.2, maxScale)
    }
  }

  const zoomOut = () => {
    if (scale.value > minScale) {
      scale.value = Math.max(scale.value - 0.2, minScale)
    }
  }

  const resetView = () => {
    scale.value = 1
    translateX.value = 0
    translateY.value = 0
  }

  const handleMouseDown = (e) => {
    if (e.target.tagName === 'circle' || e.target.tagName === 'text') return
    isDragging.value = true
    dragStartX.value = e.clientX - translateX.value
    dragStartY.value = e.clientY - translateY.value
  }

  const handleMouseMove = (e) => {
    if (!isDragging.value) return
    translateX.value = e.clientX - dragStartX.value
    translateY.value = e.clientY - dragStartY.value
  }

  const handleMouseUp = () => {
    isDragging.value = false
  }

  const handleWheel = (e) => {
    e.preventDefault()
    if (e.deltaY < 0) {
      zoomIn()
    } else {
      zoomOut()
    }
  }

  const loadKnowledgeGraph = async () => {
    loading.value = true
    try {
      await noteStore.fetchNotes()
      const notes = noteStore.notes

      if (notes.length === 0) {
        nodes.value = []
        edges.value = []
        return
      }

      const content = notes
        .map((n) => n.content)
        .filter(Boolean)
        .join('\n')

      if (content) {
        const result = await aiStore.generateKnowledgeGraph(content)
        if (result && result.nodes) {
          const centerX = 500
          const centerY = 350
          const nodeCount = result.nodes.length

          const colors = [
            '#122E8A',
            '#1a4a9a',
            '#f59e0b',
            '#10b981',
            '#ef4444',
            '#ec4899',
            '#8b5cf6',
            '#06b6d4',
          ]

          nodes.value = result.nodes.map((node, index) => {
            const isCore = index === 0
            const baseRadius = isCore ? 0 : 160 + (index % 3) * 80
            const angle = (index / (nodeCount - 1 || 1)) * Math.PI * 2 - Math.PI / 2
            const jitterX = (Math.random() - 0.5) * 60
            const jitterY = (Math.random() - 0.5) * 60

            return {
              id: index + 1,
              label: node.label || node.name,
              x: isCore ? centerX : centerX + Math.cos(angle) * baseRadius + jitterX,
              y: isCore ? centerY : centerY + Math.sin(angle) * baseRadius + jitterY,
              color: node.color || colors[index % colors.length],
              size: isCore ? 65 : 50 + Math.random() * 15,
              level: node.level || (isCore ? 0 : 1),
              isCore
            }
          })

          edges.value = (result.edges || []).map(edge => ({
            ...edge,
            curved: Math.random() > 0.5
          }))
        }
      }
    } catch (e) {
      console.error('加载知识图谱失败:', e)
    } finally {
      loading.value = false
    }
  }

  onMounted(() => {
    loadKnowledgeGraph()
  })
</script>

<template>
  <div class="knowledge-graph-page">
    <div class="page-header">
      <button class="back-btn" @click="goBack">
        <Icon icon="ri:arrow-left-line" />
        返回
      </button>
      <div class="header-actions">
        <div class="view-toggle">
          <button class="toggle-btn" :class="{ active: viewMode === 'graph' }" @click="viewMode = 'graph'">
            <Icon icon="ri:bubble-chart-line" />
            图谱
          </button>
          <button class="toggle-btn" :class="{ active: viewMode === 'tree' }" @click="viewMode = 'tree'">
            <Icon icon="ri:organization-chart" />
            树状
          </button>
        </div>
        <button class="action-btn">
          <Icon icon="ri:download-line" />
          导出
        </button>
      </div>
    </div>

    <div class="content-layout">
      <div class="graph-container">
        <div v-if="loading" class="loading-overlay">
          <Icon icon="ri:loader-4-line" class="spinning" />
          <span>正在生成知识图谱...</span>
        </div>
        <div v-else-if="nodes.length === 0" class="empty-state">
          <Icon icon="ri:bubble-chart-line" class="empty-icon" />
          <p>暂无知识图谱数据</p>
          <p class="empty-hint">添加笔记后系统将自动生成知识图谱</p>
        </div>
        <svg v-else class="graph-svg" :viewBox="viewBox" @mousedown="handleMouseDown" @mousemove="handleMouseMove"
          @mouseup="handleMouseUp" @mouseleave="handleMouseUp" @wheel="handleWheel">
          <defs>
            <marker id="arrowhead" markerWidth="8" markerHeight="6" refX="7" refY="3" orient="auto">
              <polygon points="0 0, 8 3, 0 6" fill="#c4c8ce" />
            </marker>
            <filter id="glow">
              <feGaussianBlur stdDeviation="2" result="coloredBlur" />
              <feMerge>
                <feMergeNode in="coloredBlur" />
                <feMergeNode in="SourceGraphic" />
              </feMerge>
            </filter>
          </defs>

          <template v-if="viewMode === 'graph'">
            <g class="edges">
              <line v-for="edge in edges" :key="`${edge.from}-${edge.to}`"
                :x1="nodes.find((n) => n.id === edge.from)?.x" :y1="nodes.find((n) => n.id === edge.from)?.y"
                :x2="nodes.find((n) => n.id === edge.to)?.x" :y2="nodes.find((n) => n.id === edge.to)?.y"
                stroke="#d4d8de" stroke-width="1.5" stroke-opacity="0.6" marker-end="url(#arrowhead)" />
            </g>

            <g class="nodes">
              <g v-for="node in nodes" :key="node.id" class="node"
                :class="{ selected: selectedNode?.id === node.id, core: node.isCore }" @click.stop="selectNode(node)">
                <circle :cx="node.x" :cy="node.y" :r="node.size / 2" :fill="node.color" class="node-circle" />
                <foreignObject :x="node.x - 60" :y="node.y + node.size / 2 + 8" width="120" height="50"
                  class="node-label-container">
                  <div xmlns="http://www.w3.org/1999/xhtml" class="node-label-text">
                    {{ node.label }}
                  </div>
                </foreignObject>
              </g>
            </g>
          </template>

          <template v-else>
            <g class="edges">
              <line v-for="edge in edges" :key="`tree-${edge.from}-${edge.to}`"
                :x1="treeNodes.find((n) => n.id === edge.from)?.treeX"
                :y1="treeNodes.find((n) => n.id === edge.from)?.treeY"
                :x2="treeNodes.find((n) => n.id === edge.to)?.treeX"
                :y2="treeNodes.find((n) => n.id === edge.to)?.treeY" stroke="#d4d8de" stroke-width="1.5"
                stroke-opacity="0.6" />
            </g>

            <g class="nodes">
              <g v-for="node in treeNodes" :key="`tree-${node.id}`" class="node"
                :class="{ selected: selectedNode?.id === node.id, core: node.isCore }" @click.stop="selectNode(node)">
                <circle :cx="node.treeX" :cy="node.treeY" :r="node.size / 2 || 25" :fill="node.color"
                  class="node-circle" />
                <foreignObject :x="node.treeX - 60" :y="node.treeY + (node.size / 2 || 25) + 8" width="120" height="50"
                  class="node-label-container">
                  <div xmlns="http://www.w3.org/1999/xhtml" class="node-label-text">
                    {{ node.label }}
                  </div>
                </foreignObject>
              </g>
            </g>
          </template>
        </svg>

        <div class="graph-controls">
          <button class="control-btn" @click="zoomIn" :disabled="scale >= maxScale">
            <Icon icon="ri:zoom-in-line" />
          </button>
          <button class="control-btn" @click="zoomOut" :disabled="scale <= minScale">
            <Icon icon="ri:zoom-out-line" />
          </button>
          <button class="control-btn" @click="resetView">
            <Icon icon="ri:fullscreen-line" />
          </button>
        </div>

        <div class="zoom-level">{{ Math.round(scale * 100) }}%</div>

        <div class="graph-legend">
          <div class="legend-item">
            <span class="legend-dot" style="background: #122E8A"></span>
            <span>核心概念</span>
          </div>
          <div class="legend-item">
            <span class="legend-dot" style="background: #1a4a9a"></span>
            <span>技术分支</span>
          </div>
          <div class="legend-item">
            <span class="legend-dot" style="background: #10b981"></span>
            <span>具体应用</span>
          </div>
        </div>
      </div>

      <div class="sidebar">
        <div class="sidebar-card" v-if="selectedNode">
          <h3 class="card-title">{{ selectedNode.label }}</h3>
          <div class="node-info">
            <div class="info-item">
              <span class="info-label">类型</span>
              <span class="info-value">核心概念</span>
            </div>
            <div class="info-item">
              <span class="info-label">关联节点</span>
              <span class="info-value">5 个</span>
            </div>
            <div class="info-item">
              <span class="info-label">相关笔记</span>
              <span class="info-value">12 条</span>
            </div>
            <div class="info-item">
              <span class="info-label">知识条目</span>
              <span class="info-value">28 条</span>
            </div>
          </div>
          <div class="node-actions">
            <button class="node-action-btn">
              <Icon icon="ri:eye-line" />
              查看详情
            </button>
            <button class="node-action-btn">
              <Icon icon="ri:add-line" />
              添加关联
            </button>
          </div>
        </div>

        <div class="sidebar-card empty" v-else>
          <Icon icon="ri:cursor-line" class="empty-icon" />
          <p>点击节点查看详情</p>
        </div>

        <div class="sidebar-card">
          <h4 class="card-title">统计</h4>
          <div class="stats-grid">
            <div class="stat-item">
              <span class="stat-value">{{ nodes.length }}</span>
              <span class="stat-label">节点数</span>
            </div>
            <div class="stat-item">
              <span class="stat-value">{{ edges.length }}</span>
              <span class="stat-label">连接数</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
  .knowledge-graph-page {
    height: calc(100vh - 108px);
    display: flex;
    flex-direction: column;
  }

  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
  }

  .back-btn {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 10px 16px;
    background: var(--card-bg, white);
    border: 1px solid var(--border-color, #e5e7eb);
    border-radius: 10px;
    color: var(--text-color, #374151);
    font-size: 14px;
    cursor: pointer;
    transition: all 0.2s;
  }

  .back-btn:hover {
    background: var(--bg-tertiary, #f9fafb);
    border-color: var(--primary-color);
    color: var(--primary-color);
  }

  .header-actions {
    display: flex;
    gap: 12px;
  }

  .view-toggle {
    display: flex;
    background: var(--card-bg, white);
    border-radius: 10px;
    padding: 4px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.04);
  }

  .toggle-btn {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 8px 14px;
    background: transparent;
    border: none;
    color: var(--text-secondary, #9ca3af);
    font-size: 13px;
    cursor: pointer;
    border-radius: 8px;
    transition: all 0.2s;
  }

  .toggle-btn.active {
    background: linear-gradient(135deg, var(--primary-color) 0%, var(--secondary-color) 100%);
    color: white;
  }

  .action-btn {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 10px 18px;
    background: var(--card-bg, white);
    border: 1px solid var(--border-color, #e5e7eb);
    border-radius: 10px;
    color: var(--text-color, #374151);
    font-size: 14px;
    cursor: pointer;
    transition: all 0.2s;
  }

  .action-btn:hover {
    background: var(--bg-tertiary, #f9fafb);
  }

  .content-layout {
    flex: 1;
    display: grid;
    grid-template-columns: 1fr 280px;
    gap: 20px;
    overflow: hidden;
  }

  .graph-container {
    background: var(--card-bg, white);
    border-radius: 16px;
    position: relative;
    overflow: hidden;
    cursor: grab;
  }

  .graph-container:active {
    cursor: grabbing;
  }

  .graph-svg {
    width: 100%;
    height: 100%;
  }

  .edges {
    opacity: 0.7;
  }

  .node {
    cursor: pointer;
    transition: transform 0.2s;
    transform-origin: center;
    transform-box: fill-box;
  }

  .node:hover {
    transform: scale(1.1);
  }

  .node-circle {
    transition: all 0.3s ease;
    filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.15));
  }

  .node:hover .node-circle {
    filter: drop-shadow(0 4px 8px rgba(0, 0, 0, 0.25));
  }

  .node.core .node-circle {
    filter: drop-shadow(0 4px 12px rgba(18, 46, 138, 0.4));
  }

  .node.selected .node-circle {
    stroke: #1f2937;
    stroke-width: 3;
  }

  .node-label {
    pointer-events: none;
    transition: all 0.2s ease;
  }

  .node:hover .node-label {
    fill: #1f2937;
    font-weight: 600;
  }

  .node-label-container {
    pointer-events: none;
    overflow: visible;
  }

  .node-label-text {
    text-align: center;
    font-size: 12px;
    font-weight: 500;
    color: #4b5563;
    line-height: 1.4;
    word-wrap: break-word;
    overflow-wrap: break-word;
    white-space: normal;
    max-width: 120px;
    margin: 0 auto;
    transition: all 0.2s ease;
  }

  .node:hover .node-label-text {
    color: #1f2937;
    font-weight: 600;
  }

  .graph-controls {
    position: absolute;
    bottom: 20px;
    left: 20px;
    display: flex;
    flex-direction: column;
    gap: 8px;
  }

  .control-btn {
    width: 40px;
    height: 40px;
    background: var(--card-bg, white);
    border: 1px solid var(--border-color, #e5e7eb);
    border-radius: 10px;
    color: var(--text-secondary, #6b7280);
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 18px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
    transition: all 0.2s;
  }

  .control-btn:hover:not(:disabled) {
    background: linear-gradient(135deg, var(--primary-color) 0%, var(--secondary-color) 100%);
    border-color: transparent;
    color: white;
  }

  .control-btn:disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }

  .zoom-level {
    position: absolute;
    bottom: 20px;
    left: 70px;
    background: var(--card-bg, white);
    border-radius: 8px;
    padding: 8px 12px;
    font-size: 12px;
    color: var(--text-secondary, #6b7280);
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  }

  .graph-legend {
    position: absolute;
    top: 20px;
    left: 20px;
    background: var(--card-bg, white);
    border-radius: 10px;
    padding: 12px 16px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  }

  .legend-item {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 12px;
    color: var(--text-secondary, #6b7280);
    margin-bottom: 6px;
  }

  .legend-item:last-child {
    margin-bottom: 0;
  }

  .legend-dot {
    width: 10px;
    height: 10px;
    border-radius: 50%;
  }

  .sidebar {
    display: flex;
    flex-direction: column;
    gap: 16px;
    overflow-y: auto;
  }

  .sidebar-card {
    background: var(--card-bg, white);
    border-radius: 16px;
    padding: 20px;
  }

  .sidebar-card.empty {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 40px 20px;
    color: var(--text-secondary, #9ca3af);
  }

  .empty-icon {
    font-size: 32px;
    margin-bottom: 8px;
    opacity: 0.5;
  }

  .card-title {
    font-size: 16px;
    font-weight: 600;
    color: var(--text-color, #1f2937);
    margin-bottom: 16px;
  }

  .node-info {
    display: flex;
    flex-direction: column;
    gap: 12px;
    margin-bottom: 16px;
  }

  .info-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .info-label {
    font-size: 13px;
    color: var(--text-secondary, #9ca3af);
  }

  .info-value {
    font-size: 14px;
    font-weight: 500;
    color: var(--text-color, #374151);
  }

  .node-actions {
    display: flex;
    flex-direction: column;
    gap: 8px;
  }

  .node-action-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    padding: 10px;
    background: var(--bg-tertiary, #f9fafb);
    border: none;
    border-radius: 8px;
    color: var(--text-color, #374151);
    font-size: 13px;
    cursor: pointer;
    transition: all 0.2s;
  }

  .node-action-btn:hover {
    background: linear-gradient(135deg, var(--primary-color) 0%, var(--secondary-color) 100%);
    color: white;
  }

  .stats-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }

  .stat-item {
    background: var(--bg-tertiary, #f9fafb);
    border-radius: 10px;
    padding: 12px;
    text-align: center;
  }

  .stat-value {
    display: block;
    font-size: 20px;
    font-weight: 600;
    color: var(--primary-color);
  }

  .stat-label {
    font-size: 12px;
    color: var(--text-secondary, #9ca3af);
  }

  .loading-overlay {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    height: 100%;
    gap: 16px;
    color: var(--text-secondary, #6b7280);
  }

  .spinning {
    animation: spin 1s linear infinite;
    font-size: 32px;
    color: var(--primary-color);
  }

  @keyframes spin {
    from {
      transform: rotate(0deg);
    }

    to {
      transform: rotate(360deg);
    }
  }

  .empty-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    height: 100%;
    color: var(--text-secondary, #9ca3af);
  }

  .empty-state .empty-icon {
    font-size: 64px;
    margin-bottom: 16px;
    opacity: 0.5;
  }

  .empty-state p {
    font-size: 16px;
    margin-bottom: 8px;
  }

  .empty-hint {
    font-size: 14px;
    opacity: 0.7;
  }
</style>