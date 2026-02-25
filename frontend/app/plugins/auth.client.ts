export default defineNuxtPlugin(() => {
  const { loadFromStorage } = useAuth()
  loadFromStorage()
})
