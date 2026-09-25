<template>
  <div class="shop">
    <SiteHeader />
    <main class="wrap" v-if="product">
      <nav class="crumb bar">
        <router-link to="/">首页</router-link>
        <span>/</span>
        <router-link to="/shop">商店</router-link>
        <span>/</span>
        <span>{{ product.name }}</span>
      </nav>

      <section class="detail">
        <div class="gallery">
          <div class="cover" :style="coverStyle(mainCover)"></div>
          <div v-if="colorImages.length > 1" class="thumbs">
            <button
              v-for="item in colorImages"
              :key="item.color"
              class="thumb"
              :class="{ on: color === item.color }"
              :style="coverStyle(item.url)"
              :aria-label="item.color"
              @click="pickColor(item.color)"
            />
          </div>
        </div>
        <div class="buy">
          <h1 class="serif">{{ product.name }}</h1>
          <p class="price">¥{{ money(current?.price ?? product.minPrice) }}</p>
          <p class="stock">库存 {{ current?.stock ?? product.stock ?? 0 }} 件 · {{ product.category }}</p>

          <p class="label">颜色</p>
          <div class="chips">
            <button v-for="c in colors" :key="c" :class="{ on: color === c }" @click="pickColor(c)">{{ c }}</button>
          </div>
          <p class="label">尺码</p>
          <div class="chips">
            <button
              v-for="s in sizes"
              :key="s"
              :class="{ on: size === s }"
              :disabled="!ok(s)"
              @click="size = s"
            >{{ s }}</button>
          </div>

          <div class="pick">
            <div class="stepper">
              <button type="button" @click="qty = Math.max(1, qty - 1)">−</button>
              <span>{{ qty }}</span>
              <button type="button" :disabled="qty >= maxQty" @click="qty = Math.min(maxQty, qty + 1)">+</button>
            </div>
            <button class="btn" :disabled="!current?.id || maxQty < 1" @click="add">加入购物车</button>
            <button class="wish" @click="favorite"><Collection /> 加入收藏</button>
          </div>

          <ul class="info">
            <li><Van /> 满 ¥600 免运费，48 小时内发货</li>
            <li><RefreshLeft /> 支持 7 天无理由退换</li>
            <li><Scissor /> 每笔订单可免费改衣一次</li>
          </ul>

          <div class="meta">
            <p><span>分类：</span>{{ product.category }}</p>
            <p><span>标签：</span>{{ colors.join('、') }} / {{ allSizes.join('、') }}</p>
          </div>

          <div class="share">
            <span>分享</span>
            <button v-for="name in shares" :key="name" class="icon" :aria-label="name">{{ name }}</button>
          </div>
        </div>
      </section>

      <section class="reviews">
        <h2 class="sec-title line-title">评价 ({{ summary.total || 0 }})</h2>
        <div class="review-top">
          <div class="score">
            <strong>{{ Number(summary.average || 0).toFixed(1) }}</strong>
            <span class="stars-line">{{ starsText(summary.average) }}</span>
            <span class="muted">共 {{ summary.total || 0 }} 条评价</span>
          </div>
          <div class="bars">
            <div v-for="star in [5, 4, 3, 2, 1]" :key="star" class="bar-row">
              <span class="bar-label">{{ star }} 星</span>
              <span class="bar">
                <span class="bar-fill" :style="{ width: barWidth(star) }" />
              </span>
              <span class="bar-count">{{ summary.stars?.[star] || 0 }}</span>
            </div>
          </div>
        </div>

        <div class="review-grid">
          <div class="review-form">
            <h3>留下你的评价</h3>
            <div class="stars">
              <button v-for="n in 5" :key="n" :class="{ on: n <= rate }" @click="rate = n">★</button>
            </div>
            <textarea v-model="reviewText" rows="4" maxlength="500" placeholder="说说这件衣服的版型与面料"></textarea>
            <button class="btn" :disabled="submitting" @click="submitReview">
              {{ submitting ? '提交中…' : '提交评价' }}
            </button>
            <p class="tip">{{ loggedIn ? '每个账号对同一件商品只能评价一次。' : '登录后即可评价。' }}</p>
          </div>

          <div class="review-list">
            <p v-if="!reviews.length" class="review-empty">
              暂无评价<br />
              <span>成为第一个评价的人。</span>
            </p>
            <article v-for="item in reviews" :key="item.id" class="review-item">
              <div class="review-head">
                <strong>{{ item.author }}</strong>
                <span class="stars-line">{{ starsText(item.rating) }}</span>
                <span class="muted">{{ fmtDate(item.createTime) }}</span>
              </div>
              <p class="review-text">{{ item.content || '这位顾客没有留下文字评价。' }}</p>
            </article>
            <Pagination
              v-if="reviewTotal > reviewParams.pageSize"
              :total="reviewTotal"
              v-model:page="reviewParams.pageNo"
              v-model:limit="reviewParams.pageSize"
              @pagination="loadReviews"
            />
          </div>
        </div>
      </section>

      <section class="related">
        <div class="sec-head">
          <h2 class="sec-title">相关商品</h2>
          <p class="sec-sub">你可能也会喜欢</p>
        </div>
        <div class="p-grid">
          <ProductCard v-for="item in related" :key="item.id" :product="item" />
        </div>
      </section>

      <nav class="switch">
        <button class="move" :disabled="!prev" @click="go(prev)">← 上一件</button>
        <router-link class="btn btn-ghost" to="/shop">返回商店</router-link>
        <button class="move" :disabled="!next" @click="go(next)">下一件 →</button>
      </nav>
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
import { CartApi } from '@/api/mall/client/cart'
import { ClientReviewApi, type Review, type ReviewSummary } from '@/api/mall/review'
import type { Product, Sku } from '@/api/mall/product'
import { coverStyle } from '@/utils/cover'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Collection, RefreshLeft, Scissor, Van } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const product = ref<Product>()
const related = ref<Product[]>([])
const color = ref('')
const size = ref('')
const qty = ref(1)
const rate = ref(5)
const reviewText = ref('')
const submitting = ref(false)
const reviews = ref<Review[]>([])
const reviewTotal = ref(0)
const reviewParams = reactive({ pageNo: 1, pageSize: 5 })
const summary = ref<ReviewSummary>({ total: 0, average: 0, stars: {} })
const shares = ['微', '博', 'QQ', '链']
const loggedIn = computed(() => !!localStorage.getItem('userToken'))
const starsText = (value?: number) => '★★★★★'.slice(0, Math.round(value || 0)).padEnd(5, '☆')
const fmtDate = (value?: string) => (value ? value.replace('T', ' ').slice(0, 10) : '')
const barWidth = (star: number) => {
  const total = summary.value.total || 0
  if (!total) return '0%'
  return Math.round(((summary.value.stars?.[star] || 0) / total) * 100) + '%'
}

