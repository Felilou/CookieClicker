// https://nuxt.com/docs/api/configuration/nuxt-config
export default defineNuxtConfig({
  ssr: false,
  compatibilityDate: '2025-07-15',
  devtools: { enabled: true },
  modules: ['@nuxt/ui', '@nuxt/image', '@kgierke/nuxt-basic-auth'],
  css: ['~/assets/css/main.css'],
  colorMode: {
    preference: 'dark',
  },
  
})