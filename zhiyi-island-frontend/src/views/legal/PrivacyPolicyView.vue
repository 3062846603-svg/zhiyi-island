<script setup>
  import { ref } from 'vue'
  import { useRouter } from 'vue-router'
  import { Icon } from '@iconify/vue'

  const router = useRouter()

  const goBack = () => {
    router.back()
  }

  const lastUpdated = '2024年1月1日'

  const sections = ref([
    {
      id: 'collection',
      title: '一、我们收集的信息',
      content: `1.1 您主动提供的信息
• 注册信息：用户名、邮箱地址、密码（加密存储）
• 个人资料：昵称、头像、个人简介等（可选）
• 内容信息：您上传的笔记、文档、知识条目等
• 联系信息：您通过反馈、客服等渠道提供的信息

1.2 我们自动收集的信息
• 设备信息：设备型号、操作系统、浏览器类型
• 日志信息：IP地址、访问时间、访问页面、操作记录
• Cookie信息：用于维持登录状态和优化用户体验
• 位置信息：大致地理位置（基于IP地址，可选）`
    },
    {
      id: 'usage',
      title: '二、我们如何使用信息',
      content: `我们收集的信息将用于以下目的：
• 提供、维护和改进我们的服务
• 处理您的请求和订单
• 发送服务通知和更新信息
• 个性化您的使用体验
• 进行数据分析和服务优化
• 保障服务安全和防止欺诈
• 遵守法律法规的要求`
    },
    {
      id: 'sharing',
      title: '三、信息共享与披露',
      content: `3.1 我们不会向第三方出售您的个人信息。

3.2 以下情况下我们可能共享您的信息：
• 获得您的明确同意后
• 与授权合作伙伴共享（如云服务提供商、支付服务商），他们需遵守严格的保密义务
• 法律法规要求或政府机关依法要求
• 保护我们或用户的权益、财产或安全

3.3 匿名化处理：
我们可能对收集的信息进行匿名化处理，用于统计分析和服务改进，匿名化后的信息不属于个人信息。`
    },
    {
      id: 'storage',
      title: '四、信息存储与保护',
      content: `4.1 信息存储
• 您的信息存储在中华人民共和国境内的服务器
• 我们采用业界标准的安全措施保护您的信息
• 数据传输采用 HTTPS 加密协议

4.2 安全措施
• 数据加密：敏感信息采用加密存储
• 访问控制：严格限制员工访问权限
• 安全审计：定期进行安全评估和漏洞修复
• 应急响应：建立数据安全事件应急机制

4.3 数据保留
• 我们在您使用服务期间保留您的信息
• 账号注销后，我们将在合理期限内删除或匿名化处理您的信息
• 法律法规要求保留的除外`
    },
    {
      id: 'rights',
      title: '五、您的权利',
      content: `5.1 访问和更正
您可以在个人中心查看和修改您的个人资料。

5.2 删除
您可以删除您上传的内容，或申请删除您的账号。

5.3 导出
我们提供数据导出功能，您可以导出您的笔记和知识库数据。

5.4 撤回同意
您可以在设置中关闭某些非必要的信息收集功能。

5.5 注销账号
您可以在设置中申请注销账号，注销后我们将删除您的个人信息。`
    },
    {
      id: 'cookie',
      title: '六、Cookie 政策',
      content: `我们使用 Cookie 和类似技术来：
• 保持您的登录状态
• 记住您的偏好设置
• 分析网站流量和使用情况
• 提供个性化内容

您可以通过浏览器设置管理 Cookie，但这可能影响某些功能的使用。`
    },
    {
      id: 'children',
      title: '七、未成年人保护',
      content: `我们非常重视对未成年人个人信息的保护：
• 我们的服务面向18周岁以上用户
• 如您是未成年人，请在监护人指导下使用我们的服务
• 如我们发现在未获得监护人同意的情况下收集了未成年人的信息，将尽快删除`
    },
    {
      id: 'updates',
      title: '八、隐私政策的更新',
      content: `我们可能适时修订本隐私政策：
• 重大变更时，我们会通过站内信、邮件等方式通知您
• 您继续使用服务即表示同意修订后的政策
• 您有权选择停止使用服务`
    },
    {
      id: 'contact',
      title: '九、联系我们',
      content: `如您对本隐私政策有任何疑问、意见或建议，可通过以下方式联系我们：
• 隐私邮箱：privacy@zhiyiisland.com
• 客服邮箱：support@zhiyiisland.com
• 地址：中国北京市海淀区

我们将在15个工作日内回复您的请求。`
    }
  ])
</script>

