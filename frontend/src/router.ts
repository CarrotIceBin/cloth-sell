import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  // 有帮助中心这类锚点链接，带 hash 时滚到对应位置
  scrollBehavior: (to, from, savedPosition) => {
    if (savedPosition) return savedPosition
    if (to.hash) return { el: to.hash, top: 120, behavior: 'smooth' }
    return { top: 0 }
  },
  routes: [
    { path: '/', component: () => import('@/views/mall/client/home/index.vue') },
    { path: '/shop', component: () => import('@/views/mall/client/shop/index.vue') },
    { path: '/about', component: () => import('@/views/mall/client/about/index.vue') },
    { path: '/help', component: () => import('@/views/mall/client/help/index.vue') },
    { path: '/journal', component: () => import('@/views/mall/client/journal/index.vue') },
    { path: '/journal/:id', component: () => import('@/views/mall/client/journal/detail.vue') },
    { path: '/product/:id', component: () => import('@/views/mall/client/product/detail.vue') },
    { path: '/cart', component: () => import('@/views/mall/client/cart/index.vue'), meta: { user: true } },
    { path: '/checkout', component: () => import('@/views/mall/client/order/checkout.vue'), meta: { user: true } },
    { path: '/orders', component: () => import('@/views/mall/client/order/index.vue'), meta: { user: true } },
    { path: '/login', component: () => import('@/views/mall/auth/login.vue') },
    { path: '/admin/login', component: () => import('@/views/mall/auth/admin.vue') },
    {
      path: '/admin',
      component: () => import('@/views/mall/AdminLayout.vue'),
      meta: { admin: true },
      children: [
        { path: 'product', component: () => import('@/views/mall/product/index.vue') },
        { path: 'order', component: () => import('@/views/mall/order/index.vue') },
        { path: 'journal', component: () => import('@/views/mall/journal/index.vue') },
        { path: 'review', component: () => import('@/views/mall/review/index.vue') }
      ]
    }
  ]
})

router.beforeEach((to) => {
  if (to.matched.some((item) => item.meta.admin) && !localStorage.getItem('adminToken')) return '/admin/login'
  if (to.meta.user && !localStorage.getItem('userToken')) return '/login'
})

export default router
