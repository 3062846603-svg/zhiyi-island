/**
 * 表单验证规则和验证器
 * 提供统一的表单验证配置和验证函数
 */

/**
 * 验证规则配置
 * 定义各字段的验证规则和错误消息
 */
export const ValidationRules = {
    /** 用户名验证规则 */
    username: {
        minLength: 3,
        maxLength: 20,
        pattern: /^[a-zA-Z0-9_]+$/,
        messages: {
            required: '请输入用户名',
            minLength: '用户名至少3个字符',
            maxLength: '用户名最多20个字符',
            pattern: '用户名只能包含字母、数字和下划线',
            exists: '该用户名已被使用'
        }
    },

    /** 密码验证规则 */
    password: {
        minLength: 6,
        maxLength: 20,
        messages: {
            required: '请输入密码',
            minLength: '密码至少6个字符',
            maxLength: '密码最多20个字符'
        }
    },

    /** 邮箱验证规则 */
    email: {
        pattern: /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/,
        messages: {
            required: '请输入邮箱',
            pattern: '请输入有效的邮箱地址'
        }
    },

    /** 昵称验证规则 */
    nickname: {
        maxLength: 20,
        messages: {
            maxLength: '昵称最多20个字符'
        }
    },

    /** 手机号验证规则 */
    phone: {
        maxLength: 20,
        pattern: /^1[3-9]\d{9}$/,
        messages: {
            pattern: '请输入有效的手机号',
            maxLength: '手机号最多20个字符'
        }
    },

    /** 验证码验证规则 */
    verifyCode: {
        length: 6,
        messages: {
            required: '请输入验证码',
            length: '验证码为6位'
        }
    },

    /** 笔记验证规则 */
    note: {
        title: {
            maxLength: 200,
            messages: {
                required: '请输入笔记标题',
                maxLength: '标题最多200个字符'
            }
        },
        summary: {
            maxLength: 500,
            messages: {
                maxLength: '摘要最多500个字符'
            }
        },
        content: {
            messages: {
                required: '请输入笔记内容'
            }
        }
    },

    /** 知识库验证规则 */
    knowledge: {
        title: {
            maxLength: 200,
            messages: {
                required: '请输入标题',
                maxLength: '标题最多200个字符'
            }
        }
    },

    /** 头像验证规则 */
    avatar: {
        maxSize: 5 * 1024 * 1024,
        messages: {
            maxSize: '图片大小不能超过5MB'
        }
    },

    /** 笔记图片验证规则 */
    noteImage: {
        maxSize: 10 * 1024 * 1024,
        maxCount: 9,
        messages: {
            maxSize: '图片大小不能超过10MB',
            maxCount: '最多上传9张图片'
        }
    }
}

/**
 * 验证器集合
 * 提供各字段的验证函数
 */
export const validators = {
    /**
     * 验证用户名
     * @param {string} value - 用户名值
     * @param {boolean} blurred - 是否失去焦点（未失焦时不验证）
     * @returns {{valid: boolean, message: string}} 验证结果
     */
    username: (value, blurred = true) => {
        if (!blurred) return { valid: true, message: '' }
        if (!value) return { valid: false, message: ValidationRules.username.messages.required }
        if (value.length < ValidationRules.username.minLength) {
            return { valid: false, message: ValidationRules.username.messages.minLength }
        }
        if (value.length > ValidationRules.username.maxLength) {
            return { valid: false, message: ValidationRules.username.messages.maxLength }
        }
        if (!ValidationRules.username.pattern.test(value)) {
            return { valid: false, message: ValidationRules.username.messages.pattern }
        }
        return { valid: true, message: '' }
    },

    /**
     * 验证密码
     * @param {string} value - 密码值
     * @param {boolean} blurred - 是否失去焦点
     * @returns {{valid: boolean, message: string}} 验证结果
     */
    password: (value, blurred = true) => {
        if (!blurred) return { valid: true, message: '' }
        if (!value) return { valid: false, message: ValidationRules.password.messages.required }
        if (value.length < ValidationRules.password.minLength) {
            return { valid: false, message: ValidationRules.password.messages.minLength }
        }
        if (value.length > ValidationRules.password.maxLength) {
            return { valid: false, message: ValidationRules.password.messages.maxLength }
        }
        return { valid: true, message: '' }
    },

    /**
     * 验证邮箱
     * @param {string} value - 邮箱值
     * @param {boolean} blurred - 是否失去焦点
     * @returns {{valid: boolean, message: string}} 验证结果
     */
    email: (value, blurred = true) => {
        if (!blurred) return { valid: true, message: '' }
        if (!value) return { valid: false, message: ValidationRules.email.messages.required }
        if (!ValidationRules.email.pattern.test(value)) {
            return { valid: false, message: ValidationRules.email.messages.pattern }
        }
        return { valid: true, message: '' }
    },

    /**
     * 验证昵称
     * @param {string} value - 昵称值
     * @param {boolean} blurred - 是否失去焦点
     * @returns {{valid: boolean, message: string}} 验证结果
     */
    nickname: (value, blurred = true) => {
        if (!blurred || !value) return { valid: true, message: '' }
        if (value.length > ValidationRules.nickname.maxLength) {
            return { valid: false, message: ValidationRules.nickname.messages.maxLength }
        }
        return { valid: true, message: '' }
    },

    /**
     * 验证手机号
     * @param {string} value - 手机号值
     * @param {boolean} blurred - 是否失去焦点
     * @returns {{valid: boolean, message: string}} 验证结果
     */
    phone: (value, blurred = true) => {
        if (!blurred || !value) return { valid: true, message: '' }
        if (value.length > ValidationRules.phone.maxLength) {
            return { valid: false, message: ValidationRules.phone.messages.maxLength }
        }
        if (!ValidationRules.phone.pattern.test(value)) {
            return { valid: false, message: ValidationRules.phone.messages.pattern }
        }
        return { valid: true, message: '' }
    },

    /**
     * 验证验证码
     * @param {string} value - 验证码值
     * @param {boolean} blurred - 是否失去焦点
     * @returns {{valid: boolean, message: string}} 验证结果
     */
    verifyCode: (value, blurred = true) => {
        if (!blurred) return { valid: true, message: '' }
        if (!value) return { valid: false, message: ValidationRules.verifyCode.messages.required }
        if (value.length !== ValidationRules.verifyCode.length) {
            return { valid: false, message: ValidationRules.verifyCode.messages.length }
        }
        return { valid: true, message: '' }
    },

    /**
     * 验证笔记标题
     * @param {string} value - 标题值
     * @param {boolean} blurred - 是否失去焦点
     * @returns {{valid: boolean, message: string}} 验证结果
     */
    noteTitle: (value, blurred = true) => {
        if (!blurred) return { valid: true, message: '' }
        if (!value || !value.trim()) return { valid: false, message: ValidationRules.note.title.messages.required }
        if (value.length > ValidationRules.note.title.maxLength) {
            return { valid: false, message: ValidationRules.note.title.messages.maxLength }
        }
        return { valid: true, message: '' }
    },

    /**
     * 验证笔记内容
     * @param {string} value - 内容值
     * @param {boolean} blurred - 是否失去焦点
     * @returns {{valid: boolean, message: string}} 验证结果
     */
    noteContent: (value, blurred = true) => {
        if (!blurred) return { valid: true, message: '' }
        if (!value || !value.trim()) return { valid: false, message: ValidationRules.note.content.messages.required }
        return { valid: true, message: '' }
    }
}
