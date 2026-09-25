<template>
  <div class="shop">
    <SiteHeader />
    <main class="wrap">
      <nav class="crumb bar">
        <router-link to="/">首页</router-link>
        <span>/</span>
        <span>购物车</span>
      </nav>
      <h1 class="sec-title title">购物车</h1>

      <p v-if="!list.length" class="empty">
        购物车是空的，<router-link class="link" to="/shop">去挑选商品</router-link>
      </p>

      <div v-else class="layout">
        <section class="items">
          <div class="head-row">
            <span>商品</span>
            <span>单价</span>
            <span>数量</span>
            <span>小计</span>
          </div>
          <article v-for="item in list" :key="item.id" class="row">
            <div class="info">
              <router-link class="thumb" :style="coverStyle(item.coverUrl)" :to="'/product/' + item.productId" />
              <div class="text">
                <router-link class="name" :to="'/product/' + item.productId">{{ item.name }}</router-link>
                <span class="spec">{{ item.color }} / {{ item.size }}</span>
                <button class="remove" @click="remove(item.id)">删除</button>
              </div>
            </div>
            <div class="cell unit">¥{{ money(item.price) }}</div>
            <div class="cell">
              <div class="stepper">
                <button type="button" @click="change(item, (item.qty || 1) - 1)">−</button>
                <span>{{ item.qty }}</span>
                <button type="button" :disabled="(item.qty || 0) >= (item.stock || 0)" @click="change(item, (item.qty || 1) + 1)">+</button>
              </div>
            </div>
            <div class="cell sum">¥{{ money(item.amount) }}</div>
          </article>

          <router-link class="btn btn-ghost continue" to="/shop">继续购物</router-link>
        </section>

        <aside class="panel">
          <h2>订单摘要</h2>
          <dl>
            <div><dt>商品件数</dt><dd>{{ count }} 件</dd></div>
            <div><dt>运费</dt><dd>{{ Number(freight) > 0 ? '¥' + money(freight) : '免运费' }}</dd></div>
            <div class="total"><dt>合计</dt><dd>¥{{ payable }}</dd></div>
          </dl>
          <button class="btn full" @click="router.push('/checkout')">去结算</button>
          <p class="hint">满 ¥600 免运费，支持 7 天无理由退换。</p>
        </aside>
      </div>
    </main>
    <SiteFooter />
  </div>
</template>
<script setup lang="ts">
import SiteHeader from '@/components/SiteHeader.vue'
import SiteFooter from '@/components/SiteFooter.vue'
import { CartApi, type CartItem } from '@/api/mall/client/cart'
import { coverStyle } from '@/utils/cover'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = useRouter()
const list = ref<CartItem[]>([])
const payable = ref('0.00')
const freight = ref(0)
const money = (n?: number) => Number(n || 0).toFixed(2)
const count = computed(() => list.value.reduce((sum, item) => sum + Number(item.qty || 0), 0))

const load = async () => {
  const data = await CartApi.getCartList()
  list.value = data.items || []
  payable.value = money(data.payable)
  freight.value = Number(data.freight || 0)
}

const change = async (item: CartItem, qty: number) => {
  try {
    if (qty <= 0) await CartApi.deleteCart(item.id!)
    else await CartApi.updateCart({ id: item.id!, qty })
    await load()
    window.dispatchEvent(new Event('cart-changed'))
  } catch (e: any) {
    ElMessage.error(e.message)
  }
}

const remove = (id?: number) => id && change({ id, qty: 1 }, 0)

onMounted(load)
</script>
<style scoped>
.bar { padding: 22px 0 0; }
.crumb { display: flex; gap: 8px; }
.title { text-align: left; margin: 22px 0 30px; letter-spacing: 0.14em; }
.link { text-decoration: underline; color: var(--ink); }
.layout { display: grid; grid-template-columns: minmax(0, 1fr) 340px; gap: 40px; align-items: start; }
.head-row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 90px 130px 100px;
  gap: 12px;
  padding: 0 0 12px;
  border-bottom: 1px solid var(--line);
  font-size: 11px;
  letter-spacing: 0.2em;
  color: var(--muted);
  text-transform: uppercase;
}
.head-row span:not(:first-child) { text-align: center; }
.row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 90px 130px 100px;
  gap: 12px;
  align-items: center;
  padding: 20px 0;
  border-bottom: 1px solid var(--line);
}
.info { display: flex; gap: 16px; min-width: 0; }
.thumb { width: 92px; height: 116px; flex-shrink: 0; background: var(--soft) center / cover no-repeat; }
.text { display: grid; align-content: center; gap: 8px; min-width: 0; }
.name { font-size: 15px; }
.name:hover { color: var(--gold); }
.spec { color: var(--muted); font-size: 12px; letter-spacing: 0.08em; }
.remove {
  justify-self: start;
  border: 0;
  background: none;
  padding: 0;
  font-size: 12px;
  color: var(--muted);
  text-decoration: underline;
  cursor: pointer;
}
.remove:hover { color: var(--ink); }
.cell { text-align: center; font-size: 14px; }
.unit { color: #4a4640; }
.sum { font-weight: 600; }
.stepper { display: inline-flex; align-items: center; border: 1px solid var(--line); }
.stepper button, .stepper span { width: 36px; height: 34px; display: grid; place-items: center; background: #fff; border: 0; cursor: pointer; }
.stepper button:disabled { opacity: 0.35; cursor: not-allowed; }
.stepper button + span, .stepper span + button { border-left: 1px solid var(--line); }
.continue { margin-top: 26px; }
.panel { border: 1px solid var(--line); padding: 26px; position: sticky; top: 150px; }
.panel h2 { font-size: 12px; letter-spacing: 0.24em; text-transform: uppercase; font-weight: 500; margin: 0 0 20px; }
.panel dl { margin: 0 0 22px; display: grid; gap: 14px; }
.panel dl > div { display: flex; justify-content: space-between; font-size: 13px; color: #4a4640; }
.panel dt, .panel dd { margin: 0; }
.panel .total { border-top: 1px solid var(--line); padding-top: 14px; font-size: 16px; color: var(--ink); }
.full { width: 100%; }
.hint { font-size: 12px; color: var(--muted); margin: 14px 0 0; line-height: 1.6; }
@media (max-width: 900px) {
  .layout { grid-template-columns: 1fr; }
  .head-row { display: none; }
  .row { grid-template-columns: 1fr; gap: 16px; }
  .cell { text-align: left; }
  .panel { position: static; }
}
</style>
