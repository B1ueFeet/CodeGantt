import { api } from 'boot/axios'

export async function getTasksByProject(projectId, params = {}) {
  const { data } = await api.get(`/tasks/by-project/${projectId}`, { params })
  return data
}

export async function createTask(payload) {
  const { data } = await api.post('/tasks', payload)
  return data
}

export async function updateTask(id, payload) {
  const { data } = await api.put(`/tasks/${id}`, payload)
  return data
}

export async function deleteTask(id) {
  await api.delete(`/tasks/${id}`)
}

export async function batchUpdateTasks(updates) {
  const { data } = await api.post('/tasks/batch-update', { updates })
  return data
}

export async function createDependency(payload) {
  const { data } = await api.post('/dependencies', payload)
  return data
}

export async function deleteDependency(id) {
  await api.delete(`/dependencies/${id}`)
}

export async function getLeaderSummary(userId, params = {}) {
  const { data } = await api.get(`/leaders/${userId}/summary`, { params })
  return data
}

export default {
  getTasksByProject,
  createTask,
  updateTask,
  deleteTask,
  batchUpdateTasks,
  createDependency,
  deleteDependency,
  getLeaderSummary
}