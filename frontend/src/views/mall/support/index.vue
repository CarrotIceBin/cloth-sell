<template>
  <ContentWrap>
    <h2>{{ $t('人工客服') }}</h2>
    <p role="status">{{ connected ? $t('已连接') : $t('连接中断，正在重连') }}</p>
    <p v-if="error" role="alert">{{ error }}</p>
    <div class="workspace">
      <section :aria-label="$t('会话列表')">
        <el-button :disabled="loading" @click="refresh">{{ $t('刷新') }}</el-button>
        <button v-for="item in conversations" :key="item.userId" class="conversation"
          :class="{ selected: selected === item.userId }" @click="selected = item.userId">
          <strong>{{ item.buyerName || $t('买家') }} #{{ item.userId }}</strong>
          <span>{{ item.content }}</span><small>{{ item.createTime?.replace('T', ' ') }}</small>
        </button>
        <p v-if="!conversations.length">{{ $t('暂无消息') }}</p>
        <el-button v-if="hasMore" :disabled="loading" @click="loadMore">{{ $t('加载更多') }}</el-button>
      </section>
      <SupportChatPanel v-if="selected" :key="selected" admin :user-id="selected" :connected="connected" :online="online"
        :generation="generation" :event="message" :socket-error="socketError" @sent="update" />
      <p v-else>{{ $t('请选择买家会话') }}</p>
    </div>
  </ContentWrap>
</template>
<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import ContentWrap from '@/components/ContentWrap/index.vue'
import SupportChatPanel from '@/components/SupportChatPanel.vue'
import { ChatMessage, LiveSupportApi } from '@/api/mall/support'
import { useSupportSocket } from '@/hooks/useSupportSocket'
const { connected, online, generation, message, error: socketError } = useSupportSocket(true)
const conversations = ref<ChatMessage[]>([])
const selected = ref<string>()
const loading = ref(false)
const hasMore = ref(false)
const error = ref('')
let page = 1

function update(row: ChatMessage) {
  const previous = conversations.value.find(item => item.userId === row.userId)
  if (previous && (previous.id.length > row.id.length || (previous.id.length === row.id.length && previous.id > row.id))) return
  row = { ...row, buyerName: row.buyerName || (row.senderRole === 'USER' ? row.senderName : previous?.buyerName) }
  conversations.value = [row, ...conversations.value.filter(item => item.userId !== row.userId)]
    .sort((a, b) => b.id.length - a.id.length || b.id.localeCompare(a.id))
}
async function load(reset: boolean) {
  if (loading.value) return
  loading.value = true
  try {
    const nextPage = reset ? 1 : page + 1
    const rows = await LiveSupportApi.conversations(nextPage)
    rows.forEach(update)
    page = nextPage
    hasMore.value = rows.length === 50
    error.value = ''
  } catch (cause) { error.value = cause instanceof Error ? cause.message : '请求失败' }
  finally { loading.value = false }
}
const refresh = () => load(true)
const loadMore = () => load(false)
watch(message, row => { if (row) update(row) })
watch(generation, refresh)
onMounted(refresh)
</script>
<style scoped>
.workspace { display: grid; grid-template-columns: 280px minmax(0, 1fr); gap: 20px; }
.workspace section { max-height: 560px; overflow: auto; }
.conversation { display: flex; flex-direction: column; gap: 6px; width: 100%; text-align: left; padding: 12px; margin-top: 8px; background: #fff; border: 1px solid #e7e5e4; border-radius: 6px; cursor: pointer; }
.conversation.selected { border-color: #44403c; background: #fafaf9; }
.conversation span { white-space: nowrap; overflow: hidden; text-overflow: ellipsis; max-width: 100%; }
.conversation small { color: #78716c; }
@media (max-width: 768px) { .workspace { grid-template-columns: 1fr; } .workspace section { max-height: 200px; } }
</style>
