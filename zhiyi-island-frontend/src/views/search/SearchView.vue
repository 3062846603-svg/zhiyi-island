<script setup>
  import { ref, computed, onMounted, onUnmounted, onActivated, onDeactivated } from 'vue'
  import { useRouter } from 'vue-router'
  import { Icon } from '@iconify/vue'
  import { searchApi } from '@/api/search'
  import { knowledgeApi } from '@/api/knowledge'
  import { useToastStore } from '@/stores/toast'

  const router = useRouter()
  const toast = useToastStore()

  const activeMode = ref('search')
  const searchQuery = ref('')
  const chatQuestion = ref('')
  const isSearching = ref(false)
  const isChatting = ref(false)
  const searchHistory = ref([])
  const searchResults = ref([])
  const expandedTerms = ref([])
  const chatAnswer = ref('')
  const displayedAnswer = ref('')
  const relatedContents = ref([])
  const intentInfo = ref('')
  const keywordsInfo = ref([])
  const showAnswerPanel = ref(false)
  const knowledgeBases = ref([])
  const suggestedQuestions = ref([])
  let typingTimer = null
  let isComponentActive = true

  const parseMarkdown = (text) => {
    if (!text) return ''
    let result = text
    result = result.replace(/\*\*\*(.+?)\*\*\*/g, '<strong><em>$1</em></strong>')
    result = result.replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
    result = result.replace(/\*(.+?)\*/g, '<em>$1</em>')
    result = result.replace(/`(.+?)`/g, '<code class="inline-code">$1</code>')
    result = result.replace(/^[-•]\s+(.+)$/gm, '<li>$1</li>')
    result = result.replace(/(<li>.*<\/li>)/s, '<ul>$1</ul>')
    result = result.replace(/\n/g, '<br>')
    return result
  }

  const defaultQuestions = [
    '什么是梯度下降算法？',
    '如何提高模型泛化能力？',
    '注意力机制与RNN的区别',
    '常见的认知偏误有哪些？',
  ]

  const modeTabs = [
    { key: 'search', label: 'AI智能搜索', icon: 'ri:search-eye-line' },
    { key: 'chat', label: 'AI语义问答', icon: 'ri:chat-ai-line' },
  ]

  const showSearchHistory = computed(() => {
    if (activeMode.value !== 'search') return false
    const filteredHistory = searchHistory.value.filter(item => item.searchType !== 'semantic')
    return filteredHistory.length > 0
  })

  const filteredSearchHistory = computed(() => {
    return searchHistory.value.filter(item => {
      if (activeMode.value === 'search') {
        return item.searchType !== 'semantic'
      }
      return []
    })
  })

  const showSuggestedQuestions = computed(() => {
    return !showAnswerPanel.value && !isChatting.value
  })

  const loadHistory = async () => {
    try {
      const result = await searchApi.getHistory()
      if (result.code === 200) {
        searchHistory.value = result.data
      }
    } catch (e) {
      console.error('获取搜索历史失败:', e)
    }
  }

  const loadKnowledgeBases = async () => {
    try {
      const result = await knowledgeApi.list()
      if (result.code === 200) {
        knowledgeBases.value = result.data || []
        generateSuggestedQuestions()
      }
    } catch (e) {
      console.error('获取知识库失败:', e)
      suggestedQuestions.value = defaultQuestions
    }
  }

  const generateSuggestedQuestions = () => {
    if (knowledgeBases.value.length === 0) {
      suggestedQuestions.value = defaultQuestions
      return
    }

    const questions = []
    const titles = knowledgeBases.value.slice(0, 4).map((k) => k.title)

    titles.forEach((title, index) => {
      if (index < 2) {
        questions.push(`${title}的核心概念是什么？`)
      } else {
        questions.push(`如何理解${title}？`)
      }
    })

    suggestedQuestions.value = questions.length > 0 ? questions : defaultQuestions
  }

  const handleIntelligentSearch = async () => {
    if (!searchQuery.value.trim()) {
      toast.warning('请输入搜索关键词')
      return
    }

    if (isSearching.value) {
      toast.info('正在搜索中，请稍候...')
      return
    }

    isSearching.value = true
    expandedTerms.value = []
    searchResults.value = []

    sessionStorage.setItem('intelligentSearchState', JSON.stringify({
      query: searchQuery.value,
      isSearching: true,
      activeMode: 'search',
      timestamp: Date.now()
    }))

    try {
      const result = await searchApi.intelligentSearch(searchQuery.value)
      if (result.code === 200) {
        const data = result.data
        expandedTerms.value = data.expandedTerms || []
        searchResults.value = (data.results || []).map((item) => ({
          id: item.id,
          title: item.title,
          content:
            item.content?.substring(0, 150) +
            (item.content && item.content.length > 150 ? '...' : ''),
          source: item.source || item.bookName || '未知来源',
          type: item.type || '笔记',
          icon: item.type === 'knowledge' ? 'ri:brain-line' : 'ri:book-3-line',
          color: item.type === 'knowledge' ? '#122E8A' : '#122E8A',
        }))
        isSearching.value = false

        sessionStorage.setItem('intelligentSearchState', JSON.stringify({
          query: searchQuery.value,
          expandedTerms: expandedTerms.value,
          searchResults: searchResults.value,
          isSearching: false,
          activeMode: 'search',
          timestamp: Date.now()
        }))

        loadHistory()
        if (searchResults.value.length === 0) {
          toast.info('未找到相关结果，已尝试同义词扩展搜索')
        }
      } else {
        toast.error(result.message || '搜索失败')
        sessionStorage.removeItem('intelligentSearchState')
      }
    } catch (e) {
      console.error('智能搜索失败:', e)
      toast.error('搜索失败，请稍后重试')
      sessionStorage.removeItem('intelligentSearchState')
    } finally {
      isSearching.value = false
      const savedState = sessionStorage.getItem('intelligentSearchState')
      if (savedState) {
        try {
          const state = JSON.parse(savedState)
          if (state.isSearching) {
            sessionStorage.removeItem('intelligentSearchState')
          }
        } catch (e) {
          sessionStorage.removeItem('intelligentSearchState')
        }
      }
    }
  }

  const handleSemanticChat = async () => {
    if (!chatQuestion.value.trim()) {
      toast.warning('请输入您的问题')
      return
    }

    if (isChatting.value) {
      toast.info('正在思考中，请稍候...')
      return
    }

    isChatting.value = true
    chatAnswer.value = ''
    displayedAnswer.value = ''
    relatedContents.value = []
    keywordsInfo.value = []
    intentInfo.value = ''
    showAnswerPanel.value = true

    sessionStorage.setItem('semanticChatState', JSON.stringify({
      question: chatQuestion.value,
      isChatting: true,
      activeMode: 'chat',
      timestamp: Date.now()
    }))

    try {
      const result = await searchApi.semanticChat(chatQuestion.value)
      if (result.code === 200) {
        const data = result.data
        chatAnswer.value = data.answer || '抱歉，我无法回答这个问题'
        relatedContents.value = data.relatedContents || []
        keywordsInfo.value = data.keywords || []
        intentInfo.value = getIntentLabel(data.intent)

        sessionStorage.setItem('semanticChatState', JSON.stringify({
          question: chatQuestion.value,
          answer: chatAnswer.value,
          relatedContents: relatedContents.value,
          keywordsInfo: keywordsInfo.value,
          intentInfo: intentInfo.value,
          isChatting: false,
          activeMode: 'chat',
          timestamp: Date.now()
        }))

        loadHistory()

        if (isComponentActive) {
          startTypingEffect()
        } else {
          displayedAnswer.value = chatAnswer.value
          isChatting.value = false
        }
      } else {
        toast.error(result.message || '问答失败')
        isChatting.value = false
        sessionStorage.removeItem('semanticChatState')
      }
    } catch (e) {
      console.error('语义问答失败:', e)
      toast.error('问答失败，请稍后重试')
      isChatting.value = false
      sessionStorage.removeItem('semanticChatState')
    }
  }

  const startTypingEffect = () => {
    const text = chatAnswer.value
    let index = 0
    displayedAnswer.value = ''
    isChatting.value = true

    typingTimer = setInterval(() => {
      if (index < text.length) {
        displayedAnswer.value += text[index]
        index++
      } else {
        clearInterval(typingTimer)
        typingTimer = null
        isChatting.value = false
        sessionStorage.removeItem('semanticChatState')
      }
    }, 20)
  }

  const getIntentLabel = (intent) => {
    const intentMap = {
      definition: '定义解释',
      howto: '操作指南',
      why: '原因分析',
      compare: '对比分析',
      example: '案例说明',
      search: '内容查找',
      general: '通用问答',
    }
    return intentMap[intent] || '通用问答'
  }

  const formatAnswer = (text) => {
    if (!text) return ''
    return text
      .split('\n')
      .filter((p) => p.trim())
      .map((p) => `<p>${p}</p>`)
      .join('')
  }

  const searchBySuggestion = (question) => {
    if (activeMode.value === 'search') {
      searchQuery.value = question
      handleIntelligentSearch()
    } else {
      chatQuestion.value = question
      handleSemanticChat()
    }
  }

  const searchByHistory = (item) => {
    searchQuery.value = item.keyword
    handleIntelligentSearch()
  }

  const deleteHistoryItem = async (item, event) => {
    event.stopPropagation()
    try {
      const result = await searchApi.deleteHistory(item.id)
      if (result.code === 200) {
        searchHistory.value = searchHistory.value.filter((h) => h.id !== item.id)
        toast.success('删除成功')
      }
    } catch (e) {
      toast.error('删除失败')
    }
  }

  const clearAllHistory = async () => {
    try {
      const result = await searchApi.clearHistory()
      if (result.code === 200) {
        searchHistory.value = []
        toast.success('已清空搜索历史')
      }
    } catch (e) {
      toast.error('清空失败')
    }
  }

  const goToDetail = (result) => {
    if (result.type === 'knowledge') {
      router.push(`/knowledge/${result.id}`)
    } else {
      router.push(`/notes/${result.id}`)
    }
  }

  const goToRelatedContent = (content) => {
    if (content.type === 'knowledge') {
      router.push(`/knowledge/${content.id}`)
    } else {
      router.push(`/notes/${content.id}`)
    }
  }

  const resetSearch = () => {
    searchQuery.value = ''
    searchResults.value = []
    expandedTerms.value = []
  }

  const resetChat = () => {
    if (typingTimer) {
      clearInterval(typingTimer)
      typingTimer = null
    }
    chatQuestion.value = ''
    chatAnswer.value = ''
    displayedAnswer.value = ''
    relatedContents.value = []
    keywordsInfo.value = []
    intentInfo.value = ''
    showAnswerPanel.value = false
  }

  onMounted(() => {
    loadHistory()
    loadKnowledgeBases()
    restoreSearchState()
  })

  const restoreSearchState = () => {
    const savedSearchState = sessionStorage.getItem('intelligentSearchState')
    if (savedSearchState) {
      try {
        const state = JSON.parse(savedSearchState)
        const elapsed = Date.now() - state.timestamp

        if (elapsed > 120000) {
          sessionStorage.removeItem('intelligentSearchState')
        } else if (state.isSearching) {
          activeMode.value = state.activeMode || 'search'
          isSearching.value = true
          searchQuery.value = state.query
          toast.info('智能搜索请求进行中...')
        } else if (state.searchResults) {
          activeMode.value = state.activeMode || 'search'
          isSearching.value = false
          searchQuery.value = state.query
          expandedTerms.value = state.expandedTerms || []
          searchResults.value = state.searchResults
          toast.success('智能搜索已完成')
          sessionStorage.removeItem('intelligentSearchState')
        }
      } catch (e) {
        console.error('恢复智能搜索状态失败:', e)
        sessionStorage.removeItem('intelligentSearchState')
      }
    }

    const savedChatState = sessionStorage.getItem('semanticChatState')
    if (savedChatState) {
      try {
        const state = JSON.parse(savedChatState)
        const elapsed = Date.now() - state.timestamp

        if (elapsed > 120000) {
          sessionStorage.removeItem('semanticChatState')
          isChatting.value = false
        } else if (state.isChatting) {
          activeMode.value = state.activeMode || 'chat'
          isChatting.value = true
          chatQuestion.value = state.question
          showAnswerPanel.value = true
          displayedAnswer.value = ''
          toast.info('语义问答请求进行中...')
        } else if (state.answer) {
          activeMode.value = state.activeMode || 'chat'
          isChatting.value = false
          chatQuestion.value = state.question
          chatAnswer.value = state.answer
          relatedContents.value = state.relatedContents || []
          keywordsInfo.value = state.keywordsInfo || []
          intentInfo.value = state.intentInfo || ''
          showAnswerPanel.value = true
          displayedAnswer.value = state.answer
          toast.success('语义问答已完成')
          sessionStorage.removeItem('semanticChatState')
        }
      } catch (e) {
        console.error('恢复语义问答状态失败:', e)
        sessionStorage.removeItem('semanticChatState')
        isChatting.value = false
      }
    } else {
      if (chatAnswer.value && !displayedAnswer.value) {
        displayedAnswer.value = chatAnswer.value
        isChatting.value = false
        showAnswerPanel.value = true
      }
    }
  }

  onActivated(() => {
    isComponentActive = true
    if (isSearching.value || isChatting.value) {
      console.log('AI请求进行中，状态已保持')
    }
    restoreSearchState()
  })

  onDeactivated(() => {
    isComponentActive = false
    if (typingTimer) {
      clearInterval(typingTimer)
      typingTimer = null
      isChatting.value = false
      displayedAnswer.value = chatAnswer.value
    }
  })

  onUnmounted(() => {
    if (typingTimer) {
      clearInterval(typingTimer)
      typingTimer = null
    }
  })
