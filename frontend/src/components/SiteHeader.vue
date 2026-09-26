<template>
  <div class="topbar">
    <div class="wrap">全场新品 8 折 · 满 ¥600 免运费</div>
  </div>
  <header class="head">
    <div class="wrap row">
      <nav class="quick">
        <router-link to="/shop">门店</router-link>
        <router-link to="/journal">期刊</router-link>
        <router-link to="/help#help-contact">联系</router-link>
      </nav>
      <router-link class="brand" to="/">
        <svg class="mark" viewBox="0 0 64 48" aria-hidden="true">
          <path d="M36.5 8.5a4.2 4.2 0 0 1 4.3 4.1c0 2.3-1.7 3.5-3.6 4.3L32 20.2" />
          <path d="M8 38.5 32 20.2 56 38.5" />
          <path d="M14 38.5h36" />
        </svg>
        <span>棉里</span>
        <small>MIANLI</small>
      </router-link>
      <div class="tools">
        <button class="icon" aria-label="搜索" @click="toggleSearch"><Search /></button>
        <router-link class="icon" to="/orders" aria-label="我的订单"><User /></router-link>
        <router-link class="icon" to="/cart" aria-label="购物车">
          <ShoppingCart />
          <span v-if="count > 0" class="badge">{{ count > 99 ? '99+' : count }}</span>
        </router-link>
      </div>
    </div>
    <div v-if="searchOpen" class="searchbar">
      <div class="wrap">
        <input
          ref="searchInput"
          v-model="keyword"
          type="search"
          placeholder="搜索商品，回车确认"
          @keyup.enter="search"
        />
        <button class="icon" aria-label="关闭搜索" @click="searchOpen = false"><Close /></button>
      </div>
    </div>
    <nav class="menu">
      <div class="wrap menuro">
        <router-link to="/" :class="{ on: route.path === '/' }">首页</router-link>
        <router-link
          to="/shop"
          :class="{ on: route.path.startsWith('/shop') || route.path.startsWith('/product') }"
        >商店</router-link>
        <router-link to="/cart" :class="{ on: route.path.startsWith('/cart') }">购物车</router-link>
        <router-link to="/orders" :class="{ on: route.path.startsWith('/orders') }">我的订单</router-link>
        <router-link v-if="!name" to="/login" :class="{ on: route.path === '/login' }">登录</router-link>
        <button v-else class="out" @click="logout">退出</button>
      </div>
    </nav>
  </header>
</template>
<script setup lang="ts">
import { useRoute, useRouter } from 'vue-router'
import { Close, Search, ShoppingCart, User } from '@element-plus/icons-vue'
import { CartApi, type CartItem } from '@/api/mall/client/cart'
import { AuthApi } from '@/api/mall/auth'
import { clearSession } from '@/hooks/web'

const route = useRoute()
const router = useRouter()
const name = ref(localStorage.getItem('userName') || '')
const count = ref(0)
const searchOpen = ref(false)
const keyword = ref('')
const searchInput = ref<HTMLInputElement>()

async function refreshCount() {
  if (!localStorage.getItem('userToken')) {
    count.value = 0
    return
  }
  try {
    const data = await CartApi.getCartList()
    const list = data.items || []
    count.value = list.reduce((sum, item: CartItem) => sum + Number(item.qty || 0), 0)
  } catch {
    count.value = 0
  }
}

const toggleSearch = async () => {
  searchOpen.value = !searchOpen.value
  if (searchOpen.value) {
    await nextTick()
    searchInput.value?.focus()
  }
}

const search = () => {
  const q = keyword.value.trim()
  router.push({ path: '/shop', query: q ? { q } : {} })
  searchOpen.value = false
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
.topbar {
  background: #141414;
  color: #fff;
  text-align: center;
  font-size: 11px;
  letter-spacing: 0.24em;
  padding: 10px 0;
}
.head { border-bottom: 1px solid var(--line); background: #fff; position: sticky; top: 0; z-index: 20; }
.row { display: grid; grid-template-columns: 1fr auto 1fr; align-items: center; gap: 16px; padding: 22px 0; }
.quick { display: flex; gap: 20px; font-size: 11px; letter-spacing: 0.2em; color: var(--muted); }
.quick a:hover { color: var(--ink); }
.brand { display: inline-flex; align-items: center; gap: 10px; font-family: var(--serif); font-size: 26px; letter-spacing: 0.28em; }
.brand small { font-family: "Segoe UI", sans-serif; font-size: 10px; letter-spacing: 0.4em; color: var(--muted); }
.mark { width: 32px; height: 24px; fill: none; stroke: currentColor; stroke-width: 3; stroke-linecap: round; stroke-linejoin: round; }
.tools { display: flex; justify-content: flex-end; align-items: center; gap: 16px; }
.icon {
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 22px;
  height: 22px;
  padding: 0;
  border: 0;
  background: none;
  color: var(--ink);
  cursor: pointer;
}
.icon svg { width: 19px; height: 19px; }
.badge {
  position: absolute;
  top: -6px;
  right: -8px;
  min-width: 16px;
  height: 16px;
  padding: 0 4px;
  border-radius: 8px;
  background: var(--ink);
  color: #fff;
  font-size: 10px;
  line-height: 16px;
  text-align: center;
}
.searchbar { border-top: 1px solid var(--line); padding: 14px 0; }
.searchbar .wrap { display: flex; align-items: center; gap: 10px; }
.searchbar input {
  flex: 1;
  border: 0;
  border-bottom: 1px solid var(--line);
  padding: 8px 2px;
  font-size: 14px;
  outline: none;
}
.menu { border-top: 1px solid var(--line); }
.menuro { display: flex; justify-content: center; align-items: center; gap: 38px; height: 52px; }
.menuro a, .out {
  font-size: 11px;
  letter-spacing: 0.24em;
  text-transform: uppercase;
  color: #3f3b37;
  border: 0;
  background: none;
  cursor: pointer;
  padding: 6px 0;
}
.menuro a:hover { color: var(--ink); }
.menuro a.on { color: var(--ink); box-shadow: inset 0 -1px 0 var(--ink); }
@media (max-width: 900px) {
  .row { grid-template-columns: auto 1fr auto; }
  .quick { display: none; }
  .menuro { gap: 20px; height: auto; flex-wrap: wrap; padding: 12px 0; }
}
</style>
