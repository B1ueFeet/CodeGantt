import axios from 'axios'

const api = axios.create({ baseURL: '/api' })
const authApi = axios.create({ baseURL: '/auth' })

export default ({ app }) => {
  app.config.globalProperties.$axios = axios
  app.config.globalProperties.$api = api
  app.config.globalProperties.$authApi = authApi
}

export { api, authApi }
