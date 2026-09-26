<template>
  <div class="shop">
    <SiteHeader />
    <main class="wrap">
      <header class="head">
        <p class="eyebrow">客户服务</p>
        <h1 class="sec-title">帮助中心</h1>
        <p class="sec-sub">配送、退换与尺码，都在这里</p>
      </header>

      <div class="layout">
        <nav class="side">
          <a v-for="item in sections" :key="item.id" :href="'#help-' + item.id">{{ item.label }}</a>
        </nav>

        <div class="body">
          <section id="help-contact" class="sec-block">
            <h2>联系我们</h2>
            <p>下单、物流或退换有任何疑问，先看下方常见问题；没有解决的话，用下面的方式找我们，工作日 9:00–18:00 会逐条回复。</p>
            <ul class="facts">
              <li><span>客服邮箱</span><strong>service@mylyx.store</strong></li>
              <li><span>服务时间</span><strong>周一至周五 9:00–18:00</strong></li>
              <li><span>在线客服</span><strong>登录后在「我的订单」里留言</strong></li>
            </ul>
            <p class="note">为了更快定位，来信请附上订单号。</p>
          </section>

          <section id="help-faq" class="sec-block">
            <h2>常见问题</h2>
            <dl class="faq">
              <dt>多久发货？</dt>
              <dd>现货订单在付款后 48 小时内发出；预售或改衣订单会另外说明时间。</dd>
              <dt>可以修改收货信息吗？</dt>
              <dd>未发货前可以在「我的订单」里联系我们修改；已发货的订单需要联系快递改派。</dd>
              <dt>下单后多久扣库存？</dt>
              <dd>提交订单时即刻扣减。取消订单后库存会自动加回，重新上架可能需要刷新页面。</dd>
              <dt>显示售罄还会补货吗？</dt>
              <dd>常规款通常会补，季节限定款不补。可以在商品页联系我们登记需求。</dd>
              <dt>支持哪些付款方式？</dt>
              <dd>当前为下单后标记付款，实际收款支持微信、支付宝与银行卡。</dd>
              <dt>环保与包装</dt>
              <dd>默认使用可回收纸箱与无塑料包装，如需礼品包装请在订单里备注。</dd>
            </dl>
          </section>

          <section id="help-shipping" class="sec-block">
            <h2>配送说明</h2>
            <ul class="facts">
              <li><span>运费</span><strong>满 ¥600 免运费，未满按实际快递计</strong></li>
              <li><span>发货时效</span><strong>付款后 48 小时内</strong></li>
              <li><span>配送范围</span><strong>全国除港澳台</strong></li>
              <li><span>送达时效</span><strong>省内 1–2 天，跨省 2–4 天</strong></li>
            </ul>
            <p>发货后可以在「我的订单」查看状态。遇到节假日或大促，发货与派送会顺延 1–2 天。</p>
            <p class="note">签收前请检查外包装；有明显破损建议当场拒收，之后的处理会更快。</p>
          </section>

          <section id="help-returns" class="sec-block">
            <h2>退换货</h2>
            <p>签收后 7 天内，保持吊牌与包装完整、未洗涤未穿着，可以申请退换。</p>
            <ol class="steps">
              <li>在「我的订单」里找到对应订单，联系我们说明原因。</li>
              <li>我们确认后给出寄回地址与注意事项。</li>
              <li>寄回并保留快递单号，我们收到后开始检查。</li>
              <li>检查通过后按原路退款，一般 3–5 个工作日到账。</li>
            </ol>
            <p class="note">以下情况不便退换：已洗涤或穿着、吊牌缺失、贴身衣物、明显人为损坏，以及定制改衣后的商品。</p>
          </section>

          <section id="help-size" class="sec-block">
            <h2>尺码指南</h2>
            <p>本站目前多数款式提供 <strong>S</strong> 与 <strong>M</strong>，部分款式另有 L、XL。下单前量准三处即可：胸围、腰围和衣长。</p>
            <div class="table-wrap">
              <table>
                <thead>
                  <tr>
                    <th>尺码</th>
                    <th>胸围</th>
                    <th>腰围</th>
                    <th>衣长</th>
                    <th>建议身高</th>
                    <th>建议体重</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="row in sizeTable" :key="row.size">
                    <td><strong>{{ row.size }}</strong></td>
                    <td>{{ row.chest }}</td>
                    <td>{{ row.waist }}</td>
                    <td>{{ row.length }}</td>
                    <td>{{ row.height }}</td>
                    <td>{{ row.weight }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
            <p class="note">以上为人体净尺寸，单位厘米，允许 1–2 厘米误差。介于两个尺码之间时，偏好合身选小一码，偏好宽松选大一码。</p>
            <p class="how">
              <strong>量体小贴士</strong>：胸围绕过最丰满处一周，软尺贴身但不勒；腰围量腰部最细处，避免饭后测量；衣长从肩缝最高点垂直向下，这一项决定上身效果。
            </p>
          </section>
        </div>
      </div>
    </main>
    <SiteFooter />
  </div>
</template>
<script setup lang="ts">
import SiteHeader from '@/components/SiteHeader.vue'
import SiteFooter from '@/components/SiteFooter.vue'

const sections = [
  { id: 'contact', label: '联系我们' },
  { id: 'faq', label: '常见问题' },
  { id: 'shipping', label: '配送说明' },
  { id: 'returns', label: '退换货' },
  { id: 'size', label: '尺码指南' }
]

const sizeTable = [
  { size: 'S', chest: '82–86', waist: '64–68', length: '56–58', height: '155–162', weight: '45–52 kg' },
  { size: 'M', chest: '86–90', waist: '68–72', length: '58–60', height: '160–168', weight: '50–58 kg' },
  { size: 'L', chest: '90–94', waist: '72–76', length: '60–62', height: '165–172', weight: '55–63 kg' },
  { size: 'XL', chest: '94–98', waist: '76–80', length: '62–64', height: '170–178', weight: '60–70 kg' }
]
</script>
<style scoped>
.head { text-align: center; padding: 46px 0 34px; }
.head .eyebrow { margin: 0 0 12px; }
.layout { display: grid; grid-template-columns: 180px minmax(0, 1fr); gap: 48px; padding-bottom: 60px; }
.side { position: sticky; top: 150px; align-self: start; display: grid; gap: 14px; font-size: 13px; }
.side a { color: var(--muted); padding: 4px 0; }
.side a:hover { color: var(--ink); }
.body { display: grid; gap: 12px; }
.sec-block {
  padding: 30px 0 34px;
  border-top: 1px solid var(--line);
  scroll-margin-top: 150px;
}
.sec-block h2 {
  font-family: var(--serif);
  font-size: 22px;
  font-weight: 400;
  letter-spacing: 0.1em;
  margin: 0 0 18px;
}
.sec-block p { font-size: 14px; line-height: 1.9; color: #3b3733; margin: 0 0 14px; }
.facts { list-style: none; padding: 0; margin: 0 0 14px; display: grid; gap: 10px; }
.facts li { display: grid; grid-template-columns: 100px minmax(0, 1fr); gap: 12px; font-size: 14px; }
.facts span { color: var(--muted); }
.facts strong { font-weight: 500; }
.faq { margin: 0; display: grid; gap: 4px; }
.faq dt { font-size: 14px; font-weight: 500; margin-top: 16px; }
.faq dt:first-child { margin-top: 0; }
.faq dd { font-size: 14px; line-height: 1.9; color: #3b3733; margin: 6px 0 0; }
.steps { margin: 0 0 14px; padding-left: 20px; display: grid; gap: 8px; }
.steps li { font-size: 14px; line-height: 1.9; color: #3b3733; }
.note { font-size: 13px; color: var(--muted); }
.how { font-size: 13px; color: #3b3733; line-height: 1.9; }
.table-wrap { overflow-x: auto; margin: 0 0 16px; }
table { width: 100%; border-collapse: collapse; font-size: 13px; min-width: 560px; }
th, td { border: 1px solid var(--line); padding: 12px 14px; text-align: left; white-space: nowrap; }
th { background: var(--soft); font-weight: 500; letter-spacing: 0.06em; }
@media (max-width: 900px) {
  .layout { grid-template-columns: 1fr; gap: 20px; }
  .side { position: static; display: flex; flex-wrap: wrap; gap: 18px; }
  .sec-block { scroll-margin-top: 110px; }
}
</style>
