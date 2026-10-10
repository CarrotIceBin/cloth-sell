<template>
  <div class="shop">
    <SiteHeader />
    <main class="wrap">
      <header class="head">
        <p class="eyebrow">{{ $t('客户服务') }}</p>
        <h1 class="sec-title">{{ $t('帮助中心') }}</h1>
        <p class="sec-sub">{{ $t('配送、退换与尺码，都在这里') }}</p>
      </header>

      <div class="layout">
        <nav class="side">
          <a
            v-for="item in sections"
            :key="item.id"
            :href="'#help-' + item.id"
            :class="{ active: active === item.id }"
          >{{ $t(item.label) }}</a>
        </nav>

        <div class="body">
          <section id="help-chat" class="sec-block chat-section">
            <div class="chat-heading">
              <div>
                <h2>{{ $t('在线客服') }}</h2>
                <p>{{ $t('关于商品、尺码、配送或退换，随时问我们。') }}</p>
              </div>
              <span v-if="chatMode === 'ai'" class="service-state" :class="{ ready: aiConfigured }">
                <i></i>{{ aiConfigured ? $t('AI 客服在线') : $t('智能问答在线') }}
              </span>
            </div>
            <el-radio-group v-model="chatMode" :aria-label="$t('在线客服')" style="margin-bottom: 16px">
              <el-radio-button value="ai">{{ $t('AI 客服在线') }}</el-radio-button>
              <el-radio-button value="human">{{ $t('人工客服') }}</el-radio-button>
            </el-radio-group>
            <template v-if="chatMode === 'human'">
              <p><router-link to="/chat">{{ $t('chat.title') }}</router-link></p>
              <SupportChatPanel v-if="loggedIn" />
              <router-link v-else to="/login">{{ $t('请先登录') }}</router-link>
            </template>
            <div v-else class="chat-panel">
              <div class="chat-topline">
                <div class="agent-mark">M</div>
                <div><strong>MYLYX {{ $t('客服') }}</strong><span>{{ $t('通常即时回复') }}</span></div>
                <button class="clear-button" type="button" :title="$t('清空对话')" :aria-label="$t('清空对话')" @click="clearChat">{{ $t('清空') }}</button>
              </div>
              <div ref="messageList" class="messages" aria-live="polite">
                <div v-for="(message, index) in messages" :key="index" class="message-row" :class="message.role">
                  <div v-if="message.role === 'assistant'" class="agent-mark small">M</div>
                  <p class="bubble">{{ message.content }}</p>
                </div>
                <div v-if="sending" class="message-row assistant">
                  <div class="agent-mark small">M</div>
                  <p class="bubble typing">{{ $t('正在为你整理答复') }}<span></span><span></span><span></span></p>
                </div>
              </div>
              <div v-if="messages.length === 1" class="suggestions">
                <button v-for="suggestion in suggestions" :key="suggestion" type="button" @click="sendSuggestion(suggestion)">{{ suggestion }}</button>
              </div>
              <form class="composer" @submit.prevent="sendMessage">
                <textarea
                  v-model="draft"
                  rows="1"
                  maxlength="2000"
                  :aria-label="$t('输入你的问题')"
                  :placeholder="$t('输入你的问题…')"
                  :disabled="sending"
                  @keydown.enter.exact.prevent="sendMessage"
                ></textarea>
                <button type="submit" :disabled="sending || !draft.trim()" :aria-label="$t('发送消息')" :title="$t('发送消息')">{{ $t('发送') }}</button>
              </form>
              <div class="chat-foot"><span>{{ $t('AI 回复仅供参考，订单处理请联系人工客服') }}</span><span>{{ draft.length }}/2000</span></div>
            </div>
          </section>

          <section id="help-contact" class="sec-block">
            <h2>{{ $t('联系我们') }}</h2>
            <p>{{ $t('下单、物流或退换有任何疑问，先看下方常见问题；没有解决的话，用下面的方式找我们，工作日 9:00–18:00 会逐条回复。') }}</p>
            <ul class="facts">
              <li><span>{{ $t('客服邮箱') }}</span><strong>service@mylyx.store</strong></li>
              <li><span>{{ $t('服务时间') }}</span><strong>{{ $t('周一至周五 9:00–18:00') }}</strong></li>
              <li><span>{{ $t('在线客服') }}</span><strong>{{ $t('登录后在「我的订单」里留言') }}</strong></li>
            </ul>
            <p class="note">{{ $t('为了更快定位，来信请附上订单号。') }}</p>
          </section>

          <section id="help-faq" class="sec-block">
            <h2>{{ $t('常见问题') }}</h2>
            <dl class="faq">
              <dt>{{ $t('多久发货？') }}</dt>
              <dd>{{ $t('现货订单在付款后 48 小时内发出；预售或改衣订单会另外说明时间。') }}</dd>
              <dt>{{ $t('可以修改收货信息吗？') }}</dt>
              <dd>{{ $t('未发货前可以在「我的订单」里联系我们修改；已发货的订单需要联系快递改派。') }}</dd>
              <dt>{{ $t('下单后多久扣库存？') }}</dt>
              <dd>{{ $t('提交订单时即刻扣减。取消订单后库存会自动加回，重新上架可能需要刷新页面。') }}</dd>
              <dt>{{ $t('显示售罄还会补货吗？') }}</dt>
              <dd>{{ $t('常规款通常会补，季节限定款不补。可以在商品页联系我们登记需求。') }}</dd>
              <dt>{{ $t('支持哪些付款方式？') }}</dt>
              <dd>{{ $t('当前为下单后标记付款，实际收款支持微信、支付宝与银行卡。') }}</dd>
              <dt>{{ $t('环保与包装') }}</dt>
              <dd>{{ $t('默认使用可回收纸箱与无塑料包装，如需礼品包装请在订单里备注。') }}</dd>
            </dl>
          </section>

          <section id="help-shipping" class="sec-block">
            <h2>{{ $t('配送说明') }}</h2>
            <ul class="facts">
              <li><span>{{ $t('运费') }}</span><strong>{{ $t('满 ¥600 免运费，未满按实际快递计') }}</strong></li>
              <li><span>{{ $t('发货时效') }}</span><strong>{{ $t('付款后 48 小时内') }}</strong></li>
              <li><span>{{ $t('配送范围') }}</span><strong>{{ $t('全国除港澳台') }}</strong></li>
              <li><span>{{ $t('送达时效') }}</span><strong>{{ $t('省内 1–2 天，跨省 2–4 天') }}</strong></li>
            </ul>
            <p>{{ $t('发货后可以在「我的订单」查看状态。遇到节假日或大促，发货与派送会顺延 1–2 天。') }}</p>
            <p class="note">{{ $t('签收前请检查外包装；有明显破损建议当场拒收，之后的处理会更快。') }}</p>
          </section>

          <section id="help-returns" class="sec-block">
            <h2>{{ $t('退换货') }}</h2>
            <p>{{ $t('签收后 7 天内，保持吊牌与包装完整、未洗涤未穿着，可以申请退换。') }}</p>
            <ol class="steps">
              <li>{{ $t('在「我的订单」里找到对应订单，联系我们说明原因。') }}</li>
              <li>{{ $t('我们确认后给出寄回地址与注意事项。') }}</li>
              <li>{{ $t('寄回并保留快递单号，我们收到后开始检查。') }}</li>
              <li>{{ $t('检查通过后按原路退款，一般 3–5 个工作日到账。') }}</li>
            </ol>
            <p class="note">{{ $t('以下情况不便退换：已洗涤或穿着、吊牌缺失、贴身衣物、明显人为损坏，以及定制改衣后的商品。') }}</p>
          </section>

          <section id="help-size" class="sec-block">
            <h2>{{ $t('尺码指南') }}</h2>
            <p>{{ $t('本站目前多数款式提供 S 与 M，部分款式另有 L、XL。下单前量准三处即可：胸围、腰围和衣长。') }}</p>
            <div class="table-wrap">
              <table>
                <thead>
                  <tr>
                    <th>{{ $t('尺码') }}</th>
                    <th>{{ $t('胸围') }}</th>
                    <th>{{ $t('腰围') }}</th>
                    <th>{{ $t('衣长') }}</th>
                    <th>{{ $t('建议身高') }}</th>
                    <th>{{ $t('建议体重') }}</th>
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
            <p class="note">{{ $t('以上为人体净尺寸，单位厘米，允许 1–2 厘米误差。介于两个尺码之间时，偏好合身选小一码，偏好宽松选大一码。') }}</p>
            <p class="how">
              <strong>{{ $t('量体小贴士') }}</strong>{{ $t('：胸围绕过最丰满处一周，软尺贴身但不勒；腰围量腰部最细处，避免饭后测量；衣长从肩缝最高点垂直向下，这一项决定上身效果。') }}
            </p>
          </section>
        </div>
      </div>
    </main>
    <SiteFooter />
  </div>
