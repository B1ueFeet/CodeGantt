<template>
  <q-page class="q-pa-md">
    <div class="text-h6 q-mb-md">Test Endpoint</div>

    <q-banner class="q-mb-md" rounded>
      Backend: <b>{{ baseUrl }}</b>
      <div class="text-caption">
        por ahora
      </div>
    </q-banner>

    <div class="row q-col-gutter-md">
      <!-- USERS -->
      <div class="col-12 col-md-6">
        <q-card>
          <q-card-section>
            <div class="text-subtitle1">Users</div>
          </q-card-section>

          <q-separator />

          <q-card-section class="q-gutter-sm">
            <q-btn label="Listar usuarios" color="primary" @click="listUsers" :loading="loading.users" />
            <q-btn label="Crear usuario demo" color="secondary" @click="createUserDemo" :loading="loading.createUser" />

            <q-input v-model="state.userId" label="userId (UUID)" dense outlined />
          </q-card-section>
        </q-card>
      </div>

      <!-- PROJECTS -->
      <div class="col-12 col-md-6">
        <q-card>
          <q-card-section>
            <div class="text-subtitle1">Projects</div>
          </q-card-section>

          <q-separator />

          <q-card-section class="q-gutter-sm">
            <q-btn label="Listar proyectos" color="primary" @click="listProjects" :loading="loading.projects" />
            <q-btn label="Crear proyecto demo" color="secondary" @click="createProjectDemo"
              :loading="loading.createProject" />

            <q-input v-model="state.projectId" label="projectId (UUID)" dense outlined />
          </q-card-section>
        </q-card>
      </div>

      <!-- TASKS -->
      <div class="col-12">
        <q-card>
          <q-card-section>
            <div class="text-subtitle1">Tasks</div>
          </q-card-section>

          <q-separator />

          <q-card-section class="q-gutter-sm">
            <div class="row q-col-gutter-sm">
              <div class="col-12 col-md-6">
                <q-input v-model="state.projectIdForTasks" label="projectId para tareas (UUID)" dense outlined />
              </div>
              <div class="col-12 col-md-6">
                <q-input v-model="state.taskId" label="taskId (UUID)" dense outlined />
              </div>
            </div>

            <q-btn label="Listar tareas por proyecto" color="primary" @click="listTasksByProject"
              :loading="loading.tasks" />
            <q-btn label="Crear tarea demo" color="secondary" @click="createTaskDemo" :loading="loading.createTask" />
          </q-card-section>
        </q-card>
      </div>

      <!-- OUTPUT -->
      <div class="col-12">
        <q-card>
          <q-card-section class="row items-center justify-between">
            <div class="text-subtitle1">Salida (JSON)</div>
            <q-btn flat icon="delete" label="Limpiar" @click="output = ''" />
          </q-card-section>

          <q-separator />

          <q-card-section>
            <q-input v-model="output" type="textarea" autogrow outlined readonly
              placeholder="Aquí verás las respuestas del backend..." />
          </q-card-section>
        </q-card>
      </div>
    </div>
  </q-page>
</template>

<script>
// Options API + JS
import axios from 'axios'

export default {
  name: 'TestEndpointPage',

  data() {
    return {
      baseUrl: '',
      output: '',
      state: {
        userId: '',
        projectId: '',
        projectIdForTasks: '',
        taskId: ''
      },
      loading: {
        users: false,
        createUser: false,
        projects: false,
        createProject: false,
        tasks: false,
        createTask: false
      }
    }
  },

  methods: {
    pretty(obj) {
      this.output = JSON.stringify(obj, null, 2)
    },

    async listUsers() {
      this.loading.users = true
      try {
        const res = await axios.get('/api/users')
        this.pretty(res.data)
      } catch (e) {
        this.pretty(this.normalizeError(e))
      } finally {
        this.loading.users = false
      }
    },

    async createUserDemo() {
      this.loading.createUser = true
      try {
        const demo = {
          role: 'USER',
          firstName: 'Demo',
          lastName: 'User',
          username: `demo_${Date.now()}`,
          email: `demo_${Date.now()}@test.com`,
          password: '1234'
        }
        const res = await axios.post('/api/users', demo)
        this.state.userId = res.data?.id || ''
        this.pretty(res.data)
      } catch (e) {
        this.pretty(this.normalizeError(e))
      } finally {
        this.loading.createUser = false
      }
    },

    async listProjects() {
      this.loading.projects = true
      try {
        const res = await axios.get('/api/projects')
        this.pretty(res.data)
      } catch (e) {
        this.pretty(this.normalizeError(e))
      } finally {
        this.loading.projects = false
      }
    },

    async callEndpoint(method, endpoint, body = null) {
      this.loading = true
      this.error = null
      this.response = null

      try {
        const config = { method, url: endpoint }
        if (body) config.data = body

        const { data } = await this.$api(config)
        this.response = data

      } catch (err) {
        this.error = err.response?.data || err.message
      } finally {
        this.loading = false
      }
    },

    async createProjectDemo() {
      this.loading.createProject = true
      try {
        // Necesita ownerId: usa el userId escrito o crea uno demo si está vacío
        let ownerId = this.state.userId
        if (!ownerId) {
          const userRes = await axios.post('/api/users', {
            role: 'USER',
            firstName: 'Owner',
            lastName: 'Demo',
            username: `owner_${Date.now()}`,
            email: `owner_${Date.now()}@test.com`,
            password: '1234'
          })
          ownerId = userRes.data?.id
          this.state.userId = ownerId || ''
        }

        const demo = {
          name: `Proyecto demo ${new Date().toLocaleString()}`,
          description: 'Creado desde TestEndpointPage',
          ownerId
        }
        const res = await axios.post('/api/projects', demo)
        this.state.projectId = res.data?.id || ''
        this.state.projectIdForTasks = res.data?.id || this.state.projectIdForTasks
        this.pretty(res.data)
      } catch (e) {
        this.pretty(this.normalizeError(e))
      } finally {
        this.loading.createProject = false
      }
    },

    async listTasksByProject() {
      this.loading.tasks = true
      try {
        const pid = this.state.projectIdForTasks || this.state.projectId
        if (!pid) {
          this.pretty({ error: 'Pon un projectId primero (o crea un proyecto demo).' })
          return
        }
        const res = await axios.get(`/api/tasks/by-project/${pid}`)
        this.pretty(res.data)
      } catch (e) {
        this.pretty(this.normalizeError(e))
      } finally {
        this.loading.tasks = false
      }
    },

    async createTaskDemo() {
      this.loading.createTask = true
      try {
        const projectId = this.state.projectIdForTasks || this.state.projectId
        if (!projectId) {
          this.pretty({ error: 'Pon un projectId primero (o crea un proyecto demo).' })
          return
        }

        const today = new Date()
        const yyyy = today.getFullYear()
        const mm = String(today.getMonth() + 1).padStart(2, '0')
        const dd = String(today.getDate()).padStart(2, '0')

        const demo = {
          projectId,
          name: `Tarea demo ${Date.now()}`,
          description: 'Creada desde TestEndpointPage',
          startDate: `${yyyy}-${mm}-${dd}`,
          endDate: `${yyyy}-${mm}-${dd}`
        }

        const res = await axios.post('/api/tasks', demo)
        this.state.taskId = res.data?.id || ''
        this.pretty(res.data)
      } catch (e) {
        this.pretty(this.normalizeError(e))
      } finally {
        this.loading.createTask = false
      }
    },

    normalizeError(e) {
      // axios error normalizado
      return {
        message: e?.message,
        status: e?.response?.status,
        data: e?.response?.data
      }
    }
  }
}
</script>
