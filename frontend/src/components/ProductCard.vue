<template>
  <article class="card" @click="open">
    <div class="thumb" :style="coverStyle(product.coverUrl)">
      <div class="tools">
        <button class="tool" aria-label="收藏" @click.stop="favorite"><Collection /></button>
        <button class="tool" aria-label="查看" @click.stop="open"><View /></button>
      </div>
      <span v-if="!product.stock" class="sold">已售罄</span>
    </div>
    <h3 class="name">{{ product.name }}</h3>
    <p class="price">¥{{ money(product.minPrice) }}</p>
  </article>
</template>
<script setup lang="ts">
import { ElMessage } from 'element-plus'
import { Collection, View } from '@element-plus/icons-vue'
import type { Product } from '@/api/mall/product'
import { coverStyle } from '@/utils/cover'
import { useRouter } from 'vue-router'

const props = defineProps<{ product: Product }>()
const router = useRouter()
const money = (n?: number) => Number(n || 0).toFixed(2)
const open = () => props.product.id && router.push('/product/' + props.product.id)
const favorite = () => ElMessage.info('收藏功能即将开放')
</script>
<style scoped>
.card { cursor: pointer; text-align: center; }
.thumb {
  position: relative;
  aspect-ratio: 4 / 5;
  background: var(--soft) center / cover no-repeat;
  overflow: hidden;
}
.tools {
  position: absolute;
  top: 12px;
  right: 12px;
  display: grid;
  gap: 8px;
  opacity: 0;
  transform: translateY(-4px);
  transition: opacity 0.22s, transform 0.22s;
}
.card:hover .tools { opacity: 1; transform: none; }
.tool {
  width: 32px;
  height: 32px;
  display: grid;
  place-items: center;
  border: 0;
  background: rgba(255, 255, 255, 0.92);
  color: var(--ink);
  cursor: pointer;
}
.tool:hover { background: var(--ink); color: #fff; }
.tool svg { width: 15px; height: 15px; }
.sold {
  position: absolute;
  left: 0;
  bottom: 0;
  background: var(--ink);
  color: #fff;
  font-size: 11px;
  letter-spacing: 0.16em;
  padding: 6px 12px;
}
.name { font-size: 15px; font-weight: 400; margin: 16px 0 6px; }
.price { font-size: 13px; color: var(--muted); margin: 0; letter-spacing: 0.04em; }
</style>
