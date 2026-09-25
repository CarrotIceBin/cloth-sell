<template>
  <div class="shell" :class="{ narrow }">
    <aside v-if="!narrow">
      <div class="brand">CLOTH</div>
      <router-link to="/admin/product" :class="{ on: route.path.startsWith('/admin/product') }">商品</router-link>
      <router-link to="/admin/order" :class="{ on: route.path.startsWith('/admin/order') }">订单</router-link>
      <button class="exit" @click="logout">退出</button>
    </aside>
    <div class="main">
      <header v-if="narrow" class="top">
        <router-link to="/admin/product" :class="{ on: route.path.startsWith('/admin/product') }">商品</router-link>
        <router-link to="/admin/order" :class="{ on: route.path.startsWith('/admin/order') }">订单</router-link>
        <button @click="logout">退出</button>
      </header>
      <router-view />
    </div>
  </div>
</template>
<script setup lang="ts">
import { clearSession } from '@/hooks/web'
import { AuthApi } from '@/api/mall/auth'
import { useRoute, useRouter } from 'vue-router'
const route = useRoute()
const router = useRouter()
const width = ref(window.innerWidth)
const narrow = computed(() => width.value < 768)
const onResize = () => { width.value = window.innerWidth }
const logout = async () => {
  const refreshToken = localStorage.getItem('adminRefresh')
  try {
    if (refreshToken) await AuthApi.logout(refreshToken)
  } catch { /* 本地会话仍要清掉 */ }
  clearSession('admin')
  router.push('/admin/login')
}
onMounted(() => window.addEventListener('resize', onResize))
onUnmounted(() => window.removeEventListener('resize', onResize))
</script>
<style scoped>
.shell { min-height: 100vh; display: flex; align-items: stretch; }
aside {
  position: sticky;
  top: 0;
  width: 220px;
  height: 100vh;
  flex-shrink: 0;
  background: #fff;
  border-right: 1px solid #e7e5e4;
  display: flex;
  flex-direction: column;
  padding: 28px 16px 20px;
  box-sizing: border-box;
}
.brand { letter-spacing: 0.2em; font-weight: 700; margin: 0 12px 28px; font-size: 18px; }
aside a, .top a {
  display: block;
  padding: 12px 14px;
  color: #44403c;
  border-radius: 8px;
}
aside a.on { background: #1c1917; color: #fff; }
.exit, .top button {
  margin-top: auto;
  background: none;
  border: 0;
  text-align: left;
  padding: 12px 14px;
  cursor: pointer;
  color: #57534e;
}
.main { flex: 1; min-width: 0; padding: 20px; }
.top {
  position: sticky;
  top: 0;
  z-index: 20;
  display: flex;
  gap: 8px;
  align-items: center;
  margin: -20px -20px 16px;
  padding: 12px 16px;
  background: #fff;
  border-bottom: 1px solid #e7e5e4;
}
.top a.on { background: #1c1917; color: #fff; }
.top button { margin-top: 0; margin-left: auto; }
.narrow { display: block; }
</style>
