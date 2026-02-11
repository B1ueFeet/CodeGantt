<template>
  <q-page class="q-pa-md flex flex-center">
    <q-card style="width: 420px; max-width: 95vw;">
      <q-card-section>
        <div class="text-h6">CodeGantt</div>
        <div class="text-caption text-grey-7">Inicia sesión o crea una cuenta</div>
      </q-card-section>

      <q-separator />

      <q-card-section>
        <q-tabs v-model="tab" dense>
          <q-tab name="login" label="Login" />
          <q-tab name="register" label="Registro" />
        </q-tabs>

        <q-separator class="q-my-sm" />

        <!-- LOGIN -->
        <div v-if="tab === 'login'" class="q-gutter-md">
          <q-input
            v-model="loginForm.identifier"
            label="Email o usuario"
            outlined dense
            autocomplete="username"
          />
          <q-input
            v-model="loginForm.password"
            label="Contraseña"
            type="password"
            outlined dense
            autocomplete="current-password"
          />

          <q-btn
            label="Entrar"
            color="primary"
            class="full-width"
            :loading="loading"
            @click="doLogin"
          />
        </div>

        <!-- REGISTER -->
        <div v-else class="q-gutter-md">
          <div class="row q-col-gutter-sm">
            <div class="col">
              <q-input v-model="reg.firstName" label="Nombre" outlined dense />
            </div>
            <div class="col">
              <q-input v-model="reg.lastName" label="Apellido" outlined dense />
            </div>
          </div>

          <q-input v-model="reg.username" label="Usuario" outlined dense autocomplete="username" />
          <q-input v-model="reg.email" label="Email" outlined dense autocomplete="email" />
          <q-input v-model="reg.password" label="Contraseña" type="password" outlined dense autocomplete="new-password" />

          <q-btn
            label="Crear cuenta"
            color="secondary"
            class="full-width"
            :loading="loading"
            @click="doRegister"
          />
        </div>

        <q-banner v-if="error" class="q-mt-md" rounded dense>
          {{ error }}
        </q-banner>
      </q-card-section>
    </q-card>
  </q-page>
</template>

<script>
import { login, register } from 'src/services/auth'

export default {
  name: 'AuthPage',

  data () {
    return {
      tab: 'login',
      loading: false,
      error: '',
      loginForm: {
        identifier: '',
        password: ''
      },
      reg: {
        firstName: '',
        lastName: '',
        username: '',
        email: '',
        password: ''
      }
    }
  },

  methods: {
    async doLogin () {
      this.error = ''
      this.loading = true
      try {
        await login(this.loginForm)
        this.$router.push({ path: '/' })
      } catch (e) {
        this.error = e?.data?.message || e?.message || 'No se pudo iniciar sesión'
      } finally {
        this.loading = false
      }
    },

    async doRegister () {
      this.error = ''
      this.loading = true
      try {
        await register(this.reg)
        this.$router.push({ path: '/' })
      } catch (e) {
        this.error = e?.data?.message || e?.message || 'No se pudo registrar'
      } finally {
        this.loading = false
      }
    }
  }
}
</script>
