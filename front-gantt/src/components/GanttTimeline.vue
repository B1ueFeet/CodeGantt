<template>
  <div>
    <div class="row items-center q-col-gutter-sm q-mb-md">
      <div class="col-auto">
        <q-btn-toggle
          v-model="localScale"
          toggle-color="primary"
          :options="[
            { label: 'Días', value: 'days' },
            { label: 'Semanas', value: 'weeks' },
            { label: 'Meses', value: 'months' }
          ]"
          @update:model-value="$emit('update:scale', localScale)"
        />
      </div>
    </div>

    <div class="gantt-wrapper">
      <div class="gantt-header row no-wrap">
        <div class="gantt-side header-cell">Tarea</div>

        <div class="gantt-grid row no-wrap">
          <div
            v-for="slot in slots"
            :key="slot.key"
            class="header-cell time-cell"
          >
            {{ slot.label }}
          </div>
        </div>
      </div>

      <div
        v-for="task in normalizedTasks"
        :key="task.id"
        class="gantt-row row no-wrap"
      >
        <div class="gantt-side side-cell">
          <div class="text-weight-medium">{{ task.name }}</div>
          <div class="text-caption text-grey-7">
            {{ task.startDate }} → {{ task.endDate }}
          </div>
        </div>

        <div class="gantt-grid row no-wrap relative-position">
          <div
            v-for="slot in slots"
            :key="task.id + '-' + slot.key"
            class="time-cell body-cell"
          />

          <div
            class="task-bar"
            :style="barStyle(task)"
          >
            <span class="ellipsis">{{ task.name }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'GanttTimeline',

  props: {
    tasks: {
      type: Array,
      default: () => []
    },
    scale: {
      type: String,
      default: 'days'
    }
  },

  emits: ['update:scale'],

  data() {
    return {
      localScale: 'days'
    }
  },

  computed: {
    normalizedTasks() {
      return this.tasks
        .map(task => {
          const startDate = task.startDate || this.todayIso()
          const endDate = task.endDate || this.addDaysIso(startDate, 1)

          return {
            ...task,
            startDate,
            endDate
          }
        })
        .sort((a, b) => new Date(a.startDate) - new Date(b.startDate))
    },

    rangeStart() {
      if (!this.normalizedTasks.length) {
        return this.todayIso()
      }

      const minDate = this.normalizedTasks.reduce((acc, task) => {
        return task.startDate < acc ? task.startDate : acc
      }, this.normalizedTasks[0].startDate)

      return minDate
    },

    rangeEnd() {
      if (!this.normalizedTasks.length) {
        return this.addDaysIso(this.todayIso(), 7)
      }

      const maxDate = this.normalizedTasks.reduce((acc, task) => {
        return task.endDate > acc ? task.endDate : acc
      }, this.normalizedTasks[0].endDate)

      if (this.scale === 'days') {
        return this.addDaysIso(maxDate, 2)
      }

      if (this.scale === 'weeks') {
        return this.addDaysIso(maxDate, 14)
      }

      return this.addDaysIso(maxDate, 31)
    },

    slots() {
      const items = []
      let cursor = new Date(this.rangeStart + 'T00:00:00')
      const end = new Date(this.rangeEnd + 'T00:00:00')

      while (cursor <= end) {
        const iso = this.toIso(cursor)

        if (this.scale === 'days') {
          items.push({
            key: iso,
            date: iso,
            label: this.formatDay(cursor)
          })
          cursor.setDate(cursor.getDate() + 1)
          continue
        }

        if (this.scale === 'weeks') {
          items.push({
            key: iso,
            date: iso,
            label: this.formatWeek(cursor)
          })
          cursor.setDate(cursor.getDate() + 7)
          continue
        }

        items.push({
          key: iso,
          date: iso,
          label: this.formatMonth(cursor)
        })
        cursor.setMonth(cursor.getMonth() + 1)
      }

      return items
    }
  },

  watch: {
    scale: {
      immediate: true,
      handler(value) {
        this.localScale = value
      }
    }
  },

  methods: {
    todayIso() {
      const d = new Date()
      return this.toIso(d)
    },

    toIso(date) {
      const year = date.getFullYear()
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      return `${year}-${month}-${day}`
    },

    addDaysIso(isoDate, days) {
      const d = new Date(isoDate + 'T00:00:00')
      d.setDate(d.getDate() + days)
      return this.toIso(d)
    },

    formatDay(date) {
      return `${String(date.getDate()).padStart(2, '0')}/${String(date.getMonth() + 1).padStart(2, '0')}`
    },

    formatWeek(date) {
      const start = new Date(date)
      const end = new Date(date)
      end.setDate(end.getDate() + 6)
      return `${this.formatDay(start)} - ${this.formatDay(end)}`
    },

    formatMonth(date) {
      return date.toLocaleDateString('es-ES', {
        month: 'short',
        year: 'numeric'
      })
    },

    diffDays(startIso, endIso) {
      const start = new Date(startIso + 'T00:00:00')
      const end = new Date(endIso + 'T00:00:00')
      const ms = end.getTime() - start.getTime()
      return Math.floor(ms / (1000 * 60 * 60 * 24))
    },

    totalUnits() {
      if (!this.slots.length) {
        return 1
      }
      return this.slots.length
    },

    barStyle(task) {
      let offsetUnits = 0
      let durationUnits = 1

      if (this.scale === 'days') {
        offsetUnits = this.diffDays(this.rangeStart, task.startDate)
        durationUnits = Math.max(1, this.diffDays(task.startDate, task.endDate) + 1)
      } else if (this.scale === 'weeks') {
        offsetUnits = Math.floor(this.diffDays(this.rangeStart, task.startDate) / 7)
        durationUnits = Math.max(1, Math.ceil((this.diffDays(task.startDate, task.endDate) + 1) / 7))
      } else {
        const rangeStart = new Date(this.rangeStart + 'T00:00:00')
        const taskStart = new Date(task.startDate + 'T00:00:00')
        const taskEnd = new Date(task.endDate + 'T00:00:00')

        offsetUnits =
          (taskStart.getFullYear() - rangeStart.getFullYear()) * 12 +
          (taskStart.getMonth() - rangeStart.getMonth())

        durationUnits =
          Math.max(
            1,
            ((taskEnd.getFullYear() - taskStart.getFullYear()) * 12) +
            (taskEnd.getMonth() - taskStart.getMonth()) + 1
          )
      }

      const left = (offsetUnits / this.totalUnits()) * 100
      const width = (durationUnits / this.totalUnits()) * 100

      return {
        left: `${left}%`,
        width: `${width}%`
      }
    }
  }
}
</script>

<style scoped>
.gantt-wrapper {
  border: 1px solid #ddd;
  border-radius: 8px;
  overflow-x: auto;
  background: white;
}

.gantt-header,
.gantt-row {
  min-width: 1100px;
}

.gantt-side {
  width: 240px;
  min-width: 240px;
  border-right: 1px solid #e0e0e0;
  background: #fafafa;
}

.gantt-grid {
  flex: 1;
  min-height: 56px;
}

.header-cell,
.side-cell,
.body-cell {
  padding: 12px 8px;
  border-bottom: 1px solid #eee;
}

.time-cell {
  min-width: 90px;
  width: 90px;
  border-right: 1px solid #f0f0f0;
  text-align: center;
  font-size: 12px;
}

.task-bar {
  position: absolute;
  top: 12px;
  height: 32px;
  background: #1976d2;
  color: white;
  border-radius: 6px;
  padding: 6px 8px;
  font-size: 12px;
  overflow: hidden;
  white-space: nowrap;
}
</style>