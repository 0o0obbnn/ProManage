<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <img src="/logo.svg" alt="ProManage" class="logo" />
        <h1>ProManage</h1>
        <p>智能项目管理系统</p>
      </div>

      <a-form
        ref="formRef"
        :model="formData"
        :rules="rules"
        @finish="handleSubmit"
        class="login-form"
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

        <a-form-item>
          <div class="login-options">
            <a-checkbox v-model:checked="rememberMe">记住我</a-checkbox>
            <router-link to="/forgot-password">忘记密码?</router-link>
          </div>
        </a-form-item>

        <a-form-item>
          <a-button
            type="primary"
            html-type="submit"
            size="large"
            block
            :loading="loading"
          >
            登录
          </a-button>
        </a-form-item>

        <div class="login-footer">
          <span>还没有账号? <router-link to="/register">立即注册</router-link></span>
        </div>
      </a-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { message } from 'ant-design-vue'
import { useUserStore } from '@/stores/modules/user'
import { UserOutlined, LockOutlined } from '@ant-design/icons-vue'
import type { FormInstance } from 'ant-design-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 表单引用
const formRef = ref<FormInstance>()

// 表单数据
const formData = reactive({
  username: '',
  password: ''
})

// 记住我
const rememberMe = ref(false)

// 加载状态
const loading = ref(false)

// 表单验证规则
const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, message: '长度至少3个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少6个字符', trigger: 'blur' }
  ]
}

// 提交登录
const handleSubmit = async () => {
  try {
    loading.value = true

    await userStore.login(formData.username, formData.password, rememberMe.value)

    message.success('登录成功')

    // 跳转到重定向地址或首页
    const redirect = (route.query.redirect as string) || '/dashboard'
    router.push(redirect)
  } catch (error: any) {
    message.error(error.message || '登录失败')
  } finally {
    loading.value = false
  }
}

// 使用测试账号登录（开发环境）
const loginWithTestAccount = async () => {
  if (import.meta.env.DEV) {
    formData.username = 'admin'
    formData.password = 'admin123'
    await handleSubmit()
  }
}
</script>

<style lang="scss" scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: var(--space-6);

  .login-box {
    width: 100%;
    max-width: 400px;
    background: var(--color-bg-white);
    border-radius: var(--radius-lg);
    box-shadow: var(--shadow-xl);
    padding: var(--space-8);

    .login-header {
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

    .login-form {
      .login-options {
        display: flex;
        justify-content: space-between;
        align-items: center;
      }

      .login-footer {
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
  .login-container {
    padding: var(--space-4);

    .login-box {
      padding: var(--space-6);
    }
  }
}
</style>