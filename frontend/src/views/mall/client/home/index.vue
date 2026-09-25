<template>
  <div class="shop">
    <SiteHeader />
    <main>
      <section class="hero wrap">
        <router-link class="block tall" :style="heroA" to="/shop">
          <div class="copy">
            <p class="eyebrow">本季精选</p>
            <h1>版型与面料，都值得挑剔</h1>
            <span class="btn btn-line">立即选购</span>
          </div>
        </router-link>
        <div class="stack">
          <router-link class="block" :style="heroB" to="/shop">
            <div class="copy">
              <p class="eyebrow">新品上架</p>
              <h2>欢迎来到本季新装</h2>
              <span class="btn btn-line">立即选购</span>
            </div>
          </router-link>
          <router-link class="block" :style="heroC" to="/shop">
            <div class="copy">
              <p class="eyebrow">限时价格</p>
              <h2>经典款，更亲民</h2>
              <span class="btn btn-line">立即选购</span>
            </div>
          </router-link>
        </div>
      </section>

      <section class="sec wrap">
        <div class="sec-head">
          <h2 class="sec-title">按品类挑选</h2>
          <p class="sec-sub">从日常到通勤，找到合适的那一件</p>
        </div>
        <div class="cats">
          <router-link v-for="cat in categories" :key="cat.label" class="cat" :to="{ path: '/shop', query: cat.q ? { q: cat.q } : {} }">
            <span class="ring">{{ cat.label.slice(0, 1) }}</span>
            <span class="cat-name">{{ cat.label }}</span>
          </router-link>
        </div>
      </section>

      <section class="sec wrap">
        <div class="sec-head row-head">
          <div>
            <h2 class="sec-title">新品推荐</h2>
            <p class="sec-sub">刚刚上架，数量有限</p>
          </div>
          <div class="tabs">
            <button v-for="tab in tabs" :key="tab.key" :class="{ on: sort === tab.key }" @click="switchSort(tab.key)">
              {{ tab.label }}
            </button>
          </div>
        </div>
        <p v-if="!list.length" class="empty">还没有上架商品</p>
        <div class="p-grid">
          <ProductCard v-for="item in list" :key="item.id" :product="item" />
        </div>
        <div class="more">
          <router-link class="btn btn-ghost" to="/shop">查看全部</router-link>
        </div>
      </section>

      <section class="banner" :style="banner">
        <div class="wrap">
          <h2 class="serif">每一件都值得被认真对待</h2>
          <p>选用亲肤面料，版型反复打磨，只做耐穿的那几件。</p>
          <router-link class="btn btn-light" to="/shop">了解品牌</router-link>
        </div>
      </section>

      <section class="sec wrap">
        <p class="press-title">媒体报道</p>
        <div class="press">
          <span v-for="p in press" :key="p">{{ p }}</span>
        </div>
      </section>

      <section class="sec wrap">
        <div class="sec-head">
          <h2 class="sec-title">期刊</h2>
          <p class="sec-sub">搭配灵感与面料知识</p>
        </div>
        <p v-if="!posts.length" class="empty">还没有文章</p>
        <div class="journal">
          <router-link v-for="(post, i) in posts" :key="post.id" class="post" :to="'/journal/' + post.id">
            <div class="post-cover" :style="articleStyle(post.coverUrl, i)">
              <span class="tag">{{ post.tag }}</span>
            </div>
            <h3>{{ post.title }}</h3>
            <p>{{ post.summary }}</p>
            <span class="read">阅读更多 →</span>
          </router-link>
        </div>
        <div class="more">
          <router-link class="btn btn-ghost" to="/journal">查看全部文章</router-link>
        </div>
      </section>
    </main>
    <SiteFooter />
  </div>
</template>
<script setup lang="ts">
import SiteHeader from '@/components/SiteHeader.vue'
import SiteFooter from '@/components/SiteFooter.vue'
import ProductCard from '@/components/ProductCard.vue'
import { ClientProductApi } from '@/api/mall/client/product'
import { ClientJournalApi, type Journal } from '@/api/mall/journal'
import type { Product } from '@/api/mall/product'
import { articleStyle } from '@/utils/cover'

const list = ref<Product[]>([])
const posts = ref<Journal[]>([])
const sort = ref('new')
const tabs = [
  { key: 'new', label: '最新' },
  { key: 'id', label: '综合' }
]

const heroA = {
  background:
    'radial-gradient(circle at 70% 25%, #f3e7da 0%, #ddc7b2 45%, #c2a68c 100%)'
}
const heroB = {
  background:
    'radial-gradient(circle at 30% 30%, #f6efe6 0%, #e2d2bf 55%, #cbb59c 100%)'
}
const heroC = {
  background:
    'radial-gradient(circle at 65% 40%, #efe9e2 0%, #d9cbbb 60%, #bda98f 100%)'
}
const banner = {
  background:
    'linear-gradient(135deg, #2b2621 0%, #3d362e 55%, #574c40 100%)'
}

