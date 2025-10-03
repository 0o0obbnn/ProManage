<template>
  <div class="forgot-password-container">
    <div class="forgot-password-box">
      <div class="forgot-password-header">
        <img src="/logo.svg" alt="ProManage" class="logo" />
        <h1>忘记密码</h1>
        <p v-if="step === 1">输入您的邮箱地址，我们将发送重置密码的验证码给您</p>
        <p v-else>请输入验证码和新密码</p>
      </div>

      <!-- 步骤1: 发送验证码 -->
      <a-form
        v-if="step === 1"
        ref="formRef1"
        :model="formData"
        :rules="rulesStep1"
        @finish="handleSendCode"
        class="forgot-password-form"
      >
        <a-form-item name="email">
          <a-input
            v-model:value="formData.email"
            size="large"
            placeholder="请输入邮箱地址"
          >
            <template #prefix>
              <mail-outlined />
            </template>
          </a-input>
        </a-form-item>

        <a-form-item>
          <a-button
            type="primary"
            html-type="submit"
            size="large"
            block
            :loading="loading"
          >
            发送验证码
          </a-button>
        </a-form-item>

        <div class="forgot-password-footer">
          <span>记起密码了? <router-link to="/login">返回登录</router-link></span>
        </div>
      </a-form>

      <!-- 步骤2: 重置密码 -->
      <a-form
        v-else
        ref="formRef2"
        :model="formData"
        :rules="rulesStep2"
        @finish="handleResetPassword"
        class="forgot-password-form"
      >
        <a-form-item name="verificationCode">
          <a-input
            v-model:value="formData.verificationCode"
            size="large"
            placeholder="请输入验证码"
            maxlength="6"
          >
            <template #prefix>
              <safety-outlined />
            </template>
          </a-input>
        </a-form-item>

        <a-form-item name="newPassword">
          <a-input-password
            v-model:value="formData.newPassword"
            size="large"
            placeholder="新密码"
          >
            <template #prefix>
              <lock-outlined />
            </template>
          </a-input-password>
        </a-form-item>

        <a-form-item name="confirmPassword">
          <a-input-password
            v-model:value="formData.confirmPassword"
            size="large"
            placeholder="确认新密码"
          >
            <template #prefix>
              <lock-outlined />
            </template>
          </a-input-password>
        </a-form-item>

        <a-form-item>
          <a-button
            type="primary"
            html-type="submit"
            size="large"
            block
            :loading="loading"
          >
            重置密码
          </a-button>
        </a-form-item>

        <div class="forgot-password-footer">
          <a @click="step = 1">重新发送验证码</a> |
          <router-link to="/login">返回登录</router-link>
        </div>
      </a-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { MailOutlined, LockOutlined, SafetyOutlined } from '@ant-design/icons-vue'
import type { FormInstance } from 'ant-design-vue'
import * as authApi from '@/api/modules/auth'

const router = useRouter()

// 当前步骤：1-发送验证码，2-重置密码
const step = ref(1)

// 表单引用
const formRef1 = ref<FormInstance>()
const formRef2 = ref<FormInstance>()

// 表单数据
const formData = reactive({
  email: '',
  verificationCode: '',
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

// 步骤1验证规则
const rulesStep1 = {
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入有效的邮箱地址', trigger: 'blur' }
  ]
}

// 步骤2验证规则
const rulesStep2 = {
  verificationCode: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 6, message: '验证码必须为6位', trigger: 'blur' }
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

// 发送验证码
const handleSendCode = async () => {
  try {
    loading.value = true

    await authApi.sendResetCode(formData.email)

    message.success('验证码已发送到您的邮箱，请查收')

    // 进入下一步
    step.value = 2
  } catch (error: any) {
    message.error(error.message || '发送失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 重置密码
const handleResetPassword = async () => {
  try {
    loading.value = true

    await authApi.resetPassword({
      email: formData.email,
      verificationCode: formData.verificationCode,
      newPassword: formData.newPassword,
      confirmPassword: formData.confirmPassword
    })

    message.success('密码重置成功！请使用新密码登录')

    // 跳转到登录页
    router.push('/login')
  } catch (error: any) {
    message.error(error.message || '重置失败，请检查验证码是否正确')
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.forgot-password-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: var(--space-6);

  .forgot-password-box {
    width: 100%;
    max-width: 400px;
    background: var(--color-bg-white);
    border-radius: var(--radius-lg);
    box-shadow: var(--shadow-xl);
    padding: var(--space-8);

    .forgot-password-header {
      text-align: center;
      margin-bottom: var(--space-8);

      .logo {
        width: 64px;
        height: 64px;
        margin-bottom: var(--space-4);
      }

      h1 {
        font-size: var(--font-size-4xl);
        color: var(--color-text-primary);
        margin-bottom: var(--space-2);
      }

      p {
        color: var(--color-text-secondary);
        font-size: var(--font-size-base);
      }
    }

    .forgot-password-form {
      .forgot-password-footer {
        text-align: center;
        color: var(--color-text-secondary);
        font-size: var(--font-size-sm);

        a {
          color: var(--color-primary-600);
          font-weight: var(--font-weight-medium);
        }
      }
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .forgot-password-container {
    padding: var(--space-4);

    .forgot-password-box {
      padding: var(--space-6);
    }
  }
}
</style>
