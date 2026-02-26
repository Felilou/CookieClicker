<script setup lang="ts">
definePageMeta({ ssr: false })

import type { GameState } from '~/types/game'

const route  = useRoute()
const router = useRouter()
const toast  = useToast()
const { username } = useAuth()
const { public: { apiBase } } = useRuntimeConfig()

const roomId    = route.params.id as string
const isCreating = route.query.create === 'true'

// ─── Game state ───────────────────────────────────────────────────────────────
const gameState  = ref<GameState | null>(null)
const myClicks   = ref(0)
const timeLeft   = ref(10)
let stompClient: any  = null
let timerInterval: ReturnType<typeof setInterval> | null = null

const isSpectator = computed(() =>
  !!username.value && (gameState.value?.spectators.includes(username.value) ?? false)
)
const isCreator = computed(() =>
  gameState.value?.creatorUsername === username.value
)
const canStart = computed(() =>
  isCreator.value &&
  gameState.value?.state === 'WAITING_FOR_PLAYERS' &&
  (gameState.value?.connectedPlayers.length ?? 0) >= 2
)
const sortedScores = computed(() =>
  Object.entries(gameState.value?.scores ?? {}).sort(([, a], [, b]) => b - a)
)
const timerPercent = computed(() => Math.round((timeLeft.value / 10) * 100))

// ─── Timer ────────────────────────────────────────────────────────────────────
function startTimer(endsAt: number) {
  stopTimer()
  timerInterval = setInterval(() => {
    const remaining = Math.max(0, Math.ceil((endsAt - Date.now()) / 1000))
    timeLeft.value = remaining
    if (remaining <= 0) stopTimer()
  }, 200)
}

function stopTimer() {
  if (timerInterval) { clearInterval(timerInterval); timerInterval = null }
}

// ─── WebSocket ────────────────────────────────────────────────────────────────
async function connect() {
  if (!username.value) { router.push('/'); return }

  const { Client } = await import('@stomp/stompjs')
  const SockJS     = (await import('sockjs-client')).default

  stompClient = new Client({
    webSocketFactory: () => new SockJS(`${apiBase}/ws`),
    onConnect: () => {
      stompClient.subscribe(`/room/${roomId}`, (msg: any) => {
        const state: GameState = JSON.parse(msg.body)
        const prev = gameState.value

        gameState.value = state

        if (state.state === 'IN_PROGRESS' && prev?.state !== 'IN_PROGRESS') {
          myClicks.value = 0
          startTimer(state.gameEndsAt)
          if (!isSpectator.value) {
            toast.add({ title: 'Los!', description: 'Klick so schnell du kannst!', color: 'success' })
          }
        }

        if (state.state === 'ENDED') {
          stopTimer()
          if (state.winner === username.value) {
            toast.add({ title: '🏆 Du hast gewonnen!', color: 'success' })
          }
        }
      })

      const destination = isCreating ? `/game/createRoom/${roomId}` : `/game/room/${roomId}`
      stompClient.publish({
        destination,
        body: JSON.stringify({ username: username.value, action: 'JOIN' }),
      })
    },
    onStompError: (frame: any) => {
      toast.add({ title: 'Verbindungsfehler', description: frame.headers?.message ?? 'WebSocket Fehler', color: 'error' })
    },
  })

  stompClient.activate()
}

function sendAction(action: string) {
  if (!stompClient?.connected) return
  stompClient.publish({
    destination: `/game/room/${roomId}`,
    body: JSON.stringify({ username: username.value, action }),
  })
}

function click() {
  if (gameState.value?.state !== 'IN_PROGRESS' || isSpectator.value) return
  myClicks.value++
  sendAction('CLICK')
}

function leaveRoom() {
  sendAction('LEAVE')
  stompClient?.deactivate()
  router.push('/')
}

onMounted(connect)
onUnmounted(() => { stopTimer(); stompClient?.deactivate() })
</script>

