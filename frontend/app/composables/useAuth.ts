interface TokenResponse {
  access_token: string
  expires_in: number
  refresh_token: string
  token_type: string
}

export const useAuth = () => {
  const { public: { apiBase } } = useRuntimeConfig()

  const token    = useState<string | null>('auth.token',    () => null)
  const username = useState<string | null>('auth.username', () => null)

  const isLoggedIn = computed(() => !!token.value)
  const authHeader = computed(() => token.value ? `Bearer ${token.value}` : '')

  function loadFromStorage() {
    if (!import.meta.client) return
    token.value    = localStorage.getItem('auth.token')
    username.value = localStorage.getItem('auth.username')
  }

  async function login(user: string, password: string) {
    const res = await $fetch<TokenResponse>(`${apiBase}/auth/login`, {
      method: 'POST',
      body: { username: user, password },
    })

    const usernameRes = await $fetch<{ username: string }>(`${apiBase}/auth/username`, {
      headers: { Authorization: `Bearer ${res.access_token}` },
    })

    token.value    = res.access_token
    username.value = usernameRes.username
    localStorage.setItem('auth.token',    res.access_token)
    localStorage.setItem('auth.username', usernameRes.username)
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
    token.value    = null
    username.value = null
    localStorage.removeItem('auth.token')
    localStorage.removeItem('auth.username')
  }

  return { token, username, isLoggedIn, authHeader, login, logout, register, loadFromStorage }
}
