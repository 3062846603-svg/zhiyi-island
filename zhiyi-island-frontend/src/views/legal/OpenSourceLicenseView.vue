<script setup>
  import { ref } from 'vue'
  import { useRouter } from 'vue-router'
  import { Icon } from '@iconify/vue'

  const router = useRouter()

  const goBack = () => {
    router.back()
  }

  const licenses = ref([
    {
      name: 'Vue.js',
      version: '3.x',
      license: 'MIT License',
      url: 'https://github.com/vuejs/vue/blob/main/LICENSE',
      description: '渐进式 JavaScript 框架，用于构建用户界面'
    },
    {
      name: 'Vite',
      version: '5.x',
      license: 'MIT License',
      url: 'https://github.com/vitejs/vite/blob/main/LICENSE',
      description: '下一代前端构建工具'
    },
    {
      name: 'Pinia',
      version: '2.x',
      license: 'MIT License',
      url: 'https://github.com/vuejs/pinia/blob/main/LICENSE',
      description: 'Vue 状态管理库'
    },
    {
      name: 'Vue Router',
      version: '4.x',
      license: 'MIT License',
      url: 'https://github.com/vuejs/vue-router/blob/main/LICENSE',
      description: 'Vue.js 官方路由'
    },
    {
      name: 'Axios',
      version: '1.x',
      license: 'MIT License',
      url: 'https://github.com/axios/axios/blob/master/LICENSE',
      description: '基于 Promise 的 HTTP 客户端'
    },
    {
      name: 'Element Plus',
      version: '2.x',
      license: 'MIT License',
      url: 'https://github.com/element-plus/element-plus/blob/dev/LICENSE',
      description: 'Vue 3 组件库'
    },
    {
      name: 'Iconify',
      version: '3.x',
      license: 'MIT License',
      url: 'https://github.com/iconify/iconify/blob/main/LICENSE',
      description: '统一的图标框架'
    },
    {
      name: 'Marked',
      version: '12.x',
      license: 'MIT License',
      url: 'https://github.com/markedjs/marked/blob/master/LICENSE.md',
      description: 'Markdown 解析器'
    },
    {
      name: 'DOMPurify',
      version: '3.x',
      license: 'MPL-2.0',
      url: 'https://github.com/cure53/DOMPurify/blob/main/LICENSE',
      description: 'HTML 净化库，防止 XSS 攻击'
    },
    {
      name: 'Highlight.js',
      version: '11.x',
      license: 'BSD-3-Clause',
      url: 'https://github.com/highlightjs/highlight.js/blob/main/LICENSE',
      description: '代码语法高亮'
    }
  ])

  const mitLicenseText = `MIT License

Copyright (c) [year] [fullname]

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.`

  const showLicense = ref(false)
  const selectedLicense = ref('')
</script>

