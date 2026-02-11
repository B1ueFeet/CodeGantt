import { defineBoot } from '#q-app/wrappers'
import axios from 'axios'

// API de tu app (usa proxy en dev)
const api = axios.create({
  baseURL: '' // importante: rutas tipo /api/...
})

// Request interceptor: agrega token si existe
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers = config.headers || {}
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// Response interceptor: normaliza errores (opcional pero útil)
api.interceptors.response.use(
  (res) => res,
  (err) => {
    const normalized = {
      message: err?.message,
      status: err?.response?.status,
      data: err?.response?.data
    }

    // Opcional: si token expiró o es inválido, limpias sesión
    if (normalized.status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('user')
    }

    return Promise.reject(normalized)
  }
)

export default defineBoot(({ app }) => {
  // Para Options API:
  // this.$axios -> axios "crudo"
  // this.$api   -> axios configurado con baseURL + interceptors
  app.config.globalProperties.$axios = axios
  app.config.globalProperties.$api = api
})

export { api }