</template>
<script setup lang="ts">
import { nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import SiteHeader from '@/components/SiteHeader.vue'
import SiteFooter from '@/components/SiteFooter.vue'
import { SupportApi, type SupportMessage } from '@/api/mall/client/support'
import { useI18n } from '@/hooks/web'
import { i18n } from '@/i18n'
import SupportChatPanel from '@/components/SupportChatPanel.vue'

const chatMode = ref('ai')
const loggedIn = computed(() => !!localStorage.getItem('userToken'))

const { t } = useI18n()
const sections = [
  { id: 'chat', label: '在线客服' },
  { id: 'contact', label: '联系我们' },
  { id: 'faq', label: '常见问题' },
  { id: 'shipping', label: '配送说明' },
  { id: 'returns', label: '退换货' },
  { id: 'size', label: '尺码指南' }
]

const active = ref(sections[0].id)
const messageList = ref<HTMLElement | null>(null)
const draft = ref('')
const sending = ref(false)
const aiConfigured = ref(false)
const suggestions = computed(() => [t('多久可以发货？'), t('退换货有什么要求？'), t('怎么选择适合的尺码？')])
const messages = ref<SupportMessage[]>([
  { role: 'assistant', content: t('你好，欢迎来到 MYLYX。关于商品、尺码、配送或退换的问题，我都可以帮你看看。') }
])
watch(() => i18n.global.locale.value, () => {
  if (messages.value.length === 1) {
    messages.value = [{
      role: 'assistant',
      content: t('你好，欢迎来到 MYLYX。关于商品、尺码、配送或退换的问题，我都可以帮你看看。')
    }]
  }
})
let ticking = false

async function scrollMessagesToBottom() {
  await nextTick()
  if (messageList.value) messageList.value.scrollTop = messageList.value.scrollHeight
}

async function sendMessage() {
  const content = draft.value.trim()
  if (!content || sending.value) return
  messages.value.push({ role: 'user', content })
  draft.value = ''
  sending.value = true
  await scrollMessagesToBottom()
  try {
    const result = await SupportApi.chat(messages.value.slice(-12))
    aiConfigured.value = result.aiEnabled
    messages.value.push({ role: 'assistant', content: result.reply })
  } catch (error) {
    messages.value.push({
      role: 'assistant',
      content: error instanceof Error ? `${error.message} ${t('如需人工协助，请联系 service@mylyx.store。')}` : t('暂时无法连接客服，请稍后重试。')
    })
  } finally {
    sending.value = false
    await scrollMessagesToBottom()
  }
}

function sendSuggestion(content: string) {
  draft.value = content
  void sendMessage()
}

function clearChat() {
  messages.value = [{ role: 'assistant', content: t('你好，欢迎来到 MYLYX。关于商品、尺码、配送或退换的问题，我都可以帮你看看。') }]
}

function updateActive() {
  ticking = false
  const threshold = window.innerWidth > 900 ? 170 : 130
  let current = sections[0].id
  for (const item of sections) {
    const el = document.getElementById('help-' + item.id)
    if (el && el.getBoundingClientRect().top <= threshold) current = item.id
  }
  active.value = current
}

function onScroll() {
  if (ticking) return
  ticking = true
  requestAnimationFrame(updateActive)
}

onMounted(() => {
  SupportApi.getStatus().then((status) => { aiConfigured.value = status.configured }).catch(() => {})
  window.addEventListener('scroll', onScroll, { passive: true })
  window.addEventListener('resize', onScroll)
  updateActive()
})

onBeforeUnmount(() => {
  window.removeEventListener('scroll', onScroll)
  window.removeEventListener('resize', onScroll)
})

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
.side a.active { color: var(--ink); font-weight: 500; }
.body { display: grid; min-width: 0; gap: 12px; }
.chat-section { border-top: 0; padding-top: 4px; }
.chat-heading { display: flex; align-items: flex-start; justify-content: space-between; gap: 18px; margin-bottom: 18px; }
.chat-heading h2 { font-family: var(--serif); font-size: 24px; font-weight: 400; margin: 0 0 8px; }
.chat-heading p { color: var(--muted); font-size: 13px; margin: 0; }
.service-state { display: inline-flex; align-items: center; gap: 8px; color: #77736d; font-size: 12px; white-space: nowrap; padding-top: 7px; }
.service-state i { width: 7px; height: 7px; border-radius: 50%; background: #c58b55; }
.service-state.ready i { background: #5d9475; }
.chat-panel { border: 1px solid var(--line); background: #fcfbf9; }
.chat-topline { display: flex; align-items: center; gap: 11px; padding: 14px 18px; border-bottom: 1px solid var(--line); background: #fff; }
.agent-mark { width: 34px; height: 34px; display: grid; place-items: center; flex: 0 0 auto; background: #285c4d; color: white; font-family: var(--serif); font-size: 17px; }
.chat-topline strong, .chat-topline span { display: block; }
.chat-topline strong { font-size: 13px; font-weight: 500; }
.chat-topline span { color: var(--muted); font-size: 11px; margin-top: 3px; }
.clear-button { margin-left: auto; padding: 7px 0 7px 12px; border: 0; background: none; color: var(--muted); font-size: 12px; cursor: pointer; }
.clear-button:hover { color: var(--ink); }
.messages { height: 310px; overflow-y: auto; display: flex; flex-direction: column; gap: 16px; padding: 20px; }
.message-row { display: flex; align-items: flex-start; gap: 9px; max-width: 88%; }
.message-row.user { align-self: flex-end; }
.agent-mark.small { width: 25px; height: 25px; font-size: 13px; }
.bubble { max-width: 100%; margin: 0; padding: 11px 14px; background: #fff; border: 1px solid var(--line); color: #34312d; font-size: 13px; line-height: 1.8; white-space: pre-wrap; overflow-wrap: anywhere; }
.message-row.user .bubble { background: #285c4d; border-color: #285c4d; color: #fff; }
.typing { color: var(--muted); }
.typing span { display: inline-block; width: 4px; height: 4px; margin-left: 4px; border-radius: 50%; background: #9c978f; animation: pulse 1s infinite alternate; }
.typing span:nth-child(2) { animation-delay: .2s; }
.typing span:nth-child(3) { animation-delay: .4s; }
.suggestions { display: flex; flex-wrap: wrap; gap: 8px; padding: 0 20px 15px 54px; }
.suggestions button { padding: 7px 10px; border: 1px solid #d8d4ce; border-radius: 2px; background: #fff; color: #55514b; font-size: 11px; cursor: pointer; }
.suggestions button:hover { border-color: #285c4d; color: #285c4d; }
.composer { display: flex; align-items: flex-end; gap: 12px; margin: 0 14px; padding: 13px 0 10px; border-top: 1px solid var(--line); }
.composer textarea { flex: 1; min-width: 0; width: 100%; box-sizing: border-box; min-height: 26px; max-height: 100px; resize: vertical; padding: 5px 0; border: 0; outline: 0; background: transparent; color: var(--ink); font: inherit; font-size: 13px; line-height: 1.6; }
.composer textarea::placeholder { color: #aaa49c; }
.composer button { min-width: 70px; height: 34px; border: 0; background: #285c4d; color: #fff; font-size: 12px; cursor: pointer; }
.composer button:disabled { background: #c5c2bc; cursor: not-allowed; }
.chat-foot { display: flex; justify-content: space-between; gap: 12px; padding: 0 14px 11px; color: #98928a; font-size: 10px; }
@keyframes pulse { to { opacity: .25; } }
.sec-block {
  min-width: 0;
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
@media (max-width: 560px) {
  .chat-heading { flex-direction: column; gap: 8px; }
  .service-state { padding-top: 0; }
  .messages { height: 290px; padding: 15px 12px; }
  .message-row { max-width: 94%; }
  .suggestions { padding-left: 12px; }
}
</style>
