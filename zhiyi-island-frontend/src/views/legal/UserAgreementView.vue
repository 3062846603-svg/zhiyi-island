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
      id: 'acceptance',
      title: '一、服务条款的接受',
      content: `欢迎使用知忆岛！在使用本服务之前，请您仔细阅读以下条款。

1.1 本协议是您与知忆岛之间关于使用本服务所订立的协议。请您仔细阅读本协议，您点击"同意"按钮即表示您已阅读并同意本协议的约束。

1.2 如果您不同意本协议的任何条款，请您立即停止使用本服务。您继续使用本服务即表示您接受本协议的全部条款。

1.3 我们有权在必要时修改本协议条款。您可以在相关服务页面查阅最新版本的协议条款。`
    },
    {
      id: 'account',
      title: '二、账号注册与使用',
      content: `2.1 账号注册
• 您需要注册账号才能使用本服务的部分功能
• 您应当提供真实、准确、完整的个人资料
• 您应当及时更新您的个人资料，确保其真实性

2.2 账号安全
• 您有责任妥善保管账号和密码
• 您应当对账号下的所有行为负责
• 如发现账号被盗用，应立即通知我们

2.3 账号使用规则
• 每位用户仅允许注册一个账号
• 禁止出售、转让或出租账号
• 禁止使用虚假身份注册账号`
    },
    {
      id: 'user-conduct',
      title: '三、用户行为规范',
      content: `3.1 您承诺不会利用本服务从事以下行为：
• 违反中国法律法规的行为
• 侵犯他人知识产权或其他合法权益的行为
• 传播病毒、木马或其他恶意程序
• 干扰或破坏服务的正常运行
• 收集或存储他人个人信息
• 发送垃圾信息或进行网络攻击

3.2 内容规范
• 您上传的内容不得含有违法信息
• 不得侵犯他人的著作权、商标权等
• 不得传播虚假信息或谣言
• 不得发布广告或商业推广信息

3.3 违规处理
• 我们有权删除违规内容
• 我们有权暂停或终止违规账号
• 情节严重者将移交司法机关处理`
    },
    {
      id: 'intellectual-property',
      title: '四、知识产权',
      content: `4.1 本服务的知识产权
• 知忆岛服务中的所有内容（包括但不限于软件、技术、程序、网页、文字、图片、音频、视频等）的知识产权归知忆岛所有
• 未经授权，任何人不得使用上述内容

4.2 用户内容的知识产权
• 您上传的内容（笔记、文档等）的知识产权归您所有
• 您授权我们使用、存储、展示您上传的内容以提供服务
• 您保证上传的内容不侵犯他人的知识产权

4.3 许可授权
• 我们授予您有限的、非排他的、不可转让的许可，允许您使用本服务
• 您不得对本服务进行反向工程、反编译或反汇编`
    },
    {
      id: 'privacy',
      title: '五、隐私保护',
      content: `5.1 我们重视用户隐私保护，具体内容请参见《隐私政策》。

5.2 我们收集和使用您的个人信息遵循以下原则：
• 合法、正当、必要的原则
• 公开、透明的原则
• 最小化原则

5.3 您可以在设置中查看和管理您的个人信息。`
    },
    {
      id: 'service',
      title: '六、服务内容与变更',
      content: `6.1 服务内容
• 知忆岛提供知识管理、AI摘要、笔记存储等服务
• 具体服务内容以实际提供为准

6.2 服务变更
• 我们有权随时变更、中断或终止部分或全部服务
• 如发生变更，我们将尽可能提前通知您
• 我们不对服务变更造成的损失承担责任

6.3 服务中断
• 因系统维护、升级等原因可能导致服务中断
• 我们将尽量避免服务中断并提前通知`
    },
    {
      id: 'disclaimer',
      title: '七、免责声明',
      content: `7.1 您理解并同意，在以下情况下我们不承担责任：
• 不可抗力导致的服务中断
• 您的过错导致的损失
• 第三方原因导致的损失
• 您违反本协议导致的损失

7.2 我们对以下内容不作保证：
• 服务不会中断或无错误
• 服务内容完全准确
• 服务结果完全符合您的期望

7.3 我们的责任限制：
• 在适用法律允许的最大范围内，我们的责任以您支付的服务费用为限`
    },
    {
      id: 'termination',
      title: '八、协议终止',
      content: `8.1 您可以随时注销账号终止本协议。

8.2 如您违反本协议，我们有权暂停或终止对您的服务。

8.3 协议终止后：
• 您使用本服务的权利立即终止
• 我们有权删除您的账号信息和数据
• 本协议中关于知识产权、免责声明等条款继续有效`
    },
    {
      id: 'dispute',
      title: '九、争议解决',
      content: `9.1 本协议的订立、执行、解释及争议解决均适用中华人民共和国法律。

9.2 如发生争议，双方应友好协商解决。

9.3 协商不成的，任何一方可向我们所在地人民法院提起诉讼。`
    },
    {
      id: 'miscellaneous',
      title: '十、其他条款',
      content: `10.1 本协议中的任何条款如被认定无效，不影响其他条款的效力。

10.2 我们未行使或延迟行使本协议项下的任何权利，不构成对该权利的放弃。

10.3 本协议标题仅供阅读方便，不影响条款的含义或解释。

10.4 如有任何疑问，请联系我们：
• 邮箱：legal@zhiyiisland.com
• 地址：中国北京市海淀区`
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
      <h1>用户协议</h1>
      <span class="update-time">最后更新：{{ lastUpdated }}</span>
    </div>

    <div class="page-content">
      <div class="intro-section">
        <div class="intro-icon">
          <Icon icon="ri:file-text-line" />
        </div>
        <p>欢迎使用知忆岛！请您在使用本服务前仔细阅读以下条款。使用本服务即表示您同意遵守本协议的全部内容。</p>
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

  @media (max-width: 768px) {
    .update-time {
      margin-left: 0;
      width: 100%;
    }
  }
</style>