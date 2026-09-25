<template>
  <div class="shop">
    <SiteHeader />
    <main class="wrap">
      <nav class="crumb bar">
        <router-link to="/">首页</router-link>
        <span>/</span>
        <span>我的订单</span>
      </nav>
      <h1 class="sec-title title">我的订单</h1>

      <p v-if="!list.length" class="empty">
        还没有订单，<router-link class="link" to="/shop">去挑选商品</router-link>
      </p>

      <article v-for="order in list" :key="order.id" class="order">
        <header>
          <div>
            <span class="no">订单号 {{ order.id }}</span>
            <span class="date">{{ order.createTime }}</span>
          </div>
          <span class="status" :class="order.status">{{ order.statusText }}</span>
        </header>
        <div class="body">
          <div class="lines">
            <div v-for="(line, i) in order.lines" :key="i" class="line">
              <span class="name">{{ line.productName }}</span>
              <span class="spec">{{ line.color }} / {{ line.size }} × {{ line.qty }}</span>
              <span class="amount">¥{{ money((line.price || 0) * (line.qty || 0)) }}</span>
            </div>
          </div>
          <div class="side">
            <p class="addr">{{ order.receiverName }} {{ order.receiverPhone }}</p>
            <p class="addr">{{ areaText(order) }} {{ order.address }}</p>
            <div class="foot">
              <span class="total">合计 <strong>¥{{ money(order.totalAmount) }}</strong></span>
              <button v-if="order.status === 'PENDING'" class="btn" @click="pay(order.id)">确认付款</button>
            </div>
          </div>
        </div>
      </article>

      <Pagination
        :total="total"
        v-model:page="queryParams.pageNo"
        v-model:limit="queryParams.pageSize"
        @pagination="getList"
      />
    </main>
    <SiteFooter />
  </div>
</template>
<script setup lang="ts">
import SiteHeader from '@/components/SiteHeader.vue'
import SiteFooter from '@/components/SiteFooter.vue'
import Pagination from '@/components/Pagination/index.vue'
import { ClientOrderApi } from '@/api/mall/client/order'
import type { Order } from '@/api/mall/order'
import { ElMessage } from 'element-plus'

const list = ref<Order[]>([])
const total = ref(0)
const money = (n?: number) => Number(n || 0).toFixed(2)
const areaText = (order: Order) =>
  order.province ? `${order.province}${order.city || ''}${order.district || ''}` : order.region || ''
const queryParams = reactive({ pageNo: 1, pageSize: 10 })

const getList = async () => {
  const data = await ClientOrderApi.getOrderPage(queryParams)
  list.value = data.list
  total.value = data.total
}

const pay = async (id?: number) => {
  if (!id) return
  try {
    await ClientOrderApi.payOrder(id)
    await getList()
  } catch (e: any) {
    ElMessage.error(e.message)
  }
}

onMounted(getList)
</script>
<style scoped>
.bar { padding: 22px 0 0; }
.crumb { display: flex; gap: 8px; }
.title { text-align: left; margin: 22px 0 30px; letter-spacing: 0.14em; }
.link { text-decoration: underline; color: var(--ink); }
.order { border: 1px solid var(--line); margin-bottom: 22px; }
.order header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 16px 22px;
  border-bottom: 1px solid var(--line);
  background: var(--soft);
}
.no { font-size: 13px; letter-spacing: 0.08em; }
.date { color: var(--muted); font-size: 12px; margin-left: 14px; }
.status { font-size: 11px; letter-spacing: 0.18em; padding: 6px 12px; border: 1px solid var(--ink); }
.status.PENDING { background: var(--ink); color: #fff; }
.status.CANCELLED { border-color: var(--line); color: var(--muted); }
.body { display: grid; grid-template-columns: minmax(0, 1fr) 320px; gap: 24px; padding: 22px; }
.lines { display: grid; gap: 14px; }
.line { display: grid; grid-template-columns: minmax(0, 1fr) auto auto; gap: 14px; align-items: baseline; }
.name { font-size: 14px; }
.spec { color: var(--muted); font-size: 12px; }
.amount { font-size: 13px; }
.side { border-left: 1px solid var(--line); padding-left: 24px; }
.addr { font-size: 13px; color: #4a4640; margin: 0 0 6px; }
.foot { display: flex; align-items: center; justify-content: space-between; gap: 14px; margin-top: 16px; }
.total { font-size: 13px; color: var(--muted); }
.total strong { color: var(--ink); font-size: 17px; margin-left: 6px; }
.foot .btn { padding: 10px 22px; }
@media (max-width: 900px) {
  .body { grid-template-columns: 1fr; }
  .side { border-left: 0; padding-left: 0; border-top: 1px solid var(--line); padding-top: 16px; }
}
</style>
