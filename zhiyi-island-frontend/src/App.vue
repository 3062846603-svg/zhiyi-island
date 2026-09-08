<!--
  应用根组件
  定义全局CSS变量和主题样式
-->
<script setup>
  import { onMounted } from 'vue'

  onMounted(() => {
    const detectScrollbarWidth = () => {
      const outer = document.createElement('div')
      outer.style.cssText = 'visibility:hidden;overflow:scroll;position:absolute;top:-9999px;width:100px'
      document.body.appendChild(outer)
      const inner = document.createElement('div')
      inner.style.width = '100%'
      outer.appendChild(inner)
      const scrollbarWidth = outer.offsetWidth - inner.offsetWidth
      document.body.removeChild(outer)
      document.documentElement.style.setProperty('--scrollbar-width', `${scrollbarWidth}px`)
      return scrollbarWidth
    }

    const checkScrollbarGutterSupport = () => {
      const testEl = document.createElement('div')
      testEl.style.scrollbarGutter = 'stable'
      return testEl.style.scrollbarGutter === 'stable'
    }

    if (!checkScrollbarGutterSupport()) {
      detectScrollbarWidth()
      document.documentElement.classList.add('no-scrollbar-gutter')
    }
  })
</script>

<template>
  <router-view />
</template>

<style>
  :root {
    --primary-color: #122E8A;
    --secondary-color: #122E8A;
    --accent-color: #122E8A;
    --bg-color: #ffffff;
    --bg-secondary: #ffffff;
    --bg-tertiary: #f3f4f6;
    --text-color: #122E8A;
    --text-secondary: #5a6b8a;
    --border-color: #e5e7eb;
    --card-bg: #ffffff;
    --font-size-base: 16px;
    --spacing-unit: 8px;
    --font-size-page-title: 28px;
    --font-size-section-title: 18px;
    --font-size-card-title: 16px;
    --font-size-body: 14px;
    --font-size-small: 13px;
    --font-size-xs: 12px;
    --font-size-stat-value: 24px;
  }

  * {
    margin: 0;
    padding: 0;
    box-sizing: border-box;
  }

  html {
    font-size: var(--font-size-base);
    overflow-y: scroll;
    scrollbar-gutter: stable;
    background-color: #ffffff;
    min-height: 100vh;
  }

  body {
    font-family:
      -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
    -webkit-font-smoothing: antialiased;
    -moz-osx-font-smoothing: grayscale;
    background-color: #ffffff;
    color: var(--text-color);
    transition:
      background-color 0.3s,
      color 0.3s;
    overflow-x: hidden;
    scrollbar-color: var(--border-color) #ffffff;
    min-height: 100vh;
  }

  #app {
    min-height: 100vh;
    background-color: var(--bg-color);
  }

  .no-scrollbar-gutter body {
    padding-right: var(--scrollbar-width, 0);
  }

  .no-animations * {
    animation: none !important;
    transition: none !important;
  }

  button {
    font-family: inherit;
  }

  /* ========== 全局按钮样式系统 ========== */

  /* 基础按钮样式 */
  .btn {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    gap: 6px;
    padding: 10px 20px;
    font-size: 14px;
    font-weight: 500;
    border-radius: 12px;
    cursor: pointer;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    border: 1px solid transparent;
    text-decoration: none;
    white-space: nowrap;
    user-select: none;
    -webkit-tap-highlight-color: transparent;
  }

  .btn:disabled {
    opacity: 0.6;
    cursor: not-allowed;
    transform: none !important;
    box-shadow: none !important;
  }

  /* 主要按钮 - Primary */
  .btn-primary {
    background: linear-gradient(145deg, var(--primary-color) 0%, var(--secondary-color) 100%);
    color: white;
    border-color: rgba(255, 255, 255, 0.1);
    box-shadow:
      0 4px 12px rgba(18, 46, 138, 0.25),
      0 8px 24px rgba(18, 46, 138, 0.15),
      inset 0 1px 0 rgba(255, 255, 255, 0.2);
    text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
  }

  .btn-primary:hover:not(:disabled) {
    transform: translateY(-2px);
    box-shadow:
      0 8px 20px rgba(18, 46, 138, 0.35),
      0 16px 40px rgba(18, 46, 138, 0.2),
      inset 0 1px 0 rgba(255, 255, 255, 0.3);
  }

  .btn-primary:active:not(:disabled) {
    transform: translateY(0);
    box-shadow:
      0 2px 8px rgba(18, 46, 138, 0.25),
      inset 0 2px 4px rgba(0, 0, 0, 0.1);
  }

  /* 次要按钮 - Secondary */
  .btn-secondary {
    background: linear-gradient(145deg, #ffffff 0%, #f8fafc 100%);
    color: #374151;
    border-color: rgba(0, 0, 0, 0.08);
    box-shadow:
      0 2px 4px rgba(0, 0, 0, 0.04),
      0 4px 8px rgba(0, 0, 0, 0.02),
      inset 0 1px 0 rgba(255, 255, 255, 0.8);
  }

  .btn-secondary:hover:not(:disabled) {
    background: linear-gradient(145deg, #f0f4ff 0%, #e8edfa 100%);
    border-color: var(--primary-color);
    color: var(--primary-color);
    transform: translateY(-2px);
    box-shadow:
      0 4px 12px rgba(18, 46, 138, 0.15),
      0 8px 24px rgba(18, 46, 138, 0.08),
      inset 0 1px 0 rgba(255, 255, 255, 0.9);
  }

  .btn-secondary:active:not(:disabled) {
    transform: translateY(0);
    box-shadow:
      0 1px 2px rgba(0, 0, 0, 0.04),
      inset 0 1px 2px rgba(0, 0, 0, 0.06);
  }

  /* 成功按钮 - Success */
  .btn-success {
    background: linear-gradient(145deg, #16a34a 0%, #15803d 100%);
    color: white;
    border-color: rgba(255, 255, 255, 0.1);
    box-shadow:
      0 4px 12px rgba(22, 163, 74, 0.25),
      0 8px 24px rgba(22, 163, 74, 0.15),
      inset 0 1px 0 rgba(255, 255, 255, 0.2);
    text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
  }

  .btn-success:hover:not(:disabled) {
    transform: translateY(-2px);
    box-shadow:
      0 8px 20px rgba(22, 163, 74, 0.35),
      0 16px 40px rgba(22, 163, 74, 0.2),
      inset 0 1px 0 rgba(255, 255, 255, 0.3);
  }

  .btn-success:active:not(:disabled) {
    transform: translateY(0);
    box-shadow:
      0 2px 8px rgba(22, 163, 74, 0.25),
      inset 0 2px 4px rgba(0, 0, 0, 0.1);
  }

  /* 危险按钮 - Danger */
  .btn-danger {
    background: linear-gradient(145deg, #dc2626 0%, #b91c1c 100%);
    color: white;
    border-color: rgba(255, 255, 255, 0.1);
    box-shadow:
      0 4px 12px rgba(220, 38, 38, 0.25),
      0 8px 24px rgba(220, 38, 38, 0.15),
      inset 0 1px 0 rgba(255, 255, 255, 0.2);
    text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
  }

  .btn-danger:hover:not(:disabled) {
    transform: translateY(-2px);
    box-shadow:
      0 8px 20px rgba(220, 38, 38, 0.35),
      0 16px 40px rgba(220, 38, 38, 0.2),
      inset 0 1px 0 rgba(255, 255, 255, 0.3);
  }

  .btn-danger:active:not(:disabled) {
    transform: translateY(0);
    box-shadow:
      0 2px 8px rgba(220, 38, 38, 0.25),
      inset 0 2px 4px rgba(0, 0, 0, 0.1);
  }

  /* 警告按钮 - Warning */
  .btn-warning {
    background: linear-gradient(145deg, #f59e0b 0%, #d97706 100%);
    color: white;
    border-color: rgba(255, 255, 255, 0.1);
    box-shadow:
      0 4px 12px rgba(245, 158, 11, 0.25),
      0 8px 24px rgba(245, 158, 11, 0.15),
      inset 0 1px 0 rgba(255, 255, 255, 0.2);
    text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
  }

  .btn-warning:hover:not(:disabled) {
    transform: translateY(-2px);
    box-shadow:
      0 8px 20px rgba(245, 158, 11, 0.35),
      0 16px 40px rgba(245, 158, 11, 0.2),
      inset 0 1px 0 rgba(255, 255, 255, 0.3);
  }

  .btn-warning:active:not(:disabled) {
    transform: translateY(0);
    box-shadow:
      0 2px 8px rgba(245, 158, 11, 0.25),
      inset 0 2px 4px rgba(0, 0, 0, 0.1);
  }

  /* 幽灵按钮 - Ghost */
  .btn-ghost {
    background: transparent;
    color: var(--primary-color);
    border-color: var(--primary-color);
    box-shadow: none;
  }

  .btn-ghost:hover:not(:disabled) {
    background: linear-gradient(145deg, rgba(18, 46, 138, 0.08) 0%, rgba(18, 46, 138, 0.04) 100%);
    transform: translateY(-1px);
    box-shadow: 0 2px 8px rgba(18, 46, 138, 0.1);
  }

  .btn-ghost:active:not(:disabled) {
    transform: translateY(0);
    background: rgba(18, 46, 138, 0.12);
  }

  /* 文本按钮 - Text */
  .btn-text {
    background: transparent;
    color: var(--primary-color);
    border-color: transparent;
    box-shadow: none;
    padding: 8px 12px;
  }

  .btn-text:hover:not(:disabled) {
    background: rgba(18, 46, 138, 0.08);
  }

  .btn-text:active:not(:disabled) {
    background: rgba(18, 46, 138, 0.12);
  }

  /* 按钮尺寸 */
  .btn-sm {
    padding: 6px 14px;
    font-size: 13px;
    border-radius: 8px;
  }

  .btn-lg {
    padding: 14px 28px;
    font-size: 16px;
    border-radius: 14px;
    height: 52px;
    min-height: 52px;
  }

  .btn-block {
    width: 100%;
    display: flex;
    justify-content: center;
    align-items: center;
  }

  /* 图标按钮 */
  .btn-icon {
    width: 36px;
    height: 36px;
    padding: 0;
    border-radius: 10px;
  }

  .btn-icon.btn-sm {
    width: 30px;
    height: 30px;
    border-radius: 8px;
  }

  .btn-icon.btn-lg {
    width: 44px;
    height: 44px;
    border-radius: 12px;
  }

  /* 圆形按钮 */
  .btn-circle {
    border-radius: 50%;
  }

  /* 加载状态 */
  .btn-loading {
    position: relative;
    color: transparent !important;
    pointer-events: none;
  }

  .btn-loading::after {
    content: '';
    position: absolute;
    width: 16px;
    height: 16px;
    border: 2px solid rgba(255, 255, 255, 0.3);
    border-top-color: white;
    border-radius: 50%;
    animation: btn-spin 0.8s linear infinite;
  }

  @keyframes btn-spin {
    to {
      transform: rotate(360deg);
    }
  }

  /* 按钮组 */
  .btn-group {
    display: inline-flex;
    gap: 8px;
  }

  .btn-group .btn {
    border-radius: 12px;
  }

  /* 响应式调整 */
  @media (max-width: 768px) {
    .btn {
      padding: 8px 16px;
      font-size: 13px;
    }

    .btn-lg {
      padding: 12px 24px;
      font-size: 15px;
      height: 48px;
      min-height: 48px;
    }

    .btn-icon {
      width: 32px;
      height: 32px;
    }
  }

  ::-webkit-scrollbar {
    width: 6px;
    height: 6px;
  }

  ::-webkit-scrollbar-track {
    background: #ffffff;
  }

  ::-webkit-scrollbar-thumb {
    background: var(--border-color);
    border-radius: 3px;
  }

  ::-webkit-scrollbar-thumb:hover {
    background: var(--text-secondary);
  }

  input[type="password"]::-ms-reveal {
    display: none;
  }

  input::-webkit-credentials-auto-fill-button {
    visibility: hidden;
    pointer-events: none;
    position: absolute;
    right: 0;
  }
</style>