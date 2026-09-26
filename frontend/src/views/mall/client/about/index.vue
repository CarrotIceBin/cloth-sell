<template>
  <div class="shop">
    <SiteHeader />

    <main>
      <section class="hero">
        <div class="wrap">
          <p class="eyebrow">品牌</p>
          <h1 class="serif">棉里 MIANLI</h1>
          <p class="lead">做耐穿的基础款，把版型和面料都做扎实。</p>
        </div>
      </section>

      <div class="wrap">
        <section class="sec-block">
          <h2>为什么是基础款</h2>
          <p>基础款最难做。它没有印花和廓形可以遮掩，面料的手感、肩线的落点、下摆的收法，全都摊在明面上。也正因为难，做好一件能穿很多年，比做十件穿一季的更划算。</p>
          <p>我们把精力放在三件事上：选对料、把版型改到服帖、把细节做耐穿。款式数量刻意控制在不多，方便你反复穿、反复搭。</p>
        </section>

        <section class="sec-block">
          <h2>三个坚持</h2>
          <div class="values">
            <article v-for="item in values" :key="item.title" class="value">
              <span class="index">{{ item.index }}</span>
              <h3>{{ item.title }}</h3>
              <p>{{ item.text }}</p>
            </article>
          </div>
        </section>

        <section class="sec-block">
          <h2>面料</h2>
          <p>面料决定一件衣服能穿多久、穿起来舒不舒服。我们优先选亲肤、好打理、洗后不变形的材质，常见的有下面几类。</p>
          <ul class="materials">
            <li v-for="item in materials" :key="item.name">
              <strong>{{ item.name }}</strong>
              <span>{{ item.text }}</span>
            </li>
          </ul>
        </section>

        <section class="sec-block">
          <h2>版型与制作</h2>
          <p>一件衣服在打样阶段会反复试穿调整：肩线是否压肩、袖窿是否卡腋、衣长落在胯骨还是腰线。这些位置差一两公分，上身效果完全不同。</p>
          <p>常见工艺也尽量做扎实：该加固的地方用双线，易走形的位置加衬，扣子和拉链选择耐用的款。做减法，把成本花在穿得到的地方。</p>
        </section>

        <section class="sec-block">
          <h2>包装与环保</h2>
          <p>发货默认使用可回收纸箱，尽量不用塑料。包装够用就好，不做过度的礼盒和填充。需要送礼可以在订单里备注，我们会另外处理。</p>
          <p>衣服本身的耐穿也是一种环保：少买、多穿、穿久一点，比任何口号都实在。</p>
        </section>

        <section class="sec-block">
          <h2>服务承诺</h2>
          <ul class="promises">
            <li v-for="item in promises" :key="item.label">
              <span>{{ item.label }}</span>
              <strong>{{ item.text }}</strong>
            </li>
          </ul>
          <p class="note">细则见 <router-link to="/help">帮助中心</router-link>。</p>
        </section>

        <section class="cta">
          <h2 class="serif">从一件基础款开始</h2>
          <p>先用一件穿得最勤的那件试试。</p>
          <div class="cta-actions">
            <router-link class="btn" to="/shop">去商店看看</router-link>
            <router-link class="btn btn-ghost" to="/help#help-contact">联系我们</router-link>
          </div>
        </section>
      </div>
    </main>

    <SiteFooter />
  </div>
</template>
<script setup lang="ts">
import SiteHeader from '@/components/SiteHeader.vue'
import SiteFooter from '@/components/SiteFooter.vue'

const values = [
  { index: '01', title: '面料优先', text: '先定面料再定款式。手感、透气性和水洗后的稳定性都要过关，才进入打样。' },
  { index: '02', title: '版型反复', text: '同一件打几个版，试穿到肩线、袖窿和衣长都合适才定稿，不靠宽松遮掩。' },
  { index: '03', title: '耐穿为上', text: '该加固的地方加固，尽量做减法。目标是能穿几季，而不是只穿一季。' }
]

const materials = [
  { name: '全棉', text: '亲肤透气，日常通勤最稳的选择。做过水洗的棉布缩率更小，也更好打理。' },
  { name: '羊毛与混纺', text: '挺括保暖，适合外套和大衣。混纺在保持手感的同时更好打理、不易起球。' },
  { name: '亚麻', text: '夏天首选，透气散热。天然褶皱是它的特点，越穿越柔。' },
  { name: '醋酸与真丝', text: '垂坠有光泽，适合衬衫和裙装。按洗标低温手洗或干洗，能保持质感更久。' }
]

const promises = [
  { label: '配送', text: '满 ¥600 免运费，付款后 48 小时内发货' },
  { label: '退换', text: '签收后 7 天内无理由退换，吊牌与包装保持完整' },
  { label: '改衣', text: '每笔订单可享一次免费改衣' },
  { label: '客服', text: '工作日 9:00–18:00，订单里留言即可' }
]
</script>
<style scoped>
.hero { background: var(--soft); padding: 76px 0 70px; text-align: center; }
.hero .eyebrow { margin: 0 0 16px; }
.hero h1 { font-size: 46px; font-weight: 400; letter-spacing: 0.22em; margin: 0 0 18px; }
.hero .lead { font-size: 15px; color: var(--muted); margin: 0; }
.sec-block {
  padding: 44px 0 46px;
  border-bottom: 1px solid var(--line);
  max-width: 760px;
}
.sec-block h2 {
  font-family: var(--serif);
  font-size: 24px;
  font-weight: 400;
  letter-spacing: 0.12em;
  margin: 0 0 20px;
}
.sec-block p { font-size: 15px; line-height: 1.95; color: #3b3733; margin: 0 0 16px; }
.sec-block p:last-child { margin-bottom: 0; }
.values { display: grid; grid-template-columns: repeat(3, 1fr); gap: 30px; }
.value { display: grid; gap: 10px; align-content: start; }
.index { font-family: var(--serif); font-size: 13px; letter-spacing: 0.2em; color: var(--gold); }
.value h3 { font-size: 15px; font-weight: 500; margin: 0; }
.value p { font-size: 13px; line-height: 1.9; color: var(--muted); margin: 0; }
.materials { list-style: none; padding: 0; margin: 0; display: grid; gap: 18px; }
.materials li { display: grid; gap: 6px; }
.materials strong { font-size: 14px; font-weight: 500; }
.materials span { font-size: 14px; line-height: 1.9; color: #3b3733; }
.promises { list-style: none; padding: 0; margin: 0 0 16px; display: grid; gap: 12px; }
.promises li { display: grid; grid-template-columns: 72px minmax(0, 1fr); gap: 16px; font-size: 14px; }
.promises span { color: var(--muted); }
.promises strong { font-weight: 400; }
.note { font-size: 13px; color: var(--muted); }
.note a { text-decoration: underline; }
.cta { text-align: center; padding: 70px 0 80px; }
.cta h2 { font-size: 30px; font-weight: 400; letter-spacing: 0.12em; margin: 0 0 14px; }
.cta p { font-size: 14px; color: var(--muted); margin: 0 0 28px; }
.cta-actions { display: flex; justify-content: center; gap: 14px; flex-wrap: wrap; }
@media (max-width: 768px) {
  .hero { padding: 52px 0 46px; }
  .hero h1 { font-size: 32px; }
  .values { grid-template-columns: 1fr; gap: 24px; }
  .sec-block { padding: 34px 0 36px; }
}
</style>
