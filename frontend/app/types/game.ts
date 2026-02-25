export type State = 'WAITING_FOR_PLAYERS' | 'COUNTDOWN' | 'IN_PROGRESS' | 'ENDED'

export interface GameState {
  roomId: string
  creatorUsername: string
  state: State
  connectedPlayers: string[]
  registeredPlayers: string[]
  scores: Record<string, number>
  spectators: string[]
  winner: string | null
  countdownValue: number
  gameEndsAt: number // epoch millis
}
