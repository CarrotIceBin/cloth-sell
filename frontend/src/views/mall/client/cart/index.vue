<template>
  <div>
    <ShopBar />
    <main class="page">
      <h1>购物车</h1>
      <p v-if="!list.length" class="empty">购物车是空的</p>
      <article v-for="item in list" :key="item.id" class="card">
        <div class="cover" :style="item.coverUrl ? { backgroundImage: `url(${item.coverUrl})` } : {}"></div>
        <div class="body">
          <div class="top">
            <div>
              <div class="name">{{ item.name }}</div>
              <div class="spec">{{ item.color }} / {{ item.size }}</div>
            </div>
            <div class="line-price">¥{{ lineTotal(item) }}</div>
          </div>
          <div class="actions">
            <div class="stepper">
              <button type="button" @click="change(item, (item.qty || 1) - 1)">−</button>
              <span>{{ item.qty }}</span>
              <button type="button" @click="change(item, (item.qty || 1) + 1)">+</button>
            </div>
            <span class="unit">单价 ¥{{ Number(item.price).toFixed(2) }}</span>
            <button type="button" class="remove" @click="remove(item.id)">删除</button>
          </div>
        </div>
      </article>
      <div v-if="list.length" class="checkout">
        <div>
          <div class="muted">共 {{ count }} 件</div>
          <div class="sum">合计 ¥{{ total.toFixed(2) }}</div>
        </div>
        <el-button type="primary" @click="router.push('/checkout')">去下单</el-button>
      </div>
    </main>
  </div>
</template>
<script setup lang="ts">
import ShopBar from '@/components/ShopBar.vue'
import { CartApi, type CartItem } from '@/api/mall/client/cart'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
const router = useRouter()
const list = ref<CartItem[]>([])
const total = computed(() => list.value.reduce((s, i) => s + Number(i.price || 0) * Number(i.qty || 0), 0))
const count = computed(() => list.value.reduce((s, i) => s + Number(i.qty || 0), 0))
const lineTotal = (item: CartItem) => (Number(item.price || 0) * Number(item.qty || 0)).toFixed(2)
const load = async () => { list.value = await CartApi.getCartList() }
const change = async (item: CartItem, qty: number) => {
  try {
    if (qty <= 0) await CartApi.deleteCart(item.id!)
    else await CartApi.updateCart({ id: item.id!, qty })
    await load()
    window.dispatchEvent(new Event('cart-changed'))
  } catch (e: any) { ElMessage.error(e.message) }
}
const remove = (id?: number) => id && change({ id, qty: 1 }, 0)
onMounted(load)
</script>
<style scoped>
.page { max-width: 880px; margin: 0 auto; padding: 28px 20px 120px; }
h1 { font-weight: 500; font-size: 28px; margin-top: 8px; }
.empty { color: #78716c; }
.card {
  display: flex;
  gap: 16px;
  background: #fff;
  padding: 16px;
  margin-bottom: 12px;
}
.cover { width: 96px; height: 120px; flex-shrink: 0; background: #e7e5e4 center / cover no-repeat; }
.body { flex: 1; min-width: 0; display: flex; flex-direction: column; justify-content: space-between; }
.top { display: flex; justify-content: space-between; gap: 12px; }
.name { font-size: 16px; }
.spec { color: #78716c; font-size: 13px; margin-top: 4px; }
.line-price { font-weight: 600; white-space: nowrap; }
.actions { display: flex; align-items: center; gap: 16px; margin-top: 12px; }
.stepper { display: flex; align-items: center; border: 1px solid #d6d3d1; }
.stepper button, .stepper span {
  width: 36px;
  height: 32px;
  display: grid;
  place-items: center;
  background: #fff;
  border: 0;
  cursor: pointer;
}
.stepper button + span, .stepper span + button { border-left: 1px solid #d6d3d1; }
.unit { color: #78716c; font-size: 13px; }
.remove {
  margin-left: auto;
  border: 0;
  background: none;
  color: #57534e;
  cursor: pointer;
  white-space: nowrap;
  padding: 0;
}
.checkout {
  position: sticky;
  bottom: 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  background: #fff;
  padding: 16px 18px;
  box-shadow: 0 8px 24px rgba(28, 25, 23, 0.08);
}
.muted { color: #78716c; font-size: 13px; }
.sum { font-size: 18px; font-weight: 600; }
@media (max-width: 768px) {
  .unit { display: none; }
}
</style>
