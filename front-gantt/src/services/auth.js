import { api } from 'boot/axios'

const KEY_USER = 'user'
const KEY_TOKEN = 'token'

export function getUser () {
  const raw = localStorage.getItem(KEY_USER)
  return raw ? JSON.parse(raw) : null
}

export function isLoggedIn () {
  return !!localStorage.getItem(KEY_TOKEN)
}

export async function login ({ identifier, password }) {
  // identifier = email O username
  const res = await api.post('/api/auth/login', { identifier, password })
  const { token, user } = res.data

  localStorage.setItem(KEY_TOKEN, token)
  localStorage.setItem(KEY_USER, JSON.stringify(user))
  return user
}

export async function register (payload) {
  // payload: { firstName,lastName,username,email,password }
  const res = await api.post('/api/auth/register', payload)
  const { token, user } = res.data

  localStorage.setItem(KEY_TOKEN, token)
  localStorage.setItem(KEY_USER, JSON.stringify(user))
  return user
}

export function logout () {
  localStorage.removeItem(KEY_TOKEN)
  localStorage.removeItem(KEY_USER)
}
