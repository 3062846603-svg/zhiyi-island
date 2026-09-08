<script setup>
/**
 * 富文本编辑器组件
 * 基于 wangEditor 封装的富文本编辑器，支持文本格式化、图片粘贴等功能
 */
import { ref, shallowRef, computed, onBeforeUnmount, watch } from 'vue'
import { Editor, Toolbar } from '@wangeditor/editor-for-vue'
import '@wangeditor/editor/dist/css/style.css'

/**
 * 组件属性
 * @property {string} modelValue - 编辑器内容（HTML格式）
 * @property {string} placeholder - 占位文本
 * @property {string} height - 编辑器高度
 */
const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  },
  placeholder: {
    type: String,
    default: '请输入内容...'
  },
  height: {
    type: String,
    default: '600px'
  }
})

const emit = defineEmits(['update:modelValue'])

/** 编辑器实例引用 */
const editorRef = shallowRef()

/**
 * 编辑器配置
 * 禁用内置的图片和视频上传功能
 */
const editorConfig = computed(() => ({
  placeholder: props.placeholder,
  MENU_CONF: {
    uploadImage: {
      enabled: false
    },
    uploadVideo: {
      enabled: false
    }
  }
}))

/**
 * 工具栏配置
 * 排除图片和视频相关的工具按钮
 */
const toolbarConfig = {
  excludeKeys: [
    'uploadImage',
    'uploadVideo',
    'insertVideo',
    'insertAudio',
    'insertImage',
    'group-image',
    'group-video'
  ]
}

/**
 * 编辑器创建完成回调
 * @param {Object} editor - 编辑器实例
 */
const handleCreated = (editor) => {
  editorRef.value = editor
}

/**
 * 编辑器内容变化回调
 * 获取最新HTML内容并触发更新事件
 * @param {Object} editor - 编辑器实例
 */
const handleChange = (editor) => {
  const html = editor.getHtml()
  emit('update:modelValue', html)
}

/**
 * 监听外部传入的内容变化
 * 当外部内容与编辑器内容不一致时更新编辑器
 */
watch(() => props.modelValue, (newValue) => {
  if (editorRef.value && editorRef.value.getHtml() !== newValue) {
    editorRef.value.setHtml(newValue || '<p><br></p>')
  }
})

/**
 * 组件卸载前销毁编辑器实例
 * 防止内存泄漏
 */
onBeforeUnmount(() => {
  const editor = editorRef.value
  if (editor) {
    editor.destroy()
  }
})
</script>

<template>
  <div class="rich-text-editor" :style="{ height: height }">
    <Toolbar :editor="editorRef" :defaultConfig="toolbarConfig" :mode="'default'" class="toolbar" />
    <Editor :defaultConfig="editorConfig" :modelValue="modelValue" :mode="'default'" class="editor"
      @onCreated="handleCreated" @onChange="handleChange" />
  </div>
</template>

<style scoped>
.rich-text-editor {
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.toolbar {
  border-bottom: 1px solid #e5e7eb;
  background: #fafafa;
}

.editor {
  flex: 1;
  overflow-y: auto;
}
</style>
