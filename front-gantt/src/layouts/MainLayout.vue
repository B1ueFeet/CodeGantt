<template>
  <q-layout view="lHh Lpr lFf">
    <q-header elevated class="bg-primary text-white">
      <q-toolbar>
        <q-btn
          flat
          dense
          round
          icon="menu"
          aria-label="Menu"
          @click="toggleLeftDrawer"
        />

        <q-toolbar-title>
          CodeGantt Admin
        </q-toolbar-title>

        <div class="row items-center q-gutter-sm">
          <q-chip
            v-if="session && session.roles && session.roles.length"
            color="white"
            text-color="primary"
            dense
          >
            {{ session.roles.join(', ') }}
          </q-chip>

          <q-btn
            flat
            dense
            icon="logout"
            label="Salir"
            @click="doLogout"
          />
        </div>
      </q-toolbar>
    </q-header>

    <q-drawer v-model="leftDrawerOpen" show-if-above bordered>
      <q-list padding>
        <q-item-label header>
          Navegación
        </q-item-label>

        <q-item clickable v-ripple to="/">
          <q-item-section avatar>
            <q-icon name="dashboard" />
          </q-item-section>
          <q-item-section>
            Admin
          </q-item-section>
        </q-item>

        <q-item clickable v-ripple to="/test">
          <q-item-section avatar>
            <q-icon name="api" />
          </q-item-section>
          <q-item-section>
            Test endpoint
          </q-item-section>
        </q-item>

        <q-separator class="q-my-md" />

        <q-item>
          <q-item-section>
            <q-item-label caption>Usuario</q-item-label>
            <q-item-label>{{ session ? (session.username || session.email || 'Sesión activa') : 'Invitado' }}</q-item-label>
          </q-item-section>
        </q-item>

        <q-item clickable v-ripple @click="doLogout">
          <q-item-section avatar>
            <q-icon name="logout" />
          </q-item-section>
          <q-item-section>
            Cerrar sesión
          </q-item-section>
        </q-item>
      </q-list>
    </q-drawer>

    <q-page-container>
      <router-view />
    </q-page-container>
  </q-layout>
</template>

<script>
import { logout, getSession } from 'src/services/auth'

export default {
  name: 'MainLayout',

  data() {
    return {
      leftDrawerOpen: false,
      session: null
    }
  },

  mounted() {
    this.session = getSession()
  },

  methods: {
    toggleLeftDrawer() {
      this.leftDrawerOpen = !this.leftDrawerOpen
    },

    doLogout() {
      logout()
      this.$router.push('/auth')
    }
  }
}
</script>