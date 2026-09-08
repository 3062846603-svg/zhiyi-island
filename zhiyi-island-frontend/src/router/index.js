/**
 * 路由配置文件
 * 定义应用的所有路由规则和导航守卫
 */
import { createRouter, createWebHistory } from 'vue-router'

/** 页面组件导入 */
import MainLayout from '@/views/layout/MainLayout.vue'
import LoginView from '@/views/login/LoginView.vue'
import HomeView from '@/views/home/HomeView.vue'
import NotesView from '@/views/notes/NotesView.vue'
import NoteDetailView from '@/views/notes/NoteDetailView.vue'
import KnowledgeView from '@/views/knowledge/KnowledgeView.vue'
import KnowledgeDetailView from '@/views/knowledge/KnowledgeDetailView.vue'
import SearchView from '@/views/search/SearchView.vue'
import ProfileView from '@/views/profile/ProfileView.vue'
import AddNoteView from '@/views/home/AddNoteView.vue'
import ImportView from '@/views/home/ImportView.vue'
import AISummaryView from '@/views/home/AISummaryView.vue'
import KnowledgeGraphView from '@/views/home/KnowledgeGraphView.vue'
import ProfileSettingsView from '@/views/profile/ProfileSettingsView.vue'
import PasswordSettingsView from '@/views/profile/PasswordSettingsView.vue'
import ExportDataView from '@/views/profile/ExportDataView.vue'
import EmailBindView from '@/views/profile/EmailBindView.vue'
import NotificationView from '@/views/notification/NotificationView.vue'
import DraftView from '@/views/drafts/DraftView.vue'
import AboutView from '@/views/legal/AboutView.vue'
import UserAgreementView from '@/views/legal/UserAgreementView.vue'
import PrivacyPolicyView from '@/views/legal/PrivacyPolicyView.vue'
import OpenSourceLicenseView from '@/views/legal/OpenSourceLicenseView.vue'

/** 创建路由实例 */
const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: LoginView,
      meta: { title: '登录', requiresAuth: false },
    },
    {
      path: '/',
      component: MainLayout,
      redirect: '/home',
      children: [
        {
          path: '/home',
          name: 'home',
          component: HomeView,
          meta: { title: '首页', icon: 'ri:home-4-line' },
        },
        {
          path: '/notes',
          name: 'notes',
          component: NotesView,
          meta: { title: '笔记', icon: 'ri:book-3-line' },
        },
        {
          path: '/notes/:id',
          name: 'note-detail',
          component: NoteDetailView,
          meta: { title: '笔记详情', icon: 'ri:book-3-line' },
        },
        {
          path: '/knowledge',
          name: 'knowledge',
          component: KnowledgeView,
          meta: { title: '知识库', icon: 'ri:brain-line' },
        },
        {
          path: '/knowledge/:id',
          name: 'knowledge-detail',
          component: KnowledgeDetailView,
          meta: { title: '知识详情', icon: 'ri:brain-line' },
        },
        {
          path: '/search',
          name: 'search',
          component: SearchView,
          meta: { title: '记忆搜索', icon: 'ri:search-line' },
        },
        {
          path: '/profile',
          name: 'profile',
          component: ProfileView,
          meta: { title: '个人中心', icon: 'ri:user-3-line' },
        },
        {
          path: '/home/add-note',
          name: 'add-note',
          component: AddNoteView,
          meta: { title: '添加笔记', icon: 'ri:add-circle-line' },
        },
        {
          path: '/home/import',
          name: 'import',
          component: ImportView,
          meta: { title: '导入文档', icon: 'ri:file-upload-line' },
        },
        {
          path: '/home/ai-summary',
          name: 'ai-summary',
          component: AISummaryView,
          meta: { title: 'AI 摘要', icon: 'ri:ai-generate' },
        },
        {
          path: '/home/knowledge-graph',
          name: 'knowledge-graph',
          component: KnowledgeGraphView,
          meta: { title: '知识图谱', icon: 'ri:bubble-chart-line' },
        },
        {
          path: '/profile/settings',
          name: 'profile-settings',
          component: ProfileSettingsView,
          meta: { title: '个人资料', icon: 'ri:user-settings-line' },
        },
        {
          path: '/profile/password',
          name: 'password-settings',
          component: PasswordSettingsView,
          meta: { title: '修改密码', icon: 'ri:lock-password-line' },
        },
        {
          path: '/profile/export',
          name: 'export-data',
          component: ExportDataView,
          meta: { title: '导出数据', icon: 'ri:file-download-line' },
        },
        {
          path: '/profile/email',
          name: 'email-bind',
          component: EmailBindView,
          meta: { title: '换绑邮箱', icon: 'ri:mail-line' },
        },
        {
          path: '/notification',
          name: 'notification',
          component: NotificationView,
          meta: { title: '通知中心', icon: 'ri:notification-3-line' },
        },
        {
          path: '/drafts',
          name: 'drafts',
          component: DraftView,
          meta: { title: '草稿箱', icon: 'ri:draft-line' },
        },
      ],
    },
    {
      path: '/legal/about',
      name: 'about',
      component: AboutView,
      meta: { title: '关于我们', requiresAuth: false },
    },
    {
      path: '/legal/user-agreement',
      name: 'user-agreement',
      component: UserAgreementView,
      meta: { title: '用户协议', requiresAuth: false },
    },
    {
      path: '/legal/privacy',
      name: 'privacy-policy',
      component: PrivacyPolicyView,
      meta: { title: '隐私政策', requiresAuth: false },
    },
    {
      path: '/legal/opensource',
      name: 'opensource-license',
      component: OpenSourceLicenseView,
      meta: { title: '开源许可', requiresAuth: false },
    },
  ],
})

router.beforeEach((to) => {
  const authState = sessionStorage.getItem('auth_state')
  const token = authState ? JSON.parse(authState).token : null
  const requiresAuth = to.meta.requiresAuth !== false
  const isLoginPage = to.path === '/login'

  if (requiresAuth && !token && !isLoginPage) {
    return '/login'
  } else if (token && isLoginPage) {
    return '/home'
  }
})

export default router
