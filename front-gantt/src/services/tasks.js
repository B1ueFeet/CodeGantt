import { api } from 'boot/axios'

export async function getTasksByProject(projectId) {
  const { data } = await api.get(`/tasks/by-project/${projectId}`)
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

export default {
  getTasksByProject,
  createTask,
  updateTask,
  deleteTask
}