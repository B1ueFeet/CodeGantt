import { authApi, api } from 'boot/axios'

export async function login(identifier, password) {
  const { data } = await authApi.post('/login', {
    login: identifier,
    password
  })

  localStorage.setItem('token', data.token)
  return data
}

export async function register({ firstName, lastName, username, email, password }) {
  const { data } = await api.post('/users', {
    firstName,
    lastName,
    username,
    email,
    password
  })
  return data
}

export function logout() {
  localStorage.removeItem('token')
}

export function getToken() {
  return localStorage.getItem('token')
}

export function isLoggedIn() {
  return !!getToken()
}

export function parseJwt(token) {
  try {
    const base64 = token.split('.')[1]
    const normalized = base64.replace(/-/g, '+').replace(/_/g, '/')
    const padded = normalized.padEnd(Math.ceil(normalized.length / 4) * 4, '=')
    return JSON.parse(atob(padded))
  } catch (error) {
    return null
  }
}

export function getSession() {
  const token = getToken()
  if (!token) {
    return null
  }

  const payload = parseJwt(token)
  if (!payload) {
    return null
  }

  const roles = payload.groups || payload.roles || []

  return {
    token,
    payload,
    roles,
    username: payload.preferred_username || payload.upn || '',
    email: payload.email || '',
    sub: payload.sub || ''
  }
}

export function hasRole(role) {
  const session = getSession()
  if (!session) {
    return false
  }
  return session.roles.includes(role)
}

export default {
  login,
  register,
  logout,
  getToken,
  isLoggedIn,
  parseJwt,
  getSession,
  hasRole
}