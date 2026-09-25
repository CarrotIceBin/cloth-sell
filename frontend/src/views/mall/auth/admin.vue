<template>
  <main class="page">
    <div class="card">
      <h1>管理登录</h1>
      <el-form label-position="top">
        <el-form-item label="账号"><el-input v-model="username" /></el-form-item>
        <el-form-item label="密码"><el-input v-model="password" type="password" /></el-form-item>
        <el-button type="primary" @click="submit">登录</el-button>
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
const username = ref('admin')
const password = ref('')
const submit = async () => {
  try {
    const data = await AuthApi.adminLogin({ username: username.value, password: password.value })
    setSession('admin', data.accessToken, data.name, data.permissions, data.refreshToken)
    router.push('/admin/product')
  } catch (e: any) { ElMessage.error(e.message) }
}
</script>
<style scoped>
.page { min-height: 100vh; display: grid; place-items: center; }
.card { width: min(400px, calc(100% - 32px)); background: #fff; padding: 28px; }
h1 { font-weight: 500; margin-top: 0; }
</style>
