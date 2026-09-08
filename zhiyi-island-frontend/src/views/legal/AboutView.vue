<script setup>
  import { ref } from 'vue'
  import { useRouter } from 'vue-router'
  import { Icon } from '@iconify/vue'

  const router = useRouter()

  const goBack = () => {
    router.back()
  }

  const features = ref([
    {
      icon: 'ri:magic-line',
      title: 'AI 智能摘要',
      description: '利用先进的 AI 技术，自动提取书籍和文档的核心内容，生成精炼摘要，帮助您快速掌握要点。'
    },
    {
      icon: 'ri:search-eye-line',
      title: '记忆搜索引擎',
      description: '强大的语义搜索功能，让您能够通过关键词、概念或模糊描述找到遗忘的知识片段。'
    },
    {
      icon: 'ri:mind-map',
      title: '知识图谱',
      description: '可视化展示知识之间的关联，构建个人知识网络，发现隐藏的知识连接。'
    },
    {
      icon: 'ri:book-3-line',
      title: '笔记管理',
      description: '便捷的笔记记录和管理功能，支持多种格式导入导出，让您的知识沉淀永不丢失。'
    }
  ])

  const stats = ref([
    { number: '10,000+', label: '活跃用户' },
    { number: '500,000+', label: '笔记数量' },
    { number: '1,000,000+', label: 'AI 摘要生成' },
    { number: '99.9%', label: '服务可用性' }
  ])

  const legalPages = ref([
    {
      icon: 'ri:file-text-line',
      title: '用户协议',
      description: '使用知忆岛服务前请仔细阅读本协议，了解您的权利与义务',
      path: '/legal/user-agreement',
      color: '#3b82f6'
    },
    {
      icon: 'ri:shield-user-line',
      title: '隐私政策',
      description: '我们重视并保护您的个人信息安全，了解我们如何收集和使用您的信息',
      path: '/legal/privacy',
      color: '#10b981'
    },
    {
      icon: 'ri:code-box-line',
      title: '开源许可',
      description: '知忆岛基于众多优秀的开源项目构建，感谢开源社区的贡献',
      path: '/legal/opensource',
      color: '#8b5cf6'
    }
  ])

  const goToPage = (path) => {
    router.push(path)
  }
</script>

