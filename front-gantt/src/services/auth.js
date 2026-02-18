import { authApi } from 'boot/axios'

export default {
  async login(identifier, password) {
    const { data } = await authApi.post('/auth/login', {
      login: identifier,
      password
    })

    localStorage.setItem('token', data.token)
    return data
  },

  async register({ username, email, password }) {
    const { data } = await authApi.post('/auth/register', {
      username,
      email,
      password
    })
    return data
  },

  logout() {
    localStorage.removeItem('token')
  },

  getToken() {
    return localStorage.getItem('token')
  }
}
