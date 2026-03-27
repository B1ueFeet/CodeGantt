<template>
  <q-page class="q-pa-md">
    <div class="row items-center q-col-gutter-md q-mb-md">
      <div class="col-12 col-md">
        <div class="text-h4">Panel ADMIN</div>
        <div class="text-subtitle2 text-grey-7">
          Gestión de proyectos, tablero y timeline tipo Gantt
        </div>
      </div>

      <div class="col-12 col-md-auto">
        <q-btn
          color="primary"
          icon="add"
          label="Nuevo proyecto"
          @click="openCreateProject"
        />
      </div>
    </div>

    <q-banner
      v-if="error"
      class="bg-red-1 text-negative q-mb-md"
      rounded
    >
      {{ error }}
    </q-banner>

    <div class="row q-col-gutter-md">
      <div class="col-12 col-lg-4">
        <q-card flat bordered>
          <q-card-section class="row items-center">
            <div class="text-h6">Proyectos</div>
            <q-space />
            <q-btn
              dense
              flat
              round
              icon="refresh"
              @click="loadProjects"
            />
          </q-card-section>

          <q-separator />

          <q-list separator>
            <q-item
              v-for="project in projects"
              :key="project.id"
              clickable
              :active="selectedProject && selectedProject.id === project.id"
              active-class="bg-blue-1 text-primary"
              @click="selectProject(project)"
            >
              <q-item-section>
                <q-item-label>{{ project.name }}</q-item-label>
                <q-item-label caption>
                  {{ project.description || 'Sin descripción' }}
                </q-item-label>
              </q-item-section>

              <q-item-section side>
                <div class="row q-gutter-xs">
                  <q-btn
                    flat
                    dense
                    round
                    icon="edit"
                    color="primary"
                    @click.stop="openEditProject(project)"
                  />
                  <q-btn
                    flat
                    dense
                    round
                    icon="delete"
                    color="negative"
                    @click.stop="confirmDeleteProject(project)"
                  />
                </div>
              </q-item-section>
            </q-item>

            <q-item v-if="!projects.length">
              <q-item-section>
                <q-item-label>No hay proyectos aún</q-item-label>
              </q-item-section>
            </q-item>
          </q-list>
        </q-card>
      </div>

      <div class="col-12 col-lg-8">
        <q-card flat bordered v-if="selectedProject">
          <q-card-section>
            <div class="row items-center q-col-gutter-md">
              <div class="col">
                <div class="text-h6">{{ selectedProject.name }}</div>
                <div class="text-caption text-grey-7">
                  {{ selectedProject.description || 'Sin descripción' }}
                </div>
              </div>

              <div class="col-auto">
                <q-btn-toggle
                  v-model="viewMode"
                  toggle-color="primary"
                  :options="[
                    { label: 'Tablero', value: 'board' },
                    { label: 'Timeline', value: 'gantt' }
                  ]"
                />
              </div>
            </div>
          </q-card-section>

          <q-separator />

          <q-card-section v-if="loadingTasks" class="text-center">
            <q-spinner color="primary" size="36px" />
            <div class="q-mt-sm">Cargando tareas...</div>
          </q-card-section>

          <q-card-section v-else>
            <kanban-board
              v-if="viewMode === 'board'"
              :tasks="tasks"
              @create-task="openCreateTask"
              @edit-task="openEditTask"
              @delete-task="confirmDeleteTask"
              @move-task="moveTask"
            />

            <gantt-timeline
              v-else
              :tasks="tasks"
              :scale="ganttScale"
              @update:scale="ganttScale = $event"
            />
          </q-card-section>
        </q-card>

        <q-card flat bordered v-else>
          <q-card-section class="text-center q-pa-xl">
            <q-icon name="dashboard_customize" size="64px" color="grey-5" />
            <div class="text-h6 q-mt-md">Selecciona un proyecto</div>
            <div class="text-grey-7">
              Ahí podrás ver el tablero tipo Trello y la tabla timeline
            </div>
          </q-card-section>
        </q-card>
      </div>
    </div>

    <project-form-dialog
      v-model="projectDialog"
      :project="editingProject"
      @save="saveProject"
    />

    <task-form-dialog
      v-model="taskDialog"
      :task="editingTask"
      @save="saveTask"
    />
  </q-page>
</template>

<script>
import { Notify, Dialog } from 'quasar'
import { getSession } from 'src/services/auth'
import {
  getProjects,
  createProject,
  updateProject,
  deleteProject
} from 'src/services/projects'
import {
  getTasksByProject,
  createTask,
  updateTask,
  deleteTask
} from 'src/services/tasks'

import ProjectFormDialog from 'src/components/ProjectFormDialog.vue'
import TaskFormDialog from 'src/components/TaskFormDialog.vue'
import KanbanBoard from 'src/components/KanbanBoard.vue'
import GanttTimeline from 'src/components/GanttTimeline.vue'

