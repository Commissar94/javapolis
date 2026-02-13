<template>
  <div v-if="isOpen" class="modal-overlay" @click.self="close">
    <div class="modal-content">
      <div class="modal-header">
        <h3>Доступ ограничен</h3>
        <button class="close-btn" @click="close">
          <i class="fas fa-times"></i>
        </button>
      </div>
      <div class="modal-body">
        <div class="icon-container">
          <i class="fas fa-lock"></i>
        </div>
        <p>Чтобы получить доступ к курсам и лекциям, необходимо войти в систему или зарегистрироваться.</p>
      </div>
      <div class="modal-footer">
        <button class="auth-btn login" @click="openLogin">Войти / Регистрация</button>
        <button class="auth-btn secondary" @click="close">Позже</button>
      </div>
    </div>
  </div>
</template>

<script setup>
const props = defineProps({
  isOpen: Boolean
})

const emit = defineEmits(['close', 'open-login'])

const close = () => {
  emit('close')
}

const openLogin = () => {
  emit('open-login')
  close()
}
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 2100;
  backdrop-filter: blur(4px);
}

.modal-content {
  background: var(--content-bg);
  border: 1px solid var(--border-color);
  border-radius: 16px;
  width: 90%;
  max-width: 400px;
  padding: 24px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.4);
  color: var(--text-color);
  text-align: center;
  animation: scale-in 0.3s ease-out;
}

@keyframes scale-in {
  from { transform: scale(0.9); opacity: 0; }
  to { transform: scale(1); opacity: 1; }
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.modal-header h3 {
  margin: 0;
  font-size: 1.4rem;
  color: var(--header-color);
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

.icon-container {
  font-size: 4rem;
  color: var(--accent-color);
  margin-bottom: 20px;
}

.modal-body p {
  font-size: 1.1rem;
  line-height: 1.5;
  margin-bottom: 24px;
}

.modal-footer {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.auth-btn {
  padding: 12px;
  border-radius: 8px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  border: none;
}

.auth-btn.login {
  background: var(--accent-color);
  color: white;
}

.auth-btn.secondary {
  background: transparent;
  color: var(--text-color);
  border: 1px solid var(--border-color);
}

.auth-btn:hover {
  transform: translateY(-2px);
  filter: brightness(1.1);
}
</style>
