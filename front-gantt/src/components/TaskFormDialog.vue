<template>
  <q-dialog :model-value="modelValue" @update:model-value="$emit('update:modelValue', $event)">
    <q-card style="min-width: 420px; width: 100%; max-width: 700px;">
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
              v-model="form.startDate"
              label="Fecha inicio"
              outlined
              dense
              type="date"
            />
          </div>

          <div class="col-12 col-md-6">
            <q-input
              v-model="form.endDate"
              label="Fecha fin"
              outlined
              dense
              type="date"
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

          <div class="col-12 col-md-6">
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
        </div>

        <q-banner dense rounded class="bg-grey-2 text-grey-8">
          Con tu backend actual, si no usas horas reales, el timeline se renderiza por fechas.
          Luego puedes migrar a startAt/endAt.
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
        startDate: '',
        endDate: '',
        status: 'TODO',
        progress: 0
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
          startDate: value?.startDate || '',
          endDate: value?.endDate || '',
          status: value?.status || 'TODO',
          progress: value?.progress ?? 0
        }
      }
    }
  },

  methods: {
    submitForm() {
      this.$emit('save', { ...this.form })
    }
  }
}
</script>