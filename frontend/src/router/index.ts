/**
 * Vue Router 配置
 */
import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'

// 路由配置
const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/Login.vue'),
    meta: {
      title: '登录',
      requiresAuth: false,
      layout: 'blank'
    }
  },
  {
    path: '/forgot-password',
    name: 'ForgotPassword',
    component: () => import('@/views/auth/ForgotPassword.vue'),
    meta: {
      title: '忘记密码',
      requiresAuth: false,
      layout: 'blank'
    }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/auth/Register.vue'),
    meta: {
      title: '注册',
      requiresAuth: false,
      layout: 'blank'
    }
  },
  {
    path: '/',
    redirect: '/dashboard',
    component: () => import('@/layouts/DefaultLayout.vue'),
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: {
          title: '工作台',
          requiresAuth: true,
          icon: 'DashboardOutlined'
        }
      },
      {
        path: 'projects',
        name: 'Projects',
        component: () => import('@/views/project/index.vue'),
        meta: {
          title: '项目管理',
          requiresAuth: true,
          icon: 'ProjectOutlined',
          permissions: ['project:view']
        }
      },
      {
        path: 'projects/:id',
        name: 'ProjectDetail',
        component: () => import('@/views/project/Detail.vue'),
        meta: {
          title: '项目详情',
          requiresAuth: true,
          permissions: ['project:view']
        }
      },
      {
        path: 'documents',
        name: 'Documents',
        component: () => import('@/views/document/index.vue'),
        meta: {
          title: '文档管理',
          requiresAuth: true,
          icon: 'FileTextOutlined',
          permissions: ['document:view']
        }
      },
      {
        path: 'documents/:id',
        name: 'DocumentDetail',
        component: () => import('@/views/document/Detail.vue'),
        meta: {
          title: '文档详情',
          requiresAuth: true,
          permissions: ['document:view']
        }
      },
      {
        path: 'documents/:id/edit',
        name: 'DocumentEditor',
        component: () => import('@/views/document/Editor.vue'),
        meta: {
          title: '编辑文档',
          requiresAuth: true,
          permissions: ['document:edit']
        }
      },
      {
        path: 'tasks',
        name: 'Tasks',
        component: () => import('@/views/task/index.vue'),
        meta: {
          title: '任务管理',
          requiresAuth: true,
          icon: 'CheckSquareOutlined',
          permissions: ['task:view']
        }
      },
      {
        path: 'changes',
        name: 'Changes',
        component: () => import('@/views/change/index.vue'),
        meta: {
          title: '变更管理',
          requiresAuth: true,
          icon: 'SwapOutlined',
          permissions: ['change:view']
        }
      },
      {
        path: 'test',
        name: 'Test',
        component: () => import('@/views/test/index.vue'),
        meta: {
          title: '测试管理',
          requiresAuth: true,
          icon: 'ExperimentOutlined',
          permissions: ['test:view']
        }
      },
      {
        path: 'test/bugs',
        name: 'Bugs',
        component: () => import('@/views/test/bug.vue'),
        meta: {
          title: '缺陷管理',
          requiresAuth: true,
          icon: 'BugOutlined',
          permissions: ['test:view']
        }
      }
    ]
  },
  {
    path: '/403',
    name: 'Forbidden',
    component: () => import('@/views/error/403.vue'),
    meta: {
      title: '403 - 权限不足',
      requiresAuth: false
    }
  },
  {
    path: '/404',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue'),
    meta: {
      title: '404 - 页面不存在',
      requiresAuth: false
    }
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/404'
  }
]

// 创建路由实例
const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
  scrollBehavior() {
    return { top: 0 }
  }
})

export default router