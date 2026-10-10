<template>
  <div>
    <SiteHeader v-if="!admin" />
    <main class="chat-workspace">
      <h1>{{ $t('chat.title') }}</h1>
      <p v-if="self">{{ $t('chat.myAccount') }}：{{ self.role === 'ADMIN' ? $t('客服') : $t('买家') }} #{{ self.id }}</p>
      <p role="status">{{ connected ? $t('已连接') : $t('连接中断，正在重连') }}</p>
      <p v-if="error || socketError" role="alert">{{ error || socketError }}</p>
      <el-button @click="creating = !creating">{{ $t('chat.new') }}</el-button>
      <el-button :disabled="loading" @click="refresh">{{ $t('刷新') }}</el-button>
      <el-form v-if="creating" class="create-form" label-position="top" @submit.prevent="create">
        <el-form-item :label="$t('chat.type')">
          <el-select v-model="type">
            <el-option :label="$t('chat.direct')" value="DIRECT" />
            <el-option :label="$t('chat.broadcast')" value="BROADCAST" />
            <el-option v-if="admin" :label="$t('chat.inbox')" value="INBOX" />
            <el-option :label="$t('chat.group')" value="GROUP" />
          </el-select>
        </el-form-item>
        <p v-if="type === 'INBOX'">{{ $t('chat.inboxHint') }}</p>
        <el-form-item v-if="type === 'GROUP' || type === 'BROADCAST'" :label="$t('chat.name')">
          <el-input v-model="title" maxlength="80" />
        </el-form-item>
        <p>{{ $t('chat.memberHint') }}</p>
        <div v-for="(member, index) in members" :key="index" class="member-form">
          <el-select v-model="member.role" :aria-label="$t('chat.accountType')">
            <el-option :label="$t('买家')" value="USER" />
            <el-option :label="$t('客服')" value="ADMIN" />
          </el-select>
          <el-input v-model="member.id" :aria-label="$t('chat.accountId')" :placeholder="$t('chat.accountId')" inputmode="numeric" />
          <el-button v-if="members.length > 1" @click="members.splice(index, 1)">{{ $t('删除') }}</el-button>
        </div>
        <el-button v-if="type !== 'DIRECT' && members.length < 49" @click="members.push({ role: 'USER', id: '' })">{{ $t('chat.addMember') }}</el-button>
        <el-button type="primary" :loading="creatingRoom" @click="create">{{ $t('chat.create') }}</el-button>
      </el-form>
      <div class="rooms">
        <section :aria-label="$t('会话列表')">
          <button v-for="item in rooms" :key="item.id" class="room" :class="{ selected: selected === item.id }" @click="selected = item.id">
            <strong>{{ item.title }}</strong>
            <small>{{ $t(typeLabels[item.type]) }} · {{ item.members.length }} {{ $t('chat.members') }}</small>
            <span>{{ item.lastContent || $t('暂无消息') }}</span>
          </button>
          <p v-if="!rooms.length">{{ $t('暂无消息') }}</p>
          <el-button v-if="hasMore" :disabled="loading" @click="load(false)">{{ $t('加载更多') }}</el-button>
        </section>
        <section v-if="selectedRoom">
          <h2>{{ selectedRoom.title }}</h2>
          <details><summary>{{ $t('chat.members') }}</summary>
            <p v-for="member in selectedRoom.members" :key="member.role + member.id">{{ member.name }} ·
              {{ member.role === 'ADMIN' ? $t('客服') : $t('买家') }} #{{ member.id }}</p>
          </details>
          <SupportChatPanel :key="selectedRoom.id" :admin="admin" :room="selectedRoom" :self="self" :connected="connected"
            :generation="generation" :room-event="roomEvent" :socket-error="socketError" @room-sent="onMessage" />
        </section>
        <p v-else>{{ $t('chat.select') }}</p>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import SiteHeader from '@/components/SiteHeader.vue'
import SupportChatPanel from '@/components/SupportChatPanel.vue'
import { ChatIdentity, ChatRoom, RoomMessage, chatApi } from '@/api/mall/chat'
import { useSupportSocket } from '@/hooks/useSupportSocket'
import { useI18n } from '@/hooks/web'
const { t } = useI18n()
const route = useRoute()
const admin = route.path.startsWith('/admin')
const api = chatApi(admin)
const { connected, generation, roomEvent, error: socketError } = useSupportSocket(admin)
const self = ref<ChatIdentity>()
const rooms = ref<ChatRoom[]>([])
const selected = ref<string>()
const selectedRoom = computed(() => rooms.value.find(room => room.id === selected.value))
const typeLabels = { DIRECT: 'chat.direct', BROADCAST: 'chat.broadcast', GROUP: 'chat.group' }
const type = ref('DIRECT')
const title = ref('')
const members = ref<ChatIdentity[]>([{ role: admin ? 'USER' : 'ADMIN', id: '' }])
const creating = ref(false)
const creatingRoom = ref(false)
const loading = ref(false)
const hasMore = ref(false)
const error = ref('')
let page = 1
let alive = true
let refreshAgain = false

