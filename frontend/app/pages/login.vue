<script setup lang="ts">
definePageMeta({ ssr: false })

const { login, register, isLoggedIn } = useAuth()
const router = useRouter()
const toast = useToast()

// Redirect if already logged in
onMounted(() => {
  if (isLoggedIn.value) router.replace('/')
})

// ─── Tab state ────────────────────────────────────────────────────────────────
const activeTab = ref<'login' | 'register'>('login')

// ─── Login form ───────────────────────────────────────────────────────────────
const loginForm = reactive({ username: '', password: '' })
const loginLoading = ref(false)
const loginError = ref('')

async function doLogin() {
  loginError.value = ''
  loginLoading.value = true
  try {
    await login(loginForm.username, loginForm.password)
    toast.add({ title: `Willkommen, ${loginForm.username}!`, color: 'success' })
    router.replace('/')
  } catch (e: any) {
    loginError.value = e.data?.message ?? e.message ?? 'Login fehlgeschlagen'
  } finally {
    loginLoading.value = false
  }
}

// ─── Register form ────────────────────────────────────────────────────────────
const regForm = reactive({ username: '', email: '', password: '', firstName: '', lastName: '' })
const regLoading = ref(false)
const regError = ref('')
const regSuccess = ref(false)

async function doRegister() {
  regError.value = ''
  regLoading.value = true
  try {
    await register(regForm)
    regSuccess.value = true
    toast.add({ title: 'Registrierung erfolgreich!', description: 'Du kannst dich jetzt anmelden.', color: 'success' })
    // Pre-fill login form and switch tab
    loginForm.username = regForm.username
    loginForm.password = regForm.password
    activeTab.value = 'login'
  } catch (e: any) {
    regError.value = e.data?.message ?? e.message ?? 'Registrierung fehlgeschlagen'
  } finally {
    regLoading.value = false
  }
}
</script>

<template>
  <div class="min-h-screen bg-gray-950 flex items-center justify-center p-4">
    <div class="w-full max-w-sm space-y-6">

      <!-- Logo -->
      <div class="text-center space-y-1">
        <p class="text-5xl">🍪</p>
        <h1 class="text-2xl font-bold text-white tracking-tight">CookieClicker</h1>
        <p class="text-sm text-gray-500">Klick schneller als alle anderen</p>
      </div>

      <!-- Card -->
      <UCard>
        <!-- Tab switcher -->
        <div class="flex rounded-lg bg-gray-900 p-1 mb-6 gap-1">
          <button
            class="flex-1 rounded-md py-1.5 text-sm font-medium transition-all"
            :class="activeTab === 'login'
              ? 'bg-white text-gray-900 shadow-sm'
              : 'text-gray-400 hover:text-gray-200'"
            @click="activeTab = 'login'"
          >
            Anmelden
          </button>
          <button
            class="flex-1 rounded-md py-1.5 text-sm font-medium transition-all"
            :class="activeTab === 'register'
              ? 'bg-white text-gray-900 shadow-sm'
              : 'text-gray-400 hover:text-gray-200'"
            @click="activeTab = 'register'"
          >
            Registrieren
          </button>
        </div>

        <!-- ── Login ── -->
        <form v-if="activeTab === 'login'" class="space-y-4" @submit.prevent="doLogin">
          <UFormField label="Benutzername">
            <UInput
              v-model="loginForm.username"
              placeholder="dein_username"
              icon="i-lucide-user"
              class="w-full"
              autocomplete="username"
            />
          </UFormField>

          <UFormField label="Passwort">
            <UInput
              v-model="loginForm.password"
              type="password"
              placeholder="••••••••"
              icon="i-lucide-lock"
              class="w-full"
              autocomplete="current-password"
            />
          </UFormField>

          <UAlert
            v-if="loginError"
            color="error"
            variant="soft"
            icon="i-lucide-circle-x"
            :title="loginError"
          />

          <UButton
            type="submit"
            block
            :loading="loginLoading"
            :disabled="!loginForm.username || !loginForm.password"
          >
            Anmelden
          </UButton>
        </form>

        <!-- ── Register ── -->
        <form v-else class="space-y-4" @submit.prevent="doRegister">
          <div class="grid grid-cols-2 gap-3">
            <UFormField label="Vorname">
              <UInput v-model="regForm.firstName" placeholder="Max" class="w-full" />
            </UFormField>
            <UFormField label="Nachname">
              <UInput v-model="regForm.lastName" placeholder="Mustermann" class="w-full" />
            </UFormField>
          </div>

          <UFormField label="Benutzername">
            <UInput
              v-model="regForm.username"
              placeholder="max_mustermann"
              icon="i-lucide-user"
              class="w-full"
              autocomplete="username"
            />
          </UFormField>

          <UFormField label="E-Mail">
            <UInput
              v-model="regForm.email"
              type="email"
              placeholder="max@example.com"
              icon="i-lucide-mail"
              class="w-full"
              autocomplete="email"
            />
          </UFormField>

          <UFormField label="Passwort">
            <UInput
              v-model="regForm.password"
              type="password"
              placeholder="••••••••"
              icon="i-lucide-lock"
              class="w-full"
              autocomplete="new-password"
            />
          </UFormField>

          <UAlert
            v-if="regError"
            color="error"
            variant="soft"
            icon="i-lucide-circle-x"
            :title="regError"
          />

          <UButton
            type="submit"
            block
            :loading="regLoading"
            :disabled="!regForm.username || !regForm.email || !regForm.password"
          >
            Konto erstellen
          </UButton>
        </form>
      </UCard>

    </div>
  </div>
</template>
