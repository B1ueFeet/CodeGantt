<template>
  <div class="row q-col-gutter-md">
    <div
      v-for="column in columns"
      :key="column.key"
      class="col-12 col-md-6 col-lg-3"
    >
      <q-card flat bordered class="kanban-column">
        <q-card-section class="bg-grey-2">
          <div class="row items-center">
            <div class="text-subtitle1 text-weight-bold">{{ column.label }}</div>
            <q-space />
            <q-badge color="primary">{{ tasksByStatus(column.key).length }}</q-badge>
          </div>
        </q-card-section>

        <q-separator />

        <q-card-section class="q-gutter-sm">
          <q-btn
            size="sm"
            outline
            color="primary"
            icon="add"
            label="Nueva tarea"
            class="full-width"
            @click="$emit('create-task', column.key)"
          />

          <q-card
            v-for="task in tasksByStatus(column.key)"
            :key="task.id"
            flat
            bordered
            class="q-mt-sm"
          >
            <q-card-section class="q-pa-sm">
              <div class="text-subtitle2 text-weight-medium">
                {{ task.name }}
              </div>

              <div class="text-caption text-grey-7 q-mt-xs">
                {{ task.description || 'Sin descripción' }}
              </div>

              <div class="q-mt-sm">
                <q-linear-progress
                  size="10px"
                  :value="(task.progress || 0) / 100"
                  rounded
                />
                <div class="text-caption q-mt-xs">
                  Progreso: {{ task.progress || 0 }}%
                </div>
              </div>

              <div class="text-caption text-grey-7 q-mt-sm">
                Inicio: {{ task.startDate || '-' }}
              </div>
              <div class="text-caption text-grey-7">
                Fin: {{ task.endDate || '-' }}
              </div>
            </q-card-section>

            <q-separator />

            <q-card-actions align="between">
              <div class="row q-gutter-xs">
                <q-btn
                  v-for="move in moveOptions(task.status)"
                  :key="move.value"
                  size="sm"
                  flat
                  color="primary"
                  :label="move.label"
                  @click="$emit('move-task', task, move.value)"
                />
              </div>

              <div class="row q-gutter-xs">
                <q-btn
                  size="sm"
                  flat
                  icon="edit"
                  color="primary"
                  @click="$emit('edit-task', task)"
                />
                <q-btn
                  size="sm"
                  flat
                  icon="delete"
                  color="negative"
                  @click="$emit('delete-task', task)"
                />
              </div>
            </q-card-actions>
          </q-card>
        </q-card-section>
      </q-card>
    </div>
  </div>
</template>

<script>
export default {
  name: 'KanbanBoard',

  props: {
    tasks: {
      type: Array,
      default: () => []
    }
  },

  emits: ['create-task', 'edit-task', 'delete-task', 'move-task'],

  data() {
    return {
      columns: [
        { key: 'TODO', label: 'Por hacer' },
        { key: 'IN_PROGRESS', label: 'En progreso' },
        { key: 'BLOCKED', label: 'Bloqueadas' },
        { key: 'DONE', label: 'Completadas' }
      ]
    }
  },

  methods: {
    tasksByStatus(status) {
      return this.tasks.filter(task => (task.status || 'TODO') === status)
    },

    moveOptions(currentStatus) {
      const map = {
        TODO: [
          { label: 'A progreso', value: 'IN_PROGRESS' },
          { label: 'Bloquear', value: 'BLOCKED' },
          { label: 'Completar', value: 'DONE' }
        ],
        IN_PROGRESS: [
          { label: 'A TODO', value: 'TODO' },
          { label: 'Bloquear', value: 'BLOCKED' },
          { label: 'Completar', value: 'DONE' }
        ],
        BLOCKED: [
          { label: 'A TODO', value: 'TODO' },
          { label: 'A progreso', value: 'IN_PROGRESS' },
          { label: 'Completar', value: 'DONE' }
        ],
        DONE: [
          { label: 'Reabrir', value: 'TODO' },
          { label: 'A progreso', value: 'IN_PROGRESS' }
        ]
      }

      return map[currentStatus] || []
    }
  }
}
</script>

<style scoped>
.kanban-column {
  min-height: 500px;
}
</style>