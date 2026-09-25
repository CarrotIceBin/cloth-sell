<template>
  <div class="shop">
    <SiteHeader />
    <main class="article" v-if="post">
      <nav class="crumb">
        <router-link to="/">首页</router-link>
        <span>/</span>
        <router-link to="/journal">期刊</router-link>
        <span>/</span>
        <span>{{ post.tag }}</span>
      </nav>

      <header class="head">
        <p class="tag">{{ post.tag }}</p>
        <h1 class="serif">{{ post.title }}</h1>
        <p class="meta">{{ date(post.createTime) }} · 棉里编辑部</p>
      </header>

      <div class="cover" :style="articleStyle(post.coverUrl)"></div>

      <div class="body">
        <p v-for="(para, i) in paragraphs" :key="i">{{ para }}</p>
      </div>

      <footer class="foot">
        <router-link class="btn btn-ghost" to="/journal">← 返回期刊</router-link>
        <router-link v-if="next" class="next" :to="'/journal/' + next.id">
          下一篇：{{ next.title }} →
        </router-link>
      </footer>
    </main>
    <SiteFooter />
  </div>
</template>
<script setup lang="ts">
import SiteHeader from '@/components/SiteHeader.vue'
import SiteFooter from '@/components/SiteFooter.vue'
import { ClientJournalApi, type Journal } from '@/api/mall/journal'
import { articleStyle } from '@/utils/cover'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'

const route = useRoute()
const post = ref<Journal>()
const next = ref<Journal>()

const date = (value?: string) => (value ? value.slice(0, 10) : '')
const paragraphs = computed(() =>
  (post.value?.content || '')
    .split(/\n{2,}/)
    .map((item) => item.trim())
    .filter(Boolean)
)

async function load(id: number) {
  try {
    post.value = await ClientJournalApi.getJournal(id)
  } catch (e: any) {
    ElMessage.error(e.message)
    return
  }
  const page = await ClientJournalApi.getJournalPage({ pageNo: 1, pageSize: 20 })
  const list: Journal[] = page.list || []
  const index = list.findIndex((item) => item.id === id)
  next.value = index >= 0 ? list[index + 1] : undefined
}

onMounted(() => load(Number(route.params.id)))
watch(() => route.params.id, (id) => id && load(Number(id)))
</script>
<style scoped>
.article { max-width: 760px; margin: 0 auto; padding: 24px 20px 60px; }
.crumb { display: flex; gap: 8px; font-size: 12px; color: var(--muted); letter-spacing: 0.06em; }
.crumb a:hover { color: var(--ink); }
.head { text-align: center; padding: 30px 0 26px; }
.tag {
  display: inline-block;
  font-size: 10px;
  letter-spacing: 0.24em;
  background: var(--ink);
  color: #fff;
  padding: 6px 14px;
  margin: 0 0 18px;
}
h1 { font-size: 34px; font-weight: 400; margin: 0 0 14px; letter-spacing: 0.04em; line-height: 1.35; }
.meta { font-size: 12px; color: var(--muted); letter-spacing: 0.1em; margin: 0; }
.cover { aspect-ratio: 16 / 9; margin-bottom: 34px; }
.body p { font-size: 15px; line-height: 1.95; color: #3b3733; margin: 0 0 20px; }
.foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  flex-wrap: wrap;
  border-top: 1px solid var(--line);
  padding-top: 26px;
  margin-top: 34px;
}
.next { font-size: 12px; letter-spacing: 0.12em; color: var(--muted); }
.next:hover { color: var(--ink); }
</style>