</script>

<template>
  <div class="search-page">
    <div class="mode-tabs">
      <button v-for="tab in modeTabs" :key="tab.key" class="mode-tab" :class="{ active: activeMode === tab.key }"
        @click="activeMode = tab.key">
        <Icon :icon="tab.icon" />
        <span>{{ tab.label }}</span>
      </button>
    </div>

    <div class="search-container">
      <div v-if="activeMode === 'search'" class="search-box">
        <div class="search-input-wrapper">
          <Icon icon="ri:search-line" class="search-icon" />
          <input v-model="searchQuery" type="text" placeholder="输入关键词，AI将智能理解您的搜索意图..." class="search-input"
            @keyup.enter="handleIntelligentSearch" />
          <button v-if="searchQuery" class="clear-btn" @click="searchQuery = ''">
            <Icon icon="ri:close-circle-line" />
          </button>
          <button class="search-btn" :disabled="isSearching" @click="handleIntelligentSearch">
            <Icon v-if="isSearching" icon="ri:loader-4-line" class="spin" />
            <Icon v-else icon="ri:sparkling-line" />
            {{ isSearching ? '搜索中...' : '智能搜索' }}
          </button>
        </div>

        <div class="search-features">
          <div class="feature-item">
            <Icon icon="ri:brain-line" />
            <span>语义理解</span>
          </div>
          <div class="feature-item">
            <Icon icon="ri:magic-line" />
            <span>同义词扩展</span>
          </div>
          <div class="feature-item">
            <Icon icon="ri:lightbulb-line" />
            <span>意图识别</span>
          </div>
        </div>
      </div>

      <div v-else class="chat-box">
        <div class="chat-input-wrapper">
          <Icon icon="ri:chat-ai-line" class="chat-icon" />
          <textarea v-model="chatQuestion" placeholder="输入您的问题，AI将基于您的知识库进行语义分析和回答..." class="chat-input"
            @keyup.ctrl.enter="handleSemanticChat"></textarea>
          <button class="chat-btn" :disabled="isChatting" @click="handleSemanticChat">
            <Icon v-if="isChatting" icon="ri:loader-4-line" class="spin" />
            <Icon v-else icon="ri:send-plane-line" />
            {{ isChatting ? '思考中...' : '提问' }}
          </button>
        </div>

        <div class="chat-features">
          <div class="feature-item">
            <Icon icon="ri:question-answer-line" />
            <span>知识库问答</span>
          </div>
          <div class="feature-item">
            <Icon icon="ri:file-list-3-line" />
            <span>内容引用</span>
          </div>
          <div class="feature-item">
            <Icon icon="ri:link" />
            <span>关联推荐</span>
          </div>
        </div>
      </div>
    </div>

    <div class="search-content">
      <div v-if="activeMode === 'search'">
        <div v-if="isSearching" class="thinking-section">
          <div class="thinking-animation">
            <div class="thinking-dots">
              <span></span>
              <span></span>
              <span></span>
            </div>
            <div class="thinking-text">
              <Icon icon="ri:robot-line" class="thinking-icon" />
              <span>AI 正在分析您的搜索意图...</span>
            </div>
          </div>
        </div>

        <div v-else-if="searchResults.length === 0" class="center-panel">
          <div v-if="showSearchHistory" class="history-panel">
            <div class="panel-header">
              <h3 class="panel-title">
                <Icon icon="ri:history-line" />
                搜索历史
              </h3>
              <button class="clear-all-btn" @click="clearAllHistory">
                清空全部
              </button>
            </div>
            <div class="history-grid">
              <div v-for="item in filteredSearchHistory" :key="item.id" class="history-card"
                @click="searchByHistory(item)">
                <span class="history-text">{{ item.keyword }}</span>
                <button class="delete-btn" @click="deleteHistoryItem(item, $event)">
                  <Icon icon="ri:close-line" />
                </button>
              </div>
            </div>
          </div>
          <div v-else class="empty-panel">
            <Icon icon="ri:search-eye-line" class="empty-icon" />
            <p>输入关键词开始智能搜索</p>
          </div>
        </div>

        <div v-else class="results-layout">
          <div class="sidebar-section">
            <div class="section-header">
              <h4 class="section-title">
                <Icon icon="ri:history-line" />
                搜索历史
              </h4>
              <button v-if="showSearchHistory" class="clear-all-btn" @click="clearAllHistory">
                清空
              </button>
            </div>
            <div v-if="showSearchHistory" class="history-list">
              <div v-for="item in filteredSearchHistory" :key="item.id" class="history-item"
                @click="searchByHistory(item)">
                <Icon icon="ri:time-line" class="history-icon" />
                <span class="history-text">{{ item.keyword }}</span>
                <button class="delete-btn" @click="deleteHistoryItem(item, $event)">
                  <Icon icon="ri:close-line" />
                </button>
              </div>
            </div>
            <div v-else class="empty-history">
              <Icon icon="ri:history-line" class="empty-icon" />
              <span>暂无搜索历史</span>
            </div>
          </div>

          <div class="main-section">
            <div v-if="expandedTerms.length > 0" class="ai-terms-bar">
              <Icon icon="ri:ai-generate" class="ai-terms-icon" />
              <span class="ai-terms-label">AI 扩展搜索：</span>
              <span v-for="term in expandedTerms" :key="term" class="term-tag"
                @click="searchQuery = term; handleIntelligentSearch()">
                {{ term }}
              </span>
              <button class="reset-btn" @click="resetSearch">
                <Icon icon="ri:refresh-line" />
                重新搜索
              </button>
            </div>

            <div class="results-header">
              <span class="results-count">找到 {{ searchResults.length }} 条相关结果</span>
            </div>

            <div class="results-list">
              <div v-for="result in searchResults" :key="result.id" class="result-card" @click="goToDetail(result)">
                <div class="result-header">
                  <div class="result-icon" :style="{ background: result.color }">
                    <Icon :icon="result.icon" />
                  </div>
                  <div class="result-meta">
                    <h4 class="result-title">{{ result.title }}</h4>
                    <div class="result-info">
                      <span class="result-source">{{ result.source }}</span>
                      <span class="result-type">{{ result.type }}</span>
                    </div>
                  </div>
                </div>
                <p class="result-content">{{ result.content }}</p>
                <div class="result-footer">
                  <span class="ai-match-badge">
                    <Icon icon="ri:ai-generate" />
                    AI 智能匹配
                  </span>
                  <button class="view-btn">查看详情 →</button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div v-else>
        <div v-if="isChatting && !displayedAnswer" class="thinking-section">
          <div class="thinking-animation">
            <div class="thinking-dots">
              <span></span>
              <span></span>
              <span></span>
            </div>
            <div class="thinking-text">
              <Icon icon="ri:robot-line" class="thinking-icon" />
              <span>AI 正在思考您的问题...</span>
            </div>
          </div>
        </div>

        <div v-else-if="!showAnswerPanel" class="center-panel">
          <div v-if="showSuggestedQuestions" class="suggestions-panel">
            <div class="panel-header">
              <h3 class="panel-title">
                <Icon icon="ri:lightbulb-line" />
                推荐问题
              </h3>
              <p class="panel-hint">基于您的知识库生成</p>
            </div>
            <div class="suggestions-grid">
              <div v-for="question in suggestedQuestions" :key="question" class="suggestion-card"
                @click="searchBySuggestion(question)">
                <Icon icon="ri:question-mark" class="suggestion-icon" />
                <span class="suggestion-text">{{ question }}</span>
              </div>
            </div>
          </div>
          <div v-else class="empty-panel">
            <Icon icon="ri:chat-ai-line" class="empty-icon" />
            <p>输入问题开始语义问答</p>
          </div>
        </div>

        <div v-else class="results-layout chat-results">
          <div class="main-section">
            <div v-if="intentInfo || keywordsInfo.length > 0" class="answer-meta">
              <div v-if="intentInfo" class="meta-item">
                <Icon icon="ri:focus-3-line" />
                <span class="meta-label">意图识别：</span>
                <span class="meta-value">{{ intentInfo }}</span>
              </div>
              <div v-if="keywordsInfo.length > 0" class="meta-item">
                <Icon icon="ri:key-2-line" />
                <span class="meta-label">关键词：</span>
                <div class="keywords-list">
                  <span v-for="keyword in keywordsInfo" :key="keyword" class="keyword-tag">
                    {{ keyword }}
                  </span>
                </div>
              </div>
            </div>

            <div class="answer-card">
              <div class="answer-header">
                <Icon icon="ri:robot-line" class="answer-icon" />
                <h4>AI 回答</h4>
                <span v-if="isChatting" class="typing-indicator">
                  <span class="dot"></span>
                  <span class="dot"></span>
                  <span class="dot"></span>
                </span>
              </div>
              <div class="answer-content">
                <span v-html="parseMarkdown(displayedAnswer)"></span><span v-if="isChatting" class="cursor">|</span>
              </div>
            </div>

            <div v-if="relatedContents.length > 0 && !isChatting" class="related-section">
              <h4 class="related-title">
                <Icon icon="ri:file-list-3-line" />
                相关内容
              </h4>
              <div class="related-list">
                <div v-for="content in relatedContents" :key="content.id" class="related-item"
                  @click="goToRelatedContent(content)">
                  <div class="related-header">
                    <Icon :icon="content.type === 'knowledge' ? 'ri:brain-line' : 'ri:book-3-line'"
                      class="related-type-icon" />
                    <span class="related-title-text">{{ content.title }}</span>
                    <span class="related-relevance">
                      相关度: {{ (content.relevance * 100).toFixed(0) }}%
                    </span>
                  </div>
                  <p class="related-summary">{{ content.summary }}</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
  .search-page {
    width: 100%;
  }

  .mode-tabs {
    display: flex;
    gap: 12px;
    margin-bottom: 24px;
  }

  .mode-tab {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 10px;
    padding: 16px 24px;
    background: white;
    border: 2px solid #e5e7eb;
    border-radius: 12px;
    font-size: 16px;
    font-weight: 500;
    color: #6b7280;
    cursor: pointer;
    transition: all 0.3s;
  }

  .mode-tab:hover {
    border-color: #122e8a;
    color: #122e8a;
  }

  .mode-tab.active {
    background: linear-gradient(135deg, #122e8a 0%, #1e40af 100%);
    border-color: #122e8a;
    color: white;
  }

  .mode-tab .iconify {
    font-size: 20px;
  }

  .search-container {
    margin-bottom: 32px;
  }

  .search-box,
  .chat-box {
    background: white;
    border-radius: 16px;
    padding: 24px;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
    border: 1px solid #e5e7eb;
  }

  .search-input-wrapper {
    display: flex;
    align-items: center;
    background: #f9fafb;
    border-radius: 12px;
    padding: 12px 16px;
    border: 2px solid transparent;
    transition: border-color 0.2s;
  }

  .search-input-wrapper:focus-within {
    border-color: #122e8a;
    background: white;
  }

  .search-icon {
    font-size: 20px;
    color: #9ca3af;
    margin-right: 12px;
  }

  .search-input {
    flex: 1;
    border: none;
    outline: none;
    font-size: 16px;
    color: #1f2937;
    background: transparent;
  }

  .search-input::placeholder {
    color: #9ca3af;
  }

  .clear-btn {
    padding: 8px;
    background: transparent;
    border: none;
    color: #9ca3af;
    cursor: pointer;
    border-radius: 8px;
    transition: all 0.2s;
  }

  .clear-btn:hover {
    background: #fee2e2;
    color: #ef4444;
  }

  .search-btn {
    display: flex;
    align-items: center;
    gap: 8px;
    height: 44px;
    padding: 0 24px;
    background: linear-gradient(135deg, #122e8a 0%, #1e40af 100%);
    color: white;
    border: none;
    border-radius: 10px;
    font-size: 14px;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.2s;
    margin-left: 12px;
  }

  .search-btn:hover:not(:disabled) {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(18, 46, 138, 0.3);
  }

  .search-btn:disabled {
    opacity: 0.7;
    cursor: not-allowed;
  }

  .chat-input-wrapper {
    display: flex;
    flex-direction: column;
    gap: 12px;
  }

  .chat-icon {
    font-size: 20px;
    color: #9ca3af;
  }

  .chat-input {
    width: 100%;
    min-height: 100px;
    padding: 16px;
    border: 2px solid #e5e7eb;
    border-radius: 12px;
    font-size: 16px;
    color: #1f2937;
    resize: vertical;
    transition: border-color 0.2s;
  }

  .chat-input:focus {
    outline: none;
    border-color: #122e8a;
  }

  .chat-input::placeholder {
    color: #9ca3af;
  }

  .chat-btn {
    align-self: flex-end;
    display: flex;
    align-items: center;
    gap: 8px;
    height: 44px;
    padding: 0 24px;
    background: linear-gradient(135deg, #122e8a 0%, #1e40af 100%);
    color: white;
    border: none;
    border-radius: 10px;
    font-size: 14px;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.2s;
  }

  .chat-btn:hover:not(:disabled) {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(18, 46, 138, 0.3);
  }

  .chat-btn:disabled {
    opacity: 0.7;
    cursor: not-allowed;
  }

  .search-features,
  .chat-features {
    display: flex;
    gap: 24px;
    margin-top: 16px;
    padding-top: 16px;
    border-top: 1px solid #f3f4f6;
  }

  .feature-item {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 14px;
    color: #6b7280;
  }

  .feature-item .iconify {
    font-size: 18px;
    color: #122e8a;
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

  .search-content {
    min-height: auto;
  }

  .center-panel {
    padding: 24px 0 0 0;
  }

  .history-panel,
  .suggestions-panel {
    width: 100%;
  }

  .panel-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
  }

  .panel-title {
    display: flex;
    align-items: center;
    gap: 10px;
    font-size: 20px;
    font-weight: 600;
    color: #1f2937;
    margin: 0;
  }

  .panel-title .iconify {
    font-size: 24px;
    color: #122e8a;
  }

  .panel-hint {
    font-size: 14px;
    color: #9ca3af;
    margin: 0;
  }

  .history-grid {
    display: flex;
    flex-wrap: wrap;
    gap: 12px;
  }

  .history-card {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 14px 18px;
    background: white;
    border: 1px solid #e5e7eb;
    border-radius: 12px;
    cursor: pointer;
    transition: all 0.2s;
    min-width: 180px;
    max-width: 280px;
    box-shadow:
      0 2px 4px rgba(0, 0, 0, 0.04),
      0 4px 8px rgba(0, 0, 0, 0.02);
  }

  .history-card:hover {
    border-color: #122e8a;
    box-shadow:
      0 4px 8px rgba(18, 46, 138, 0.1),
      0 8px 16px rgba(18, 46, 138, 0.08);
    transform: translateY(-2px);
  }

  .history-card .history-text {
    flex: 1;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    font-size: 14px;
    color: #374151;
  }

  .history-card .delete-btn {
    opacity: 0;
    padding: 4px;
    background: transparent;
    border: none;
    color: #d1d5db;
    cursor: pointer;
    border-radius: 4px;
    transition: all 0.2s;
  }

  .history-card:hover .delete-btn {
    opacity: 1;
  }

  .history-card .delete-btn:hover {
    color: #ef4444;
    background: #fee2e2;
  }

  .suggestions-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 16px;
  }

  .suggestion-card {
    display: flex;
    align-items: flex-start;
    gap: 12px;
    padding: 20px;
    background: white;
    border: 1px solid #e5e7eb;
    border-radius: 12px;
    cursor: pointer;
    transition: all 0.2s;
  }

  .suggestion-card:hover {
    border-color: #122e8a;
    box-shadow: 0 4px 12px rgba(18, 46, 138, 0.1);
    transform: translateY(-2px);
  }

  .suggestion-card .suggestion-icon {
    font-size: 20px;
    color: #122e8a;
    flex-shrink: 0;
    margin-top: 2px;
  }

  .suggestion-card .suggestion-text {
    font-size: 15px;
    color: #374151;
    line-height: 1.5;
  }

  .empty-panel {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 60px 20px;
    color: #9ca3af;
  }

  .empty-icon {
    font-size: 48px;
    color: #d1d5db;
    margin-bottom: 16px;
  }

  .empty-panel p {
    font-size: 16px;
    margin: 0;
  }

  .results-layout {
    display: grid;
    grid-template-columns: minmax(180px, 280px) 1fr;
    gap: 24px;
  }

  .results-layout.chat-results {
    grid-template-columns: 1fr;
  }

  .section-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 14px;
    font-weight: 600;
    color: #6b7280;
    margin-bottom: 16px;
  }

  .sidebar-section {
    background: white;
    border-radius: 16px;
    padding: 20px;
    height: fit-content;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
    border: 1px solid #e5e7eb;
  }

  .empty-history {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 24px 16px;
    color: #9ca3af;
    font-size: 13px;
    gap: 8px;
  }

  .empty-history .empty-icon {
    font-size: 24px;
    opacity: 0.5;
  }

  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
  }

  .section-header .section-title {
    margin-bottom: 0;
  }

  .clear-all-btn {
    padding: 6px 14px;
    background: white;
    border: 1px solid #e5e7eb;
    border-radius: 8px;
    font-size: 13px;
    color: #6b7280;
    cursor: pointer;
    transition: all 0.2s;
    box-shadow:
      0 1px 2px rgba(0, 0, 0, 0.05),
      0 2px 4px rgba(0, 0, 0, 0.02);
  }

  .clear-all-btn:hover {
    background: #fee2e2;
    border-color: #ef4444;
    color: #ef4444;
    box-shadow:
      0 2px 4px rgba(239, 68, 68, 0.15),
      0 4px 8px rgba(239, 68, 68, 0.1);
    transform: translateY(-1px);
  }

  .history-list {
    display: flex;
    flex-direction: column;
    gap: 4px;
  }

  .history-item {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 10px 12px;
    border-radius: 10px;
    color: #6b7280;
    font-size: 14px;
    cursor: pointer;
    transition: all 0.2s;
    background: white;
    box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
  }

  .history-item:hover {
    background: #f3f4f6;
    color: #1f2937;
    box-shadow:
      0 2px 4px rgba(0, 0, 0, 0.06),
      0 4px 8px rgba(0, 0, 0, 0.03);
    transform: translateX(4px);
  }

  .history-icon {
    font-size: 16px;
    color: #9ca3af;
    flex-shrink: 0;
  }

  .history-text {
    flex: 1;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .delete-btn {
    padding: 4px;
    background: transparent;
    border: none;
    color: #d1d5db;
    cursor: pointer;
    border-radius: 4px;
    opacity: 0;
    transition: all 0.2s;
    flex-shrink: 0;
  }

  .history-item:hover .delete-btn {
    opacity: 1;
  }

  .delete-btn:hover {
    background: #fee2e2;
    color: #ef4444;
  }

  .main-section {
    min-height: auto;
  }

  .suggestions {
    background: white;
    border-radius: 16px;
    padding: 24px;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
    border: 1px solid #e5e7eb;
  }

  .suggestion-list {
    display: flex;
    flex-direction: column;
    gap: 12px;
  }

  .suggestion-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 16px;
    background: #f9fafb;
    border-radius: 12px;
    cursor: pointer;
    transition: all 0.2s;
  }

  .suggestion-item:hover {
    background: #f3f4f6;
    transform: translateX(4px);
  }

  .suggestion-icon {
    font-size: 18px;
    color: #122e8a;
  }

  .suggestion-item span {
    font-size: 15px;
    color: #374151;
  }

  .results {
    background: white;
    border-radius: 16px;
    padding: 24px;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
    border: 1px solid #e5e7eb;
  }

  .results-header {
    margin-bottom: 20px;
  }

  .results-count {
    font-size: 14px;
    color: #6b7280;
  }

  .ai-terms-bar {
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    gap: 8px;
    padding: 12px 16px;
    background: #f9fafb;
    border-radius: 12px;
    margin-bottom: 16px;
  }

  .ai-terms-icon {
    font-size: 16px;
    color: #7c3aed;
  }

  .ai-terms-label {
    font-size: 13px;
    color: #6b7280;
    white-space: nowrap;
  }

  .term-tag {
    padding: 4px 12px;
    background: white;
    border: 1px solid #e5e7eb;
    border-radius: 14px;
    font-size: 12px;
    color: #7c3aed;
    cursor: pointer;
    transition: all 0.2s;
  }

  .term-tag:hover {
    background: #122e8a;
    border-color: #122e8a;
    color: white;
  }

  .results-list {
    display: flex;
    flex-direction: column;
    gap: 16px;
  }

  .result-card {
    background: #f9fafb;
    border-radius: 12px;
    padding: 20px;
    transition: all 0.2s;
    cursor: pointer;
  }

  .result-card:hover {
    background: #f3f4f6;
    transform: translateX(4px);
  }

  .result-header {
    display: flex;
    gap: 14px;
    margin-bottom: 12px;
  }

  .result-icon {
    width: 40px;
    height: 40px;
    border-radius: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    font-size: 18px;
    flex-shrink: 0;
  }

  .result-title {
    font-size: 16px;
    font-weight: 600;
    color: #1f2937;
    margin-bottom: 4px;
  }

  .result-info {
    display: flex;
    gap: 12px;
    font-size: 13px;
    color: #9ca3af;
  }

  .result-type {
    padding: 2px 8px;
    background: #e5e7eb;
    border-radius: 4px;
  }

  .result-content {
    font-size: 14px;
    color: #6b7280;
    line-height: 1.6;
    margin-bottom: 16px;
  }

  .result-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .ai-match-badge {
    display: flex;
    align-items: center;
    gap: 4px;
    font-size: 12px;
    color: #7c3aed;
    background: #f3f0ff;
    padding: 4px 10px;
    border-radius: 12px;
  }

  .view-btn {
    padding: 8px 16px;
    background: transparent;
    border: 1px solid #e5e7eb;
    border-radius: 8px;
    color: #6b7280;
    font-size: 13px;
    cursor: pointer;
    transition: all 0.2s;
  }

  .view-btn:hover {
    background: #122e8a;
    border-color: #122e8a;
    color: white;
  }

  .empty-results {
    text-align: center;
    padding: 60px 20px;
    color: #9ca3af;
  }

  .empty-icon {
    font-size: 48px;
    color: #d1d5db;
    margin-bottom: 16px;
  }

  .empty-results p {
    font-size: 16px;
    margin-bottom: 8px;
  }

  .empty-hint {
    font-size: 14px;
    color: #d1d5db;
  }

  .chat-answer-section {
    display: flex;
    flex-direction: column;
    gap: 20px;
  }

  .answer-meta {
    display: flex;
    gap: 24px;
    padding: 12px 16px;
    background: #f9fafb;
    border-radius: 12px;
    margin-bottom: 12px;
  }

  .meta-item {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 14px;
  }

  .meta-item .iconify {
    font-size: 18px;
    color: #122e8a;
  }

  .meta-label {
    color: #6b7280;
  }

  .meta-value {
    color: #1f2937;
    font-weight: 500;
  }

  .keywords-list {
    display: flex;
    gap: 8px;
    flex-wrap: wrap;
  }

  .keyword-tag {
    padding: 4px 12px;
    background: white;
    border: 1px solid #e5e7eb;
    border-radius: 14px;
    font-size: 12px;
    color: #122e8a;
  }

  .answer-card {
    background: linear-gradient(180deg, #ffffff 0%, #f8fafc 100%);
    border-radius: 16px;
    padding: 24px;
    box-shadow:
      0 2px 4px rgba(0, 0, 0, 0.02),
      0 4px 8px rgba(0, 0, 0, 0.04),
      inset 0 1px 0 rgba(255, 255, 255, 0.8);
    border: 1px solid rgba(0, 0, 0, 0.05);
  }

  .answer-header {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 16px;
  }

  .answer-icon {
    font-size: 24px;
    color: #122e8a;
  }

  .answer-header h4 {
    font-size: 18px;
    font-weight: 600;
    color: #1f2937;
    margin: 0;
  }

  .answer-content {
    font-size: 15px;
    color: #374151;
    line-height: 1.8;
  }

  .answer-content :deep(strong) {
    font-weight: 600;
    color: #1f2937;
  }

  .answer-content :deep(em) {
    font-style: italic;
    color: #4b5563;
  }

  .answer-content :deep(.inline-code) {
    background: #f3f4f6;
    padding: 2px 6px;
    border-radius: 4px;
    font-family: 'Consolas', 'Monaco', monospace;
    font-size: 14px;
    color: #dc2626;
  }

  .answer-content :deep(ul) {
    margin: 12px 0;
    padding-left: 20px;
  }

  .answer-content :deep(li) {
    margin-bottom: 6px;
  }

  .answer-content :deep(br) {
    display: block;
    content: '';
    margin-bottom: 8px;
  }

  .related-section {
    margin-top: 8px;
  }

  .related-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 16px;
    font-weight: 600;
    color: #1f2937;
    margin-bottom: 16px;
  }

  .related-list {
    display: flex;
    flex-direction: column;
    gap: 12px;
  }

  .related-item {
    background: #f9fafb;
    border-radius: 12px;
    padding: 16px;
    cursor: pointer;
    transition: all 0.2s;
  }

  .related-item:hover {
    background: #f3f4f6;
    transform: translateX(4px);
  }

  .related-header {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 8px;
  }

  .related-type-icon {
    font-size: 18px;
    color: #122e8a;
  }

  .related-title-text {
    flex: 1;
    font-size: 15px;
    font-weight: 500;
    color: #1f2937;
  }

  .related-relevance {
    font-size: 12px;
    color: #6b7280;
    background: white;
    padding: 2px 8px;
    border-radius: 8px;
  }

  .related-summary {
    font-size: 13px;
    color: #6b7280;
    line-height: 1.6;
    margin: 0;
    overflow: hidden;
    text-overflow: ellipsis;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    line-clamp: 2;
    -webkit-box-orient: vertical;
  }

  .thinking-section {
    display: flex;
    justify-content: center;
    align-items: center;
    min-height: 120px;
  }

  .thinking-animation {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 16px;
  }

  .thinking-dots {
    display: flex;
    gap: 8px;
  }

  .thinking-dots span {
    width: 12px;
    height: 12px;
    background: #122e8a;
    border-radius: 50%;
    animation: bounce 1.4s ease-in-out infinite both;
  }

  .thinking-dots span:nth-child(1) {
    animation-delay: -0.32s;
  }

  .thinking-dots span:nth-child(2) {
    animation-delay: -0.16s;
  }

  @keyframes bounce {

    0%,
    80%,
    100% {
      transform: scale(0);
    }

    40% {
      transform: scale(1);
    }
  }

  .thinking-text {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 16px;
    color: #6b7280;
  }

  .thinking-icon {
    font-size: 24px;
    color: #122e8a;
    animation: pulse 1.5s ease-in-out infinite;
  }

  @keyframes pulse {

    0%,
    100% {
      opacity: 1;
    }

    50% {
      opacity: 0.5;
    }
  }

  .reset-btn {
    display: flex;
    align-items: center;
    gap: 4px;
    padding: 4px 12px;
    background: transparent;
    border: 1px solid #e5e7eb;
    border-radius: 14px;
    font-size: 12px;
    color: #6b7280;
    cursor: pointer;
    transition: all 0.2s;
    margin-left: auto;
  }

  .reset-btn:hover {
    background: #122e8a;
    border-color: #122e8a;
    color: white;
  }

  .suggestion-hint {
    font-size: 12px;
    color: #9ca3af;
    margin-bottom: 12px;
  }

  .typing-indicator {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    margin-left: 12px;
  }

  .typing-indicator .dot {
    width: 6px;
    height: 6px;
    background: #122e8a;
    border-radius: 50%;
    animation: typing-bounce 1.4s ease-in-out infinite both;
  }

  .typing-indicator .dot:nth-child(1) {
    animation-delay: -0.32s;
  }

  .typing-indicator .dot:nth-child(2) {
    animation-delay: -0.16s;
  }

  @keyframes typing-bounce {

    0%,
    80%,
    100% {
      transform: scale(0.6);
      opacity: 0.5;
    }

    40% {
      transform: scale(1);
      opacity: 1;
    }
  }

  .cursor {
    animation: blink 1s step-end infinite;
    color: #122e8a;
    font-weight: 300;
    margin-left: 2px;
  }

  @keyframes blink {

    0%,
    100% {
      opacity: 1;
    }

    50% {
      opacity: 0;
    }
  }
</style>