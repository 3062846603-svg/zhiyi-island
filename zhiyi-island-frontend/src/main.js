/**
 * 应用入口文件
 * 初始化 Vue 应用、Pinia 状态管理、路由和 Element Plus
 */
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'

import App from './App.vue'
import router from './router'

const pinia = createPinia()

pinia.use(({ store }) => {
  if (store.$id === 'auth') {
    const savedState = sessionStorage.getItem('auth_state')
    if (savedState) {
      const parsed = JSON.parse(savedState)
      store.$patch(parsed)
    }

    store.$subscribe((mutation, state) => {
      sessionStorage.setItem('auth_state', JSON.stringify({
        token: state.token,
        userInfo: state.userInfo,
      }))
    })
  }
})

const app = createApp(App)

app.use(pinia)
app.use(router)
app.use(ElementPlus)

app.mount('#app')
