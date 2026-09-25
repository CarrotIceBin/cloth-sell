<template>
  <div>
    <ShopBar />
    <main class="page" v-if="product">
      <router-link class="back" to="/">返回</router-link>
      <div class="detail">
        <div class="cover" :style="coverStyle(product.coverUrl)"></div>
        <div class="buy">
          <p class="cat">{{ product.category }}</p>
          <h1>{{ product.name }}</h1>
          <p class="price">¥{{ Number(current?.price || product.minPrice || 0).toFixed(2) }}</p>
          <p class="stock">库存 {{ current?.stock ?? product.stock }}</p>
          <p class="label">颜色</p>
          <div class="chips">
            <button v-for="c in colors" :key="c" :class="{ on: color === c }" @click="color = c">{{ c }}</button>
          </div>
          <p class="label">尺码</p>
          <div class="chips">
            <button v-for="s in sizes" :key="s" :class="{ on: size === s }" :disabled="!ok(s)" @click="size = s">{{ s }}</button>
          </div>
          <el-button type="primary" class="add" @click="add">加入购物车</el-button>
        </div>
      </div>
    </main>
  </div>
</template>
<script setup lang="ts">
import ShopBar from '@/components/ShopBar.vue'
import { ClientProductApi } from '@/api/mall/client/product'
import { CartApi } from '@/api/mall/client/cart'
import type { Product, Sku } from '@/api/mall/product'
import { coverStyle } from '@/utils/cover'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const product = ref<Product>()
const color = ref('')
const size = ref('')
const colors = computed(() => [...new Set((product.value?.skus || []).map((s) => s.color || ''))])
const sizes = computed(() => [...new Set((product.value?.skus || []).filter((s) => s.color === color.value).map((s) => s.size || ''))])
const current = computed(() => (product.value?.skus || []).find((s) => s.color === color.value && s.size === size.value))
const ok = (s: string) => (product.value?.skus || []).some((sku: Sku) => sku.color === color.value && sku.size === s && (sku.stock || 0) > 0)
const add = async () => {
  if (!localStorage.getItem('userToken')) { router.push('/login'); return }
  if (!current.value?.id) return
  try {
    await CartApi.createCart({ skuId: current.value.id, qty: 1 })
    window.dispatchEvent(new Event('cart-changed'))
    ElMessage.success('已加入购物车')
  } catch (e: any) { ElMessage.error(e.message) }
}
onMounted(async () => {
  product.value = await ClientProductApi.getProduct(Number(route.params.id))
  color.value = colors.value[0] || ''
  size.value = sizes.value.find(ok) || sizes.value[0] || ''
})
</script>
<style scoped>
.page { max-width: 1120px; margin: 0 auto; padding: 24px 20px 48px; }
.back { font-size: 14px; color: #57534e; }
.detail { display: grid; grid-template-columns: minmax(280px, 1fr) minmax(280px, 1fr); gap: 40px; margin-top: 16px; }
.cover { aspect-ratio: 3 / 4; background: #e7e5e4 center / cover no-repeat; }
.cat { letter-spacing: 0.12em; color: #78716c; margin: 0; }
h1 { font-weight: 500; font-size: 32px; margin: 8px 0; }
.price { font-size: 20px; margin: 0; }
.stock, .label { color: #57534e; font-size: 14px; }
.chips { display: flex; flex-wrap: wrap; gap: 8px; }
.chips button { border: 1px solid #d6d3d1; background: #fff; padding: 8px 14px; cursor: pointer; }
.chips button.on { background: #1c1917; color: #fff; border-color: #1c1917; }
.chips button:disabled { opacity: 0.35; cursor: not-allowed; }
.add { margin-top: 16px; min-width: 160px; }
@media (max-width: 768px) {
  .detail { grid-template-columns: 1fr; }
}
</style>
