<template>
  <div class="change-password-container">
    <a-card title="修改密码" :bordered="false">
      <a-form
        ref="formRef"
        :model="formData"
        :rules="rules"
        @finish="handleSubmit"
        :label-col="{ span: 6 }"
        :wrapper-col="{ span: 14 }"
      >
        <a-form-item label="旧密码" name="oldPassword">
          <a-input-password
            v-model:value="formData.oldPassword"
            placeholder="请输入旧密码"
            autocomplete="current-password"
          />
        </a-form-item>

        <a-form-item label="新密码" name="newPassword">
          <a-input-password
            v-model:value="formData.newPassword"
            placeholder="请输入新密码（6-20个字符）"
            autocomplete="new-password"
          />
          <div v-if="passwordStrength" class="password-strength-indicator">
            <div :class="['strength-bar', `strength-${passwordStrength.level}`]">
              <div class="strength-fill" :style="{ width: passwordStrength.percent + '%' }"></div>
            </div>
            <span :class="['strength-text', `strength-${passwordStrength.level}`]">
              {{ passwordStrength.text }}
            </span>
          </div>
        </a-form-item>

        <a-form-item label="确认密码" name="confirmPassword">
          <a-input-password
            v-model:value="formData.confirmPassword"
            placeholder="请再次输入新密码"
            autocomplete="new-password"
          />
        </a-form-item>

        <a-form-item :wrapper-col="{ offset: 6, span: 14 }">
          <a-space>
            <a-button type="primary" html-type="submit" :loading="loading">
              提交修改
            </a-button>
            <a-button @click="handleReset">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue'
import { message } from 'ant-design-vue'
import type { FormInstance } from 'ant-design-vue'
import * as authApi from '@/api/modules/auth'

// 表单引用
const formRef = ref<FormInstance>()

// 表单数据
const formData = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 加载状态
const loading = ref(false)

// 自定义验证函数
const validateConfirmPassword = (_rule: any, value: string) => {
  if (value && value !== formData.newPassword) {
    return Promise.reject(new Error('两次输入的密码不一致'))
  }
  return Promise.resolve()
}

// 表单验证规则
const rules = {
  oldPassword: [
    { required: true, message: '请输入旧密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码至少6个字符', trigger: 'blur' },
    { max: 20, message: '密码最多20个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

// 密码强度检查
interface PasswordStrength {
  level: 'weak' | 'medium' | 'strong'
  percent: number
  text: string
}

const passwordStrength = computed<PasswordStrength | null>(() => {
  const password = formData.newPassword
  if (!password) return null

  let score = 0
  const checks = {
    length: password.length >= 8,
    hasLowercase: /[a-z]/.test(password),
    hasUppercase: /[A-Z]/.test(password),
    hasNumber: /\d/.test(password),
    hasSpecial: /[!@#$%^&*(),.?":{}|<>]/.test(password)
  }

  // 计算强度分数
  if (checks.length) score += 20
  if (checks.hasLowercase) score += 20
  if (checks.hasUppercase) score += 20
  if (checks.hasNumber) score += 20
  if (checks.hasSpecial) score += 20

  // 确定强度等级
  let level: 'weak' | 'medium' | 'strong'
  let text: string

  if (score < 40) {
    level = 'weak'
    text = '弱'
  } else if (score < 80) {
    level = 'medium'
    text = '中'
  } else {
    level = 'strong'
    text = '强'
  }

  return { level, percent: score, text }
})

// 提交修改
const handleSubmit = async () => {
  try {
    loading.value = true

    await authApi.changePassword({
      oldPassword: formData.oldPassword,
      newPassword: formData.newPassword,
      confirmPassword: formData.confirmPassword
    })

    message.success('密码修改成功！')

    // 重置表单
    formRef.value?.resetFields()
  } catch (error: any) {
    message.error(error.message || '修改失败，请检查旧密码是否正确')
  } finally {
    loading.value = false
  }
}

// 重置表单
const handleReset = () => {
  formRef.value?.resetFields()
}
</script>

<style lang="scss" scoped>
.change-password-container {
  max-width: 600px;
  margin: 0 auto;
  padding: var(--space-6);

  .password-strength-indicator {
    margin-top: var(--space-2);

    .strength-bar {
      height: 4px;
      background-color: var(--color-border);
      border-radius: 2px;
      overflow: hidden;
      margin-bottom: var(--space-1);

      .strength-fill {
        height: 100%;
        transition: width 0.3s ease;
      }

      &.strength-weak .strength-fill {
        background-color: #ff4d4f;
      }

      &.strength-medium .strength-fill {
        background-color: #faad14;
      }

      &.strength-strong .strength-fill {
        background-color: #52c41a;
      }
    }

    .strength-text {
      font-size: var(--font-size-sm);

      &.strength-weak {
        color: #ff4d4f;
      }

      &.strength-medium {
        color: #faad14;
      }

      &.strength-strong {
        color: #52c41a;
      }
    }
  }
}
</style>
