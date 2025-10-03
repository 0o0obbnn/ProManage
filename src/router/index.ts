import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router';
import DefaultLayout from '@/layouts/DefaultLayout.vue';

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    component: DefaultLayout,
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: {
          title: '仪表板'
        }
      },
      {
        path: 'tasks',
        name: 'Tasks',
        component: () => import('@/views/tasks/index.vue'),
        meta: {
          title: '任务管理'
        }
      },
      {
        path: 'tasks/:id',
        name: 'TaskDetail',
        component: () => import('@/views/tasks/detail.vue'),
        meta: {
          title: '任务详情'
        }
      },
      {
        path: 'documents',
        name: 'Documents',
        component: () => import('@/views/documents/index.vue'),
        meta: {
          title: '文档管理'
        }
      },
      {
        path: 'documents/:id',
        name: 'DocumentDetail',
        component: () => import('@/views/documents/detail.vue'),
        meta: {
          title: '文档详情'
        }
      },
      {
        path: 'changes',
        name: 'Changes',
        component: () => import('@/views/changes/index.vue'),
        meta: {
          title: '变更管理'
        }
      },
      {
        path: 'changes/:id',
        name: 'ChangeDetail',
        component: () => import('@/views/changes/detail.vue'),
        meta: {
          title: '变更详情'
        }
      },
      {
        path: 'tests',
        name: 'Tests',
        component: () => import('@/views/tests/index.vue'),
        meta: {
          title: '测试管理'
        }
      },
      {
        path: 'tests/:id',
        name: 'TestDetail',
        component: () => import('@/views/tests/detail.vue'),
        meta: {
          title: '测试详情'
        }
      },
      {
        path: 'notifications',
        name: 'Notifications',
        component: () => import('@/views/notification/index.vue'),
        meta: {
          title: '通知中心'
        }
      }
    ]
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/login.vue'),
    meta: {
      title: '登录',
      hideLayout: true
    }
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue'),
    meta: {
      title: '404',
      hideLayout: true
    }
  }
];

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition;
    } else {
      return { top: 0 };
    }
  }
});

// 路由守卫
router.beforeEach((to, from, next) => {
  // 设置页面标题
  if (to.meta.title) {
    document.title = `${to.meta.title} - ProManage`;
  }

  // 这里可以添加权限验证等逻辑
  next();
});

export default router;