<template>
  <div class="about-page">
    <div class="page-header">
      <button class="btn btn-secondary" @click="goBack">
        <Icon icon="ri:arrow-left-line" />
        返回
      </button>
      <h1>关于我们</h1>
    </div>

    <div class="page-content">
      <section class="hero-section">
        <div class="hero-content">
          <div class="hero-icon">
            <Icon icon="ri:ai-generate" />
          </div>
          <h2>知忆岛</h2>
          <p class="hero-subtitle">AI 驱动的智能知识管理平台</p>
          <p class="hero-desc">
            在信息爆炸的时代，我们致力于帮助每个人更好地管理知识、沉淀智慧。
            知忆岛运用前沿的人工智能技术，让知识管理变得简单、高效、智能。
          </p>
        </div>
        <div class="hero-stats">
          <div class="stat-item" v-for="stat in stats" :key="stat.label">
            <div class="stat-number">{{ stat.number }}</div>
            <div class="stat-label">{{ stat.label }}</div>
          </div>
        </div>
      </section>

      <section class="features-section">
        <h3 class="section-title">核心功能</h3>
        <div class="features-grid">
          <div class="feature-card" v-for="feature in features" :key="feature.title">
            <div class="feature-icon">
              <Icon :icon="feature.icon" />
            </div>
            <h4>{{ feature.title }}</h4>
            <p>{{ feature.description }}</p>
          </div>
        </div>
      </section>

      <section class="legal-section">
        <h3 class="section-title">法律条款与声明</h3>
        <p class="section-desc">使用知忆岛服务前，请仔细阅读以下条款和政策</p>
        <div class="legal-grid">
          <div class="legal-card" v-for="page in legalPages" :key="page.path" @click="goToPage(page.path)">
            <div class="legal-icon" :style="{ background: page.color }">
              <Icon :icon="page.icon" />
            </div>
            <div class="legal-content">
              <h4>{{ page.title }}</h4>
              <p>{{ page.description }}</p>
            </div>
            <div class="legal-arrow">
              <Icon icon="ri:arrow-right-line" />
            </div>
          </div>
        </div>
      </section>

      <section class="team-section">
        <h3 class="section-title">我们的团队</h3>
        <div class="team-content">
          <p>我们是一支充满激情的团队，专注于 AI 和知识管理技术研发。我们的使命是让知识管理更智能，让学习更高效。</p>
          <div class="team-values">
            <div class="value-item">
              <Icon icon="ri:lightbulb-line" />
              <span>创新驱动</span>
            </div>
            <div class="value-item">
              <Icon icon="ri:user-heart-line" />
              <span>用户至上</span>
            </div>
            <div class="value-item">
              <Icon icon="ri:shield-check-line" />
              <span>安全可靠</span>
            </div>
            <div class="value-item">
              <Icon icon="ri:rocket-line" />
              <span>持续进化</span>
            </div>
          </div>
        </div>
      </section>

      <section class="contact-section">
        <h3 class="section-title">联系我们</h3>
        <div class="contact-grid">
          <div class="contact-item">
            <div class="contact-icon">
              <Icon icon="ri:mail-line" />
            </div>
            <div class="contact-info">
              <span class="contact-label">客服邮箱</span>
              <span class="contact-value">support@zhiyiisland.com</span>
            </div>
          </div>
          <div class="contact-item">
            <div class="contact-icon">
              <Icon icon="ri:github-line" />
            </div>
            <div class="contact-info">
              <span class="contact-label">GitHub</span>
              <span class="contact-value">github.com/zhiyiisland</span>
            </div>
          </div>
          <div class="contact-item">
            <div class="contact-icon">
              <Icon icon="ri:global-line" />
            </div>
            <div class="contact-info">
              <span class="contact-label">官方网站</span>
              <span class="contact-value">www.zhiyiisland.com</span>
            </div>
          </div>
          <div class="contact-item">
            <div class="contact-icon">
              <Icon icon="ri:map-pin-line" />
            </div>
            <div class="contact-info">
              <span class="contact-label">公司地址</span>
              <span class="contact-value">中国北京市海淀区</span>
            </div>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<style scoped>
  .about-page {
    max-width: 1000px;
    margin: 20px auto 0 auto;
  }

  .page-header {
    display: flex;
    align-items: center;
    gap: 16px;
    margin-bottom: 24px;
  }

  .page-header h1 {
    font-size: 24px;
    font-weight: 600;
    color: var(--text-color, #1f2937);
  }

  .page-content {
    display: flex;
    flex-direction: column;
    gap: 32px;
  }

  .hero-section {
    background: linear-gradient(135deg, var(--primary-color) 0%, var(--secondary-color) 100%);
    border-radius: 20px;
    padding: 48px;
    color: white;
  }

  .hero-content {
    text-align: center;
    margin-bottom: 40px;
  }

  .hero-icon {
    width: 100px;
    height: 100px;
    margin: 0 auto 24px;
    background: rgba(255, 255, 255, 0.2);
    backdrop-filter: blur(10px);
    border-radius: 24px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 50px;
  }

  .hero-section h2 {
    font-size: 40px;
    font-weight: 700;
    margin-bottom: 8px;
    letter-spacing: 2px;
  }

  .hero-subtitle {
    font-size: 18px;
    opacity: 0.95;
    margin-bottom: 16px;
    font-weight: 500;
  }

  .hero-desc {
    font-size: 15px;
    opacity: 0.9;
    line-height: 1.8;
    max-width: 600px;
    margin: 0 auto;
  }

  .hero-stats {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 20px;
    padding-top: 32px;
    border-top: 1px solid rgba(255, 255, 255, 0.2);
  }

  .hero-stats .stat-item {
    text-align: center;
  }

  .hero-stats .stat-number {
    font-size: 28px;
    font-weight: 700;
    margin-bottom: 4px;
  }

  .hero-stats .stat-label {
    font-size: 13px;
    opacity: 0.85;
  }

  .section-title {
    font-size: 20px;
    font-weight: 600;
    color: var(--text-color, #1f2937);
    margin-bottom: 12px;
  }

  .section-desc {
    font-size: 14px;
    color: var(--text-secondary, #6b7280);
    margin-bottom: 20px;
  }

  .features-section {
    background: var(--card-bg, white);
    border-radius: 16px;
    padding: 28px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  }

  .features-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 20px;
  }

  .feature-card {
    padding: 24px;
    background: var(--bg-tertiary, #f9fafb);
    border-radius: 14px;
    transition: all 0.3s;
  }

  .feature-card:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
  }

  .feature-icon {
    width: 56px;
    height: 56px;
    background: linear-gradient(135deg, var(--primary-color) 0%, var(--secondary-color) 100%);
    border-radius: 14px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 28px;
    color: white;
    margin-bottom: 16px;
  }

  .feature-card h4 {
    font-size: 17px;
    font-weight: 600;
    color: var(--text-color, #1f2937);
    margin-bottom: 8px;
  }

  .feature-card p {
    font-size: 14px;
    color: var(--text-secondary, #6b7280);
    line-height: 1.7;
  }

  .legal-section {
    background: var(--card-bg, white);
    border-radius: 16px;
    padding: 28px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  }

  .legal-grid {
    display: flex;
    flex-direction: column;
    gap: 16px;
  }

  .legal-card {
    display: flex;
    align-items: center;
    gap: 20px;
    padding: 24px;
    background: var(--bg-tertiary, #f9fafb);
    border-radius: 14px;
    cursor: pointer;
    transition: all 0.3s;
    border: 2px solid transparent;
  }

  .legal-card:hover {
    border-color: var(--primary-color);
    transform: translateX(8px);
    box-shadow: 0 4px 16px rgba(18, 46, 138, 0.1);
  }

  .legal-icon {
    width: 56px;
    height: 56px;
    border-radius: 14px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 28px;
    color: white;
    flex-shrink: 0;
  }

  .legal-content {
    flex: 1;
  }

  .legal-content h4 {
    font-size: 17px;
    font-weight: 600;
    color: var(--text-color, #1f2937);
    margin-bottom: 6px;
  }

  .legal-content p {
    font-size: 14px;
    color: var(--text-secondary, #6b7280);
    line-height: 1.5;
  }

  .legal-arrow {
    width: 40px;
    height: 40px;
    background: var(--card-bg, white);
    border-radius: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 20px;
    color: var(--text-secondary, #9ca3af);
    transition: all 0.2s;
    flex-shrink: 0;
  }

  .legal-card:hover .legal-arrow {
    background: var(--primary-color);
    color: white;
  }

  .team-section {
    background: var(--card-bg, white);
    border-radius: 16px;
    padding: 28px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  }

  .team-content p {
    font-size: 15px;
    color: var(--text-secondary, #6b7280);
    line-height: 1.8;
    margin-bottom: 24px;
  }

  .team-values {
    display: flex;
    gap: 16px;
    flex-wrap: wrap;
  }

  .value-item {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 14px 24px;
    background: linear-gradient(135deg, rgba(18, 46, 138, 0.05) 0%, rgba(18, 46, 138, 0.1) 100%);
    border-radius: 10px;
    font-size: 15px;
    color: var(--text-color, #374151);
    font-weight: 500;
  }

  .value-item .iconify {
    font-size: 20px;
    color: var(--primary-color);
  }

  .contact-section {
    background: var(--card-bg, white);
    border-radius: 16px;
    padding: 28px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  }

  .contact-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 16px;
  }

  .contact-item {
    display: flex;
    align-items: center;
    gap: 16px;
    padding: 20px;
    background: var(--bg-tertiary, #f9fafb);
    border-radius: 12px;
  }

  .contact-icon {
    width: 48px;
    height: 48px;
    background: linear-gradient(135deg, var(--primary-color) 0%, var(--secondary-color) 100%);
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 22px;
    color: white;
    flex-shrink: 0;
  }

  .contact-info {
    display: flex;
    flex-direction: column;
    gap: 2px;
  }

  .contact-label {
    font-size: 12px;
    color: var(--text-secondary, #9ca3af);
  }

  .contact-value {
    font-size: 14px;
    color: var(--text-color, #374151);
    font-weight: 500;
  }

  @media (max-width: 768px) {
    .hero-section {
      padding: 32px 24px;
    }

    .hero-section h2 {
      font-size: 28px;
    }

    .hero-stats {
      grid-template-columns: repeat(2, 1fr);
    }

    .features-grid {
      grid-template-columns: 1fr;
    }

    .contact-grid {
      grid-template-columns: 1fr;
    }

    .legal-card {
      padding: 20px;
    }

    .legal-icon {
      width: 48px;
      height: 48px;
      font-size: 24px;
    }
  }
</style>