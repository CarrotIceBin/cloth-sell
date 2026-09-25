<template>
  <div class="shop">
    <SiteHeader />
    <main class="wrap">
      <nav class="crumb bar">
        <router-link to="/">首页</router-link>
        <span>/</span>
        <router-link to="/cart">购物车</router-link>
        <span>/</span>
        <span>结算</span>
      </nav>
      <h1 class="sec-title title">填写收货信息</h1>

      <div class="layout">
        <section class="form">
          <el-form :model="form" label-position="top" @submit.prevent="submit">
            <el-form-item label="收货人">
              <el-input v-model="form.receiverName" placeholder="请填写收货人姓名" />
            </el-form-item>
            <el-form-item label="手机">
              <el-input v-model="form.receiverPhone" maxlength="11" placeholder="11 位手机号" />
            </el-form-item>
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
            <el-form-item label="详细地址">
              <el-input v-model="form.address" type="textarea" :rows="3" placeholder="街道、门牌号等" />
            </el-form-item>
          </el-form>
        </section>

        <aside class="panel">
          <h2>订单摘要</h2>
          <ul class="lines">
            <li v-for="item in items" :key="item.id">
              <span class="thumb" :style="coverStyle(item.coverUrl)" />
              <div>
                <p class="name">{{ item.name }}</p>
                <p class="spec">{{ item.color }} / {{ item.size }} × {{ item.qty }}</p>
              </div>
              <span class="amount">¥{{ money(item.amount) }}</span>
            </li>
          </ul>
          <dl>
            <div><dt>运费</dt><dd>{{ Number(freight) > 0 ? '¥' + money(freight) : '免运费' }}</dd></div>
            <div class="total"><dt>应付</dt><dd>¥{{ payable }}</dd></div>
          </dl>
          <button class="btn full" :disabled="submitting" @click="submit">
            {{ submitting ? '提交中…' : '提交订单' }}
          </button>
          <p class="hint">提交后可在「我的订单」里确认付款。</p>
        </aside>
      </div>
    </main>
    <SiteFooter />
  </div>
</template>
<script setup lang="ts">
import SiteHeader from '@/components/SiteHeader.vue'
import SiteFooter from '@/components/SiteFooter.vue'
import { ClientOrderApi } from '@/api/mall/client/order'
import { CartApi, type CartItem } from '@/api/mall/client/cart'
import { coverStyle } from '@/utils/cover'
import { pcaTextArr } from 'element-china-area-data'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = useRouter()
const area = ref<string[]>([])
const items = ref<CartItem[]>([])
const payable = ref('0.00')
const freight = ref(0)
const submitting = ref(false)
const money = (n?: number) => Number(n || 0).toFixed(2)
const form = reactive({ receiverName: '', receiverPhone: '', province: '', city: '', district: '', address: '' })

function onArea(value?: string[]) {
  form.province = value?.[0] || ''
  form.city = value?.[1] || ''
  form.district = value?.[2] || ''
}

const submit = async () => {
  if (!form.receiverName.trim() || !form.receiverPhone.trim()) {
    ElMessage.error('请填写收货人和手机')
    return
  }
  if (!form.province || !form.city || !form.district) {
    ElMessage.error('请选择省市区')
    return
  }
  if (!form.address.trim()) {
    ElMessage.error('请填写详细地址')
    return
  }
  submitting.value = true
  try {
    await ClientOrderApi.createOrder(form)
    window.dispatchEvent(new Event('cart-changed'))
    router.push('/orders')
  } catch (e: any) {
    ElMessage.error(e.message)
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  const data = await CartApi.getCartList()
  items.value = data.items || []
  payable.value = money(data.payable)
  freight.value = Number(data.freight || 0)
})
</script>
<style scoped>
.bar { padding: 22px 0 0; }
.crumb { display: flex; gap: 8px; }
.title { text-align: left; margin: 22px 0 30px; letter-spacing: 0.14em; }
.layout { display: grid; grid-template-columns: minmax(0, 1fr) 360px; gap: 48px; align-items: start; padding-bottom: 40px; }
.form { max-width: 520px; }
.panel { border: 1px solid var(--line); padding: 26px; position: sticky; top: 150px; }
.panel h2 { font-size: 12px; letter-spacing: 0.24em; text-transform: uppercase; font-weight: 500; margin: 0 0 18px; }
.lines { list-style: none; margin: 0 0 20px; padding: 0 0 20px; border-bottom: 1px solid var(--line); display: grid; gap: 16px; }
.lines li { display: grid; grid-template-columns: 56px minmax(0, 1fr) auto; gap: 12px; align-items: center; }
.thumb { width: 56px; height: 70px; background: var(--soft) center / cover no-repeat; }
.name { font-size: 13px; margin: 0 0 4px; }
.spec { font-size: 12px; color: var(--muted); margin: 0; }
.amount { font-size: 13px; }
.panel dl { margin: 0 0 22px; display: grid; gap: 14px; }
.panel dl > div { display: flex; justify-content: space-between; font-size: 13px; color: #4a4640; }
.panel dt, .panel dd { margin: 0; }
.panel .total { border-top: 1px solid var(--line); padding-top: 14px; font-size: 16px; color: var(--ink); }
.full { width: 100%; }
.full:disabled { opacity: 0.5; cursor: not-allowed; }
.hint { font-size: 12px; color: var(--muted); margin: 14px 0 0; line-height: 1.6; }
@media (max-width: 900px) {
  .layout { grid-template-columns: 1fr; gap: 28px; }
  .panel { position: static; }
}
</style>
