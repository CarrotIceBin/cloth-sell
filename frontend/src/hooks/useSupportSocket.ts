import { onMounted, onUnmounted, ref, shallowRef } from 'vue'
import { ChatMessage, LiveSupportApi } from '@/api/mall/support'
import type { RoomEvent } from '@/api/mall/chat'

export function useSupportSocket(admin: boolean) {
  const connected = ref(false)
  const online = ref(false)
  const generation = ref(0)
  const message = shallowRef<ChatMessage>()
  const roomEvent = shallowRef<RoomEvent>()
  const error = ref('')
  let socket: WebSocket | undefined
  let stopped = false
  let retry: ReturnType<typeof setTimeout> | undefined
  let heartbeat: ReturnType<typeof setInterval> | undefined
  let attempts = 0
  let lastPong = 0
  const tokenKey = admin ? 'adminToken' : 'userToken'

  function clearHeartbeat() {
    clearInterval(heartbeat)
    heartbeat = undefined
  }

  function schedule() {
    if (stopped) return
    if (!localStorage.getItem(tokenKey)) {
      error.value = '请先登录'
      return
    }
    clearTimeout(retry)
    retry = setTimeout(connect, Math.min(1000 * 2 ** attempts++, 30000))
  }

  async function connect() {
    if (stopped || !localStorage.getItem(tokenKey)) { error.value = '请先登录'; return }
    try {
      const ticket = await LiveSupportApi.ticket(admin)
      if (stopped) return
      const url = new URL('/mall/support/ws', window.location.href)
      url.protocol = url.protocol === 'https:' ? 'wss:' : 'ws:'
      url.searchParams.set('ticket', ticket.ticket)
      const current = new WebSocket(url)
      socket = current
      current.onopen = () => {
        if (stopped) { current.close(); return }
        connected.value = true
        error.value = ''
        attempts = 0
        lastPong = Date.now()
        generation.value++
        clearHeartbeat()
        heartbeat = setInterval(() => {
          if (!localStorage.getItem(tokenKey) || Date.now() - lastPong > 45000) current.close()
          else if (current.readyState === WebSocket.OPEN) current.send('ping')
        }, 20000)
      }
      current.onmessage = (event) => {
        if (stopped) return
        if (event.data === 'pong') { lastPong = Date.now(); return }
        try {
          const payload = JSON.parse(event.data)
          if (payload.type === 'presence') online.value = payload.data === true
          if (payload.type === 'message') message.value = payload.data
          if (payload.type === 'roomMessage') roomEvent.value = payload.data
        } catch { current.close() }
      }
      current.onerror = () => current.close()
      current.onclose = () => {
        connected.value = false
        online.value = false
        clearHeartbeat()
        schedule()
      }
    } catch (cause) {
      error.value = cause instanceof Error ? cause.message : '暂时无法连接客服，请稍后重试。'
      schedule()
    }
  }

  onMounted(connect)
  onUnmounted(() => {
    stopped = true
    clearTimeout(retry)
    clearHeartbeat()
    socket?.close()
  })
  return { connected, online, generation, message, roomEvent, error }
}