<template>
  <div class="legal-page">
    <div class="page-header">
      <button class="btn btn-secondary" @click="goBack">
        <Icon icon="ri:arrow-left-line" />
        返回
      </button>
      <h1>隐私政策</h1>
      <span class="update-time">最后更新：{{ lastUpdated }}</span>
    </div>

    <div class="page-content">
      <div class="intro-section">
        <div class="intro-icon">
          <Icon icon="ri:shield-user-line" />
        </div>
        <p>知忆岛深知个人信息对您的重要性，我们将按照法律法规要求，采取相应安全保护措施，尽力保护您的个人信息安全可控。</p>
      </div>

      <div class="highlight-section">
        <div class="highlight-item">
          <Icon icon="ri:shield-check-line" />
          <div class="highlight-content">
            <h4>数据安全</h4>
            <p>采用业界标准加密技术</p>
          </div>
        </div>
        <div class="highlight-item">
          <Icon icon="ri:user-settings-line" />
          <div class="highlight-content">
            <h4>用户控制</h4>
            <p>您可随时管理个人信息</p>
          </div>
        </div>
        <div class="highlight-item">
          <Icon icon="ri:lock-line" />
          <div class="highlight-content">
            <h4>隐私保护</h4>
            <p>严格遵守相关法律法规</p>
          </div>
        </div>
      </div>

      <div class="toc-section">
        <h3>目录</h3>
        <div class="toc-list">
          <a v-for="section in sections" :key="section.id" :href="'#' + section.id" class="toc-item">
            {{ section.title }}
          </a>
        </div>
      </div>

      <div class="sections-container">
        <section v-for="section in sections" :key="section.id" :id="section.id" class="content-section">
          <h3 class="section-title">{{ section.title }}</h3>
          <div class="section-content" v-html="section.content.replace(/\n/g, '<br>')"></div>
        </section>
      </div>

      <div class="commitment-section">
        <h3 class="section-title">我们的承诺</h3>
        <div class="commitment-box">
          <Icon icon="ri:shield-check-line" />
          <p>我们承诺遵守《中华人民共和国个人信息保护法》《中华人民共和国网络安全法》等法律法规，保护您的个人信息安全。您的隐私对我们至关重要。</p>
        </div>
      </div>

    </div>
  </div>
</template>

<style scoped>
  .legal-page {
    max-width: 900px;
    margin: 20px auto 0 auto;
  }

  .page-header {
    display: flex;
    align-items: center;
    gap: 16px;
    margin-bottom: 24px;
    flex-wrap: wrap;
  }

  .page-header h1 {
    font-size: 24px;
    font-weight: 600;
    color: var(--text-color, #1f2937);
  }

  .update-time {
    font-size: 13px;
    color: var(--text-secondary, #9ca3af);
    margin-left: auto;
  }

  .page-content {
    display: flex;
    flex-direction: column;
    gap: 24px;
  }

  .intro-section {
    display: flex;
    align-items: center;
    gap: 16px;
    padding: 20px;
    background: var(--card-bg, white);
    border-radius: 12px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  }

  .intro-icon {
    width: 48px;
    height: 48px;
    background: linear-gradient(135deg, var(--primary-color) 0%, var(--secondary-color) 100%);
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 24px;
    color: white;
    flex-shrink: 0;
  }

  .intro-section p {
    font-size: 15px;
    color: var(--text-secondary, #6b7280);
    line-height: 1.8;
  }

  .highlight-section {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 16px;
  }

  .highlight-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 16px;
    background: var(--card-bg, white);
    border-radius: 12px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  }

  .highlight-item .iconify {
    font-size: 32px;
    color: var(--primary-color);
  }

  .highlight-content h4 {
    font-size: 14px;
    font-weight: 600;
    color: var(--text-color, #1f2937);
    margin-bottom: 2px;
  }

  .highlight-content p {
    font-size: 12px;
    color: var(--text-secondary, #6b7280);
  }

  .toc-section {
    background: var(--card-bg, white);
    border-radius: 12px;
    padding: 20px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  }

  .toc-section h3 {
    font-size: 16px;
    font-weight: 600;
    color: var(--text-color, #1f2937);
    margin-bottom: 16px;
  }

  .toc-list {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
  }

  .toc-item {
    padding: 8px 16px;
    background: var(--bg-tertiary, #f9fafb);
    border-radius: 6px;
    font-size: 13px;
    color: var(--text-secondary, #6b7280);
    text-decoration: none;
    transition: all 0.2s;
  }

  .toc-item:hover {
    background: var(--primary-color);
    color: white;
  }

  .sections-container {
    background: var(--card-bg, white);
    border-radius: 12px;
    padding: 24px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  }

  .content-section {
    margin-bottom: 32px;
    padding-bottom: 32px;
    border-bottom: 1px solid var(--border-color, #e5e7eb);
  }

  .content-section:last-child {
    margin-bottom: 0;
    padding-bottom: 0;
    border-bottom: none;
  }

  .section-title {
    font-size: 18px;
    font-weight: 600;
    color: var(--text-color, #1f2937);
    margin-bottom: 16px;
    padding-left: 12px;
    border-left: 3px solid var(--primary-color);
  }

  .section-content {
    font-size: 14px;
    color: var(--text-secondary, #6b7280);
    line-height: 1.8;
  }

  .commitment-section {
    background: var(--card-bg, white);
    border-radius: 12px;
    padding: 24px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  }

  .commitment-box {
    display: flex;
    align-items: flex-start;
    gap: 16px;
    padding: 20px;
    background: linear-gradient(135deg, rgba(18, 46, 138, 0.05) 0%, rgba(18, 46, 138, 0.1) 100%);
    border-radius: 12px;
    border: 1px solid rgba(18, 46, 138, 0.2);
  }

  .commitment-box .iconify {
    font-size: 32px;
    color: var(--primary-color);
    flex-shrink: 0;
  }

  .commitment-box p {
    font-size: 14px;
    color: var(--text-secondary, #6b7280);
    line-height: 1.8;
  }

  @media (max-width: 768px) {
    .update-time {
      margin-left: 0;
      width: 100%;
    }

    .highlight-section {
      grid-template-columns: 1fr;
    }
  }
</style>