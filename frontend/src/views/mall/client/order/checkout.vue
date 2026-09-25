<template>
  <div>
    <ShopBar />
    <main class="page">
      <div class="card">
        <h1>填写收货信息</h1>
        <el-form :model="form" label-position="top" @submit.prevent="submit">
          <el-form-item label="收货人"><el-input v-model="form.receiverName" /></el-form-item>
          <el-form-item label="手机"><el-input v-model="form.receiverPhone" maxlength="11" /></el-form-item>
          <el-form-item label="省市区">
            <el-cascader
              v-model="area"
              :options="pcaTextArr"
              clearable
              placeholder="请选择省市区"
              style="width: 100%"
              @change="onArea"
            />
          </el-form-item>
          <el-form-item label="详细地址"><el-input v-model="form.address" /></el-form-item>
          <el-button type="primary" @click="submit">提交订单</el-button>
        </el-form>
      </div>
    </main>
  </div>
</template>
<script setup lang="ts">
import ShopBar from '@/components/ShopBar.vue'
import { ClientOrderApi } from '@/api/mall/client/order'
import { pcaTextArr } from 'element-china-area-data'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
const router = useRouter()
const area = ref<string[]>([])
const form = reactive({ receiverName: '', receiverPhone: '', province: '', city: '', district: '', address: '' })
function onArea(value?: string[]) {
  form.province = value?.[0] || ''
  form.city = value?.[1] || ''
  form.district = value?.[2] || ''
}
const submit = async () => {
  if (!form.province || !form.city || !form.district) {
    ElMessage.error('请选择省市区')
    return
  }
  try {
    await ClientOrderApi.createOrder(form)
    router.push('/orders')
  } catch (e: any) { ElMessage.error(e.message) }
}
</script>
<style scoped>
.page { max-width: 560px; margin: 0 auto; padding: 28px 20px 48px; }
.card { background: #fff; padding: 24px; }
h1 { font-weight: 500; font-size: 28px; margin-top: 0; }
</style>
