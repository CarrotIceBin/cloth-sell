import { readFile } from 'node:fs/promises'
import { test } from 'node:test'
import assert from 'node:assert/strict'
import vm from 'node:vm'
import { transform } from 'esbuild'

const source = await readFile(new URL('../src/hooks/useSupportSocket.ts', import.meta.url), 'utf8')
const compiled = await transform(source, { loader: 'ts', format: 'cjs' })

function setup(ticket = async () => ({ ticket: 'one-use-ticket' })) {
  const mounted = [], unmounted = [], sockets = [], retries = new Map(), intervals = new Map()
  const storage = new Map([['userToken', 'token']])
  let id = 0
  class FakeSocket {
    static OPEN = 1
    readyState = 1
    sent = []
    constructor(url) { this.url = url; sockets.push(this) }
    send(value) { this.sent.push(value) }
    close() { this.readyState = 3; this.onclose?.() }
  }
  const exports = {}
  const context = vm.createContext({
    exports, module: { exports }, URL, Date, JSON, Math,
    window: { location: { href: 'https://shop.example.test/help' } },
    localStorage: { getItem: key => storage.get(key) }, WebSocket: FakeSocket,
    setTimeout: callback => { retries.set(++id, callback); return id }, clearTimeout: key => retries.delete(key),
    setInterval: callback => { intervals.set(++id, callback); return id }, clearInterval: key => intervals.delete(key),
    require: name => name === 'vue'
      ? { ref: value => ({ value }), shallowRef: value => ({ value }), onMounted: fn => mounted.push(fn), onUnmounted: fn => unmounted.push(fn) }
      : { LiveSupportApi: { ticket } }
  })
  vm.runInContext(compiled.code, context)
  const state = context.module.exports.useSupportSocket(false)
  return { state, mounted, unmounted, sockets, retries, intervals, storage }
}

test('uses a short-lived ticket over wss, handles presence and messages, and cleans up', async () => {
  const env = setup()
  await env.mounted[0]()
  const socket = env.sockets[0]
  assert.equal(socket.url.protocol, 'wss:')
  assert.equal(socket.url.searchParams.get('ticket'), 'one-use-ticket')
  assert.equal(socket.url.toString().includes('userToken'), false)
  socket.onopen()
  assert.equal(env.state.connected.value, true)
  socket.onmessage({ data: JSON.stringify({ type: 'presence', data: true }) })
  assert.equal(env.state.online.value, true)
  socket.onmessage({ data: JSON.stringify({ type: 'message', data: { id: '9007199254740993' } }) })
  assert.equal(env.state.message.value.id, '9007199254740993')
  socket.onmessage({ data: JSON.stringify({ type: 'roomMessage', data: { id: '2', roomId: '11', content: 'group message' } }) })
  assert.equal(env.state.roomEvent.value.roomId, '11')
  assert.equal(env.state.message.value.id, '9007199254740993')
  env.intervals.values().next().value()
  assert.deepEqual(socket.sent, ['ping'])
  env.unmounted[0]()
  assert.equal(socket.readyState, 3)
  assert.equal(env.intervals.size, 0)
  assert.equal(env.retries.size, 0)
})

test('reconnect requests a fresh ticket and stops when the login is cleared', async () => {
  let calls = 0
  const env = setup(async () => ({ ticket: `ticket-${++calls}` }))
  await env.mounted[0]()
  env.sockets[0].onopen()
  env.sockets[0].close()
  assert.equal(env.state.online.value, false)
  await env.retries.values().next().value()
  assert.equal(calls, 2)
  assert.equal(env.sockets[1].url.searchParams.get('ticket'), 'ticket-2')
  env.storage.clear()
  env.sockets[1].close()
  assert.equal(env.state.error.value, '请先登录')
  env.unmounted[0]()
})

test('unmount while fetching a ticket never opens a late socket', async () => {
  let resolve
  const env = setup(() => new Promise(done => { resolve = done }))
  const connecting = env.mounted[0]()
  env.unmounted[0]()
  resolve({ ticket: 'late' })
  await connecting
  assert.equal(env.sockets.length, 0)
  assert.equal(env.retries.size, 0)
})
