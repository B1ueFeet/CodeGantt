<template>
  <div class="gantt-page">
    <div class="gantt-toolbar row items-center q-col-gutter-sm q-mb-sm">
      <div class="col-auto">
        <q-btn flat dense icon="unfold_more" label="Expand all" />
      </div>

      <div class="col-auto">
        <q-btn flat dense icon="unfold_less" label="Collapse all" />
      </div>

      <div class="col-auto">
        <q-btn flat dense icon="sort" label="Cascade sorting" />
      </div>

      <q-space />

      <div class="col-auto">
        <q-btn flat dense icon="filter_list" label="Filter" />
      </div>

      <div class="col-auto">
        <q-btn-toggle
          v-model="localScale"
          toggle-color="primary"
          unelevated
          :options="[
            { label: 'Days', value: 'days' },
            { label: 'Weeks', value: 'weeks' },
            { label: 'Months', value: 'months' }
          ]"
          @update:model-value="$emit('update:scale', localScale)"
        />
      </div>

      <div class="col-auto">
        <q-btn-dropdown flat dense label="View">
          <q-list style="min-width: 180px">
            <q-item clickable v-close-popup @click="labelMode = 'right'">
              <q-item-section>On the right</q-item-section>
            </q-item>
            <q-item clickable v-close-popup @click="labelMode = 'inside'">
              <q-item-section>Inside</q-item-section>
            </q-item>
            <q-item clickable v-close-popup @click="labelMode = 'hidden'">
              <q-item-section>Do not display</q-item-section>
            </q-item>
          </q-list>
        </q-btn-dropdown>
      </div>
    </div>

    <div class="gantt-shell">
      <div class="gantt-table">
        <div class="gantt-header-row">
          <div class="gantt-side gantt-side-header">
            <div class="side-grid side-grid-header">
              <div class="col-index">#</div>
              <div class="col-task">Task name</div>
              <div class="col-assigned">Assigned</div>
              <div class="col-duration">Duration</div>
              <div class="col-estimation">Estimation</div>
            </div>
          </div>

          <div
            class="gantt-grid gantt-grid-header"
            :style="{ width: `${gridWidth}px` }"
          >
            <div
              v-for="slot in slots"
              :key="slot.key"
              class="time-header-cell"
              :style="{ width: `${slotWidth}px` }"
            >
              {{ slot.label }}
            </div>
          </div>
        </div>

        <div
          v-for="(task, index) in normalizedTasks"
          :key="task.id"
          class="gantt-row"
        >
          <div class="gantt-side gantt-side-body">
            <div class="side-grid side-grid-body">
              <div class="col-index text-grey-7">
                {{ task.wbs || (index + 1) }}
              </div>

              <div
                class="col-task task-name-cell"
                :style="{ paddingLeft: `${12 + ((task.level || 0) * 18)}px` }"
              >
                <q-icon
                  name="chevron_right"
                  size="16px"
                  class="text-grey-5 q-mr-xs"
                />
                <div class="task-name-block">
                  <div class="task-name text-weight-medium">
                    {{ task.name }}
                  </div>
                  <div class="task-dates text-caption text-grey-6">
                    {{ task.startDate }} - {{ task.endDate }}
                  </div>
                </div>
              </div>

              <div class="col-assigned">
                <div class="avatar-stack" v-if="task.assignees.length">
                  <q-avatar
                    v-for="(assignee, idx) in task.assignees.slice(0, 3)"
                    :key="`${task.id}-assignee-${idx}`"
                    size="28px"
                    class="avatar-overlap"
                  >
                    {{ initials(assigneeLabel(assignee)) }}
                  </q-avatar>

                  <div
                    v-if="task.assignees.length > 3"
                    class="extra-assignees"
                  >
                    +{{ task.assignees.length - 3 }}
                  </div>
                </div>

                <div v-else class="text-grey-5">—</div>
              </div>

              <div class="col-duration text-grey-8">
                {{ durationLabel(task) }}
              </div>

              <div class="col-estimation text-grey-8">
                {{ estimationLabel(task) }}
              </div>
            </div>
          </div>

          <div
            class="gantt-grid gantt-grid-body"
            :style="{ width: `${gridWidth}px` }"
          >
            <div
              v-for="slot in slots"
              :key="`${task.id}-${slot.key}`"
              class="time-body-cell"
              :style="{ width: `${slotWidth}px` }"
            />

            <div
              v-if="todayMarkerLeft >= 0"
              class="today-marker"
              :style="{ left: `${todayMarkerLeft}px` }"
            />

            <div
              class="task-bar"
              :class="statusClass(task.status)"
              :style="barStyle(task)"
            >
              <span v-if="labelMode === 'inside'" class="task-bar-label-inside ellipsis">
                {{ task.name }}
              </span>
            </div>

            <div
              v-if="labelMode === 'right'"
              class="task-bar-label-right ellipsis"
              :style="barLabelStyle(task)"
            >
              {{ task.name }}
            </div>
          </div>
        </div>

        <div v-if="!normalizedTasks.length" class="empty-state">
          No hay tareas para mostrar en el Gantt
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
      default: 'months'
    }
  },

  emits: ['update:scale'],

  data() {
    return {
      localScale: this.scale,
      labelMode: 'right'
    }
  },

  watch: {
    scale(value) {
      this.localScale = value
    }
  },

  computed: {
    slotWidth() {
      if (this.localScale === 'days') return 56
      if (this.localScale === 'weeks') return 90
      return 140
    },

    normalizedTasks() {
      return [...this.tasks]
        .map((task, index) => {
          const startDate = this.normalizeDateOnly(task.startDate || task.startAt)
          const endDate = this.normalizeDateOnly(task.endDate || task.endAt || task.startDate || task.startAt)

          return {
            ...task,
            wbs: task.wbs || `${index + 1}`,
            level: Number(task.level || 0),
            startDate,
            endDate,
            assignees: Array.isArray(task.assignees) ? task.assignees : [],
            estimatedHours: Number(task.estimatedHours || 0)
          }
        })
        .sort((a, b) => new Date(a.startDate) - new Date(b.startDate))
    },

    rangeStart() {
      if (!this.normalizedTasks.length) {
        return this.startOfDay(new Date())
      }

      const minDate = this.normalizedTasks.reduce((min, task) => {
        const current = new Date(`${task.startDate}T00:00:00`)
        return current < min ? current : min
      }, new Date(`${this.normalizedTasks[0].startDate}T00:00:00`))

      return this.startOfUnit(minDate)
    },

    rangeEnd() {
      if (!this.normalizedTasks.length) {
        const base = this.startOfDay(new Date())
        const next = new Date(base)
        next.setDate(next.getDate() + 30)
        return next
      }

      const maxDate = this.normalizedTasks.reduce((max, task) => {
        const current = new Date(`${task.endDate}T00:00:00`)
        return current > max ? current : max
      }, new Date(`${this.normalizedTasks[0].endDate}T00:00:00`))

      const end = this.endOfUnit(maxDate)

      if (this.localScale === 'days') {
        end.setDate(end.getDate() + 3)
      } else if (this.localScale === 'weeks') {
        end.setDate(end.getDate() + 14)
      } else {
        end.setMonth(end.getMonth() + 1)
      }

      return end
    },

    slots() {
      const items = []
      const cursor = new Date(this.rangeStart)
      const end = new Date(this.rangeEnd)

      while (cursor <= end) {
        if (this.localScale === 'days') {
          items.push({
            key: this.toDateOnly(cursor),
            label: cursor.toLocaleDateString('en-US', {
              day: '2-digit',
              month: 'short'
            }),
            start: new Date(cursor)
          })

          cursor.setDate(cursor.getDate() + 1)
          continue
        }

        if (this.localScale === 'weeks') {
          const weekStart = new Date(cursor)
          const weekEnd = new Date(cursor)
          weekEnd.setDate(weekEnd.getDate() + 6)

          items.push({
            key: `${this.toDateOnly(weekStart)}-${this.toDateOnly(weekEnd)}`,
            label: `${weekStart.toLocaleDateString('en-US', {
              day: '2-digit',
              month: 'short'
            })} - ${weekEnd.toLocaleDateString('en-US', {
              day: '2-digit',
              month: 'short'
            })}`,
            start: new Date(weekStart)
          })

          cursor.setDate(cursor.getDate() + 7)
          continue
        }

        const monthStart = new Date(cursor)

        items.push({
          key: `${monthStart.getFullYear()}-${monthStart.getMonth() + 1}`,
          label: monthStart.toLocaleDateString('en-US', {
            month: 'short',
            year: 'numeric'
          }),
          start: new Date(monthStart)
        })

        cursor.setMonth(cursor.getMonth() + 1)
      }

      return items
    },

    gridWidth() {
      return this.slots.length * this.slotWidth
    },

    todayMarkerLeft() {
      const today = new Date()
      const dateOnly = this.toDateOnly(today)

      if (this.localScale === 'days') {
        const offset = this.diffDays(this.toDateOnly(this.rangeStart), dateOnly)
        return offset >= 0 ? (offset * this.slotWidth) : -1
      }

      if (this.localScale === 'weeks') {
        const offset = Math.floor(this.diffDays(this.toDateOnly(this.rangeStart), dateOnly) / 7)
        return offset >= 0 ? (offset * this.slotWidth) : -1
      }

      const offset =
        ((today.getFullYear() - this.rangeStart.getFullYear()) * 12) +
        (today.getMonth() - this.rangeStart.getMonth())

      return offset >= 0 ? (offset * this.slotWidth) : -1
    }
  },

  methods: {
    normalizeDateOnly(value) {
      if (!value) return this.toDateOnly(new Date())

      const raw = String(value)
      if (raw.length >= 10) return raw.slice(0, 10)

      return this.toDateOnly(new Date(value))
    },

    toDateOnly(date) {
      const year = date.getFullYear()
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      return `${year}-${month}-${day}`
    },

    startOfDay(date) {
      const result = new Date(date)
      result.setHours(0, 0, 0, 0)
      return result
    },

    startOfUnit(date) {
      const result = this.startOfDay(date)

      if (this.localScale === 'weeks') {
        const day = result.getDay()
        const diff = day === 0 ? -6 : 1 - day
        result.setDate(result.getDate() + diff)
      }

      if (this.localScale === 'months') {
        result.setDate(1)
      }

      return result
    },

    endOfUnit(date) {
      const result = this.startOfDay(date)

      if (this.localScale === 'weeks') {
        const day = result.getDay()
        const diff = day === 0 ? 0 : 7 - day
        result.setDate(result.getDate() + diff)
      }

      if (this.localScale === 'months') {
        result.setMonth(result.getMonth() + 1, 0)
      }

      return result
    },

    diffDays(startIso, endIso) {
      const start = new Date(`${startIso}T00:00:00`)
      const end = new Date(`${endIso}T00:00:00`)
      const diff = end.getTime() - start.getTime()
      return Math.floor(diff / (1000 * 60 * 60 * 24))
    },

    barStyle(task) {
      const offsetUnits = this.taskOffsetUnits(task)
      const durationUnits = this.taskDurationUnits(task)

      return {
        left: `${offsetUnits * this.slotWidth + 8}px`,
        width: `${Math.max(40, durationUnits * this.slotWidth - 16)}px`
      }
    },

    barLabelStyle(task) {
      const offsetUnits = this.taskOffsetUnits(task)
      const durationUnits = this.taskDurationUnits(task)
      const left = (offsetUnits * this.slotWidth) + Math.max(40, durationUnits * this.slotWidth - 16) + 16

      return {
        left: `${left}px`
      }
    },

    taskOffsetUnits(task) {
      if (this.localScale === 'days') {
        return this.diffDays(this.toDateOnly(this.rangeStart), task.startDate)
      }

      if (this.localScale === 'weeks') {
        return Math.floor(this.diffDays(this.toDateOnly(this.rangeStart), task.startDate) / 7)
      }

      const start = new Date(`${task.startDate}T00:00:00`)
      return ((start.getFullYear() - this.rangeStart.getFullYear()) * 12) +
        (start.getMonth() - this.rangeStart.getMonth())
    },

    taskDurationUnits(task) {
      if (this.localScale === 'days') {
        return Math.max(1, this.diffDays(task.startDate, task.endDate) + 1)
      }

      if (this.localScale === 'weeks') {
        return Math.max(1, Math.ceil((this.diffDays(task.startDate, task.endDate) + 1) / 7))
      }

      const start = new Date(`${task.startDate}T00:00:00`)
      const end = new Date(`${task.endDate}T00:00:00`)

      return Math.max(
        1,
        ((end.getFullYear() - start.getFullYear()) * 12) +
          (end.getMonth() - start.getMonth()) + 1
      )
    },

    durationLabel(task) {
      const days = this.diffDays(task.startDate, task.endDate) + 1
      return `${days}d`
    },

    estimationLabel(task) {
      const hours = Number(task.estimatedHours || 0)
      if (!hours) return '—'
      return `${hours}h`
    },

    statusClass(status) {
      const map = {
        TODO: 'status-todo',
        IN_PROGRESS: 'status-progress',
        BLOCKED: 'status-blocked',
        DONE: 'status-done'
      }

      return map[status] || 'status-todo'
    },

    assigneeLabel(assignee) {
      const name =
        [assignee?.firstName, assignee?.lastName]
          .filter(Boolean)
          .join(' ')
          .trim()

      return name || assignee?.username || String(assignee?.userId || 'U')
    },

    initials(text) {
      const safe = String(text || '').trim()
      if (!safe) return 'U'

      const parts = safe.split(/\s+/).slice(0, 2)
      return parts.map(part => part[0]?.toUpperCase() || '').join('')
    }
  }
}
</script>

