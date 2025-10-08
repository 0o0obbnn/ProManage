import { describe, it, expect, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import Login from '../Login.vue'

describe('Login', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('renders form elements', () => {
    const wrapper = mount(Login)
    expect(wrapper.find('input').exists()).toBe(true)
  })
})
