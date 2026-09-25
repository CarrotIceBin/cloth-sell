<template>
  <main class="page">
    <div class="card">
      <h1>{{ register ? '注册' : '登录' }}</h1>
      <el-form label-position="top">
        <el-form-item label="手机"><el-input v-model="phone" maxlength="11" /></el-form-item>
        <el-form-item label="密码"><el-input v-model="password" type="password" /></el-form-item>
        <el-button type="primary" @click="submit">{{ register ? '注册并登录' : '登录' }}</el-button>
        <el-button @click="register = !register">{{ register ? '去登录' : '去注册' }}</el-button>
      </el-form>
    </div>
  </main>
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
    router.push('/')
  } catch (e: any) { ElMessage.error(e.message) }
}
</script>
<style scoped>
.page { min-height: 100vh; display: grid; place-items: center; }
.card { width: min(400px, calc(100% - 32px)); background: #fff; padding: 28px; }
h1 { font-weight: 500; margin-top: 0; }
</style>