const money = (n?: number) => Number(n || 0).toFixed(2)
const colors = computed(() => [...new Set((product.value?.skus || []).map((s) => s.color || ''))])
const allSizes = computed(() => [...new Set((product.value?.skus || []).map((s) => s.size || ''))])
const sizes = computed(() =>
  [...new Set((product.value?.skus || []).filter((s) => s.color === color.value).map((s) => s.size || ''))]
)
const current = computed(() =>
  (product.value?.skus || []).find((s) => s.color === color.value && s.size === size.value)
)
// 一个颜色一张图，选中的颜色决定主图
const colorImages = computed(() => {
  const skus = product.value?.skus || []
  return colors.value.map((c) => {
    const hit = skus.find((s: Sku) => s.color === c && s.coverUrl)
    return { color: c, url: hit?.coverUrl || product.value?.coverUrl }
  })
})
const mainCover = computed(() =>
  colorImages.value.find((item) => item.color === color.value)?.url || product.value?.coverUrl
)
const maxQty = computed(() => Math.max(0, current.value?.stock || 0))
const ok = (s: string) =>
  (product.value?.skus || []).some(
    (sku: Sku) => sku.color === color.value && sku.size === s && (sku.stock || 0) > 0
  )

const prev = computed(() => neighbor(-1))
const next = computed(() => neighbor(1))

function neighbor(offset: number) {
  const index = related.value.findIndex((item) => item.id === product.value?.id)
  if (index < 0) return undefined
  return related.value[index + offset]?.id
}

const pickColor = (c: string) => {
  color.value = c
  if (!sizes.value.includes(size.value) || !ok(size.value)) {
    size.value = sizes.value.find(ok) || sizes.value[0] || ''
  }
}

const go = (id?: number) => id && router.push('/product/' + id)

const favorite = () => ElMessage.info('收藏功能即将开放')

