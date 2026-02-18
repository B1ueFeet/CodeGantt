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
  console.log('Registering user with data:', { firstName, lastName, username, email, password })
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

export default {
  login,
  register,
  logout,
  getToken,
  isLoggedIn
}
