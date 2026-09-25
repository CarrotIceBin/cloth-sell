<template>
  <div class="shop auth">
    <div class="card">
      <router-link class="brand" to="/">
        <svg class="mark" viewBox="0 0 64 48" aria-hidden="true">
          <path d="M36.5 8.5a4.2 4.2 0 0 1 4.3 4.1c0 2.3-1.7 3.5-3.6 4.3L32 20.2" />
          <path d="M8 38.5 32 20.2 56 38.5" />
          <path d="M14 38.5h36" />
        </svg>
        <span>棉里</span>
      </router-link>
      <h1 class="serif">{{ register ? '注册账号' : '欢迎回来' }}</h1>
      <p class="sub">{{ register ? '注册后即可加入购物车并下单' : '登录后继续你的购物车与订单' }}</p>

      <el-form label-position="top">
        <el-form-item label="手机">
          <el-input v-model="phone" maxlength="11" placeholder="11 位手机号" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="password" type="password" placeholder="至少 6 位" @keyup.enter="submit" />
        </el-form-item>
      </el-form>

      <button class="btn full" @click="submit">{{ register ? '注册并登录' : '登录' }}</button>
      <button class="switch" @click="register = !register">
        {{ register ? '已有账号？去登录' : '还没有账号？去注册' }}
      </button>
      <router-link class="back" to="/">← 返回商店</router-link>
    </div>
  </div>
</template>
<script setup lang="ts">
import { AuthApi } from '@/api/mall/auth'
import { setSession } from '@/hooks/web'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = useRouter()
const phone = ref('')
const password = ref('')
const register = ref(false)

const submit = async () => {
  try {
    const data = register.value
      ? await AuthApi.register({ phone: phone.value, password: password.value })
      : await AuthApi.login({ phone: phone.value, password: password.value })
    setSession('user', data.accessToken, data.name, data.permissions, data.refreshToken)
    window.dispatchEvent(new Event('cart-changed'))
    router.push('/')
  } catch (e: any) {
    ElMessage.error(e.message)
  }
}
</script>
<style scoped>
.auth {
  display: grid;
  place-items: center;
  background: var(--soft);
  padding: 60px 20px;
}
.card { width: min(420px, 100%); background: #fff; padding: 44px 40px; border: 1px solid var(--line); }
.brand { display: inline-flex; align-items: center; gap: 10px; font-family: var(--serif); font-size: 22px; letter-spacing: 0.3em; }
.mark { width: 28px; height: 22px; fill: none; stroke: currentColor; stroke-width: 3; stroke-linecap: round; stroke-linejoin: round; }
h1 { font-size: 26px; font-weight: 400; margin: 26px 0 8px; letter-spacing: 0.08em; }
.sub { color: var(--muted); font-size: 13px; margin: 0 0 26px; }
.full { width: 100%; }
.switch {
  width: 100%;
  border: 0;
  background: none;
  padding: 16px 0 0;
  font-size: 12px;
  color: var(--muted);
  cursor: pointer;
}
.switch:hover { color: var(--ink); }
.back { display: block; text-align: center; margin-top: 22px; font-size: 12px; letter-spacing: 0.12em; color: var(--muted); }
.back:hover { color: var(--ink); }
</style>
