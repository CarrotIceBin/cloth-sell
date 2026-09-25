<template>
  <div class="shop">
    <SiteHeader />
    <main class="wrap">
      <nav class="crumb bar">
        <router-link to="/">首页</router-link>
        <span>/</span>
        <span>期刊</span>
      </nav>
      <header class="head">
        <h1 class="sec-title">期刊</h1>
        <p class="sec-sub">搭配灵感与面料知识</p>
      </header>

      <div class="tags">
        <button
          v-for="tag in tags"
          :key="tag.label"
          :class="{ on: active === tag.value }"
          @click="pick(tag.value)"
        >{{ tag.label }}</button>
      </div>

      <p v-if="!list.length" class="empty">还没有文章</p>
      <div class="grid">
        <router-link v-for="(post, i) in list" :key="post.id" class="post" :to="'/journal/' + post.id">
          <div class="cover" :style="articleStyle(post.coverUrl, i)">
            <span class="tag">{{ post.tag }}</span>
          </div>
          <div class="body">
            <p class="date">{{ date(post.createTime) }}</p>
            <h2>{{ post.title }}</h2>
            <p class="text">{{ post.summary }}</p>
            <span class="read">阅读更多 →</span>
          </div>
        </router-link>
      </div>

      <Pagination
        :total="total"
        v-model:page="queryParams.pageNo"
        v-model:limit="queryParams.pageSize"
        @pagination="load"
      />
    </main>
    <SiteFooter />
  </div>
</template>
<script setup lang="ts">
import SiteHeader from '@/components/SiteHeader.vue'
import SiteFooter from '@/components/SiteFooter.vue'
import Pagination from '@/components/Pagination/index.vue'
import { ClientJournalApi, type Journal } from '@/api/mall/journal'
import { articleStyle } from '@/utils/cover'

const list = ref<Journal[]>([])
const total = ref(0)
const active = ref('')
const queryParams = reactive({
  pageNo: 1,
  pageSize: 9,
  tag: undefined as string | undefined
})

const tags = [
  { label: '全部', value: '' },
  { label: '搭配', value: '搭配' },
  { label: '面料', value: '面料' },
  { label: '指南', value: '指南' }
]

const date = (value?: string) => (value ? value.slice(0, 10) : '')

const load = async () => {
  const data = await ClientJournalApi.getJournalPage(queryParams)
  list.value = data.list
  total.value = data.total
}

const pick = (tag: string) => {
  active.value = tag
  queryParams.tag = tag || undefined
  queryParams.pageNo = 1
  load()
}

onMounted(load)
</script>
<style scoped>
.bar { padding: 22px 0 0; }
.crumb { display: flex; gap: 8px; }
.head { text-align: center; padding: 34px 0 26px; }
.tags {
  display: flex;
  justify-content: center;
  flex-wrap: wrap;
  gap: 22px;
  padding-bottom: 22px;
  margin-bottom: 34px;
  border-bottom: 1px solid var(--line);
}
.tags button {
  border: 0;
  background: none;
  padding: 4px 0;
  font-size: 12px;
  letter-spacing: 0.16em;
  color: var(--muted);
  cursor: pointer;
}
.tags button:hover { color: var(--ink); }
.tags button.on { color: var(--ink); box-shadow: inset 0 -1px 0 var(--ink); }
.grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 34px 26px; }
.post { display: block; }
.cover { position: relative; aspect-ratio: 16 / 11; }
.cover::after {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, rgba(28, 25, 23, 0), rgba(28, 25, 23, 0.14));
}
.tag {
  position: absolute;
  left: 0;
  bottom: 0;
  z-index: 1;
  background: var(--ink);
  color: #fff;
  font-size: 10px;
  letter-spacing: 0.2em;
  padding: 6px 12px;
}
.body { padding-top: 16px; }
.date { font-size: 11px; letter-spacing: 0.14em; color: var(--muted); margin: 0 0 8px; }
.post h2 { font-size: 17px; font-weight: 400; margin: 0 0 8px; }
.post:hover h2 { color: var(--gold); }
.text { color: var(--muted); font-size: 13px; margin: 0 0 12px; line-height: 1.7; }
.read { font-size: 11px; letter-spacing: 0.2em; text-transform: uppercase; }
@media (max-width: 900px) {
  .grid { grid-template-columns: 1fr; }
}
</style>
