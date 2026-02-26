const PERSISTENCE_URL = 'http://localhost:8082'

export interface PlayerScore {
  username: string
  score: number
}

export interface GameDTO {
  winnerUsername: string
  players: PlayerScore[]
  timestamp: string // ISO-8601, z.B. "2024-01-15T14:30:00"
}

export interface StatsResponse {
  totalGames: number
}

export const useStats = () => {
  const { authHeader } = useAuth()

  const fetchGlobalStats = () =>
    $fetch<StatsResponse>(`${PERSISTENCE_URL}/api/stats`)

  const fetchMyGames = () =>
    $fetch<GameDTO[]>(`${PERSISTENCE_URL}/api/stats/my-games`, {
      headers: { Authorization: authHeader.value },
    })

  return { fetchGlobalStats, fetchMyGames }
}
