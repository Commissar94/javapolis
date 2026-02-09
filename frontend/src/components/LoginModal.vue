<template>
  <div v-if="isOpen" class="modal-overlay" @click.self="close">
    <div class="modal-content">
      <div class="modal-header">
        <div class="auth-tabs">
          <button 
            class="tab-btn" 
            :class="{ active: mode === 'login' }" 
            @click="mode = 'login'"
          >Вход</button>
          <button 
            class="tab-btn" 
            :class="{ active: mode === 'register' }" 
            @click="mode = 'register'"
          >Регистрация</button>
        </div>
        <button class="close-btn" @click="close">
          <i class="fas fa-times"></i>
        </button>
      </div>
      
      <form v-if="mode === 'login'" @submit.prevent="handleLogin" class="auth-form">
        <div class="form-group">
          <label for="login-username">Имя пользователя</label>
          <input 
            type="text" 
            id="login-username" 
            v-model="loginUsername" 
            placeholder="Введите имя" 
            required 
            ref="usernameInput"
          >
        </div>
        <div class="form-group">
          <label for="login-password">Пароль</label>
          <input 
            type="password" 
            id="login-password" 
            v-model="loginPassword" 
            placeholder="Введите пароль" 
            required
          >
        </div>
        <div v-if="error" class="error-message">
          {{ error }}
        </div>
        <div class="form-actions">
          <button type="submit" :disabled="loading" class="submit-btn">
            <span v-if="loading">Вход...</span>
            <span v-else>Войти</span>
          </button>
        </div>
      </form>

      <form v-else @submit.prevent="handleRegister" class="auth-form">
        <div class="form-group">
          <label for="reg-username">Имя пользователя *</label>
          <input 
            type="text" 
            id="reg-username" 
            v-model="regUsername" 
            placeholder="Придумайте логин" 
            required
          >
        </div>
        <div class="form-group">
          <label for="reg-email">Email *</label>
          <input 
            type="email" 
            id="reg-email" 
            v-model="regEmail" 
            placeholder="example@mail.com" 
            required
          >
        </div>
        <div class="form-group">
          <label for="reg-password">Пароль *</label>
          <input 
            type="password" 
            id="reg-password" 
            v-model="regPassword" 
            placeholder="Минимум 8 символов" 
            required
          >
        </div>
        <div class="form-group">
          <label for="reg-confirm">Подтвердите пароль *</label>
          <input 
            type="password" 
            id="reg-confirm" 
            v-model="regConfirm" 
            placeholder="Повторите пароль" 
            required
          >
        </div>
        <div v-if="error" class="error-message">
          {{ error }}
        </div>
        <div v-if="success" class="success-message">
          {{ success }}
        </div>
        <div class="form-actions">
          <button type="submit" :disabled="loading" class="submit-btn">
            <span v-if="loading">Загрузка...</span>
            <span v-else>Создать аккаунт</span>
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, nextTick } from 'vue'
import axios from 'axios'

const props = defineProps({
  isOpen: Boolean
})

const emit = defineEmits(['close', 'login-success'])

const mode = ref('login')
const loginUsername = ref('')
const loginPassword = ref('')
const regUsername = ref('')
const regEmail = ref('')
const regPassword = ref('')
const regConfirm = ref('')
const error = ref('')
const success = ref('')
const loading = ref(false)
const usernameInput = ref(null)

watch(() => props.isOpen, (newVal) => {
  if (newVal) {
    error.value = ''
    success.value = ''
    loginUsername.value = ''
    loginPassword.value = ''
    regUsername.value = ''
    regEmail.value = ''
    regPassword.value = ''
    regConfirm.value = ''
    nextTick(() => {
      if (usernameInput.value) usernameInput.value.focus()
    })
  }
})

const close = () => {
  emit('close')
}

const handleLogin = async () => {
  loading.value = true
  error.value = ''
  try {
    const response = await axios.post('/api/auth/login', {
      username: loginUsername.value,
      password: loginPassword.value
    })
    if (response.data.success) {
      emit('login-success', response.data.username)
      close()
    } else {
      error.value = response.data.message || 'Ошибка входа'
    }
  } catch (err) {
    if (err.response && err.response.data && err.response.data.message) {
      error.value = err.response.data.message
    } else {
      error.value = 'Не удалось подключиться к серверу'
    }
  } finally {
    loading.value = false
  }
}

