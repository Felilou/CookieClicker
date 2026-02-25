<script setup lang="ts">
definePageMeta({ ssr: false })

// ─── Shared state ──────────────────────────────────────────────────────────────
const token = ref('')
const log = ref<Array<{ section: string; ok: boolean; data: string }>>([])

function addLog(section: string, data: unknown, ok = true) {
  log.value.unshift({ section, ok, data: JSON.stringify(data, null, 2) })
  if (log.value.length > 30) log.value.pop()
}

// ─── Auth Service :8081 ────────────────────────────────────────────────────────
const reg = reactive({ username: '', email: '', password: '', firstName: '', lastName: '' })
const loginForm = reactive({ username: '', password: '' })

async function doRegister() {
  try {
    const res = await $fetch('/auth/register', { baseURL: 'http://localhost:8081', method: 'POST', body: reg })
    addLog('Register', res)
  } catch (e: any) { addLog('Register', e.data ?? e.message, false) }
}

async function doLogin() {
  try {
    const res = await $fetch<any>('/auth/login', { baseURL: 'http://localhost:8081', method: 'POST', body: loginForm })
    token.value = res.access_token ?? ''
    addLog('Login', res)
  } catch (e: any) { addLog('Login', e.data ?? e.message, false) }
}

async function doValidate() {
  try {
    const res = await $fetch('/auth/validate', { baseURL: 'http://localhost:8081', headers: { Authorization: `Bearer ${token.value}` } })
    addLog('Validate', res)
  } catch (e: any) { addLog('Validate', e.data ?? e.message, false) }
}

// ─── Persistence Service :8082 ─────────────────────────────────────────────────
const gameForm = reactive({ player1Name: '', player2Name: '', player1Score: 0, player2Score: 0 })

async function getStats() {
  try {
    const res = await $fetch('/api/stats', { baseURL: 'http://localhost:8082' })
    addLog('Stats', res)
  } catch (e: any) { addLog('Stats', e.data ?? e.message, false) }
}

async function addGame() {
  try {
    await $fetch('/api/stats/game', { baseURL: 'http://localhost:8082', method: 'POST', body: gameForm })
    addLog('Add Game', 'Created')
  } catch (e: any) { addLog('Add Game', e.data ?? e.message, false) }
}

async function getMyGames() {
  try {
    const res = await $fetch('/api/stats/my-games', { baseURL: 'http://localhost:8082', headers: { Authorization: `Bearer ${token.value}` } })
    addLog('My Games', res)
  } catch (e: any) { addLog('My Games', e.data ?? e.message, false) }
}

// ─── Game Service :8080 (WebSocket / STOMP) ────────────────────────────────────
const roomId = ref('')
const wsUsername = ref('')
const wsStatus = ref<'disconnected' | 'connecting' | 'connected'>('disconnected')
let stompClient: any = null

async function connectAndCreate() {
  const { Client } = await import('@stomp/stompjs')
  const SockJS = (await import('sockjs-client')).default

  wsStatus.value = 'connecting'
  stompClient = new Client({
    webSocketFactory: () => new SockJS('http://localhost:8080/ws'),
    onConnect: () => {
      wsStatus.value = 'connected'
      stompClient.subscribe(`/room/${roomId.value}`, (msg: any) => {
        addLog('Room Update', JSON.parse(msg.body))
      })
      stompClient.publish({ destination: `/game/createRoom/${roomId.value}` })
      addLog('Create Room', `Room "${roomId.value}" created, subscribed to /room/${roomId.value}`)
    },
    onDisconnect: () => { wsStatus.value = 'disconnected' },
    onStompError: (frame: any) => { addLog('WS Error', frame.headers?.message ?? frame, false) },
  })
  stompClient.activate()
}

function sendAction(action: 'JOIN' | 'LEAVE') {
  if (!stompClient?.connected) return
  stompClient.publish({
    destination: `/game/room/${roomId.value}`,
    body: JSON.stringify({ username: wsUsername.value, action }),
  })
}

function wsDisconnect() {
  stompClient?.deactivate()
  stompClient = null
  wsStatus.value = 'disconnected'
}

onUnmounted(wsDisconnect)
</script>