const loadReviews = async () => {
  const productId = Number(route.params.id)
  const [page, sum] = await Promise.all([
    ClientReviewApi.getReviewPage({ ...reviewParams, productId }),
    ClientReviewApi.getSummary(productId)
  ])
  reviews.value = page.list
  reviewTotal.value = page.total
  summary.value = sum
}

const submitReview = async () => {
  if (!loggedIn.value) {
    router.push('/login')
    return
  }
  submitting.value = true
  try {
    await ClientReviewApi.createReview({
      productId: Number(route.params.id),
      rating: rate.value,
      content: reviewText.value.trim() || undefined
    })
    ElMessage.success('评价已提交')
    reviewText.value = ''
    reviewParams.pageNo = 1
    await loadReviews()
  } catch (e: any) {
    ElMessage.error(e.message)
  } finally {
    submitting.value = false
  }
}

const add = async () => {
  if (!localStorage.getItem('userToken')) {
    router.push('/login')
    return
  }
  if (!current.value?.id) return
  try {
    await CartApi.createCart({ skuId: current.value.id, qty: qty.value })
    window.dispatchEvent(new Event('cart-changed'))
    ElMessage.success('已加入购物车')
  } catch (e: any) {
    ElMessage.error(e.message)
  }
}

async function load(id: number) {
  product.value = await ClientProductApi.getProduct(id)
  color.value = colors.value[0] || ''
  size.value = sizes.value.find(ok) || sizes.value[0] || ''
  qty.value = 1
  reviewParams.pageNo = 1
  await loadReviews()
  const page = await ClientProductApi.getProductPage({ pageNo: 1, pageSize: 12, sortBy: 'id', sortOrder: 'desc' })
  related.value = page.list.filter((item: Product) => item.id !== id).slice(0, 4)
}

