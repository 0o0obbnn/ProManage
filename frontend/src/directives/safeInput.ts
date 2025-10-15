/**
 * 安全输入指令
 * 自动清理用户输入，防止XSS攻击
 */

import type { App, DirectiveBinding } from 'vue'
import { sanitizeText, cleanUserInput } from '@/utils/security'

interface SafeInputElement extends HTMLInputElement {
  _safeInputHandler?: (e: Event) => void
}

export const safeInput = {
  mounted(el: SafeInputElement, binding: DirectiveBinding) {
    const maxLength = binding.value?.maxLength || 1000
    const trim = binding.value?.trim !== false

    const handler = (e: Event) => {
      const target = e.target as HTMLInputElement
      let value = target.value

      // 清理输入
      value = cleanUserInput(value, maxLength)

      // 移除HTML标签
      value = sanitizeText(value)

      // 可选的trim
      if (trim) {
        value = value.trim()
      }

      // 更新值
      if (target.value !== value) {
        target.value = value
        // 触发input事件以更新v-model
        target.dispatchEvent(new Event('input', { bubbles: true }))
      }
    }

    el._safeInputHandler = handler
    el.addEventListener('blur', handler)
  },

  unmounted(el: SafeInputElement) {
    if (el._safeInputHandler) {
      el.removeEventListener('blur', el._safeInputHandler)
      delete el._safeInputHandler
    }
  }
}

export const setupSafeInput = (app: App) => {
  app.directive('safe-input', safeInput)
}
