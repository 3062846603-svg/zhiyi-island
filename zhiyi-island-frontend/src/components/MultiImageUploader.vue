<script setup>
/**
 * 多图片上传组件
 * 支持多图片上传、拖拽上传、预览和删除功能
 */
import { ref, computed, watch } from 'vue'
import { Icon } from '@iconify/vue'
import { fileApi } from '@/api/file'

/**
 * 组件属性
 * @property {Array<string>} modelValue - 图片URL数组
 * @property {number} maxCount - 最大图片数量
 * @property {number} maxSize - 单张图片最大大小（字节）
 */
const props = defineProps({
  modelValue: {
    type: Array,
    default: () => []
  },
  maxCount: {
    type: Number,
    default: 9
  },
  maxSize: {
    type: Number,
    default: 5 * 1024 * 1024
  }
})

const emit = defineEmits(['update:modelValue', 'error'])

/** 文件输入框引用 */
const fileInput = ref(null)
/** 上传中状态 */
const uploading = ref(false)
/** 当前上传的图片索引 */
const uploadingIndex = ref(-1)

/**
 * 图片列表计算属性
 * 支持双向绑定
 */
const images = computed({
  get: () => props.modelValue || [],
  set: (val) => emit('update:modelValue', val)
})

/**
 * 是否可以继续添加图片
 */
const canAddMore = computed(() => images.value.length < props.maxCount)

/**
 * 触发文件选择
 * 打开文件选择对话框
 */
const triggerUpload = () => {
  if (canAddMore.value && !uploading.value) {
    fileInput.value?.click()
  }
}

/**
 * 文件选择变化处理
 * @param {Event} e - 文件选择事件
 */
const handleFileChange = (e) => {
  const files = Array.from(e.target.files)
  if (files.length > 0) {
    uploadFiles(files)
  }
  e.target.value = ''
}

/**
 * 批量上传文件
 * 验证文件类型和大小后逐个上传
 * @param {Array<File>} files - 要上传的文件数组
 */
const uploadFiles = async (files) => {
  const remainingSlots = props.maxCount - images.value.length
  const filesToUpload = files.slice(0, remainingSlots)

  for (let i = 0; i < filesToUpload.length; i++) {
    const file = filesToUpload[i]
    
    if (!file.type.startsWith('image/')) {
      emit('error', `${file.name} 不是图片文件`)
      continue
    }

    if (file.size > props.maxSize) {
      emit('error', `${file.name} 大小超过 ${Math.round(props.maxSize / 1024 / 1024)}MB`)
      continue
    }

    uploading.value = true
    uploadingIndex.value = images.value.length + i
    
    try {
      const result = await fileApi.uploadNoteImage(file)
      if (result.code === 200) {
        images.value = [...images.value, result.data.url]
      } else {
        emit('error', result.message || `${file.name} 上传失败`)
      }
    } catch (e) {
      emit('error', e.message || `${file.name} 上传失败`)
    } finally {
      uploading.value = false
      uploadingIndex.value = -1
    }
  }
}

/**
 * 删除指定索引的图片
 * @param {number} index - 图片索引
 */
const removeImage = (index) => {
  const newImages = [...images.value]
  newImages.splice(index, 1)
  images.value = newImages
}

/**
 * 拖拽悬停处理
 * @param {DragEvent} e - 拖拽事件
 */
const handleDragOver = (e) => {
  e.preventDefault()
}

/**
 * 拖拽放下处理
 * 处理拖拽上传的文件
 * @param {DragEvent} e - 拖拽事件
 */
const handleDrop = (e) => {
  e.preventDefault()
  if (canAddMore.value && !uploading.value) {
    const files = Array.from(e.dataTransfer.files)
    if (files.length > 0) {
      uploadFiles(files)
    }
  }
}
</script>

<template>
  <div class="multi-image-uploader">
    <div class="image-list">
      <div v-for="(img, index) in images" :key="index" class="image-item">
        <img :src="img" :alt="`图片${index + 1}`" class="preview-image" />
        <div class="image-overlay">
          <button class="remove-btn" @click="removeImage(index)">
            <Icon icon="ri:close-line" />
          </button>
        </div>
        <span class="image-index">{{ index + 1 }}</span>
      </div>

      <div
        v-if="canAddMore"
        class="upload-btn"
        :class="{ uploading }"
        @click="triggerUpload"
        @dragover="handleDragOver"
        @drop="handleDrop"
      >
        <Icon v-if="!uploading" icon="ri:add-line" class="add-icon" />
        <Icon v-else icon="ri:loader-4-line" class="add-icon spin" />
        <span class="upload-text">{{ uploading ? '上传中...' : '添加图片' }}</span>
        <span class="count-text">{{ images.length }}/{{ maxCount }}</span>
      </div>
    </div>

    <input
      ref="fileInput"
      type="file"
      accept="image/*"
      multiple
      hidden
      @change="handleFileChange"
    />
  </div>
</template>

<style scoped>
.multi-image-uploader {
  width: 100%;
}

.image-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.image-item {
  width: 100px;
  height: 100px;
  border-radius: 8px;
  overflow: hidden;
  position: relative;
  background: #f5f5f5;
}

.preview-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.2s ease;
}

.image-item:hover .image-overlay {
  opacity: 1;
}

.remove-btn {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  border: none;
  background: white;
  color: #333;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  transition: transform 0.2s ease;
}

.remove-btn:hover {
  transform: scale(1.1);
}

.image-index {
  position: absolute;
  bottom: 4px;
  right: 4px;
  background: rgba(0, 0, 0, 0.6);
  color: white;
  font-size: 10px;
  padding: 1px 5px;
  border-radius: 4px;
}

.upload-btn {
  width: 100px;
  height: 100px;
  border: 2px dashed #d9d9d9;
  border-radius: 8px;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  transition: all 0.2s ease;
  background: #fafafa;
}

.upload-btn:hover {
  border-color: #3B5FD9;
  background: #f5f7ff;
}

.upload-btn.uploading {
  cursor: wait;
  opacity: 0.7;
}

.add-icon {
  font-size: 24px;
  color: #999;
}

.add-icon.spin {
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

.upload-text {
  font-size: 12px;
  color: #666;
}

.count-text {
  font-size: 10px;
  color: #999;
}
</style>