onMounted(() => load(Number(route.params.id)))
watch(() => route.params.id, (id) => id && load(Number(id)))
</script>
<style scoped>
.bar { padding: 22px 0 0; }
.crumb { display: flex; gap: 8px; flex-wrap: wrap; }
.detail { display: grid; grid-template-columns: minmax(0, 1.05fr) minmax(0, 1fr); gap: 56px; padding: 30px 0 10px; }
.cover { aspect-ratio: 4 / 5; background: var(--soft) center / cover no-repeat; }
.thumbs { display: flex; flex-wrap: wrap; gap: 10px; margin-top: 12px; }
.thumb {
  width: 64px;
  height: 80px;
  padding: 0;
  border: 1px solid var(--line);
  background: var(--soft) center / cover no-repeat;
  cursor: pointer;
  transition: border-color 0.18s;
}
.thumb:hover { border-color: var(--ink); }
.thumb.on { border-color: var(--ink); box-shadow: 0 0 0 1px var(--ink); }
.buy h1 { font-size: 32px; font-weight: 400; margin: 0 0 14px; }
.price { font-size: 22px; margin: 0 0 8px; letter-spacing: 0.04em; }
.stock { color: var(--muted); font-size: 13px; margin: 0 0 24px; }
.label { font-size: 12px; letter-spacing: 0.2em; color: var(--muted); margin: 20px 0 10px; text-transform: uppercase; }
.chips { display: flex; flex-wrap: wrap; gap: 10px; }
.chips button {
  border: 1px solid var(--line);
  background: #fff;
  padding: 9px 18px;
  font-size: 13px;
  cursor: pointer;
  transition: 0.18s;
}
.chips button:hover { border-color: var(--ink); }
.chips button.on { background: var(--ink); color: #fff; border-color: var(--ink); }
.chips button:disabled { opacity: 0.32; cursor: not-allowed; }
.pick { display: flex; align-items: center; gap: 14px; flex-wrap: wrap; margin: 26px 0 8px; }
.stepper { display: flex; align-items: center; border: 1px solid var(--line); }
.stepper button, .stepper span {
  width: 42px;
  height: 44px;
  display: grid;
  place-items: center;
  background: #fff;
  border: 0;
  cursor: pointer;
}
.stepper button:disabled { opacity: 0.35; cursor: not-allowed; }
.stepper button + span, .stepper span + button { border-left: 1px solid var(--line); }
.pick .btn { padding: 15px 44px; }
.pick .btn:disabled { opacity: 0.45; cursor: not-allowed; }
.wish {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  border: 0;
  background: none;
  font-size: 12px;
  color: var(--muted);
  cursor: pointer;
}
.wish:hover { color: var(--ink); }
.wish svg { width: 14px; height: 14px; }
.info { list-style: none; padding: 22px 0; margin: 22px 0 0; border-top: 1px solid var(--line); display: grid; gap: 12px; }
.info li { display: flex; align-items: center; gap: 10px; font-size: 13px; color: #4a4640; }
.info svg { width: 16px; height: 16px; color: var(--gold); }
.meta { border-top: 1px solid var(--line); padding-top: 18px; font-size: 13px; color: #4a4640; }
.meta p { margin: 0 0 6px; }
.meta span { color: var(--muted); }
.share { display: flex; align-items: center; gap: 10px; margin-top: 18px; font-size: 12px; color: var(--muted); letter-spacing: 0.16em; }
.icon {
  width: 28px;
  height: 28px;
  display: grid;
  place-items: center;
  border: 1px solid var(--line);
  background: #fff;
  font-size: 11px;
  cursor: pointer;
}
.icon:hover { background: var(--ink); color: #fff; border-color: var(--ink); }
.reviews { padding: 56px 0; border-top: 1px solid var(--line); margin-top: 46px; }
.line-title { font-size: 22px; letter-spacing: 0.2em; margin-bottom: 30px; }
.review-top {
  display: grid;
  grid-template-columns: 200px minmax(0, 1fr);
  gap: 40px;
  align-items: center;
  padding-bottom: 34px;
  margin-bottom: 34px;
  border-bottom: 1px solid var(--line);
}
.score { display: grid; gap: 6px; justify-items: start; }
.score strong { font-family: var(--serif); font-size: 44px; line-height: 1; font-weight: 400; }
.stars-line { color: var(--gold); letter-spacing: 0.16em; font-size: 14px; }
.muted { color: var(--muted); font-size: 12px; }
.bars { display: grid; gap: 10px; }
.bar-row { display: grid; grid-template-columns: 48px minmax(0, 1fr) 40px; gap: 12px; align-items: center; font-size: 12px; color: var(--muted); }
.bar { height: 6px; background: var(--soft); display: block; }
.bar-fill { display: block; height: 100%; background: var(--gold); }
.bar-count { text-align: right; }
.review-grid { display: grid; grid-template-columns: 1fr 1.2fr; gap: 48px; }
.review-form h3 { font-size: 15px; font-weight: 400; margin: 0 0 14px; }
.stars { display: flex; gap: 6px; margin-bottom: 16px; }
.stars button { border: 0; background: none; font-size: 18px; color: #d8d2ca; cursor: pointer; padding: 0; }
.stars button.on { color: var(--gold); }
.review-form textarea {
  width: 100%;
  box-sizing: border-box;
  border: 1px solid var(--line);
  padding: 12px;
  font: inherit;
  font-size: 13px;
  outline: none;
}
.review-form textarea:focus { border-color: var(--ink); }
.review-form .btn { width: 100%; margin-top: 14px; }
.review-form .btn:disabled { opacity: 0.5; cursor: not-allowed; }
.tip { color: var(--muted); font-size: 12px; margin: 10px 0 0; }
.review-list { display: grid; gap: 20px; align-content: start; }
.review-empty { text-align: center; color: var(--ink); font-size: 14px; margin: 0; line-height: 1.9; }
.review-empty span { color: var(--muted); font-size: 13px; }
.review-item { border: 1px solid var(--line); padding: 18px; }
.review-head { display: flex; align-items: center; gap: 12px; margin-bottom: 8px; }
.review-head strong { font-size: 14px; font-weight: 500; }
.review-head .muted { margin-left: auto; }
.review-text { font-size: 13px; color: #4a4640; line-height: 1.8; margin: 0; }
.related { padding: 20px 0 60px; }
.switch { display: flex; align-items: center; justify-content: space-between; gap: 16px; padding: 26px 0 60px; }
.move { border: 0; background: none; font-size: 12px; letter-spacing: 0.16em; color: var(--ink); cursor: pointer; }
.move:disabled { color: #c9c3bb; cursor: not-allowed; }
@media (max-width: 900px) {
  .detail { grid-template-columns: 1fr; gap: 28px; }
  .review-grid, .review-top { grid-template-columns: 1fr; gap: 24px; }
}
</style>
