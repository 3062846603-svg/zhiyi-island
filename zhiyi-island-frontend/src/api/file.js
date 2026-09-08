/**
 * 文件上传相关 API
 * 包含笔记图片和用户头像上传接口
 */
import api from '@/utils/request'

export const fileApi = {
  /**
   * 上传笔记图片
   * 支持常见图片格式，单张图片最大10MB
   * @param {File} file - 图片文件
   * @returns {Promise} 上传结果，包含图片URL
   */
  uploadNoteImage(file) {
    const formData = new FormData()
    formData.append('file', file)
    return api.post('/file/upload/note-image', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  /**
   * 上传用户头像
   * 支持常见图片格式，头像最大2MB
   * @param {File} file - 头像文件
   * @returns {Promise} 上传结果，包含头像URL
   */
  uploadAvatar(file) {
    const formData = new FormData()
    formData.append('file', file)
    return api.post('/file/upload/avatar', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  }
}
