<template>
  <div class="shop">
    <SiteHeader />
    <main class="wrap">
      <nav class="crumb bar">
        <router-link to="/">首页</router-link>
        <span>/</span>
        <span>商店</span>
      </nav>
      <p v-if="keyword" class="searching">
        搜索「{{ keyword }}」共 {{ total }} 件
        <button @click="clearSearch">清除</button>
      </p>
      <div class="filters">
        <div class="cats">
          <button
            v-for="cat in categories"
            :key="cat.label"
            :class="{ on: name === cat.q }"
            @click="pick(cat.q)"
          >{{ cat.label }}</button>
        </div>
        <label class="sort">
          <span>排序</span>
          <select v-model="sortKey" @change="switchSort">
            <option value="default">默认排序</option>
            <option value="new">最新上架</option>
            <option value="name">名称</option>
          </select>
        </label>
      </div>

      <p v-if="!list.length" class="empty">还没有上架商品</p>
      <div class="p-grid">
        <ProductCard v-for="item in list" :key="item.id" :product="item" />
      </div>
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
import ProductCard from '@/components/ProductCard.vue'
import Pagination from '@/components/Pagination/index.vue'
import { ClientProductApi } from '@/api/mall/client/product'
import type { Product } from '@/api/mall/product'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const list = ref<Product[]>([])
const total = ref(0)
const name = ref('')
const keyword = ref('')
const sortKey = ref('default')
const queryParams = reactive({
  pageNo: 1,
  pageSize: 12,
  name: undefined as string | undefined,
  sortBy: undefined as string | undefined,
  sortOrder: undefined as string | undefined
})

const categories = [
  { label: '全部', q: '' },
  { label: '衬衫', q: '衬衫' },
  { label: '裙装', q: '裙' },
  { label: '裤装', q: '裤' },
  { label: '针织', q: '针织' },
  { label: '外套', q: '外套' }
]

const getList = async () => {
  const data = await ClientProductApi.getProductPage(queryParams)
  list.value = data.list
  total.value = data.total
}

const pick = (q: string) => {
  name.value = q
  queryParams.name = q || undefined
  queryParams.pageNo = 1
  keyword.value = ''
  if (route.query.q) router.replace({ path: '/shop' })
  getList()
}

const clearSearch = () => {
  keyword.value = ''
  queryParams.name = undefined
  queryParams.pageNo = 1
  router.replace({ path: '/shop' })
  getList()
}

const switchSort = () => {
  if (sortKey.value === 'new') {
    queryParams.sortBy = 'createTime'
    queryParams.sortOrder = 'desc'
  } else if (sortKey.value === 'name') {
    queryParams.sortBy = 'name'
    queryParams.sortOrder = 'asc'
  } else {
    queryParams.sortBy = undefined
    queryParams.sortOrder = undefined
  }
  queryParams.pageNo = 1
  getList()
}

watch(
  () => route.query.q,
  (q) => {
    keyword.value = typeof q === 'string' ? q : ''
    queryParams.name = keyword.value || undefined
    queryParams.pageNo = 1
    name.value = ''
    getList()
  },
  { immediate: true }
)
</script>
<style scoped>
.bar { padding: 22px 0 0; }
.crumb { display: flex; gap: 8px; }
.searching { font-size: 13px; color: var(--muted); margin: 16px 0 0; }
.searching button {
  border: 0;
  background: none;
  color: var(--ink);
  text-decoration: underline;
  cursor: pointer;
  margin-left: 6px;
}
.filters {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  padding: 18px 0;
  margin-bottom: 26px;
  border-bottom: 1px solid var(--line);
}
.cats { display: flex; flex-wrap: wrap; gap: 22px; }
.cats button {
  border: 0;
  background: none;
  padding: 4px 0;
  font-size: 12px;
  letter-spacing: 0.14em;
  color: var(--muted);
  cursor: pointer;
}
.cats button:hover { color: var(--ink); }
.cats button.on { color: var(--ink); box-shadow: inset 0 -1px 0 var(--ink); }
.sort { display: inline-flex; align-items: center; gap: 10px; font-size: 12px; color: var(--muted); letter-spacing: 0.12em; }
.sort select {
  border: 1px solid var(--line);
  background: #fff;
  padding: 8px 10px;
  font-size: 12px;
  color: var(--ink);
  outline: none;
}
</style>
