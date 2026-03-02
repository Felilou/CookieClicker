interface TokenResponse {
  access_token: string
  expires_in: number
  refresh_expires_in: number
  refresh_token: string
  token_type: string
}

const REFRESH_MARGIN_MS = 60_000 // refresh 60s before expiry

let refreshTimer: ReturnType<typeof setTimeout> | null = null

export const useAuth = () => {
  const { public: { apiBase } } = useRuntimeConfig()

  const token        = useState<string | null>('auth.token',        () => null)
  const refreshToken = useState<string | null>('auth.refreshToken', () => null)
  const expiresAt    = useState<number | null>('auth.expiresAt',    () => null)
  const username     = useState<string | null>('auth.username',     () => null)

  const isLoggedIn = computed(() => !!token.value)
  const authHeader = computed(() => token.value ? `Bearer ${token.value}` : '')

  function saveTokens(res: TokenResponse, user: string) {
    const exp = Date.now() + res.expires_in * 1000
    token.value        = res.access_token
    refreshToken.value = res.refresh_token
    expiresAt.value    = exp
    username.value     = user
    localStorage.setItem('auth.token',        res.access_token)
    localStorage.setItem('auth.refreshToken', res.refresh_token)
    localStorage.setItem('auth.expiresAt',    String(exp))
    localStorage.setItem('auth.username',     user)
    scheduleRefresh(exp)
  }

  function scheduleRefresh(exp: number) {
    if (!import.meta.client) return
    if (refreshTimer) clearTimeout(refreshTimer)
    const delay = exp - Date.now() - REFRESH_MARGIN_MS
    if (delay <= 0) {
      // already near/past expiry — refresh immediately
      refresh()
      return
    }
    refreshTimer = setTimeout(() => refresh(), delay)
  }

  async function refresh() {
    if (!refreshToken.value) { logout(); return }
    try {
      const res = await $fetch<TokenResponse>(`${apiBase}/auth/refresh`, {
        method: 'POST',
        body: { refresh_token: refreshToken.value },
      })
      saveTokens(res, username.value ?? '')
    } catch {
      logout()
    }
  }

  function loadFromStorage() {
    if (!import.meta.client) return
    const t   = localStorage.getItem('auth.token')
    const rt  = localStorage.getItem('auth.refreshToken')
    const exp = localStorage.getItem('auth.expiresAt')
    const u   = localStorage.getItem('auth.username')
    if (!t || !rt || !exp) return
    token.value        = t
    refreshToken.value = rt
    expiresAt.value    = Number(exp)
    username.value     = u
    scheduleRefresh(Number(exp))
  }

  async function login(user: string, password: string) {
    const res = await $fetch<TokenResponse>(`${apiBase}/auth/login`, {
      method: 'POST',
      body: { username: user, password },
    })

    const usernameRes = await $fetch<{ username: string }>(`${apiBase}/auth/username`, {
      headers: { Authorization: `Bearer ${res.access_token}` },
    })

    saveTokens(res, usernameRes.username)
  }

  async function register(data: {
    username: string
    email: string
    password: string
    firstName?: string
    lastName?: string
  }) {
    await $fetch(`${apiBase}/auth/register`, {
      method: 'POST',
      body: data,
    })
  }

  function logout() {
    if (refreshTimer) { clearTimeout(refreshTimer); refreshTimer = null }
    token.value        = null
    refreshToken.value = null
    expiresAt.value    = null
    username.value     = null
    localStorage.removeItem('auth.token')
    localStorage.removeItem('auth.refreshToken')
    localStorage.removeItem('auth.expiresAt')
    localStorage.removeItem('auth.username')
  }

  return { token, username, isLoggedIn, authHeader, login, logout, register, loadFromStorage, refresh }
}
