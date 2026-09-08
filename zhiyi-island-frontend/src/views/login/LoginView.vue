<!--
  登录/注册视图组件
  提供用户登录、注册和找回密码功能
-->
<script setup>
  import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
  import { useRouter } from 'vue-router'
  import { Icon } from '@iconify/vue'
  import { ElTooltip } from 'element-plus'
  import { useAuthStore } from '@/stores/auth'
  import { useToastStore } from '@/stores/toast'
  import { userApi, authApi } from '@/api/user'
  import { resetRedirectFlag } from '@/utils/request'
  import { ValidationRules } from '@/utils/validation'

  const router = useRouter()
  const authStore = useAuthStore()
  const toast = useToastStore()

  /** 当前表单类型：login/register/forgot */
  const currentForm = ref('login')

  /** 登录方式：password/code */
  const loginMethod = ref('password')

  /** 高度过渡动画相关 */
  const loginContentRef = ref(null)

  const onBeforeEnter = (el) => {
    el.style.opacity = '0'
    el.style.transform = 'translateX(20px)'
  }

  const onEnter = (el, done) => {
    const newHeight = el.scrollHeight
    const wrapper = el.parentElement
    if (wrapper) {
      wrapper.style.height = newHeight + 'px'
      wrapper.style.transition = 'height 0.4s ease'
    }
    requestAnimationFrame(() => {
      el.style.transition = 'opacity 0.3s ease, transform 0.3s ease'
      el.style.opacity = '1'
      el.style.transform = 'translateX(0)'
    })
    setTimeout(done, 400)
  }

  const onLeave = (el, done) => {
    const wrapper = el.parentElement
    if (wrapper) {
      wrapper.style.height = el.scrollHeight + 'px'
    }
    el.style.transition = 'opacity 0.3s ease, transform 0.3s ease'
    el.style.opacity = '0'
    el.style.transform = 'translateX(-20px)'
    setTimeout(done, 300)
  }

  /** 登录表单数据 */
  const loginForm = ref({
    username: '',
    password: '',
    email: '',
    verifyCode: '',
    remember: false,
  })

  /** 注册表单数据 */
  const registerForm = ref({
    email: '',
    password: '',
    confirmPassword: '',
    verifyCode: '',
    agree: false,
  })

  /** 找回密码表单数据 */
  const forgotForm = ref({
    email: '',
    code: '',
    newPassword: '',
    confirmPassword: '',
  })

  /** 密码显示状态 */
  const showPassword = ref(false)
  const showConfirmPassword = ref(false)
  const showNewPassword = ref(false)
  const showForgotConfirmPassword = ref(false)
  const isLoading = ref(false)
  const sendingCode = ref(false)

  /** 验证码倒计时 */
  const loginCountdown = ref(0)
  const registerCountdown = ref(0)
  const forgotCountdown = ref(0)
  let loginCountdownTimer = null
  let registerCountdownTimer = null
  let forgotCountdownTimer = null

  /** 滑块验证相关状态 */
  const sliderVerified = ref(false)
  const sliderPosition = ref(-2)
  const isDragging = ref(false)
  const sliderStartX = ref(0)
  const sliderTrackRef = ref(null)

  const getSliderWidth = () => {
    return sliderTrackRef.value ? sliderTrackRef.value.offsetWidth : 280
  }

  /** 表单验证 */
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

  const loginEmailBlurred = ref(false)
  const registerEmailBlurred = ref(false)
  const forgotEmailBlurred = ref(false)
  const registerPasswordBlurred = ref(false)
  const registerConfirmPasswordBlurred = ref(false)
  const forgotPasswordBlurred = ref(false)
  const forgotConfirmPasswordBlurred = ref(false)

  const loginEmailValid = computed(() => {
    return emailRegex.test(loginForm.value.email)
  })

  const loginEmailInvalid = computed(() => {
    return loginEmailBlurred.value && loginForm.value.email && !emailRegex.test(loginForm.value.email)
  })

  const registerEmailValid = computed(() => {
    return emailRegex.test(registerForm.value.email)
  })

  const registerEmailInvalid = computed(() => {
    return registerEmailBlurred.value && registerForm.value.email && !emailRegex.test(registerForm.value.email)
  })

  const forgotEmailValid = computed(() => {
    return emailRegex.test(forgotForm.value.email)
  })

  const forgotEmailInvalid = computed(() => {
    return forgotEmailBlurred.value && forgotForm.value.email && !emailRegex.test(forgotForm.value.email)
  })

  const forgotPasswordValid = computed(() => {
    return forgotForm.value.newPassword.length >= ValidationRules.password.minLength && forgotForm.value.newPassword.length <= ValidationRules.password.maxLength
  })

  const forgotPasswordMatch = computed(() => {
    return forgotForm.value.newPassword === forgotForm.value.confirmPassword && forgotForm.value.confirmPassword !== ''
  })

  const forgotPasswordInvalid = computed(() => {
    return forgotPasswordBlurred.value && forgotForm.value.newPassword && !forgotPasswordValid.value
  })

  const forgotConfirmPasswordInvalid = computed(() => {
    return forgotConfirmPasswordBlurred.value && forgotForm.value.confirmPassword && !forgotPasswordMatch.value
  })

  const registerPasswordValid = computed(() => {
    return registerForm.value.password.length >= ValidationRules.password.minLength && registerForm.value.password.length <= ValidationRules.password.maxLength
  })

  const registerPasswordMatch = computed(() => {
    return registerForm.value.password === registerForm.value.confirmPassword && registerForm.value.confirmPassword !== ''
  })

  const registerPasswordInvalid = computed(() => {
    return registerPasswordBlurred.value && registerForm.value.password && !registerPasswordValid.value
  })

  const registerConfirmPasswordInvalid = computed(() => {
    return registerConfirmPasswordBlurred.value && registerForm.value.confirmPassword && !registerPasswordMatch.value
  })

  const canSendLoginCode = computed(() => {
    return loginEmailValid.value && sliderVerified.value && loginCountdown.value === 0
  })

  const canSendRegisterCode = computed(() => {
    return registerEmailValid.value && sliderVerified.value && registerCountdown.value === 0
  })

  const canSendForgotCode = computed(() => {
    return forgotEmailValid.value && sliderVerified.value && forgotCountdown.value === 0
  })

  const canSubmitForgot = computed(() => {
    return forgotEmailValid.value &&
      forgotForm.value.code &&
      forgotPasswordValid.value &&
      forgotPasswordMatch.value
  })

  /** 表单标题 */
  const formTitle = computed(() => {
    const titles = {
      login: '欢迎回来',
      register: '创建账户',
      forgot: '找回密码',
    }
    return titles[currentForm.value]
  })

  const formSubtitle = computed(() => {
    const subtitles = {
      login: '登录您的知忆岛账户',
      register: '开始您的知识管理之旅',
      forgot: '重置您的账户密码',
    }
    return subtitles[currentForm.value]
  })

  /** 切换表单类型 */
  const switchForm = (form) => {
    currentForm.value = form
    sliderVerified.value = false
    sliderPosition.value = -2
    loginMethod.value = 'password'
    loginEmailBlurred.value = false
    registerEmailBlurred.value = false
    forgotEmailBlurred.value = false
  }

  /** 切换登录方式 */
  const switchLoginMethod = (method) => {
    loginMethod.value = method
    sliderVerified.value = false
    sliderPosition.value = -2
    loginEmailBlurred.value = false
  }

  /** 滑块鼠标按下事件 */
  const handleSliderMouseDown = (e) => {
    if (sliderVerified.value) return
    isDragging.value = true
    sliderStartX.value = e.clientX - sliderPosition.value
  }

  /** 滑块鼠标移动事件 */
  const handleSliderMouseMove = (e) => {
    if (!isDragging.value) return
    const newPosition = e.clientX - sliderStartX.value
    sliderPosition.value = Math.max(-2, Math.min(newPosition, getSliderWidth() - 44))
  }

  /** 滑块鼠标释放事件 */
  const handleSliderMouseUp = () => {
    if (!isDragging.value) return
    isDragging.value = false

    if (sliderPosition.value >= getSliderWidth() - 70) {
      sliderVerified.value = true
      sliderPosition.value = getSliderWidth() - 44
    } else {
      sliderPosition.value = -2
    }
  }

  /** 触摸开始事件 */
  const handleTouchStart = (e) => {
    if (sliderVerified.value) return
    isDragging.value = true
    sliderStartX.value = e.touches[0].clientX - sliderPosition.value
  }

  /** 触摸移动事件 */
  const handleTouchMove = (e) => {
    if (!isDragging.value) return
    e.preventDefault()
    const newPosition = e.touches[0].clientX - sliderStartX.value
    sliderPosition.value = Math.max(-2, Math.min(newPosition, getSliderWidth() - 44))
  }

  /** 触摸结束事件 */
  const handleTouchEnd = () => {
    if (!isDragging.value) return
    isDragging.value = false

    if (sliderPosition.value >= getSliderWidth() - 70) {
      sliderVerified.value = true
      sliderPosition.value = getSliderWidth() - 44
    } else {
      sliderPosition.value = -2
    }
  }

  const startCountdown = (type) => {
    if (type === 'login') {
      loginCountdown.value = 60
      loginCountdownTimer = setInterval(() => {
        loginCountdown.value--
        if (loginCountdown.value <= 0) {
          clearInterval(loginCountdownTimer)
        }
      }, 1000)
    } else if (type === 'register') {
      registerCountdown.value = 60
      registerCountdownTimer = setInterval(() => {
        registerCountdown.value--
        if (registerCountdown.value <= 0) {
          clearInterval(registerCountdownTimer)
        }
      }, 1000)
    } else if (type === 'forgot') {
      forgotCountdown.value = 60
      forgotCountdownTimer = setInterval(() => {
        forgotCountdown.value--
        if (forgotCountdown.value <= 0) {
          clearInterval(forgotCountdownTimer)
        }
      }, 1000)
    }
  }

  const sendLoginCode = async () => {
    if (!canSendLoginCode.value || sendingCode.value) return

    sendingCode.value = true
    try {
      const result = await userApi.sendEmailCode(loginForm.value.email)
      if (result.code === 200) {
        toast.success('验证码已发送，请查收邮件')
        startCountdown('login')
      } else {
        toast.error(result.message || '发送验证码失败')
      }
    } catch (e) {
      toast.error(e.message || '发送验证码失败')
    } finally {
      sendingCode.value = false
    }
  }

  const sendRegisterCode = async () => {
    if (!canSendRegisterCode.value || sendingCode.value) return

    sendingCode.value = true
    try {
      const result = await userApi.sendEmailCode(registerForm.value.email)
      if (result.code === 200) {
        toast.success('验证码已发送，请查收邮件')
        startCountdown('register')
      } else {
        toast.error(result.message || '发送验证码失败')
      }
    } catch (e) {
      toast.error(e.message || '发送验证码失败')
    } finally {
      sendingCode.value = false
    }
  }

  const sendForgotCode = async () => {
    if (!canSendForgotCode.value || sendingCode.value) return

    sendingCode.value = true
    try {
      const response = await userApi.sendEmailCode(forgotForm.value.email)
      if (response.code === 200) {
        toast.success('验证码已发送到您的邮箱')
        startCountdown('forgot')
      } else {
        toast.error(response.message || '发送验证码失败')
      }
    } catch (error) {
      toast.error(error.message || '发送验证码失败')
    } finally {
      sendingCode.value = false
    }
  }

  const errorMessage = ref('')

  const handleLogin = async () => {
    if (loginMethod.value === 'password') {
      if (!loginForm.value.username || !loginForm.value.password) {
        toast.warning('请输入用户名/邮箱和密码')
        return
      }
    } else {
      if (!loginForm.value.email || !loginForm.value.verifyCode) {
        toast.warning('请输入邮箱和验证码')
        return
      }
      if (!loginEmailValid.value) {
        toast.warning('请输入有效的邮箱地址')
        return
      }
    }

    isLoading.value = true
    errorMessage.value = ''

    try {
      let response
      if (loginMethod.value === 'password') {
        response = await authStore.login({
          username: loginForm.value.username,
          password: loginForm.value.password,
        })
      } else {
        response = await authStore.loginWithCode({
          email: loginForm.value.email,
          code: loginForm.value.verifyCode,
        })
      }

      if (response.code === 200) {
        toast.success('登录成功，欢迎回来！')
        router.push('/home')
      } else {
        toast.error(response.message || '登录失败')
      }
    } catch (error) {
      toast.error(error.message || '登录失败，请稍后重试')
    } finally {
      isLoading.value = false
    }
  }

  const handleRegister = async () => {
    if (!registerForm.value.email || !registerForm.value.password) {
      toast.warning('请填写完整信息')
      return
    }

    if (!registerEmailValid.value) {
      toast.warning('请输入有效的邮箱地址')
      return
    }

    if (!registerForm.value.verifyCode) {
      toast.warning('请输入验证码')
      return
    }

    if (registerForm.value.password.length < 6 || registerForm.value.password.length > 20) {
      toast.warning('密码长度需在6-20位之间')
      return
    }

    if (registerForm.value.password !== registerForm.value.confirmPassword) {
      toast.warning('两次密码输入不一致')
      return
    }

    if (!registerForm.value.agree) {
      toast.warning('请阅读并同意服务条款和隐私政策')
      return
    }

    isLoading.value = true
    errorMessage.value = ''

    try {
      const response = await authStore.register({
        email: registerForm.value.email,
        password: registerForm.value.password,
        code: registerForm.value.verifyCode,
      })

      if (response.code === 200) {
        toast.success('注册成功，请登录！')
        currentForm.value = 'login'
        loginForm.value.username = registerForm.value.email
      } else {
        toast.error(response.message || '注册失败')
      }
    } catch (error) {
      toast.error(error.message || '注册失败，请稍后重试')
    } finally {
      isLoading.value = false
    }
  }

  const handleForgot = async () => {
    if (!canSubmitForgot.value) {
      if (!forgotEmailValid.value) {
        toast.warning('请输入有效的邮箱地址')
      } else if (!forgotForm.value.code) {
        toast.warning('请输入验证码')
      } else if (!forgotPasswordValid.value) {
        toast.warning('密码长度必须在6-20个字符之间')
      } else if (!forgotPasswordMatch.value) {
        toast.warning('两次密码输入不一致')
      }
      return
    }

    isLoading.value = true
    try {
      const response = await authApi.resetPassword({
        email: forgotForm.value.email,
        code: forgotForm.value.code,
        newPassword: forgotForm.value.newPassword,
      })
      if (response.code === 200) {
        toast.success('密码重置成功，请登录')
        currentForm.value = 'login'
        forgotForm.value = { email: '', code: '', newPassword: '', confirmPassword: '' }
      } else {
        toast.error(response.message || '重置密码失败')
      }
    } catch (error) {
      toast.error(error.message || '重置密码失败')
    } finally {
      isLoading.value = false
    }
  }

  onMounted(() => {
    document.addEventListener('mousemove', handleSliderMouseMove)
    document.addEventListener('mouseup', handleSliderMouseUp)
    document.addEventListener('touchmove', handleTouchMove)
    document.addEventListener('touchend', handleTouchEnd)

    if (sessionStorage.getItem('token_expired') === 'true') {
      sessionStorage.removeItem('token_expired')
      resetRedirectFlag()
      toast.warning('您的登录已过期，请重新登录')
    }
  })

  onUnmounted(() => {
    document.removeEventListener('mousemove', handleSliderMouseMove)
    document.removeEventListener('mouseup', handleSliderMouseUp)
    document.removeEventListener('touchmove', handleTouchMove)
    document.removeEventListener('touchend', handleTouchEnd)
    if (loginCountdownTimer) clearInterval(loginCountdownTimer)
    if (registerCountdownTimer) clearInterval(registerCountdownTimer)
    if (forgotCountdownTimer) clearInterval(forgotCountdownTimer)
  })
