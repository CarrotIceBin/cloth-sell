<template>
  <div>
    <ShopBar />
    <main class="page">
      <h1>服装</h1>
      <p v-if="!list.length" class="empty">还没有上架商品</p>
      <div class="grid">
        <button v-for="item in list" :key="item.id" class="tile" @click="open(item.id)">
          <div class="cover" :style="cover(item)"></div>
          <div class="meta">
            <span>{{ item.name }}</span>
            <strong>¥{{ money(item.minPrice) }}</strong>
          </div>
        </button>
      </div>
      <Pagination :total="total" v-model:page="queryParams.pageNo" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </main>
  </div>
</template>
<script setup lang="ts">
import ShopBar from '@/components/ShopBar.vue'
import Pagination from '@/components/Pagination/index.vue'
import { ClientProductApi } from '@/api/mall/client/product'
import type { Product } from '@/api/mall/product'
import { useRouter } from 'vue-router'

const router = useRouter()
const list = ref<Product[]>([])
const total = ref(0)
const queryParams = reactive({ pageNo: 1, pageSize: 10, sortBy: undefined as string | undefined, sortOrder: undefined as string | undefined })
const money = (n?: number) => Number(n || 0).toFixed(2)
const cover = (item: Product) => item.coverUrl ? { backgroundImage: `url(${item.coverUrl})` } : {}
const getList = async () => {
  const data = await ClientProductApi.getProductPage(queryParams)
  list.value = data.list
  total.value = data.total
}
const open = (id?: number) => router.push('/product/' + id)
onMounted(getList)
</script>
<style scoped>
.page { max-width: 1120px; margin: 0 auto; padding: 28px 20px 48px; }
h1 { font-weight: 500; font-size: 28px; margin: 0 0 20px; }
.empty { color: #78716c; }
.grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(240px, 1fr)); gap: 22px; }
.tile { padding: 0; border: 0; background: transparent; text-align: left; cursor: pointer; color: inherit; }
.cover { aspect-ratio: 3 / 4; background: #e7e5e4 center / cover no-repeat; }
.meta { display: flex; justify-content: space-between; gap: 12px; padding: 12px 2px 0; font-size: 14px; }
@media (max-width: 768px) {
  .grid { grid-template-columns: 1fr; }
}
</style>
