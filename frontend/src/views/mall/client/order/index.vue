<template>
  <div>
    <ShopBar />
    <main class="page">
      <h1>我的订单</h1>
      <p v-if="!list.length" class="empty">还没有订单</p>
      <article v-for="order in list" :key="order.id" class="card">
        <div class="head">
          <span>订单 {{ order.id }}</span>
          <span>{{ order.statusText }}</span>
        </div>
        <p class="addr">{{ order.receiverName }} {{ order.receiverPhone }} · {{ areaText(order) }} {{ order.address }}</p>
        <div v-for="(line, i) in order.lines" :key="i" class="line">
          {{ line.productName }} {{ line.color }} / {{ line.size }} × {{ line.qty }}
        </div>
        <div class="foot">
          <strong>¥{{ Number(order.totalAmount).toFixed(2) }}</strong>
          <el-button v-if="order.status === 'PENDING'" type="primary" @click="pay(order.id)">确认付款</el-button>
        </div>
      </article>
      <Pagination :total="total" v-model:page="queryParams.pageNo" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </main>
  </div>
</template>
<script setup lang="ts">
import ShopBar from '@/components/ShopBar.vue'
import { ClientOrderApi } from '@/api/mall/client/order'
import type { Order } from '@/api/mall/order'
import Pagination from '@/components/Pagination/index.vue'
import { ElMessage } from 'element-plus'
const list = ref<Order[]>([])
const total = ref(0)
const areaText = (order: Order) => order.province ? `${order.province}${order.city || ''}${order.district || ''}` : (order.region || '')
const queryParams = reactive({ pageNo: 1, pageSize: 10 })
const getList = async () => {
  const data = await ClientOrderApi.getOrderPage(queryParams)
  list.value = data.list
  total.value = data.total
}
const pay = async (id?: number) => {
  if (!id) return
  try { await ClientOrderApi.payOrder(id); await getList() } catch (e: any) { ElMessage.error(e.message) }
}
onMounted(getList)
</script>
<style scoped>
.page { max-width: 860px; margin: 0 auto; padding: 28px 20px 48px; }
h1 { font-weight: 500; font-size: 28px; }
.empty { color: #78716c; }
.card { background: #fff; padding: 18px; margin-bottom: 12px; }
.head, .foot { display: flex; justify-content: space-between; align-items: center; }
.addr, .line { color: #57534e; font-size: 14px; }
.foot { margin-top: 12px; }
</style>
