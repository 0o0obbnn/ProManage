<script setup lang="ts">
import { onMounted } from 'vue'
import { useUserStore } from '@/stores/modules/user'

const userStore = useUserStore()

// 应用初始化
onMounted(() => {
  // 从本地存储恢复用户状态
  userStore.restoreFromLocalStorage()
})
</script>

<template>
  <a-config-provider
    :theme="{
      token: {
        colorPrimary: '#1890ff',
        borderRadius: 4
      }
    }"
  >
    <router-view />
  </a-config-provider>
</template>

<style scoped>
/* App 级别样式 */
</style>