const categories = [
  { label: '上装', q: '衫' },
  { label: '裙装', q: '裙' },
  { label: '裤装', q: '裤' },
  { label: '外套', q: '外套' },
  { label: '针织', q: '针织' },
  { label: '羽绒', q: '羽绒' }
]

const press = ['ELLE', 'BAZAAR', 'COSMOPOLITAN', 'VOGUE', 'VANITY FAIR', 'GLAMOUR']

const load = async () => {
  const data = await ClientProductApi.getProductPage({
    pageNo: 1,
    pageSize: 8,
    sortBy: sort.value === 'new' ? 'createTime' : 'id',
    sortOrder: 'desc'
  })
  list.value = data.list
}

const loadJournal = async () => {
  try {
    const data = await ClientJournalApi.getJournalPage({ pageNo: 1, pageSize: 3 })
    posts.value = data.list
  } catch {
    posts.value = []
  }
}

const switchSort = (key: string) => {
  sort.value = key
  load()
}

onMounted(() => {
  load()
  loadJournal()
})
</script>
<style scoped>
.hero { display: grid; grid-template-columns: 1.15fr 1fr; gap: 18px; padding: 26px 0 0; }
.stack { display: grid; gap: 18px; }
.block { position: relative; display: block; min-height: 220px; overflow: hidden; }
.block.tall { min-height: 100%; }
.block::after {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(90deg, rgba(28, 25, 23, 0.22), rgba(28, 25, 23, 0));
}
.copy {
  position: absolute;
  left: 34px;
  bottom: 34px;
  z-index: 1;
  max-width: 78%;
  color: #fff;
}
.copy h1, .copy h2 { font-family: var(--serif); font-weight: 400; margin: 10px 0 18px; line-height: 1.25; }
.copy h1 { font-size: 34px; }
.copy h2 { font-size: 24px; }
.copy .eyebrow { color: rgba(255, 255, 255, 0.85); }
.cats { display: grid; grid-template-columns: repeat(6, 1fr); gap: 18px; }
.cat { display: grid; justify-items: center; gap: 12px; }
.ring {
  width: 86px;
  height: 86px;
  display: grid;
  place-items: center;
  border: 1px solid var(--line);
  border-radius: 50%;
  font-family: var(--serif);
  font-size: 24px;
  color: var(--ink);
  transition: border-color 0.2s, background 0.2s, color 0.2s;
}
.cat:hover .ring { background: var(--ink); border-color: var(--ink); color: #fff; }
.cat-name { font-size: 12px; letter-spacing: 0.18em; color: var(--muted); }
.row-head { display: flex; align-items: flex-end; justify-content: space-between; gap: 20px; text-align: left; }
.tabs { display: flex; gap: 20px; }
.tabs button {
  border: 0;
  background: none;
  padding: 4px 0;
  font-size: 12px;
  letter-spacing: 0.18em;
  color: var(--muted);
  cursor: pointer;
}
.tabs button.on { color: var(--ink); box-shadow: inset 0 -1px 0 var(--ink); }
.more { display: flex; justify-content: center; margin-top: 40px; }
.banner { padding: 90px 0; text-align: center; color: #fff; }
.banner h2 { font-size: 34px; font-weight: 400; letter-spacing: 0.08em; margin: 0 0 14px; }
.banner p { color: rgba(255, 255, 255, 0.78); font-size: 14px; margin: 0 0 28px; }
.press-title {
  text-align: center;
  font-size: 11px;
  letter-spacing: 0.28em;
  text-transform: uppercase;
  color: var(--muted);
  margin: 0 0 26px;
}
.press {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 44px;
  font-family: var(--serif);
  font-size: 20px;
  letter-spacing: 0.2em;
  color: #b3aca4;
}
.journal { display: grid; grid-template-columns: repeat(3, 1fr); gap: 26px; }
.post-cover { aspect-ratio: 16 / 11; position: relative; }
.tag {
  position: absolute;
  left: 0;
  bottom: 0;
  background: var(--ink);
  color: #fff;
  font-size: 10px;
  letter-spacing: 0.2em;
  padding: 6px 12px;
}
.post h3 { font-size: 17px; font-weight: 400; margin: 16px 0 8px; }
.post p { color: var(--muted); font-size: 13px; margin: 0 0 12px; line-height: 1.7; }
.read { font-size: 11px; letter-spacing: 0.2em; text-transform: uppercase; }
.read:hover { color: var(--gold); }
@media (max-width: 900px) {
  .hero { grid-template-columns: 1fr; }
  .cats { grid-template-columns: repeat(3, 1fr); }
  .journal { grid-template-columns: 1fr; }
  .row-head { flex-direction: column; align-items: flex-start; }
  .block { min-height: 260px; }
}
@media (max-width: 520px) {
  .cats { grid-template-columns: repeat(2, 1fr); }
}
</style>