<style scoped>
.gantt-page {
  width: 100%;
}

.gantt-toolbar {
  padding: 4px 0 8px;
}

.gantt-shell {
  border: 1px solid #e6e8ec;
  border-radius: 14px;
  overflow: auto;
  background: #fff;
  max-height: 72vh;
}

.gantt-table {
  min-width: max-content;
  background: #fff;
}

.gantt-header-row,
.gantt-row {
  display: flex;
  min-width: max-content;
}

.gantt-header-row {
  position: sticky;
  top: 0;
  z-index: 30;
}

.gantt-side {
  width: 640px;
  min-width: 640px;
  max-width: 640px;
  position: sticky;
  left: 0;
  border-right: 1px solid #e6e8ec;
  background: #fff;
}

.gantt-side-header {
  z-index: 40;
  background: #f8f9fb;
}

.gantt-side-body {
  z-index: 10;
}

.side-grid {
  display: grid;
  grid-template-columns: 60px minmax(260px, 1fr) 150px 100px 110px;
  align-items: center;
}

.side-grid-header {
  min-height: 52px;
  background: #f8f9fb;
  color: #4b5563;
  font-weight: 700;
  border-bottom: 1px solid #e6e8ec;
}

.side-grid-body {
  min-height: 64px;
  border-bottom: 1px solid #eef1f4;
  background: #fff;
}