</script>

<template>
  <div class="login-page">
    <div v-if="isDragging" class="drag-overlay" @mouseup="handleSliderMouseUp" @touchend="handleTouchEnd"></div>
    <div class="login-left">
      <div class="brand">
        <Icon icon="ri:ai-generate" class="brand-icon" />
        <span class="brand-name">知忆岛</span>
      </div>
      <div class="illustration">
        <div class="floating-cards">
          <div class="card card-1">
            <Icon icon="ri:book-3-line" />
          </div>
          <div class="card card-2">
            <Icon icon="ri:brain-line" />
          </div>
          <div class="card card-3">
            <Icon icon="ri:lightbulb-line" />
          </div>
        </div>
        <h2>AI 智能笔记与知识库</h2>
        <p>用 AI 技术帮助你更好地管理知识、搜索记忆</p>
      </div>
      <div class="features">
        <div class="feature-item">
          <Icon icon="ri:magic-line" />
          <span>AI 智能摘要</span>
        </div>
        <div class="feature-item">
          <Icon icon="ri:search-eye-line" />
          <span>记忆搜索引擎</span>
        </div>
        <div class="feature-item">
          <Icon icon="ri:mind-map" />
          <span>知识图谱</span>
        </div>
      </div>
    </div>

    <div class="login-right">
      <div class="form-container">
        <Transition name="form-fade" mode="out-in">
          <div :key="currentForm" class="form-wrapper">
            <div class="form-header">
              <h1>{{ formTitle }}</h1>
              <p>{{ formSubtitle }}</p>
            </div>

            <Transition name="form-slide" mode="out-in">
              <form v-if="currentForm === 'login'" key="login" class="login-form" @submit.prevent="handleLogin">
                <div class="login-method-tabs">
                  <button type="button" class="btn btn-text" :class="{ 'active-tab': loginMethod === 'password' }"
                    @click="switchLoginMethod('password')">
                    <Icon icon="ri:lock-password-line" />
                    密码登录
                  </button>
                  <button type="button" class="btn btn-text" :class="{ 'active-tab': loginMethod === 'code' }"
                    @click="switchLoginMethod('code')">
                    <Icon icon="ri:mail-line" />
                    验证码登录
                  </button>
                </div>

                <div class="login-content-wrapper">
                  <Transition :css="false" mode="out-in" @before-enter="onBeforeEnter" @enter="onEnter"
                    @leave="onLeave">
                    <div v-if="loginMethod === 'password'" key="password-login" class="login-content">
                      <div class="input-group">
                        <label>用户名 / 邮箱</label>
                        <div class="input-wrapper">
                          <Icon icon="ri:user-line" class="input-icon" />
                          <input v-model="loginForm.username" type="text" placeholder="请输入用户名或邮箱" />
                        </div>
                      </div>

                      <div class="input-group">
                        <label>密码</label>
                        <div class="input-wrapper">
                          <Icon icon="ri:lock-line" class="input-icon" />
                          <input v-model="loginForm.password" :type="showPassword ? 'text' : 'password'"
                            placeholder="请输入密码" />
                          <button type="button" class="toggle-password" @click="showPassword = !showPassword">
                            <Icon :icon="showPassword ? 'ri:eye-off-line' : 'ri:eye-line'" />
                          </button>
                        </div>
                      </div>
                    </div>

                    <div v-else key="code-login" class="login-content">
                      <div class="input-group">
                        <label>邮箱地址</label>
                        <ElTooltip :visible="loginEmailInvalid" content="请输入有效的邮箱地址" effect="light" placement="top">
                          <template #default>
                            <div class="input-wrapper" :class="{
                                'input-success': loginEmailBlurred && loginEmailValid,
                                'input-error': loginEmailInvalid,
                              }">
                              <Icon icon="ri:mail-line" class="input-icon" />
                              <input v-model="loginForm.email" type="text" placeholder="请输入邮箱地址"
                                @blur="loginEmailBlurred = true" />
                              <Icon v-if="loginEmailBlurred && loginEmailValid" icon="ri:checkbox-circle-fill"
                                class="status-icon success" />
                              <Icon v-else-if="loginEmailInvalid" icon="ri:close-circle-fill"
                                class="status-icon error" />
                            </div>
                          </template>
                        </ElTooltip>
                      </div>

                      <div class="input-group">
                        <label>验证码</label>
                        <div class="input-wrapper">
                          <Icon icon="ri:shield-check-line" class="input-icon" />
                          <input v-model="loginForm.verifyCode" type="text" placeholder="请输入验证码" maxlength="6" />
                        </div>
                        <div class="verify-row" :class="{ 'is-dragging': isDragging }">
                          <div class="slider-verify-wrapper" v-if="!sliderVerified && loginCountdown === 0">
                            <div class="slider-track" ref="sliderTrackRef">
                              <div class="slider-fill" :style="{ width: Math.max(0, sliderPosition + 2) + 'px' }"></div>
                              <div class="slider-thumb" :class="{ verified: sliderVerified }"
                                :style="{ left: sliderPosition + 'px' }" @mousedown="handleSliderMouseDown"
                                @touchstart="handleTouchStart">
                                <Icon v-if="sliderVerified" icon="ri:check-line" />
                                <Icon v-else icon="ri:arrow-right-double-line" />
                              </div>
                              <span class="slider-hint" v-if="!sliderVerified && sliderPosition === -2">
                                向右滑动验证
                              </span>
                            </div>
                          </div>
                          <div class="verify-success-inline" v-if="sliderVerified">
                            <Icon icon="ri:checkbox-circle-fill" />
                            <span>已验证</span>
                          </div>
                          <button type="button" class="btn btn-sm btn-ghost"
                            :disabled="!canSendLoginCode || sendingCode" @click="sendLoginCode">
                            <Icon v-if="sendingCode" icon="ri:loader-4-line" class="spin" />
                            <span v-else>{{ loginCountdown > 0 ? `${loginCountdown}s` : '获取验证码' }}</span>
                          </button>
                        </div>
                      </div>
                    </div>
                  </Transition>
                </div>

                <div class="form-options">
                  <label class="checkbox-label">
                    <input v-model="loginForm.remember" type="checkbox" />
                    <span class="checkmark"></span>
                    记住我
                  </label>
                  <button type="button" class="link-btn" @click="switchForm('forgot')">
                    忘记密码？
                  </button>
                </div>

                <button type="submit" class="btn btn-primary btn-lg btn-block" :disabled="isLoading">
                  <Icon v-if="isLoading" icon="ri:loader-4-line" class="spin" />
                  <span v-else>登 录</span>
                </button>

                <p class="switch-text">
                  还没有账户？
                  <button type="button" class="link-btn" @click="switchForm('register')">
                    立即注册
                  </button>
                </p>
              </form>

              <form v-else-if="currentForm === 'register'" key="register" class="register-form"
                @submit.prevent="handleRegister">
                <div class="input-group">
                  <label>邮箱地址</label>
                  <ElTooltip :visible="registerEmailInvalid" content="请输入有效的邮箱地址" effect="light" placement="top">
                    <template #default>
                      <div class="input-wrapper" :class="{
                          'input-success': registerEmailBlurred && registerEmailValid,
                          'input-error': registerEmailInvalid,
                        }">
                        <Icon icon="ri:mail-line" class="input-icon" />
                        <input v-model="registerForm.email" type="text" placeholder="请输入邮箱地址"
                          @blur="registerEmailBlurred = true" />
                        <Icon v-if="registerEmailBlurred && registerEmailValid" icon="ri:checkbox-circle-fill"
                          class="status-icon success" />
                        <Icon v-else-if="registerEmailInvalid" icon="ri:close-circle-fill" class="status-icon error" />
                      </div>
                    </template>
                  </ElTooltip>
                </div>

                <div class="input-group">
                  <label>邮箱验证码</label>
                  <div class="input-wrapper">
                    <Icon icon="ri:shield-check-line" class="input-icon" />
                    <input v-model="registerForm.verifyCode" type="text" placeholder="请输入验证码" maxlength="6" />
                  </div>
                  <div class="verify-row" :class="{ 'is-dragging': isDragging }">
                    <div class="slider-verify-wrapper" v-if="!sliderVerified && registerCountdown === 0">
                      <div class="slider-track" ref="sliderTrackRef">
                        <div class="slider-fill" :style="{ width: Math.max(0, sliderPosition + 2) + 'px' }"></div>
                        <div class="slider-thumb" :class="{ verified: sliderVerified }"
                          :style="{ left: sliderPosition + 'px' }" @mousedown="handleSliderMouseDown"
                          @touchstart="handleTouchStart">
                          <Icon v-if="sliderVerified" icon="ri:check-line" />
                          <Icon v-else icon="ri:arrow-right-double-line" />
                        </div>
                        <span class="slider-hint" v-if="!sliderVerified && sliderPosition === -2">
                          向右滑动验证
                        </span>
                      </div>
                    </div>
                    <div class="verify-success-inline" v-if="sliderVerified">
                      <Icon icon="ri:checkbox-circle-fill" />
                      <span>已验证</span>
                    </div>
                    <button type="button" class="btn btn-sm btn-ghost" :disabled="!canSendRegisterCode || sendingCode"
                      @click="sendRegisterCode">
                      <Icon v-if="sendingCode" icon="ri:loader-4-line" class="spin" />
                      <span v-else>{{ registerCountdown > 0 ? `${registerCountdown}s` : '获取验证码' }}</span>
                    </button>
                  </div>
                </div>

                <div class="input-group">
                  <label>密码</label>
                  <ElTooltip :visible="registerPasswordInvalid" :content="ValidationRules.password.messages.minLength"
                    effect="light" placement="top">
                    <template #default>
                      <div class="input-wrapper" :class="{
                          'input-success': registerPasswordBlurred && registerPasswordValid,
                          'input-error': registerPasswordInvalid,
                        }">
                        <Icon icon="ri:lock-line" class="input-icon" />
                        <input v-model="registerForm.password" :type="showPassword ? 'text' : 'password'"
                          placeholder="至少6位" @blur="registerPasswordBlurred = true" />
                        <button type="button" class="toggle-password" @click="showPassword = !showPassword">
                          <Icon :icon="showPassword ? 'ri:eye-off-line' : 'ri:eye-line'" />
                        </button>
                      </div>
                    </template>
                  </ElTooltip>
                </div>

                <div class="input-group">
                  <label>确认密码</label>
                  <ElTooltip :visible="registerConfirmPasswordInvalid" content="两次密码输入不一致" effect="light"
                    placement="top">
                    <template #default>
                      <div class="input-wrapper" :class="{
                          'input-success': registerConfirmPasswordBlurred && registerPasswordMatch,
                          'input-error': registerConfirmPasswordInvalid,
                        }">
                        <Icon icon="ri:lock-2-line" class="input-icon" />
                        <input v-model="registerForm.confirmPassword" :type="showConfirmPassword ? 'text' : 'password'"
                          placeholder="再次输入" @blur="registerConfirmPasswordBlurred = true" />
                        <button type="button" class="toggle-password"
                          @click="showConfirmPassword = !showConfirmPassword">
                          <Icon :icon="showConfirmPassword ? 'ri:eye-off-line' : 'ri:eye-line'" />
                        </button>
                      </div>
                    </template>
                  </ElTooltip>
                </div>

                <label class="checkbox-label agreement">
                  <input v-model="registerForm.agree" type="checkbox" />
                  <span class="checkmark"></span>
                  我已阅读并同意 <router-link to="/legal/user-agreement">服务条款</router-link> 和 <router-link
                    to="/legal/privacy">隐私政策</router-link>
                </label>

                <button type="submit" class="btn btn-primary btn-lg btn-block" :disabled="isLoading">
                  <Icon v-if="isLoading" icon="ri:loader-4-line" class="spin" />
                  <span v-else>注 册</span>
                </button>

                <p class="switch-text">
                  已有账户？
                  <button type="button" class="link-btn" @click="switchForm('login')">
                    立即登录
                  </button>
                </p>
              </form>

              <form v-else key="forgot" class="forgot-form" @submit.prevent="handleForgot">
                <div class="input-group">
                  <label>邮箱地址</label>
                  <ElTooltip :visible="forgotEmailInvalid" content="请输入有效的邮箱地址" effect="light" placement="top">
                    <template #default>
                      <div class="input-wrapper" :class="{
                          'input-success': forgotEmailBlurred && forgotEmailValid,
                          'input-error': forgotEmailInvalid,
                        }">
                        <Icon icon="ri:mail-line" class="input-icon" />
                        <input v-model="forgotForm.email" type="text" placeholder="请输入注册时的邮箱地址"
                          @blur="forgotEmailBlurred = true" />
                        <Icon v-if="forgotEmailBlurred && forgotEmailValid" icon="ri:checkbox-circle-fill"
                          class="status-icon success" />
                        <Icon v-else-if="forgotEmailInvalid" icon="ri:close-circle-fill" class="status-icon error" />
                      </div>
                    </template>
                  </ElTooltip>
                </div>

                <div class="input-group">
                  <label>验证码</label>
                  <div class="input-wrapper">
                    <Icon icon="ri:shield-check-line" class="input-icon" />
                    <input v-model="forgotForm.code" type="text" placeholder="请输入验证码" maxlength="6" />
                  </div>
                  <div class="verify-row" :class="{ 'is-dragging': isDragging }">
                    <div class="slider-verify-wrapper" v-if="!sliderVerified && forgotCountdown === 0">
                      <div class="slider-track" ref="sliderTrackRef">
                        <div class="slider-fill" :style="{ width: Math.max(0, sliderPosition + 2) + 'px' }"></div>
                        <div class="slider-thumb" :class="{ verified: sliderVerified }"
                          :style="{ left: sliderPosition + 'px' }" @mousedown="handleSliderMouseDown"
                          @touchstart="handleTouchStart">
                          <Icon v-if="sliderVerified" icon="ri:check-line" />
                          <Icon v-else icon="ri:arrow-right-double-line" />
                        </div>
                        <span class="slider-hint" v-if="!sliderVerified && sliderPosition === -2">
                          向右滑动验证
                        </span>
                      </div>
                    </div>
                    <div class="verify-success-inline" v-if="sliderVerified">
                      <Icon icon="ri:checkbox-circle-fill" />
                      <span>已验证</span>
                    </div>
                    <button type="button" class="btn btn-sm btn-ghost" :disabled="!canSendForgotCode || sendingCode"
                      @click="sendForgotCode">
                      <Icon v-if="sendingCode" icon="ri:loader-4-line" class="spin" />
                      <span v-else>{{ forgotCountdown > 0 ? `${forgotCountdown}s` : '获取验证码' }}</span>
                    </button>
                  </div>
                </div>

                <div class="input-group">
                  <label>新密码</label>
                  <ElTooltip :visible="forgotPasswordInvalid" :content="ValidationRules.password.messages.minLength"
                    effect="light" placement="top">
                    <template #default>
                      <div class="input-wrapper" :class="{
                          'input-success': forgotPasswordBlurred && forgotPasswordValid,
                          'input-error': forgotPasswordInvalid,
                        }">
                        <Icon icon="ri:lock-line" class="input-icon" />
                        <input v-model="forgotForm.newPassword" :type="showNewPassword ? 'text' : 'password'"
                          placeholder="6-20位字符" @blur="forgotPasswordBlurred = true" />
                        <button type="button" class="toggle-password" @click="showNewPassword = !showNewPassword">
                          <Icon :icon="showNewPassword ? 'ri:eye-off-line' : 'ri:eye-line'" />
                        </button>
                      </div>
                    </template>
                  </ElTooltip>
                </div>

                <div class="input-group">
                  <label>确认密码</label>
                  <ElTooltip :visible="forgotConfirmPasswordInvalid" content="两次密码输入不一致" effect="light" placement="top">
                    <template #default>
                      <div class="input-wrapper" :class="{
                          'input-success': forgotConfirmPasswordBlurred && forgotPasswordMatch,
                          'input-error': forgotConfirmPasswordInvalid,
                        }">
                        <Icon icon="ri:lock-2-line" class="input-icon" />
                        <input v-model="forgotForm.confirmPassword"
                          :type="showForgotConfirmPassword ? 'text' : 'password'" placeholder="再次输入新密码"
                          @blur="forgotConfirmPasswordBlurred = true" />
                        <button type="button" class="toggle-password"
                          @click="showForgotConfirmPassword = !showForgotConfirmPassword">
                          <Icon :icon="showForgotConfirmPassword ? 'ri:eye-off-line' : 'ri:eye-line'" />
                        </button>
                      </div>
                    </template>
                  </ElTooltip>
                </div>

                <button type="submit" class="btn btn-primary btn-lg btn-block"
                  :disabled="isLoading || !canSubmitForgot">
                  <Icon v-if="isLoading" icon="ri:loader-4-line" class="spin" />
                  <span v-else>重置密码</span>
                </button>

                <p class="switch-text">
                  想起密码了？
                  <button type="button" class="link-btn" @click="switchForm('login')">
                    返回登录
                  </button>
                </p>
              </form>
            </Transition>
          </div>
        </Transition>
      </div>
    </div>
  </div>
