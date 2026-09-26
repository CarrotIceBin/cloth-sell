<template>
  <footer class="foot">
    <div class="wrap feat">
      <div v-for="item in features" :key="item.title" class="feat-item">
        <component :is="item.icon" class="feat-icon" />
        <strong>{{ item.title }}</strong>
        <span>{{ item.text }}</span>
      </div>
    </div>
    <div class="wrap cols">
      <div class="col">
        <h4>关于我们</h4>
        <router-link to="/about">品牌故事</router-link>
        <router-link to="/shop">全部商品</router-link>
        <router-link to="/journal">期刊</router-link>
        <router-link to="/cart">购物车</router-link>
        <router-link to="/orders">我的订单</router-link>
      </div>
      <div class="col news">
        <h4>订阅我们的最新动态</h4>
        <p class="tip">新品上架、限时折扣与搭配灵感，第一时间送达。</p>
        <div class="sub">
          <input v-model="email" type="email" placeholder="请输入邮箱" @keyup.enter="subscribe" />
          <button class="btn" @click="subscribe">订阅</button>
        </div>
        <p class="tiny">订阅即表示同意接收我们的邮件，可随时取消。</p>
      </div>
      <div class="col">
        <h4>客户服务</h4>
        <router-link to="/help#help-contact">联系我们</router-link>
        <router-link to="/help#help-faq">常见问题</router-link>
        <router-link to="/help#help-shipping">配送说明</router-link>
        <router-link to="/help#help-returns">退换货</router-link>
        <router-link to="/help#help-size">尺码指南</router-link>
      </div>
    </div>
    <div class="bottom">
      <div class="wrap">© 2026 棉里 MIANLI · 保留所有权利</div>
    </div>
  </footer>
</template>
<script setup lang="ts">
import { ElMessage } from 'element-plus'
import { Box, CreditCard, Scissor } from '@element-plus/icons-vue'

const email = ref('')
const features = [
  { icon: Box, title: '免费配送与退换', text: '满 ¥600 免运费，7 天无理由' },
  { icon: Scissor, title: '免费改衣', text: '所有订单可预约一次修改' },
  { icon: CreditCard, title: '安全支付', text: '支持微信、支付宝与银行卡' }
]

const subscribe = () => {
  if (!email.value.trim()) {
    ElMessage.error('请填写邮箱')
    return
  }
  ElMessage.info('订阅功能即将开放')
}
</script>
<style scoped>
.foot { border-top: 1px solid var(--line); margin-top: 40px; }
.feat {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
  padding: 46px 0;
  border-bottom: 1px solid var(--line);
}
.feat-item { text-align: center; display: grid; gap: 8px; justify-items: center; }
.feat-icon { width: 26px; height: 26px; color: var(--gold); }
.feat-item strong { font-size: 12px; letter-spacing: 0.18em; font-weight: 500; }
.feat-item span { color: var(--muted); font-size: 12px; }
.cols { display: grid; grid-template-columns: 1fr 1.4fr 1fr; gap: 40px; padding: 48px 0; }
.col { display: grid; align-content: start; gap: 12px; font-size: 13px; color: #46423d; }
.col h4 {
  font-size: 11px;
  letter-spacing: 0.22em;
  text-transform: uppercase;
  color: var(--ink);
  margin: 0 0 8px;
  font-weight: 500;
}
.col a:hover { color: var(--ink); }
.news { text-align: center; justify-items: center; }
.news .tip { color: var(--muted); font-size: 13px; margin: 0; }
.sub { display: flex; width: 100%; max-width: 420px; }
.sub input {
  flex: 1;
  border: 1px solid var(--line);
  border-right: 0;
  padding: 12px 14px;
  font-size: 13px;
  outline: none;
}
.sub input:focus { border-color: var(--ink); }
.tiny { font-size: 11px; color: var(--muted); margin: 0; }
.bottom {
  border-top: 1px solid var(--line);
  padding: 20px 0;
  font-size: 11px;
  letter-spacing: 0.14em;
  color: var(--muted);
  text-align: center;
}
@media (max-width: 768px) {
  .feat, .cols { grid-template-columns: 1fr; padding: 32px 0; }
}
</style>