function sortRooms() {
  rooms.value.sort((a, b) => b.lastMessageId.length - a.lastMessageId.length || b.lastMessageId.localeCompare(a.lastMessageId)
    || b.id.length - a.id.length || b.id.localeCompare(a.id))
}
function mergeRoom(room: ChatRoom) {
  const previous = rooms.value.find(item => item.id === room.id)
  if (previous && (previous.lastMessageId.length > room.lastMessageId.length ||
      (previous.lastMessageId.length === room.lastMessageId.length && previous.lastMessageId > room.lastMessageId))) return
  rooms.value = [room, ...rooms.value.filter(item => item.id !== room.id)]
  sortRooms()
}
async function onMessage(message: RoomMessage) {
  if (!alive) return
  const room = rooms.value.find(item => item.id === message.roomId)
  if (!room) {
    try { const room = await api.room(message.roomId); if (alive) mergeRoom(room) }
    catch (cause) { if (alive) error.value = cause instanceof Error ? cause.message : t('请求失败') }
    return
  }
  mergeRoom({ ...room, lastMessageId: message.id, lastContent: message.content })
}
async function load(reset: boolean) {
  if (loading.value) { if (reset) refreshAgain = true; return }
  loading.value = true
  try {
    const nextPage = reset ? 1 : page + 1
    const rows = await api.rooms(nextPage)
    if (!alive) return
    rows.forEach(mergeRoom)
    page = nextPage
    hasMore.value = rows.length === 50
    error.value = ''
  } catch (cause) { if (alive) error.value = cause instanceof Error ? cause.message : t('请求失败') }
  finally {
    loading.value = false
    if (alive && refreshAgain) { refreshAgain = false; void load(true) }
  }
}
const refresh = () => load(true)
async function create() {
  if (creatingRoom.value) return
  const participants = type.value === 'DIRECT' ? members.value.slice(0, 1) : members.value
  if (participants.some(member => !/^[1-9]\d*$/.test(member.id.trim()))) { error.value = t('chat.invalidId'); return }
  creatingRoom.value = true
  try {
    const created = await api.create(type.value, title.value, participants.map(member => ({ ...member, id: member.id.trim() })))
    if (!alive) return
    created.forEach(mergeRoom)
    selected.value = created[0]?.id
    creating.value = false
    error.value = ''
  } catch (cause) { if (alive) error.value = cause instanceof Error ? cause.message : t('请求失败') }
  finally { creatingRoom.value = false }
}
watch(generation, refresh)
watch(roomEvent, event => {
  if (!event) return
  if ('id' in event) void onMessage(event)
  else void refresh()
})
onMounted(async () => {
  try { self.value = await api.me(); await refresh() }
  catch (cause) { if (alive) error.value = cause instanceof Error ? cause.message : t('请求失败') }
})
onUnmounted(() => { alive = false })
</script>

<style scoped>
.chat-workspace { max-width: 1100px; margin: 0 auto; padding: 24px; }
.rooms { display: grid; grid-template-columns: 280px minmax(0, 1fr); gap: 24px; margin-top: 20px; }
.rooms > section:first-child { max-height: 600px; overflow: auto; }
.room { display: flex; flex-direction: column; gap: 6px; width: 100%; padding: 12px; margin-bottom: 8px; border: 1px solid #e7e5e4; border-radius: 8px; background: #fff; cursor: pointer; text-align: left; }
.room.selected { border-color: #44403c; background: #fafaf9; }
.room span { white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.room small { color: #78716c; }
.create-form { margin-top: 20px; padding: 16px; border: 1px solid #e7e5e4; border-radius: 8px; }
.member-form { display: flex; gap: 8px; margin-bottom: 12px; }
.member-form .el-select { width: 140px; flex-shrink: 0; }
details { margin-bottom: 12px; }
@media (max-width: 768px) { .rooms { grid-template-columns: 1fr; } .rooms > section:first-child { max-height: 220px; } .chat-workspace { padding: 16px; } }
</style>
