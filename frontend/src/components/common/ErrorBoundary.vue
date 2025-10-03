<template>
  <div v-if="hasError" class="error-boundary">
    <a-result
      status="error"
      title="页面出现错误"
      :sub-title="errorMessage"
    >
      <template #extra>
        <a-space>
          <a-button type="primary" @click="handleRetry">
            重试
          </a-button>
          <a-button @click="handleReport">
            报告问题
          </a-button>
          <a-button @click="handleGoHome">
            返回首页
          </a-button>
        </a-space>
      </template>
    </a-result>
    
    <!-- 开发环境下显示详细错误信息 -->
    <div v-if="isDevelopment" class="error-details">
      <a-collapse>
        <a-collapse-panel key="1" header="错误详情">
          <pre class="error-stack">{{ errorStack }}</pre>
        </a-collapse-panel>
      </a-collapse>
    </div>
  </div>
  
  <slot v-else />
</template>

<script setup lang="ts">
import { ref, onErrorCaptured, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'

interface Props {
  fallback?: string
  onError?: (error: Error, errorInfo: any) => void
}

const props = withDefaults(defineProps<Props>(), {
  fallback: '页面加载失败，请刷新重试'
})

const router = useRouter()

const hasError = ref(false)
const errorMessage = ref('')
const errorStack = ref('')
const isDevelopment = ref(import.meta.env.DEV)

// 错误处理
onErrorCaptured((error: Error, instance: any, info: string) => {
  console.error('ErrorBoundary caught an error:', error, info)
  
  hasError.value = true
  errorMessage.value = error.message || props.fallback
  errorStack.value = error.stack || ''
  
  // 调用外部错误处理函数
  if (props.onError) {
    props.onError(error, { instance, info })
  }
  
  // 发送错误报告到监控系统
  reportError(error, { instance, info })
  
  return false // 阻止错误继续传播
})

// 重试处理
const handleRetry = () => {
  hasError.value = false
  errorMessage.value = ''
  errorStack.value = ''
  
  // 重新加载当前页面
  window.location.reload()
}

// 报告问题
const handleReport = () => {
  const errorReport = {
    message: errorMessage.value,
    stack: errorStack.value,
    url: window.location.href,
    userAgent: navigator.userAgent,
    timestamp: new Date().toISOString()
  }
  
  // 这里可以集成错误报告服务，如Sentry
  console.log('Error report:', errorReport)
  
  message.success('问题已报告，我们会尽快处理')
}

// 返回首页
const handleGoHome = () => {
  router.push('/')
}

// 发送错误到监控系统
const reportError = (error: Error, errorInfo: any) => {
  // 这里可以集成错误监控服务
  if (typeof window !== 'undefined' && window.gtag) {
    window.gtag('event', 'exception', {
      description: error.message,
      fatal: false
    })
  }
}

// 全局错误监听
const handleGlobalError = (event: ErrorEvent) => {
  console.error('Global error:', event.error)
  reportError(event.error, { type: 'global' })
}

const handleUnhandledRejection = (event: PromiseRejectionEvent) => {
  console.error('Unhandled promise rejection:', event.reason)
  reportError(new Error(event.reason), { type: 'unhandledRejection' })
}

onMounted(() => {
  window.addEventListener('error', handleGlobalError)
  window.addEventListener('unhandledrejection', handleUnhandledRejection)
})

onUnmounted(() => {
  window.removeEventListener('error', handleGlobalError)
  window.removeEventListener('unhandledrejection', handleUnhandledRejection)
})
</script>

<style scoped lang="scss">
.error-boundary {
  padding: 24px;
  min-height: 400px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.error-details {
  margin-top: 24px;
  
  .error-stack {
    background: #f5f5f5;
    padding: 16px;
    border-radius: 4px;
    font-size: 12px;
    line-height: 1.5;
    overflow-x: auto;
    white-space: pre-wrap;
    word-break: break-word;
  }
}
</style>
