<template>
  <q-dialog :model-value="modelValue" @update:model-value="$emit('update:modelValue', $event)">
    <q-card style="min-width: 420px; width: 100%; max-width: 600px;">
      <q-card-section class="row items-center q-pb-none">
        <div class="text-h6">{{ isEdit ? 'Editar proyecto' : 'Nuevo proyecto' }}</div>
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

        <q-input
          v-model.number="form.userHourLimit"
          label="Límite de horas por usuario"
          outlined
          dense
          type="number"
          min="0"
          step="0.5"
          hint="Ejemplo: 40"
        />
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
  name: 'ProjectFormDialog',

  props: {
    modelValue: {
      type: Boolean,
      default: false
    },
    project: {
      type: Object,
      default: null
    }
  },

  emits: ['update:modelValue', 'save'],

  data() {
    return {
      form: {
        name: '',
        description: '',
        userHourLimit: 40
      }
    }
  },

  computed: {
    isEdit() {
      return !!(this.project && this.project.id)
    }
  },

  watch: {
    project: {
      immediate: true,
      handler(value) {
        this.form = {
          name: value?.name || '',
          description: value?.description || '',
          userHourLimit: Number(value?.userHourLimit ?? 40)
        }
      }
    }
  },

  methods: {
    submitForm() {
      if (!this.form.name || !this.form.name.trim()) {
        this.$q.notify({
          type: 'negative',
          message: 'El nombre del proyecto es obligatorio'
        })
        return
      }

      if (Number(this.form.userHourLimit) < 0) {
        this.$q.notify({
          type: 'negative',
          message: 'El límite de horas no puede ser negativo'
        })
        return
      }

      this.$emit('save', {
        name: this.form.name.trim(),
        description: this.form.description,
        userHourLimit: Number(this.form.userHourLimit || 0)
      })
    }
  }
}
</script>