const handleRegister = async () => {
  if (regPassword.value !== regConfirm.value) {
    error.value = 'Пароли не совпадают'
    return
  }
  if (regPassword.value.length < 8) {
    error.value = 'Пароль слишком короткий'
    return
  }

  loading.value = true
  error.value = ''
  success.value = ''
  try {
    const response = await axios.post('/api/registration/register', {
      username: regUsername.value,
      email: regEmail.value,
      password: regPassword.value
    })
    if (response.data.success) {
      success.value = 'Регистрация успешна! Входим...'
      // Пытаемся сразу войти
      setTimeout(async () => {
        try {
          const loginRes = await axios.post('/api/auth/login', {
            username: regUsername.value,
            password: regPassword.value
          })
          if (loginRes.data.success) {
            emit('login-success', loginRes.data.username)
            close()
          } else {
            mode.value = 'login'
            loginUsername.value = regUsername.value
            success.value = ''
          }
        } catch (e) {
          mode.value = 'login'
          loginUsername.value = regUsername.value
          success.value = ''
        }
      }, 1000)
    } else {
      error.value = response.data.message || 'Ошибка регистрации'
    }
  } catch (err) {
    if (err.response && err.response.data && err.response.data.message) {
      error.value = err.response.data.message
    } else {
      error.value = 'Ошибка при регистрации'
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 2000;
  backdrop-filter: blur(2px);
}

.modal-content {
  background: var(--content-bg);
  border: 1px solid var(--border-color);
  border-radius: 12px;
  width: 100%;
  max-width: 400px;
  padding: 24px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.2);
  color: var(--text-color);
  animation: slide-up 0.3s ease-out;
}

@keyframes slide-up {
  from { transform: translateY(20px); opacity: 0; }
  to { transform: translateY(0); opacity: 1; }
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.auth-tabs {
  display: flex;
  gap: 20px;
}

.tab-btn {
  background: none;
  border: none;
  font-size: 1.2rem;
  font-weight: 600;
  color: var(--text-color);
  opacity: 0.5;
  cursor: pointer;
  padding: 4px 0;
  transition: all 0.2s;
  border-bottom: 2px solid transparent;
}

.tab-btn.active {
  opacity: 1;
  border-bottom-color: var(--accent-color);
}

.tab-btn:hover {
  opacity: 0.8;
}

.close-btn {
  background: none;
  border: none;
  color: var(--text-color);
  cursor: pointer;
  font-size: 1.2rem;
  opacity: 0.6;
  transition: opacity 0.2s;
}

.close-btn:hover {
  opacity: 1;
}

.auth-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.success-message {
  color: #10b981;
  font-size: 0.9rem;
  background: rgba(16, 185, 129, 0.1);
  padding: 8px 12px;
  border-radius: 6px;
  border: 1px solid rgba(16, 185, 129, 0.2);
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
  text-align: left;
}

.form-group label {
  font-size: 0.9rem;
  font-weight: 600;
  color: var(--text-color);
}

.form-group input {
  padding: 10px 12px;
  border-radius: 6px;
  border: 1px solid var(--border-color);
  background: var(--bg-color);
  color: var(--text-color);
  font-size: 1rem;
  transition: border-color 0.2s;
}

.form-group input:focus {
  outline: none;
  border-color: var(--accent-color);
}

.error-message {
  color: #ef4444;
  font-size: 0.9rem;
  background: rgba(239, 68, 68, 0.1);
  padding: 8px 12px;
  border-radius: 6px;
  border: 1px solid rgba(239, 68, 68, 0.2);
}

.submit-btn {
  background: var(--accent-color);
  color: white;
  border: none;
  padding: 12px;
  border-radius: 6px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: opacity 0.2s;
  margin-top: 8px;
}

.submit-btn:hover {
  opacity: 0.9;
}

.submit-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>
