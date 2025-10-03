<template>
  <div class="register-container">
    <div class="register-box">
      <div class="register-header">
        <img src="/logo.svg" alt="ProManage" class="logo" />
        <h1>创建账号</h1>
        <p>加入 ProManage，开始您的项目管理之旅</p>
      </div>

      <a-form
        ref="formRef"
        :model="formData"
        :rules="rules"
        @finish="handleSubmit"
        class="register-form"
      >
        <a-form-item name="username">
          <a-input
            v-model:value="formData.username"
            size="large"
            placeholder="用户名"
          >
            <template #prefix>
              <user-outlined />
            </template>
          </a-input>
        </a-form-item>

        <a-form-item name="email">
          <a-input
            v-model:value="formData.email"
            size="large"
            placeholder="邮箱地址"
          >
            <template #prefix>
              <mail-outlined />
            </template>
          </a-input>
        </a-form-item>

        <a-form-item name="password">
          <a-input-password
            v-model:value="formData.password"
            size="large"
            placeholder="密码"
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
            placeholder="确认密码"
          >
            <template #prefix>
              <lock-outlined />
            </template>
          </a-input-password>
        </a-form-item>

        <a-form-item name="agreement">
          <a-checkbox v-model:checked="formData.agreement">
            我已阅读并同意 <a href="#" @click.prevent="showTerms">服务条款</a> 和 <a href="#" @click.prevent="showPrivacy">隐私政策</a>
          </a-checkbox>
        </a-form-item>

        <a-form-item>
          <a-button
            type="primary"
            html-type="submit"
            size="large"
            block
            :loading="loading"
          >
            创建账号
          </a-button>
        </a-form-item>

        <div class="register-footer">
          <span>已有账号? <router-link to="/login">立即登录</router-link></span>
        </div>
      </a-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { UserOutlined, MailOutlined, LockOutlined } from '@ant-design/icons-vue'
import type { FormInstance } from 'ant-design-vue'
import * as authApi from '@/api/modules/auth'

const router = useRouter()

// 表单引用
const formRef = ref<FormInstance>()

// 表单数据
const formData = reactive({
  username: '',
  email: '',
  password: '',
  confirmPassword: '',
  agreement: false
})

// 加载状态
const loading = ref(false)

// 自定义验证函数
const validateConfirmPassword = (_rule: any, value: string) => {
  if (value && value !== formData.password) {
    return Promise.reject(new Error('两次输入的密码不一致'))
  }
  return Promise.resolve()
}

// 表单验证规则
const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, message: '用户名至少3个字符', trigger: 'blur' },
    { max: 20, message: '用户名最多20个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入有效的邮箱地址', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6个字符', trigger: 'blur' },
    { max: 20, message: '密码最多20个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ],
  agreement: [
    { 
      validator: (_rule: any, value: boolean) => {
        if (!value) {
          return Promise.reject(new Error('请阅读并同意服务条款和隐私政策'))
        }
        return Promise.resolve()
      }, 
      trigger: 'change' 
    }
  ]
}

// 提交注册
const handleSubmit = async () => {
  try {
    loading.value = true

    // 调用注册API
    await authApi.register({
      username: formData.username,
      email: formData.email,
      password: formData.password,
      confirmPassword: formData.confirmPassword
    })

    message.success('账号创建成功！请登录')

    // 跳转到登录页面
    router.push('/login')
  } catch (error: any) {
    message.error(error.message || '注册失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 显示服务条款
const showTerms = () => {
  message.info('服务条款功能开发中...')
}

// 显示隐私政策
const showPrivacy = () => {
  message.info('隐私政策功能开发中...')
}
</script>

<style lang="scss" scoped>
.register-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: var(--space-6);

  .register-box {
    width: 100%;
    max-width: 400px;
    background: var(--color-bg-white);
    border-radius: var(--radius-lg);
    box-shadow: var(--shadow-xl);
    padding: var(--space-8);

    .register-header {
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

    .register-form {
      .register-footer {
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
  .register-container {
    padding: var(--space-4);

    .register-box {
      padding: var(--space-6);
    }
  }
}
</style>
