/**
 * 路由守卫配置
 */
import type { Router } from 'vue-router'
import { useUserStore } from '@/stores/modules/user'
import { message } from 'ant-design-vue'
import { getToken, isTokenExpired, clearAuth } from '@/utils/auth'

/**
 * 白名单路由（不需要登录）
 */
const whiteList = ['Login', 'Register', 'ForgotPassword', 'NotFound', 'Forbidden']

/**
 * 配置路由守卫
 */
export function setupRouterGuards(router: Router) {
  // 全局前置守卫
  router.beforeEach(async (to, from, next) => {
    const userStore = useUserStore()

    // 设置页面标题
    document.title = to.meta.title ? `${to.meta.title} - ProManage` : 'ProManage'

    // 检查token是否过期
    const token = getToken()
    if (token && isTokenExpired(token, 0)) {
      console.warn('Token已过期，清除认证信息')
      clearAuth()
      await userStore.logout()
    }

    // 首次加载时从localStorage恢复用户状态
    if (!userStore.isLoggedIn && getToken()) {
      userStore.restoreFromLocalStorage()
    }

    // 检查是否需要登录
    const requiresAuth = to.meta.requiresAuth !== false // 默认需要登录

    if (requiresAuth) {
      // 未登录，重定向到登录页
      if (!userStore.isLoggedIn) {
        message.warning('请先登录')
        next({
          name: 'Login',
          query: { redirect: to.fullPath }
        })
        return
      }

      // 已登录但没有用户信息，尝试获取
      if (!userStore.userInfo) {
        try {
          await userStore.fetchUserInfo()
        } catch (error) {
          console.error('获取用户信息失败:', error)
          message.error('登录状态已失效，请重新登录')
          clearAuth()
          await userStore.logout()
          next({
            name: 'Login',
            query: { redirect: to.fullPath }
          })
          return
        }
      }

      // 检查权限
      if (to.meta.permissions) {
        const permissions = to.meta.permissions as string[]
        const hasPermission = permissions.some((permission) =>
          userStore.hasPermission(permission)
        )

        if (!hasPermission) {
          message.error('您没有权限访问此页面')
          next({ name: 'Forbidden' })
          return
        }
      }

      // 检查角色权限
      if (to.meta.roles) {
        const requiredRoles = to.meta.roles as string[]
        const userRoles = userStore.userRoles.map(r => r.roleCode)
        const hasRole = requiredRoles.some(role => userRoles.includes(role))

        if (!hasRole) {
          message.error('您的角色无权访问此页面')
          next({ name: 'Forbidden' })
          return
        }
      }
    }

    // 如果已登录且访问登录页，重定向到首页
    if (whiteList.includes(to.name as string) && userStore.isLoggedIn) {
      // 只有访问登录、注册页面时才重定向，403/404页面不重定向
      if (['Login', 'Register', 'ForgotPassword'].includes(to.name as string)) {
        next({ name: 'Dashboard' })
        return
      }
    }

    next()
  })

  // 全局后置钩子
  router.afterEach((to, from, failure) => {
    // 页面访问统计（可选）
    if (!failure) {
      // console.log(`Navigated from ${from.path} to ${to.path}`)
    }
  })

  // 全局错误处理
  router.onError((error) => {
    console.error('路由错误:', error)
    message.error('页面加载失败，请刷新重试')
  })
}
