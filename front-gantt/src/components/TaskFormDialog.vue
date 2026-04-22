<template>
  <q-dialog :model-value="modelValue" @update:model-value="$emit('update:modelValue', $event)">
    <q-card style="min-width: 460px; width: 100%; max-width: 760px;">
      <q-card-section class="row items-center q-pb-none">
        <div class="text-h6">{{ isEdit ? 'Editar tarea' : 'Nueva tarea' }}</div>
        <q-space />
        <q-btn flat round dense icon="close" v-close-popup />
      </q-card-section>

      <q-card-section class="q-gutter-md">
        <q-input
          v-model="form.name"
          label="Nombre"
          outlined
          dense
        />

        <q-input
          v-model="form.description"
          label="Descripción"
          outlined
          dense
          type="textarea"
          autogrow
        />

        <div class="row q-col-gutter-md">
          <div class="col-12 col-md-6">
            <q-input
              v-model="form.startAtLocal"
              label="Inicio"
              outlined
              dense
              type="datetime-local"
            />
          </div>

          <div class="col-12 col-md-6">
            <q-input
              v-model="form.endAtLocal"
              label="Fin"
              outlined
              dense
              type="datetime-local"
            />
          </div>
        </div>

        <div class="row q-col-gutter-md">
          <div class="col-12 col-md-6">
            <q-select
              v-model="form.status"
              :options="statusOptions"
              label="Estado"
              outlined
              dense
              emit-value
              map-options
            />
          </div>

          <div class="col-12 col-md-3">
            <q-input
              v-model.number="form.progress"
              label="Progreso (%)"
              outlined
              dense
              type="number"
              min="0"
              max="100"
            />
          </div>

          <div class="col-12 col-md-3">
            <q-input
              v-model.number="form.estimatedHours"
              label="Horas estimadas"
              outlined
              dense
              type="number"
              min="0"
              step="0.5"
            />
          </div>
        </div>

        <q-banner dense rounded class="bg-blue-1 text-primary">
          Esta versión ya guarda usando <strong>startAt</strong>, <strong>endAt</strong> y <strong>estimatedHours</strong>.
        </q-banner>
      </q-card-section>

      <q-card-actions align="right">
        <q-btn flat label="Cancelar" v-close-popup />
        <q-btn color="primary" :label="isEdit ? 'Guardar' : 'Crear'" @click="submitForm" />
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>

<script>
export default {
  name: 'TaskFormDialog',

  props: {
    modelValue: {
      type: Boolean,
      default: false
    },
    task: {
      type: Object,
      default: null
    }
  },

  emits: ['update:modelValue', 'save'],

  data() {
    return {
      statusOptions: [
        { label: 'TODO', value: 'TODO' },
        { label: 'IN_PROGRESS', value: 'IN_PROGRESS' },
        { label: 'BLOCKED', value: 'BLOCKED' },
        { label: 'DONE', value: 'DONE' }
      ],
      form: {
        name: '',
        description: '',
        startAtLocal: '',
        endAtLocal: '',
        status: 'TODO',
        progress: 0,
        estimatedHours: 0
      }
    }
  },

  computed: {
    isEdit() {
      return !!(this.task && this.task.id)
    }
  },

  watch: {
    task: {
      immediate: true,
      handler(value) {
        this.form = {
          name: value?.name || '',
          description: value?.description || '',
          startAtLocal: value?.startAtLocal || '',
          endAtLocal: value?.endAtLocal || '',
          status: value?.status || 'TODO',
          progress: Number(value?.progress ?? 0),
          estimatedHours: Number(value?.estimatedHours ?? 0)
        }
      }
    }
  },

  methods: {
    submitForm() {
      if (!this.form.name || !this.form.name.trim()) {
        this.$q.notify({
          type: 'negative',
          message: 'El nombre de la tarea es obligatorio'
        })
        return
      }

      this.$emit('save', {
        ...this.form,
        name: this.form.name.trim()
      })
    }
  }
}
</script>