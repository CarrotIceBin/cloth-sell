<template>
  <div class="live-chat">
    <p role="status">{{ connectedState ? $t('已连接') : $t('连接中断，正在重连') }}
      <template v-if="!room"> · {{ onlineState ? $t('人工客服在线') : $t('客服离线，可以留言') }}</template></p>
    <p v-if="socketError" role="alert">{{ socketError }}</p>
    <button v-if="hasOlder" type="button" :disabled="loading" @click="loadOlder">{{ $t('加载更多') }}</button>
    <div ref="list" class="chat-messages" aria-live="polite" :aria-label="$t('聊天记录')">
      <div v-for="item in messages" :key="item.id" class="chat-message" :class="{ mine: isMine(item) }">
        <small>{{ !room && item.senderRole === 'ADMIN' ? $t('客服') : item.senderName }} · {{ item.createTime?.replace('T', ' ') }}</small>
        <p>{{ item.content }}</p>
      </div>
      <p v-if="!messages.length && !loading">{{ $t('暂无消息') }}</p>
    </div>
    <p v-if="error" role="alert">{{ error }} <button type="button" :disabled="loading" @click="sync">{{ $t('刷新') }}</button></p>
    <p v-if="room && !room.canSend">{{ $t('chat.receiveOnly') }}</p>
    <form v-else @submit.prevent="send">
      <textarea v-model="draft" maxlength="2000" rows="3" :disabled="sending" :aria-label="$t('输入你的问题')"
        :placeholder="$t('输入你的问题…')" @keydown.enter.exact.prevent="send" />
      <button type="submit" :disabled="sending || !draft.trim()">{{ sending ? $t('发送中') : $t('发送') }}</button>
    </form>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, onUnmounted, ref, watch } from 'vue'
import { ChatMessage, LiveSupportApi } from '@/api/mall/support'
import { ChatIdentity, ChatRoom, RoomEvent, RoomMessage, chatApi } from '@/api/mall/chat'
import { useSupportSocket } from '@/hooks/useSupportSocket'
const props = defineProps<{ admin?: boolean; userId?: string; connected?: boolean; online?: boolean;
  generation?: number; event?: ChatMessage; socketError?: string; room?: ChatRoom; self?: ChatIdentity; roomEvent?: RoomEvent }>()
const emit = defineEmits<{ sent: [message: ChatMessage]; roomSent: [message: RoomMessage] }>()
const live = props.admin || props.room ? null : useSupportSocket(false)
const connectedState = computed(() => live ? live.connected.value : props.connected)
const onlineState = computed(() => live ? live.online.value : props.online)
const socketError = computed(() => live ? live.error.value : props.socketError)
type Message = ChatMessage | RoomMessage
const messages = ref<Message[]>([])
const draft = ref('')
const error = ref('')
const sending = ref(false)
const loading = ref(false)
const hasOlder = ref(false)
const list = ref<HTMLElement>()
let alive = true
let syncing = false
let initialized = false
let resync = false
let cursor: string | undefined
let pending: { clientId: string; content: string } | undefined

function isMine(item: Message) {
  return props.room && 'senderId' in item ? props.self?.role === item.senderRole && props.self?.id === item.senderId
    : !!props.admin === (item.senderRole === 'ADMIN')
}
function history(cursor: { before?: string; after?: string } = {}): Promise<Message[]> {
  return props.room ? chatApi(!!props.admin).history(props.room.id, cursor)
    : LiveSupportApi.history(!!props.admin, props.userId, cursor)
}
function merge(rows: Message[]) {
  if (!alive) return
  const byId = new Map(messages.value.map(row => [row.id, row]))
  rows.forEach(row => byId.set(row.id, row))
  messages.value = [...byId.values()].sort((a, b) => a.id.length - b.id.length || a.id.localeCompare(b.id))
}

async function bottom() {
  await nextTick()
  if (list.value) list.value.scrollTop = list.value.scrollHeight
}

async function sync() {
  if (!alive) return
  if (syncing) { resync = true; return }
  syncing = true
  loading.value = true
  try {
    if (!initialized) {
      const rows = await history()
      hasOlder.value = rows.length === 50
      merge(rows)
      initialized = rows.length > 0
      cursor = rows.at(-1)?.id
    } else {
      while (alive) {
        const rows = await history({ after: cursor })
        merge(rows)
        cursor = rows.at(-1)?.id || cursor
        if (rows.length < 50) break
      }
    }
    if (alive) error.value = ''
    await bottom()
  } catch (cause) {
    if (alive) error.value = cause instanceof Error ? cause.message : '暂时无法连接客服，请稍后重试。'
  } finally {
    loading.value = false
    syncing = false
    if (resync && alive) { resync = false; void sync() }
  }
}

async function loadOlder() {
  loading.value = true
  try {
    const height = list.value?.scrollHeight || 0
    const rows = await history({ before: messages.value[0]?.id })
    merge(rows)
    hasOlder.value = rows.length === 50
    await nextTick()
    if (list.value) list.value.scrollTop += list.value.scrollHeight - height
  } catch (cause) { error.value = cause instanceof Error ? cause.message : '请求失败' }
  finally { loading.value = false }
}

async function send() {
  const content = draft.value.trim()
  if (!content || sending.value) return
  if (!pending || pending.content !== content) pending = { content, clientId: crypto.randomUUID() }
  sending.value = true
  try {
    const row = props.room ? await chatApi(!!props.admin).send(props.room.id, pending.clientId, content)
      : await LiveSupportApi.send(!!props.admin, props.userId, pending.clientId, content)
    if (!alive) return
    merge([row])
    draft.value = ''
    pending = undefined
    error.value = ''
    if ('roomId' in row) emit('roomSent', row)
    else emit('sent', row)
    await bottom()
  } catch (cause) { if (alive) error.value = cause instanceof Error ? cause.message : '请求失败' }
  finally { sending.value = false }
}

watch(() => live ? live.generation.value : props.generation, sync)
watch(() => live ? live.message.value : props.event, row => {
  if (!props.room && row && (!props.admin || row.userId === props.userId)) { merge([row]); void bottom() }
})
watch(() => props.roomEvent, event => {
  if (event && 'id' in event && event.roomId === props.room?.id) { merge([event]); void bottom() }
})
onMounted(sync)
onUnmounted(() => { alive = false })
</script>

<style scoped>
.live-chat { border: 1px solid #e7e5e4; border-radius: 12px; padding: 16px; }
.chat-messages { height: 350px; overflow-y: auto; padding: 12px; background: #fafaf9; }
.chat-message { margin: 12px 0; max-width: 85%; }
.chat-message.mine { margin-left: auto; text-align: right; }
.chat-message small { color: #78716c; }
.chat-message p { background: #fff; border: 1px solid #e7e5e4; border-radius: 8px; padding: 12px; white-space: pre-wrap; overflow-wrap: anywhere; text-align: left; }
.chat-message.mine p { background: #f0fdf4; }
form { display: flex; gap: 12px; margin-top: 12px; }
textarea { flex: 1; min-width: 0; resize: vertical; border: 1px solid #d6d3d1; border-radius: 6px; padding: 10px; font: inherit; }
button { cursor: pointer; border: 1px solid #d6d3d1; border-radius: 6px; background: #fff; padding: 8px 14px; }
button:disabled { cursor: default; opacity: .5; }
</style>
