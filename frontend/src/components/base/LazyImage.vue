<template>
  <picture class="lazy-image-container">
    <!-- WebP格式 -->
    <source
      v-if="webpSrc"
      :srcset="webpSrc"
      type="image/webp"
    />
    
    <!-- 原始格式 -->
    <img
      v-lazy="lazySrc"
      :alt="alt"
      :width="width"
      :height="height"
      :class="['lazy-image', { 'lazy-image--loaded': loaded }]"
      @load="onLoad"
      @error="onError"
    />
  </picture>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'

interface Props {
  src: string
  webpSrc?: string
  alt?: string
  placeholder?: string
  width?: number | string
  height?: number | string
}

const props = withDefaults(defineProps<Props>(), {
  alt: '',
  placeholder: '/images/placeholder.png'
})

const emit = defineEmits(['load', 'error'])

const loaded = ref(false)
const error = ref(false)

// 懒加载的图片地址
const lazySrc = computed(() => {
  return error.value ? props.placeholder : props.src
})

// 加载成功
const onLoad = (e: Event) => {
  loaded.value = true
  emit('load', e)
}

// 加载失败
const onError = (e: Event) => {
  error.value = true
  emit('error', e)
}
</script>

<style scoped lang="scss">
.lazy-image-container {
  display: inline-block;
  width: 100%;
  height: 100%;
}

.lazy-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: opacity 0.3s ease;
  
  &--loaded {
    opacity: 1;
  }
}
</style>