</template>

<style scoped>
  .login-page {
    --spacing-xs: 8px;
    --spacing-sm: 12px;
    --spacing-md: 16px;
    --spacing-lg: 20px;
    --spacing-xl: 24px;
    --transition-fast: 0.2s;
    --transition-normal: 0.4s;
    --transition-slow: 0.5s;

    display: flex;
    min-height: 100vh;
    background: linear-gradient(135deg,
        var(--primary-color, #122E8A) 0%,
        var(--secondary-color, #122E8A) 100%);
    position: relative;
  }

  .drag-overlay {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    z-index: 9999;
    cursor: grabbing;
  }

  .login-left {
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: center;
    padding: 60px;
    color: #ffffff;
  }

  .brand {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 60px;
  }

  .brand-icon {
    font-size: 42px;
  }

  .brand-name {
    font-size: 28px;
    font-weight: 700;
    letter-spacing: 2px;
  }

  .illustration {
    margin-bottom: 60px;
  }

  .floating-cards {
    display: flex;
    gap: 20px;
    margin-bottom: 40px;
  }

  .card {
    width: 80px;
    height: 80px;
    background: rgba(255, 255, 255, 0.2);
    backdrop-filter: blur(10px);
    border-radius: 16px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 32px;
    animation: float 3s ease-in-out infinite;
  }

  .card-1 {
    animation-delay: 0s;
  }

  .card-2 {
    animation-delay: 0.5s;
  }

  .card-3 {
    animation-delay: 1s;
  }

  @keyframes float {

    0%,
    100% {
      transform: translateY(0);
    }

    50% {
      transform: translateY(-15px);
    }
  }

  .illustration h2 {
    font-size: 32px;
    font-weight: 700;
    margin-bottom: 16px;
  }

  .illustration p {
    font-size: 16px;
    opacity: 0.9;
    line-height: 1.6;
  }

  .features {
    display: flex;
    gap: 24px;
  }

  .feature-item {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 14px;
    opacity: 0.9;
  }

  .feature-item .iconify {
    font-size: 20px;
  }

  .login-right {
    width: 520px;
    background: var(--bg-secondary);
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 40px;
    border-radius: 24px 0 0 24px;
    box-shadow: -10px 0 40px rgba(18, 46, 138, 0.08);
  }

  .form-container {
    width: 100%;
    max-width: 400px;
  }

  .form-wrapper {
    width: 100%;
  }

  .form-header {
    text-align: center;
    margin-bottom: 32px;
  }

  .form-header h1 {
    font-size: 28px;
    font-weight: 700;
    color: #1f2937;
    margin-bottom: 8px;
  }

  .form-header p {
    font-size: 14px;
    color: #6b7280;
  }

  .login-method-tabs {
    display: flex;
    gap: 8px;
    margin-bottom: 24px;
    padding: 4px;
    background: #f3f4f6;
    border-radius: 12px;
  }

  .login-method-tabs .btn {
    flex: 1;
    justify-content: center;
  }

  .login-content {
    display: flex;
    flex-direction: column;
    gap: 0;
  }

  .login-content>* {
    flex-shrink: 0;
  }

  .login-content-wrapper {
    overflow: hidden;
    height: auto;
  }

  .input-group {
    margin-bottom: var(--spacing-lg);
  }

  .form-row {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: var(--spacing-md);
  }

  .form-row.user-email-row {
    grid-template-columns: 0.85fr 1.15fr;
    margin-bottom: var(--spacing-lg);
  }

  .form-row.user-email-row .input-group {
    margin-bottom: 0;
  }

  .verify-row {
    display: flex;
    align-items: center;
    gap: var(--spacing-sm);
    margin-top: var(--spacing-sm);
    min-height: 40px;
    width: 100%;
  }

  .verify-row.is-dragging .inline-code-btn {
    pointer-events: none;
  }

  .slider-verify-wrapper {
    flex: 1;
    min-width: 0;
  }

  .verify-row .inline-code-btn {
    flex-shrink: 0;
    padding: 10px 16px;
    font-size: 13px;
    white-space: nowrap;
  }

  .verify-row .btn {
    height: 40px;
    min-width: 100px;
    padding: 0 16px;
    font-size: 13px;
  }

  .verify-row .verify-success-inline {
    flex: 1;
    min-width: 0;
    margin-top: 0;
    justify-content: center;
    height: 40px;
    border-radius: 10px;
  }

  .label-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: var(--spacing-xs);
  }

  .label-row label {
    margin-bottom: 0;
  }

  .input-group label {
    display: block;
    font-size: 14px;
    font-weight: 500;
    color: #374151;
    margin-bottom: var(--spacing-xs);
  }

  .input-wrapper {
    position: relative;
    display: flex;
    align-items: center;
  }

  .input-wrapper.input-error input {
    border-color: #ef4444;
  }

  .input-wrapper.input-success input {
    border-color: #22c55e;
  }

  .status-icon {
    position: absolute;
    right: 14px;
    font-size: 18px;
  }

  .status-icon.spin {
    animation: spin 1s linear infinite;
    color: #9ca3af;
  }

  .status-icon.error {
    color: #ef4444;
  }

  .status-icon.success {
    color: #22c55e;
  }

  @keyframes spin {
    from {
      transform: rotate(0deg);
    }

    to {
      transform: rotate(360deg);
    }
  }

  .input-icon {
    position: absolute;
    left: 14px;
    font-size: 18px;
    color: #9ca3af;
    z-index: 1;
  }

  .input-wrapper input {
    width: 100%;
    padding: 12px 14px 12px 44px;
    border: 1px solid #e5e7eb;
    border-radius: 10px;
    font-size: 14px;
    color: #374151;
    transition: all 0.2s;
    outline: none;
  }

  .input-wrapper input:focus {
    border-color: var(--primary-color, #122E8A);
    box-shadow: 0 0 0 3px rgba(18, 46, 138, 0.1);
  }

  .input-wrapper input::placeholder {
    color: #9ca3af;
  }

  .toggle-password {
    position: absolute;
    right: 14px;
    background: none;
    border: none;
    color: #9ca3af;
    cursor: pointer;
    font-size: 18px;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 0;
    z-index: 1;
  }

  .toggle-password:hover {
    color: #6b7280;
  }

  .field-hint {
    display: block;
    font-size: 12px;
    margin-top: 6px;
    line-height: 1.4;
  }

  .field-hint.error {
    color: #ef4444;
  }

  .field-hint.success {
    color: #22c55e;
  }

  .slider-verify-inline {
    margin-top: var(--spacing-sm);
  }

  .slider-track {
    height: 40px;
    background: #f3f4f6;
    border-radius: 10px;
    position: relative;
    overflow: hidden;
    border: 1px solid #e5e7eb;
  }

  .slider-fill {
    position: absolute;
    left: 0;
    top: -1px;
    height: 42px;
    background: linear-gradient(135deg,
        var(--primary-color, #122E8A) 0%,
        var(--secondary-color, #122E8A) 100%);
    opacity: 0.3;
    transition: none;
  }

  .slider-thumb {
    position: absolute;
    top: -1px;
    left: -2px;
    width: 44px;
    height: 42px;
    background: var(--bg-secondary, #ffffff);
    border: 1px solid var(--border-color, #e5e7eb);
    border-radius: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: grab;
    font-size: 18px;
    color: var(--text-secondary, #9ca3af);
    transition: border-color 0.2s, color 0.2s, background 0.2s;
    user-select: none;
    box-shadow: 0 2px 4px rgba(18, 46, 138, 0.1);
    touch-action: none;
  }

  .slider-thumb:hover {
    color: var(--primary-color, #122E8A);
  }

  .slider-thumb.verified {
    background: linear-gradient(135deg,
        var(--primary-color, #122E8A) 0%,
        var(--secondary-color, #122E8A) 100%);
    border-color: transparent;
    color: white;
  }

  .slider-hint {
    position: absolute;
    left: 50%;
    top: 50%;
    transform: translate(-50%, -50%);
    font-size: 13px;
    color: #9ca3af;
    pointer-events: none;
  }

  .verify-success-inline {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    gap: 6px;
    height: 40px;
    padding: 0 14px;
    background: #ecfdf5;
    border-radius: 8px;
    font-size: 13px;
    color: #10b981;
    margin-top: var(--spacing-sm);
  }

  .verify-success-inline .iconify {
    font-size: 16px;
  }

  .form-options {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: var(--spacing-xl);
  }

  .checkbox-label {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 14px;
    color: #6b7280;
    cursor: pointer;
    position: relative;
    padding-left: 24px;
  }

  .checkbox-label input {
    position: absolute;
    opacity: 0;
    cursor: pointer;
  }

  .checkmark {
    position: absolute;
    left: 0;
    top: 50%;
    transform: translateY(-50%);
    width: 16px;
    height: 16px;
    border: 1px solid #d1d5db;
    border-radius: 4px;
    transition: all 0.2s;
  }

  .checkbox-label input:checked+.checkmark {
    background: var(--primary-color, #122E8A);
    border-color: var(--primary-color, #122E8A);
  }

  .checkbox-label input:checked+.checkmark::after {
    content: '';
    position: absolute;
    left: 5px;
    top: 2px;
    width: 4px;
    height: 8px;
    border: solid white;
    border-width: 0 2px 2px 0;
    transform: rotate(45deg);
  }

  .link-btn {
    background: none;
    border: none;
    color: var(--primary-color, #122E8A);
    font-size: 14px;
    cursor: pointer;
    transition: opacity 0.2s;
  }

  .link-btn:hover {
    opacity: 0.8;
  }

  .spin {
    animation: spin 1s linear infinite;
  }

  .active-tab {
    background: rgba(18, 46, 138, 0.08) !important;
    color: var(--primary-color) !important;
  }

  .switch-text {
    text-align: center;
    margin-top: var(--spacing-xl);
    font-size: 14px;
    color: #6b7280;
  }

  .agreement {
    margin-top: var(--spacing-lg);
    margin-bottom: var(--spacing-lg);
    font-size: 13px !important;
  }

  .agreement a {
    color: var(--primary-color, #122E8A);
    text-decoration: none;
  }

  .agreement a:hover {
    text-decoration: underline;
  }

  .form-fade-enter-active,
  .form-fade-leave-active {
    transition: opacity var(--transition-normal) cubic-bezier(0.4, 0, 0.2, 1);
  }

  .form-fade-enter-from,
  .form-fade-leave-to {
    opacity: 0;
  }

  .form-slide-enter-active,
  .form-slide-leave-active {
    transition: opacity 0.25s ease, transform 0.25s ease;
  }

  .form-slide-enter-from {
    opacity: 0;
    transform: translateY(10px);
  }

  .form-slide-leave-to {
    opacity: 0;
    transform: translateY(-10px);
  }

  @media (max-width: 1024px) {
    .login-left {
      display: none;
    }

    .login-right {
      width: 100%;
      border-radius: 0;
    }
  }

  @media (max-width: 480px) {
    .login-right {
      padding: var(--spacing-xl) var(--spacing-lg);
    }

    .form-row {
      grid-template-columns: 1fr;
      gap: 0;
    }

    .form-row .input-group {
      margin-bottom: var(--spacing-lg);
    }

    .form-row.user-email-row {
      grid-template-columns: 1fr;
    }

    .verify-row {
      flex-direction: column;
      align-items: stretch;
    }

    .verify-row .inline-code-btn {
      width: 100%;
      justify-content: center;
    }
  }
</style>