<template>
  <q-dialog v-model="show">
    <q-card style="min-width: 520px">
      <q-card-section>
        <div class="text-h6">Importar MS Project (XML)</div>
      </q-card-section>

      <q-card-section>
        <q-uploader
          url="#"
          ref="uploader"
          :auto-upload="false"
          accept=".xml"
          @added="onAdded"
        />

        <div v-if="preview.length" class="q-mt-md">
          <div class="text-subtitle2">Vista previa</div>
          <q-list>
            <q-item v-for="t in preview" :key="t.uid">
              <q-item-section>
                <q-item-label>{{ t.name }}</q-item-label>
                <q-item-label caption>{{ t.start }} | {{ t.durationMinutes }} min | preds: {{ t.predecessors.join(',') }}</q-item-label>
              </q-item-section>
            </q-item>
          </q-list>
        </div>
      </q-card-section>

      <q-card-actions align="right">
        <q-btn flat label="Cerrar" @click="close" />
        <q-btn color="primary" label="Importar" :disabled="!file" @click="doImport" />
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>

<script>
import { ref } from 'vue'
import { api } from 'src/boot/axios'

export default {
  name: 'ImportMsProjectDialog',
  props: {
    modelValue: Boolean,
    projectId: String
  },
  emits: ['update:modelValue','imported'],
  setup(props, { emit }) {
    const show = ref(props.modelValue)
    const file = ref(null)
    const preview = ref([])

    const onAdded = (files) => {
      if (files && files.length) {
        file.value = files[0]
        const reader = new FileReader()
        reader.onload = (e) => {
          // simple parse to extract some tags for preview
          const parser = new DOMParser()
          const doc = parser.parseFromString(e.target.result, 'application/xml')
          const tasks = Array.from(doc.getElementsByTagName('Task'))
          preview.value = tasks.map(t => ({
            uid: t.getElementsByTagName('UID')[0]?.textContent || '',
            name: t.getElementsByTagName('Name')[0]?.textContent || '',
            start: t.getElementsByTagName('Start')[0]?.textContent || '',
            durationMinutes: (t.getElementsByTagName('Duration')[0]?.textContent || '').replace('PT','')
          }))
        }
        reader.readAsText(file.value)
      }
    }

    const close = () => { show.value = false; emit('update:modelValue', false) }

    const doImport = async () => {
      if (!file.value) return
      const fd = new FormData()
      fd.append('file', file.value)
      const res = await api.post(`/import/projects/${props.projectId}/msproject`, fd, { headers: { 'Content-Type': 'multipart/form-data' } })
      emit('imported', res.data)
      close()
    }

    return { show, file, preview, onAdded, close, doImport }
  }
}
</script>