<template>
  <div class="min-h-screen bg-gray-950 text-gray-100 p-6">
    <div class="max-w-7xl mx-auto space-y-4">

      <!-- Header + token bar -->
      <div class="flex items-center justify-between">
        <h1 class="text-xl font-bold text-white">CockieClicker Testbench</h1>
        <span class="text-xs text-gray-500">Services must be running locally</span>
      </div>
      <div class="flex items-center gap-3 rounded-lg bg-gray-900 border border-gray-800 px-4 py-2 text-sm">
        <span class="text-gray-500 shrink-0">Token:</span>
        <span v-if="token" class="text-green-400 truncate flex-1 font-mono text-xs">{{ token }}</span>
        <span v-else class="text-gray-600 italic flex-1">— login to store token —</span>
        <UButton v-if="token" size="xs" color="neutral" variant="ghost" @click="token = ''">Clear</UButton>
      </div>

      <!-- Three service columns -->
      <div class="grid grid-cols-1 md:grid-cols-3 gap-4">

        <!-- ── Auth Service ── -->
        <div class="rounded-xl bg-gray-900 border border-gray-800 p-5 space-y-4">
          <h2 class="text-xs font-semibold text-blue-400 uppercase tracking-widest">Auth Service :8081</h2>

          <div class="space-y-2">
            <p class="text-xs text-gray-600">POST /auth/register</p>
            <UInput v-model="reg.username" size="sm" placeholder="username" />
            <UInput v-model="reg.email" size="sm" placeholder="email" />
            <UInput v-model="reg.password" size="sm" type="password" placeholder="password" />
            <UInput v-model="reg.firstName" size="sm" placeholder="firstName" />
            <UInput v-model="reg.lastName" size="sm" placeholder="lastName" />
            <UButton size="sm" block @click="doRegister">Register</UButton>
          </div>

          <hr class="border-gray-800" />

          <div class="space-y-2">
            <p class="text-xs text-gray-600">POST /auth/login</p>
            <UInput v-model="loginForm.username" size="sm" placeholder="username" />
            <UInput v-model="loginForm.password" size="sm" type="password" placeholder="password" />
            <UButton size="sm" block color="primary" @click="doLogin">Login → store token</UButton>
          </div>

          <hr class="border-gray-800" />

          <div class="space-y-2">
            <p class="text-xs text-gray-600">GET /auth/validate</p>
            <UButton size="sm" block variant="outline" :disabled="!token" @click="doValidate">Validate token</UButton>
          </div>
        </div>

        <!-- ── Persistence Service ── -->
        <div class="rounded-xl bg-gray-900 border border-gray-800 p-5 space-y-4">
          <h2 class="text-xs font-semibold text-purple-400 uppercase tracking-widest">Persistence Service :8082</h2>

          <div class="space-y-2">
            <p class="text-xs text-gray-600">GET /api/stats</p>
            <UButton size="sm" block @click="getStats">Get total stats</UButton>
          </div>

          <hr class="border-gray-800" />

          <div class="space-y-2">
            <p class="text-xs text-gray-600">POST /api/stats/game</p>
            <div class="grid grid-cols-2 gap-2">
              <UInput v-model="gameForm.player1Name" size="sm" placeholder="player1" />
              <UInput v-model="gameForm.player2Name" size="sm" placeholder="player2" />
              <UInput v-model.number="gameForm.player1Score" size="sm" type="number" placeholder="score 1" />
              <UInput v-model.number="gameForm.player2Score" size="sm" type="number" placeholder="score 2" />
            </div>
            <UButton size="sm" block @click="addGame">Add game</UButton>
          </div>

          <hr class="border-gray-800" />

          <div class="space-y-2">
            <p class="text-xs text-gray-600">GET /api/stats/my-games (needs token)</p>
            <UButton size="sm" block variant="outline" :disabled="!token" @click="getMyGames">Get my games</UButton>
          </div>
        </div>

        <!-- ── Game Service WebSocket ── -->
        <div class="rounded-xl bg-gray-900 border border-gray-800 p-5 space-y-4">
          <div class="flex items-center justify-between">
            <h2 class="text-xs font-semibold text-green-400 uppercase tracking-widest">Game Service :8080</h2>
            <span
              class="text-xs font-mono"
              :class="wsStatus === 'connected' ? 'text-green-400' : wsStatus === 'connecting' ? 'text-yellow-400' : 'text-gray-600'"
            >● {{ wsStatus }}</span>
          </div>

          <div class="space-y-2">
            <p class="text-xs text-gray-600">WebSocket → createRoom</p>
            <UInput v-model="roomId" size="sm" placeholder="Room ID (e.g. room-1)" />
            <UButton size="sm" block color="primary" :disabled="wsStatus !== 'disconnected' || !roomId" @click="connectAndCreate">
              Connect + Create Room
            </UButton>
            <UButton size="sm" block color="neutral" variant="outline" :disabled="wsStatus === 'disconnected'" @click="wsDisconnect">
              Disconnect
            </UButton>
          </div>

          <hr class="border-gray-800" />

          <div class="space-y-2">
            <p class="text-xs text-gray-600">Send JOIN / LEAVE to room</p>
            <UInput v-model="wsUsername" size="sm" placeholder="username" />
            <div class="grid grid-cols-2 gap-2">
              <UButton size="sm" :disabled="wsStatus !== 'connected' || !wsUsername" @click="sendAction('JOIN')">Join</UButton>
              <UButton size="sm" color="neutral" variant="outline" :disabled="wsStatus !== 'connected' || !wsUsername" @click="sendAction('LEAVE')">Leave</UButton>
            </div>
          </div>
        </div>
      </div>

      <!-- Response Log -->
      <div class="rounded-xl bg-gray-900 border border-gray-800 p-5">
        <div class="flex items-center justify-between mb-3">
          <h2 class="text-xs font-semibold text-gray-500 uppercase tracking-widest">Response Log</h2>
          <UButton size="xs" color="neutral" variant="ghost" @click="log = []">Clear</UButton>
        </div>
        <p v-if="!log.length" class="text-sm text-gray-700 italic">No responses yet.</p>
        <div v-for="(entry, i) in log" :key="i" class="mb-3 text-xs">
          <span :class="entry.ok ? 'text-green-400' : 'text-red-400'" class="font-semibold">{{ entry.section }}</span>
          <pre class="mt-1 p-2 rounded bg-gray-950 border border-gray-800 text-gray-300 overflow-x-auto whitespace-pre-wrap font-mono">{{ entry.data }}</pre>
        </div>
      </div>

    </div>
  </div>
</template>