<template>
  <div class="min-h-screen">

    <!-- Header -->
    <header class="border-b border-gray-800 px-6 py-3">
      <div class="max-w-lg mx-auto flex items-center justify-between">
        <div>
          <p class="font-semibold text-white text-sm">{{ roomId }}</p>
          <p class="text-xs text-gray-500">
            Host: <span class="text-gray-300">{{ gameState?.creatorUsername ?? '…' }}</span>
          </p>
        </div>
        <UButton size="xs" color="neutral" variant="ghost" icon="i-lucide-door-open" @click="leaveRoom">
          Verlassen
        </UButton>
      </div>
    </header>

    <main class="max-w-lg mx-auto px-6 py-8">

      <!-- Connecting skeleton -->
      <template v-if="!gameState">
        <UCard class="text-center py-12 text-gray-500">Verbinde…</UCard>
      </template>

      <!-- ── WAITING_FOR_PLAYERS ─────────────────────────────────────────────── -->
      <template v-else-if="gameState.state === 'WAITING_FOR_PLAYERS'">
        <UCard>
          <template #header>
            <div class="flex items-center justify-between">
              <span class="font-semibold">Spieler im Raum</span>
              <UBadge
                :label="`${gameState.connectedPlayers.length} / ∞`"
                color="primary"
                variant="subtle"
              />
            </div>
          </template>

          <div class="space-y-2 mb-6">
            <div
              v-for="player in gameState.connectedPlayers"
              :key="player"
              class="flex items-center gap-3 rounded-lg px-3 py-2 bg-gray-900"
            >
              <UAvatar :text="player.slice(0, 2).toUpperCase()" size="xs" />
              <span class="text-sm" :class="player === gameState.creatorUsername ? 'text-amber-300 font-semibold' : 'text-gray-200'">
                {{ player }}
              </span>
              <UBadge v-if="player === gameState.creatorUsername" label="Host" color="warning" variant="subtle" size="xs" class="ml-auto" />
              <UBadge v-if="player === username" label="Du" color="primary" variant="subtle" size="xs" :class="player !== gameState.creatorUsername ? 'ml-auto' : ''" />
            </div>
          </div>

          <USeparator class="mb-4" />

          <div v-if="isCreator">
            <UButton
              block
              :disabled="!canStart"
              icon="i-lucide-play"
              @click="sendAction('START_GAME')"
            >
              Spiel starten
            </UButton>
            <p v-if="!canStart" class="text-xs text-gray-500 text-center mt-2">
              Mindestens 2 Spieler erforderlich
            </p>
          </div>
          <p v-else class="text-sm text-gray-500 text-center">
            Warte auf den Host…
          </p>
        </UCard>
      </template>

      <!-- ── COUNTDOWN ──────────────────────────────────────────────────────── -->
      <template v-else-if="gameState.state === 'COUNTDOWN'">
        <UCard class="text-center py-12">
          <p class="text-sm text-gray-400 uppercase tracking-widest mb-4">Bereit machen!</p>
          <span class="text-9xl font-black text-white leading-none tabular-nums">
            {{ gameState.countdownValue }}
          </span>
        </UCard>
      </template>

      <!-- ── IN_PROGRESS ────────────────────────────────────────────────────── -->
      <template v-else-if="gameState.state === 'IN_PROGRESS'">
        <div class="space-y-4">

          <!-- Timer bar -->
          <UCard>
            <div class="flex items-center justify-between mb-2">
              <span class="text-xs text-gray-400 uppercase tracking-widest">Zeit</span>
              <span
                class="text-xl font-black tabular-nums"
                :class="timeLeft <= 3 ? 'text-red-400' : 'text-white'"
              >
                {{ timeLeft }}s
              </span>
            </div>
            <UProgress
              :value="timerPercent"
              :color="timeLeft <= 3 ? 'error' : 'primary'"
            />
          </UCard>

          <!-- Click button or spectator notice -->
          <UCard class="text-center py-8">
            <template v-if="!isSpectator">
              <button
                class="w-36 h-36 rounded-full bg-amber-500 hover:bg-amber-400 active:scale-95 active:bg-amber-600 transition-all shadow-2xl shadow-amber-900/60 text-5xl select-none"
                @click="click"
              >
                🍪
              </button>
              <p class="text-2xl font-black text-white mt-4">{{ myClicks }}</p>
              <p class="text-xs text-gray-500">Klicks</p>
            </template>
            <template v-else>
              <p class="text-4xl mb-3">👀</p>
              <p class="text-gray-400 text-sm">Du schaust zu</p>
            </template>
          </UCard>

          <!-- Live scores -->
          <UCard>
            <template #header>
              <span class="text-sm font-semibold">Aktuell</span>
            </template>
            <div class="space-y-2">
              <div
                v-for="([player, score], i) in sortedScores"
                :key="player"
                class="flex items-center gap-3 text-sm rounded-lg px-3 py-1.5"
                :class="player === username ? 'bg-primary-950/30' : ''"
              >
                <span class="text-gray-500 w-4 text-right text-xs shrink-0">{{ i + 1 }}</span>
                <UAvatar :text="player.slice(0, 2).toUpperCase()" size="xs" />
                <span :class="player === username ? 'text-primary-300 font-semibold' : 'text-gray-300'">{{ player }}</span>
                <span class="ml-auto font-mono font-bold text-white">{{ score }}</span>
              </div>
            </div>
          </UCard>

        </div>
      </template>

      <!-- ── ENDED ──────────────────────────────────────────────────────────── -->
      <template v-else-if="gameState.state === 'ENDED'">
        <div class="space-y-4">

          <!-- Winner card -->
          <UCard class="text-center py-8">
            <p class="text-5xl mb-3">🏆</p>
            <p class="text-xs text-gray-500 uppercase tracking-widest mb-1">Gewinner</p>
            <p class="text-3xl font-black" :class="gameState.winner === username ? 'text-amber-400' : 'text-white'">
              {{ gameState.winner }}
            </p>
            <p v-if="gameState.winner === username" class="text-sm text-amber-500 mt-1">
              Das bist du! 🎉
            </p>
          </UCard>

          <!-- Final scores -->
          <UCard>
            <template #header>
              <span class="text-sm font-semibold">Endstand</span>
            </template>
            <div class="space-y-2">
              <div
                v-for="([player, score], i) in sortedScores"
                :key="player"
                class="flex items-center gap-3 rounded-lg px-3 py-2 text-sm"
                :class="player === gameState.winner ? 'bg-amber-500/10 border border-amber-500/20' : 'bg-gray-900'"
              >
                <span class="text-gray-500 w-4 text-right text-xs shrink-0">{{ i + 1 }}</span>
                <UAvatar :text="player.slice(0, 2).toUpperCase()" size="xs" />
                <span :class="player === gameState.winner ? 'text-amber-300 font-bold' : 'text-gray-300'">{{ player }}</span>
                <span class="ml-auto font-mono font-bold text-white">{{ score }} Klicks</span>
              </div>
            </div>
          </UCard>

          <UButton block variant="outline" icon="i-lucide-arrow-left" @click="router.push('/')">
            Zurück zur Lobby
          </UButton>
        </div>
      </template>

    </main>
  </div>
</template>