.side-grid > div {
  padding: 0 12px;
  box-sizing: border-box;
}

.gantt-grid {
  position: relative;
}

.gantt-grid-header {
  display: flex;
  min-height: 52px;
  background: #f8f9fb;
  border-bottom: 1px solid #e6e8ec;
}

.time-header-cell {
  flex: 0 0 auto;
  min-height: 52px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-right: 1px solid #e6e8ec;
  color: #4b5563;
  font-weight: 700;
  font-size: 12px;
  padding: 0 8px;
  box-sizing: border-box;
  background: #f8f9fb;
}

.gantt-grid-body {
  min-height: 64px;
  display: flex;
  position: relative;
  border-bottom: 1px solid #eef1f4;
  background:
    linear-gradient(180deg, rgba(248, 250, 252, 0.9), rgba(255, 255, 255, 0.95));
}

.time-body-cell {
  flex: 0 0 auto;
  min-height: 64px;
  border-right: 1px solid #eef1f4;
  box-sizing: border-box;
}

.today-marker {
  position: absolute;
  top: 0;
  bottom: 0;
  width: 2px;
  background: rgba(25, 118, 210, 0.55);
  z-index: 2;
}

.task-bar {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  height: 24px;
  border-radius: 7px;
  display: flex;
  align-items: center;
  padding: 0 10px;
  color: #fff;
  font-size: 12px;
  font-weight: 700;
  z-index: 5;
  box-shadow: 0 6px 14px rgba(0, 0, 0, 0.10);
}

.task-bar-label-inside {
  display: block;
  width: 100%;
}

.task-bar-label-right {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  font-size: 12px;
  color: #5f6368;
  max-width: 220px;
  z-index: 6;
}

.status-todo {
  background: linear-gradient(90deg, #8e9aaf, #6c7a89);
}

.status-progress {
  background: linear-gradient(90deg, #c356c7, #a93eb0);
}

.status-blocked {
  background: linear-gradient(90deg, #eb5757, #d63031);
}

.status-done {
  background: linear-gradient(90deg, #52b788, #2d936c);
}

.task-name-cell {
  display: flex;
  align-items: center;
  min-width: 0;
}

.task-name-block {
  min-width: 0;
}

.task-name {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.task-dates {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.avatar-stack {
  display: flex;
  align-items: center;
}

.avatar-overlap {
  margin-left: -8px;
  border: 2px solid #fff;
  background: #e8edf5;
  color: #374151;
  font-size: 11px;
}

.avatar-overlap:first-child {
  margin-left: 0;
}

.extra-assignees {
  margin-left: 6px;
  font-size: 12px;
  color: #6b7280;
  font-weight: 600;
}

.empty-state {
  padding: 32px;
  text-align: center;
  color: #6b7280;
}

.ellipsis {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>