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
          v-if="!isEdit"
          v-model="form.ownerId"
          label="Owner ID (UUID)"
          outlined
          dense
          hint="Con tu backend actual, el create de project pide ownerId"
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
        ownerId: ''
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
          ownerId: value?.ownerId || ''
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