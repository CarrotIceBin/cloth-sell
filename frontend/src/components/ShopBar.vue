<template>
  <header class="bar">
    <router-link class="brand" to="/">
      <svg class="mark" viewBox="0 0 64 48" aria-hidden="true">
        <path d="M36.5 8.5a4.2 4.2 0 0 1 4.3 4.1c0 2.3-1.7 3.5-3.6 4.3L32 20.2" />
        <path d="M8 38.5 32 20.2 56 38.5" />
        <path d="M14 38.5h36" />
      </svg>
      <span>CLOTH</span>
    </router-link>
    <nav>
      <router-link to="/" :class="{ on: route.path === '/' }">首页</router-link>
      <router-link class="cart" to="/cart" :class="{ on: route.path.startsWith('/cart') }">
        <ShoppingCart class="cart-icon" />
        <span>购物车</span>
      </router-link>
      <router-link to="/orders" :class="{ on: route.path.startsWith('/orders') }">我的订单</router-link>
      <router-link v-if="!name" to="/login" :class="{ on: route.path === '/login' }">登录</router-link>
      <button v-else class="text" @click="logout">退出</button>
    </nav>
  </header>
  <router-link v-if="route.path !== '/cart'" class="float-cart" to="/cart" aria-label="购物车">
    <ShoppingCart />
    <span v-if="count > 0" class="badge">{{ count > 99 ? '99+' : count }}</span>
  </router-link>
</template>
<script setup lang="ts">
import { useRoute, useRouter } from 'vue-router'
import { ShoppingCart } from '@element-plus/icons-vue'
import { CartApi, type CartItem } from '@/api/mall/client/cart'
import { AuthApi } from '@/api/mall/auth'
import { clearSession } from '@/hooks/web'
const route = useRoute()
const router = useRouter()
const name = ref(localStorage.getItem('userName') || '')
const count = ref(0)

async function refreshCount() {
  if (!localStorage.getItem('userToken')) {
    count.value = 0
    return
  }
  try {
    const data = await CartApi.getCartList() as { items?: CartItem[] }
    const list = data.items || []
    count.value = list.reduce((sum, item) => sum + Number(item.qty || 0), 0)
  } catch {
    count.value = 0
  }
}

const logout = async () => {
  const refreshToken = localStorage.getItem('userRefresh')
  try {
    if (refreshToken) await AuthApi.logout(refreshToken)
  } catch { /* 本地会话仍要清掉 */ }
  clearSession('user')
  name.value = ''
  count.value = 0
  router.push('/')
}

onMounted(() => {
  refreshCount()
  window.addEventListener('cart-changed', refreshCount)
})
onUnmounted(() => window.removeEventListener('cart-changed', refreshCount))
watch(() => route.path, refreshCount)
</script>
<style scoped>
.bar {
  position: sticky;
  top: 0;
  z-index: 20;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 72px;
  padding: 0 40px;
  background: rgba(246, 244, 241, 0.96);
  border-bottom: 1px solid #e7e5e4;
  backdrop-filter: blur(8px);
}
.brand { display: inline-flex; align-items: center; gap: 8px; font-weight: 600; letter-spacing: 0.18em; font-size: 18px; color: #1c1917; }
.mark { width: 30px; height: 22px; display: block; fill: none; stroke: currentColor; stroke-width: 3.2; stroke-linecap: round; stroke-linejoin: round; }
nav { display: flex; gap: 8px; align-items: center; }
nav a, .text {
  padding: 8px 14px;
  font-size: 14px;
  color: #44403c;
  background: none;
  border: 0;
  cursor: pointer;
}
nav a.on { color: #1c1917; box-shadow: inset 0 -2px 0 #1c1917; }
.cart { display: inline-flex; align-items: center; gap: 6px; }
.cart-icon { width: 18px; height: 18px; }
.float-cart {
  position: fixed;
  right: 24px;
  bottom: 24px;
  z-index: 30;
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: #1c1917;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 24px rgba(28, 25, 23, 0.28);
}
.float-cart svg { width: 26px; height: 26px; }
.badge {
  position: absolute;
  top: -2px;
  right: -2px;
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  border-radius: 9px;
  background: #fff;
  color: #1c1917;
  border: 1px solid #1c1917;
  font-size: 11px;
  line-height: 18px;
  text-align: center;
}
@media (max-width: 768px) {
  .bar { padding: 0 16px; height: 60px; }
  nav { gap: 0; }
  nav a, .text { padding: 8px 8px; font-size: 13px; }
}
</style>
