<script setup lang="ts">
definePageMeta({ ssr: false })

import type { State } from '~/types/game'

const { username, logout } = useAuth()
const router = useRouter()

// ─── Rooms ────────────────────────────────────────────────────────────────────
interface RoomInfo {
  roomId: string
  creatorUsername: string
  playerCount: number
  state: State
}

const rooms = ref<RoomInfo[]>([])
const loadingRooms = ref(false)

async function fetchRooms() {
  loadingRooms.value = true
  try {
    rooms.value = await $fetch<RoomInfo[]>('http://localhost:8080/api/rooms')
  } catch {
    rooms.value = []
  } finally {
    loadingRooms.value = false
  }
}

onMounted(fetchRooms)

// ─── Create room ──────────────────────────────────────────────────────────────
const newRoomId = ref('')
const showCreateForm = ref(false)

function createRoom() {
  const id = newRoomId.value.trim() || `${username.value}-${Date.now().toString(36)}`
  newRoomId.value = ''
  showCreateForm.value = false
  router.push(`/room/${id}?create=true`)
}

function joinRoom(roomId: string) {
  router.push(`/room/${roomId}`)
}

function doLogout() {
  logout()
  router.push('/login')
}

// ─── State display ────────────────────────────────────────────────────────────
const stateConfig: Record<State, { label: string; color: 'primary' | 'warning' | 'success' | 'neutral' }> = {
  WAITING_FOR_PLAYERS: { label: 'Wartet',    color: 'primary'  },
  COUNTDOWN:           { label: 'Countdown', color: 'warning'  },
  IN_PROGRESS:         { label: 'Läuft',     color: 'success'  },
  ENDED:               { label: 'Beendet',   color: 'neutral'  },
}

const joinableStates: State[] = ['WAITING_FOR_PLAYERS']
</script>

<template>
  <div class="min-h-screen">

    <!-- Header -->
    <header class="border-b border-gray-800 px-6 py-3">
      <div class="max-w-2xl mx-auto flex items-center justify-between">
        <div class="flex items-center gap-2">
          <span class="text-xl">🍪</span>
          <span class="font-bold text-white">CookieClicker</span>
        </div>
        <div class="flex items-center gap-3">
          <UAvatar :text="username?.slice(0, 2).toUpperCase()" size="sm" />
          <span class="text-sm text-gray-300">{{ username }}</span>
          <UButton
            size="xs"
            color="neutral"
            variant="ghost"
            icon="i-lucide-bar-chart-2"
            @click="router.push('/stats')"
          >
            Stats
          </UButton>
          <UButton
            size="xs"
            color="neutral"
            variant="ghost"
            icon="i-lucide-log-out"
            @click="doLogout"
          >
            Abmelden
          </UButton>
        </div>
      </div>
    </header>

    <!-- Main -->
    <main class="max-w-2xl mx-auto px-6 py-8 space-y-6">

      <!-- Room list header -->
      <div class="flex items-center justify-between">
        <h2 class="text-lg font-semibold text-white">Aktive Räume</h2>
        <div class="flex gap-2">
          <UButton
            size="sm"
            color="neutral"
            variant="ghost"
            icon="i-lucide-refresh-cw"
            :loading="loadingRooms"
            @click="fetchRooms"
          />
          <UButton
            size="sm"
            icon="i-lucide-plus"
            @click="showCreateForm = !showCreateForm"
          >
            Neuer Raum
          </UButton>
        </div>
      </div>

      <!-- Create room form -->
      <UCard v-if="showCreateForm">
        <div class="flex gap-3 items-end">
          <UFormField label="Raumname (optional)" class="flex-1">
            <UInput
              v-model="newRoomId"
              placeholder="z.B. spenger-turnier"
              class="w-full"
              @keyup.enter="createRoom"
            />
          </UFormField>
          <UButton @click="createRoom">Erstellen</UButton>
          <UButton color="neutral" variant="ghost" @click="showCreateForm = false">Abbrechen</UButton>
        </div>
      </UCard>

      <!-- Room list -->
      <div v-if="rooms.length" class="space-y-3">
        <UCard
          v-for="room in rooms"
          :key="room.roomId"
          class="hover:border-gray-600 transition-colors cursor-default"
        >
          <div class="flex items-center justify-between">
            <div class="space-y-1">
              <div class="flex items-center gap-2">
                <span class="font-semibold text-white">{{ room.roomId }}</span>
                <UBadge
                  :label="stateConfig[room.state].label"
                  :color="stateConfig[room.state].color"
                  variant="subtle"
                  size="sm"
                />
              </div>
              <p class="text-xs text-gray-500">
                Host: <span class="text-gray-300">{{ room.creatorUsername }}</span>
                · {{ room.playerCount }} Spieler
              </p>
            </div>
            <UButton
              size="sm"
              :variant="joinableStates.includes(room.state) ? 'solid' : 'outline'"
              :color="joinableStates.includes(room.state) ? 'primary' : 'neutral'"
              @click="joinRoom(room.roomId)"
            >
              {{ room.state === 'ENDED' ? 'Ergebnisse' : 'Beitreten' }}
            </UButton>
          </div>
        </UCard>
      </div>

      <!-- Empty state -->
      <div v-else-if="!loadingRooms" class="text-center py-16 space-y-3">
        <p class="text-4xl">🍪</p>
        <p class="text-gray-400">Keine aktiven Räume</p>
        <UButton variant="outline" @click="showCreateForm = true">Ersten Raum erstellen</UButton>
      </div>

      <div v-else class="text-center py-16 text-gray-600">
        Lade Räume…
      </div>

    </main>
  </div>
</template>