<template>
  <div class="legal-page">
    <div class="page-header">
      <button class="btn btn-secondary" @click="goBack">
        <Icon icon="ri:arrow-left-line" />
        返回
      </button>
      <h1>开源许可</h1>
    </div>

    <div class="page-content">
      <div class="intro-section">
        <div class="intro-icon">
          <Icon icon="ri:code-box-line" />
        </div>
        <p>知忆岛基于众多优秀的开源项目构建。我们感谢这些开源社区的贡献者，以下是本项目中使用的开源软件及其许可证信息。</p>
      </div>

      <div class="licenses-section">
        <h3 class="section-title">开源软件列表</h3>
        <div class="licenses-grid">
          <div class="license-card" v-for="item in licenses" :key="item.name">
            <div class="license-header">
              <div class="license-icon">
                <Icon icon="ri:code-s-slash-line" />
              </div>
              <div class="license-info">
                <h4>{{ item.name }}</h4>
                <span class="version">v{{ item.version }}</span>
              </div>
            </div>
            <p class="license-desc">{{ item.description }}</p>
            <div class="license-footer">
              <span class="license-type">{{ item.license }}</span>
              <a :href="item.url" target="_blank" class="license-link">
                <Icon icon="ri:external-link-line" />
                查看许可
              </a>
            </div>
          </div>
        </div>
      </div>

      <div class="license-types-section">
        <h3 class="section-title">许可证类型说明</h3>
        <div class="types-grid">
          <div class="type-card">
            <div class="type-header">
              <span class="type-badge mit">MIT</span>
              <h4>MIT License</h4>
            </div>
            <p>MIT 许可证是一种宽松的开源许可证，允许用户自由使用、复制、修改、合并、发布、分发、再授权和销售软件副本。</p>
          </div>
          <div class="type-card">
            <div class="type-header">
              <span class="type-badge mpl">MPL-2.0</span>
              <h4>Mozilla Public License 2.0</h4>
            </div>
            <p>MPL 是一种弱 copyleft 许可证，允许代码与专有代码混合，但修改后的 MPL 代码必须保持开源。</p>
          </div>
          <div class="type-card">
            <div class="type-header">
              <span class="type-badge bsd">BSD</span>
              <h4>BSD 3-Clause License</h4>
            </div>
            <p>BSD 3-Clause 许可证允许自由使用和分发，但禁止使用项目名称进行推广，且需保留版权声明。</p>
          </div>
        </div>
      </div>

      <div class="resources-section">
        <h3 class="section-title">其他资源</h3>
        <div class="resources-list">
          <div class="resource-item">
            <Icon icon="ri:font-size" />
            <div class="resource-info">
              <h4>字体资源</h4>
              <p>本项目使用的字体为系统默认字体，无需额外授权。</p>
            </div>
          </div>
          <div class="resource-item">
            <Icon icon="ri:remixicon-line" />
            <div class="resource-info">
              <h4>图标资源</h4>
              <p>使用 Remix Icon 图标库，遵循 Apache 2.0 许可证。</p>
            </div>
          </div>
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
  }

  .page-header h1 {
    font-size: 24px;
    font-weight: 600;
    color: var(--text-color, #1f2937);
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

  .section-title {
    font-size: 18px;
    font-weight: 600;
    color: var(--text-color, #1f2937);
    margin-bottom: 20px;
    padding-left: 12px;
    border-left: 3px solid var(--primary-color);
  }

  .licenses-section {
    background: var(--card-bg, white);
    border-radius: 12px;
    padding: 24px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  }

  .licenses-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 16px;
  }

  .license-card {
    padding: 20px;
    background: var(--bg-tertiary, #f9fafb);
    border-radius: 12px;
    transition: all 0.2s;
  }

  .license-card:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  }

  .license-header {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 12px;
  }

  .license-icon {
    width: 40px;
    height: 40px;
    background: linear-gradient(135deg, var(--primary-color) 0%, var(--secondary-color) 100%);
    border-radius: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 20px;
    color: white;
  }

  .license-info h4 {
    font-size: 16px;
    font-weight: 600;
    color: var(--text-color, #1f2937);
    margin-bottom: 2px;
  }

  .version {
    font-size: 12px;
    color: var(--text-secondary, #9ca3af);
  }

  .license-desc {
    font-size: 14px;
    color: var(--text-secondary, #6b7280);
    line-height: 1.6;
    margin-bottom: 12px;
  }

  .license-footer {
    display: flex;
    align-items: center;
    justify-content: space-between;
  }

  .license-type {
    font-size: 12px;
    padding: 4px 10px;
    background: rgba(18, 46, 138, 0.1);
    color: var(--primary-color);
    border-radius: 4px;
    font-weight: 500;
  }

  .license-link {
    display: flex;
    align-items: center;
    gap: 4px;
    font-size: 13px;
    color: var(--primary-color);
    text-decoration: none;
    transition: all 0.2s;
  }

  .license-link:hover {
    text-decoration: underline;
  }

  .license-types-section {
    background: var(--card-bg, white);
    border-radius: 12px;
    padding: 24px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  }

  .types-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 16px;
  }

  .type-card {
    padding: 20px;
    background: var(--bg-tertiary, #f9fafb);
    border-radius: 12px;
  }

  .type-header {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-bottom: 12px;
  }

  .type-badge {
    padding: 4px 8px;
    border-radius: 4px;
    font-size: 11px;
    font-weight: 600;
    text-transform: uppercase;
  }

  .type-badge.mit {
    background: #dcfce7;
    color: #16a34a;
  }

  .type-badge.mpl {
    background: #fef3c7;
    color: #d97706;
  }

  .type-badge.bsd {
    background: #dbeafe;
    color: #2563eb;
  }

  .type-header h4 {
    font-size: 14px;
    font-weight: 600;
    color: var(--text-color, #1f2937);
  }

  .type-card p {
    font-size: 13px;
    color: var(--text-secondary, #6b7280);
    line-height: 1.6;
  }

  .resources-section {
    background: var(--card-bg, white);
    border-radius: 12px;
    padding: 24px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  }

  .resources-list {
    display: flex;
    flex-direction: column;
    gap: 12px;
  }

  .resource-item {
    display: flex;
    align-items: flex-start;
    gap: 16px;
    padding: 16px;
    background: var(--bg-tertiary, #f9fafb);
    border-radius: 10px;
  }

  .resource-item .iconify {
    font-size: 24px;
    color: var(--primary-color);
    margin-top: 2px;
  }

  .resource-info h4 {
    font-size: 15px;
    font-weight: 600;
    color: var(--text-color, #1f2937);
    margin-bottom: 4px;
  }

  .resource-info p {
    font-size: 14px;
    color: var(--text-secondary, #6b7280);
  }

  @media (max-width: 768px) {
    .licenses-grid {
      grid-template-columns: 1fr;
    }

    .types-grid {
      grid-template-columns: 1fr;
    }

    .links-grid {
      grid-template-columns: 1fr;
    }
  }
</style>