export default {
  name: 'IndexPage',

  components: {
    ProjectFormDialog,
    TaskFormDialog,
    KanbanBoard,
    GanttTimeline
  },

  data() {
    return {
      loadingProjects: false,
      loadingTasks: false,
      error: '',
      projects: [],
      tasks: [],
      selectedProject: null,
      projectDialog: false,
      taskDialog: false,
      editingProject: null,
      editingTask: null,
      viewMode: 'board',
      ganttScale: 'days',
      pendingTaskStatus: 'TODO',
      session: null
    }
  },

  mounted() {
    this.session = getSession()
    this.loadProjects()
  },

  methods: {
    async loadProjects() {
      this.loadingProjects = true
      this.error = ''

      try {
        this.projects = await getProjects()

        if (this.projects.length && !this.selectedProject) {
          this.selectedProject = this.projects[0]
          await this.loadTasks(this.selectedProject.id)
        }

        if (this.selectedProject) {
          const updatedSelected = this.projects.find(p => p.id === this.selectedProject.id)
          if (updatedSelected) {
            this.selectedProject = updatedSelected
          }
        }
      } catch (error) {
        this.error = this.extractError(error, 'No se pudieron cargar los proyectos')
      } finally {
        this.loadingProjects = false
      }
    },

    async selectProject(project) {
      this.selectedProject = project
      await this.loadTasks(project.id)
    },

    async loadTasks(projectId) {
      this.loadingTasks = true
      this.error = ''

      try {
        this.tasks = await getTasksByProject(projectId)
      } catch (error) {
        this.error = this.extractError(error, 'No se pudieron cargar las tareas')
      } finally {
        this.loadingTasks = false
      }
    },

    openCreateProject() {
      this.editingProject = {
        name: '',
        description: '',
        ownerId: this.session?.sub || ''
      }
      this.projectDialog = true
    },

    openEditProject(project) {
      this.editingProject = { ...project }
      this.projectDialog = true
    },

    async saveProject(form) {
      try {
        if (this.editingProject && this.editingProject.id) {
          await updateProject(this.editingProject.id, {
            name: form.name,
            description: form.description
          })

          Notify.create({
            type: 'positive',
            message: 'Proyecto actualizado'
          })
        } else {
          await createProject({
            name: form.name,
            description: form.description,
            ownerId: form.ownerId
          })

          Notify.create({
            type: 'positive',
            message: 'Proyecto creado'
          })
        }

        this.projectDialog = false
        this.editingProject = null
        await this.loadProjects()
      } catch (error) {
        Notify.create({
          type: 'negative',
          message: this.extractError(error, 'No se pudo guardar el proyecto')
        })
      }
    },

    confirmDeleteProject(project) {
      Dialog.create({
        title: 'Eliminar proyecto',
        message: `¿Seguro que deseas eliminar "${project.name}"?`,
        cancel: true,
        persistent: true
      }).onOk(async () => {
        try {
          await deleteProject(project.id)

          if (this.selectedProject && this.selectedProject.id === project.id) {
            this.selectedProject = null
            this.tasks = []
          }

          Notify.create({
            type: 'positive',
            message: 'Proyecto eliminado'
          })

          await this.loadProjects()
        } catch (error) {
          Notify.create({
            type: 'negative',
            message: this.extractError(error, 'No se pudo eliminar el proyecto')
          })
        }
      })
    },

    openCreateTask(status) {
      this.pendingTaskStatus = status || 'TODO'
      this.editingTask = {
        name: '',
        description: '',
        startDate: this.todayIso(),
        endDate: this.todayIso(),
        status: this.pendingTaskStatus,
        progress: 0
      }
      this.taskDialog = true
    },

    openEditTask(task) {
      this.editingTask = { ...task }
      this.taskDialog = true
    },

    async saveTask(form) {
      if (!this.selectedProject) {
        return
      }

      try {
        if (this.editingTask && this.editingTask.id) {
          await updateTask(this.editingTask.id, {
            name: form.name,
            description: form.description,
            startDate: form.startDate || null,
            endDate: form.endDate || null,
            status: form.status,
            progress: form.progress || 0
          })

          Notify.create({
            type: 'positive',
            message: 'Tarea actualizada'
          })
        } else {
          await createTask({
            projectId: this.selectedProject.id,
            name: form.name,
            description: form.description,
            startDate: form.startDate || this.todayIso(),
            endDate: form.endDate || this.todayIso()
          })

          Notify.create({
            type: 'positive',
            message: 'Tarea creada'
          })
        }

        this.taskDialog = false
        this.editingTask = null
        await this.loadTasks(this.selectedProject.id)
      } catch (error) {
        Notify.create({
          type: 'negative',
          message: this.extractError(error, 'No se pudo guardar la tarea')
        })
      }
    },

    confirmDeleteTask(task) {
      Dialog.create({
        title: 'Eliminar tarea',
        message: `¿Seguro que deseas eliminar "${task.name}"?`,
        cancel: true,
        persistent: true
      }).onOk(async () => {
        try {
          await deleteTask(task.id)

          Notify.create({
            type: 'positive',
            message: 'Tarea eliminada'
          })

          await this.loadTasks(this.selectedProject.id)
        } catch (error) {
          Notify.create({
            type: 'negative',
            message: this.extractError(error, 'No se pudo eliminar la tarea')
          })
        }
      })
    },

    async moveTask(task, nextStatus) {
      try {
        await updateTask(task.id, {
          name: task.name,
          description: task.description,
          startDate: task.startDate,
          endDate: task.endDate,
          status: nextStatus,
          progress: this.progressForStatus(nextStatus, task.progress)
        })

        Notify.create({
          type: 'positive',
          message: `Tarea movida a ${nextStatus}`
        })

        await this.loadTasks(this.selectedProject.id)
      } catch (error) {
        Notify.create({
          type: 'negative',
          message: this.extractError(error, 'No se pudo mover la tarea')
        })
      }
    },

    progressForStatus(status, currentProgress) {
      if (status === 'DONE') {
        return 100
      }

      if (status === 'TODO' && currentProgress === 100) {
        return 0
      }

      return currentProgress || 0
    },

    extractError(error, fallback) {
      return (
        error?.response?.data?.message ||
        error?.response?.statusText ||
        error?.message ||
        fallback
      )
    },

    todayIso() {
      const d = new Date()
      const year = d.getFullYear()
      const month = String(d.getMonth() + 1).padStart(2, '0')
      const day = String(d.getDate()).padStart(2, '0')
      return `${year}-${month}-${day}`
    }
  }
}
</script>