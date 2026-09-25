import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', component: () => import('@/views/mall/client/product/index.vue') },
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
        { path: 'order', component: () => import('@/views/mall/order/index.vue') }
      ]
    }
  ]
})

router.beforeEach((to) => {
  if (to.matched.some((item) => item.meta.admin) && !localStorage.getItem('adminToken')) return '/admin/login'
  if (to.meta.user && !localStorage.getItem('userToken')) return '/login'
})

export default router
