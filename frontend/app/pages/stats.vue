<script setup lang="ts">
definePageMeta({ ssr: false })

import type { GameDTO, StatsResponse } from '~/composables/useStats'

const { username } = useAuth()
const router = useRouter()
const { fetchGlobalStats, fetchMyGames } = useStats()

// ─── Global stats ──────────────────────────────────────────────────────────
const globalStats = ref<StatsResponse | null>(null)

// ─── My games ──────────────────────────────────────────────────────────────
const myGames = ref<GameDTO[]>([])
const loading = ref(true)
const error = ref('')

onMounted(async () => {
  try {
    const [stats, games] = await Promise.all([fetchGlobalStats(), fetchMyGames()])
    globalStats.value = stats
    myGames.value = games
  } catch (e: any) {
    error.value = e.message ?? 'Fehler beim Laden der Statistiken'
  } finally {
    loading.value = false
  }
})

function formatDate(iso: string) {
  return new Date(iso).toLocaleString('de-AT', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
}

function isWinner(game: GameDTO) {
  return game.winnerUsername === username.value
}

function sortedPlayers(game: GameDTO) {
  return [...game.players].sort((a, b) => b.score - a.score)
}
</script>

<template>
  <div class="min-h-screen">

    <!-- Header -->
    <header class="border-b border-gray-800 px-6 py-3">
      <div class="max-w-2xl mx-auto flex items-center justify-between">
        <div class="flex items-center gap-3">
          <UButton
            size="xs"
            color="neutral"
            variant="ghost"
            icon="i-lucide-arrow-left"
            @click="router.push('/')"
          >
            Zurück
          </UButton>
          <span class="text-xl">🍪</span>
          <span class="font-bold text-white">Statistiken</span>
        </div>
        <div class="flex items-center gap-2">
          <UAvatar :text="username?.slice(0, 2).toUpperCase()" size="sm" />
          <span class="text-sm text-gray-300">{{ username }}</span>
        </div>
      </div>
    </header>

    <!-- Main -->
    <main class="max-w-2xl mx-auto px-6 py-8 space-y-6">

      <!-- Loading -->
      <div v-if="loading" class="text-center py-16 text-gray-500">
        Lade Statistiken…
      </div>

      <!-- Error -->
      <UAlert
        v-else-if="error"
        color="error"
        variant="soft"
        icon="i-lucide-circle-x"
        :title="error"
      />

      <template v-else>

        <!-- Global stats -->
        <UCard>
          <div class="flex items-center gap-4">
            <div class="p-3 bg-gray-800 rounded-xl">
              <UIcon name="i-lucide-globe" class="text-primary-400 text-2xl" />
            </div>
            <div>
              <p class="text-sm text-gray-400">Gesamt gespielte Partien</p>
              <p class="text-3xl font-bold text-white">{{ globalStats?.totalGames ?? 0 }}</p>
            </div>
          </div>
        </UCard>

        <!-- My games -->
        <div>
          <h2 class="text-lg font-semibold text-white mb-3">Meine Partien</h2>

          <div v-if="myGames.length" class="space-y-3">
            <UCard
              v-for="(game, i) in myGames"
              :key="i"
              :class="isWinner(game) ? 'border-success-500/40' : 'border-gray-700'"
            >
              <div class="flex items-start justify-between gap-4">

                <!-- Result badge + date -->
                <div class="space-y-1 min-w-0">
                  <div class="flex items-center gap-2 flex-wrap">
                    <UBadge
                      :label="isWinner(game) ? 'Gewonnen' : 'Verloren'"
                      :color="isWinner(game) ? 'success' : 'neutral'"
                      variant="subtle"
                    />
                    <span class="text-xs text-gray-500">{{ formatDate(game.timestamp) }}</span>
                  </div>

                  <!-- Player scores -->
                  <div class="mt-2 space-y-1">
                    <div
                      v-for="player in sortedPlayers(game)"
                      :key="player.username"
                      class="flex items-center gap-2 text-sm"
                    >
                      <span
                        :class="player.username === username ? 'text-white font-semibold' : 'text-gray-400'"
                      >{{ player.username }}</span>
                      <span class="text-gray-600">·</span>
                      <span class="tabular-nums font-mono text-gray-300">{{ player.score }} Klicks</span>
                      <UIcon
                        v-if="player.username === game.winnerUsername"
                        name="i-lucide-crown"
                        class="text-yellow-400 text-xs"
                      />
                    </div>
                  </div>
                </div>

              </div>
            </UCard>
          </div>

          <!-- Empty state -->
          <div v-else class="text-center py-12 space-y-2">
            <p class="text-3xl">🍪</p>
            <p class="text-gray-400">Noch keine Partien gespielt</p>
            <UButton variant="outline" size="sm" @click="router.push('/')">Jetzt spielen</UButton>
          </div>
        </div>

      </template>
    </main>
  </div>
</template>
