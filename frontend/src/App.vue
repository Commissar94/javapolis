<template>
  <div id="layout" :class="theme">
    <nav>
      <div class="nav-left">
        <router-link to="/" class="nav-link">Главная</router-link>
        <router-link to="/city" class="nav-link">Город</router-link>
        <router-link to="/university" class="nav-link">Университет</router-link>
      </div>
      
      <div class="nav-right">
        <template v-if="user">
          <router-link :to="'/profile/' + user.username" class="username-link">
            <span class="username">
              <i class="fas fa-user"></i>
              {{ user.username }}
            </span>
          </router-link>
          <button @click="handleLogout" class="auth-btn logout">Выйти</button>
        </template>
        <template v-else>
          <button @click="isLoginModalOpen = true" class="auth-btn login">Личный кабинет</button>
        </template>

        <button @click="toggleTheme" class="theme-toggle">
          <i :class="theme === 'dark' ? 'fas fa-sun' : 'fas fa-moon'"></i>
        </button>
      </div>
    </nav>
    <main>
      <router-view />
    </main>

    <LoginModal 
      :is-open="isLoginModalOpen" 
      @close="isLoginModalOpen = false"
      @login-success="handleLoginSuccess"
    />
  </div>
</template>

<script setup>
import { ref, onMounted, provide } from 'vue'
import axios from 'axios'
import LoginModal from './components/LoginModal.vue'

const theme = ref('dark')
const user = ref(null)
const isLoginModalOpen = ref(false)

provide('openLogin', () => {
  isLoginModalOpen.value = true
})

provide('user', user)

onMounted(async () => {
  const savedTheme = localStorage.getItem('theme')
  if (savedTheme) {
    theme.value = savedTheme
  } else {
    // По умолчанию темная тема, как просил пользователь
    theme.value = 'dark'
  }
  await checkAuthStatus()
})

const checkAuthStatus = async () => {
  try {
    const response = await axios.get('/api/auth/status')
    if (response.data.authenticated) {
      user.value = { username: response.data.username }
    }
  } catch (e) {
    console.error('Failed to check auth status', e)
  }
}

const handleLoginSuccess = (username) => {
  user.value = { username }
}

const handleLogout = async () => {
  try {
    await axios.post('/api/auth/logout')
  } catch (e) {
    console.error('Logout failed', e)
  } finally {
    user.value = null
    // После выхода перенаправляем на главную, если мы в защищенной зоне
    // Но для начала просто обновим статус
    await checkAuthStatus()
  }
}

const toggleTheme = () => {
  theme.value = theme.value === 'light' ? 'dark' : 'light'
  localStorage.setItem('theme', theme.value)
}
</script>

<style>
:root {
  /* Light Theme Variables */
  --bg-color: #ffffff;
  --text-color: #2c3e50;
  --nav-bg: #f8f9fa;
  --link-color: #2c3e50;
  --link-active: #42b983;
  --border-color: #eef2f6;
  --sidebar-bg: #fdfdfd;
  --content-bg: #ffffff;
  --card-bg: #ffffff;
  --hover-bg: #f1f5f9;
  --accent-color: #0284c7;
  --header-color: #1e293b;
}

#layout.dark {
  /* Dark Theme Variables */
  --bg-color: #1e1e1e;
  --text-color: #d4d4d4;
  --nav-bg: #252526;
  --link-color: #cccccc;
  --link-active: #42b983;
  --border-color: #333333;
  --sidebar-bg: #1e1e1e;
  --content-bg: #1e1e1e;
  --card-bg: #2d2d2d;
  --hover-bg: #37373d;
  --accent-color: #4fc1ff;
  --header-color: #ffffff;
}

html, body {
  margin: 0;
  padding: 0;
  background-color: var(--bg-color);
  color: var(--text-color);
  transition: background-color 0.3s, color 0.3s;
  min-height: 100vh;
}

#layout {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  min-height: 100vh;
  background-color: var(--bg-color);
}

nav {
  padding: 0 30px;
  height: 60px;
  background-color: var(--nav-bg);
  border-bottom: 1px solid var(--border-color);
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: sticky;
  top: 0;
  z-index: 1000;
}

.nav-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.nav-link {
  text-decoration: none;
  color: var(--link-color);
  padding: 8px 16px;
  border-radius: 6px;
  font-weight: 500;
  transition: all 0.2s;
  font-size: 0.95rem;
}

.nav-link:hover {
  background-color: var(--hover-bg);
  color: var(--text-color);
}

.nav-link.router-link-active {
  color: var(--accent-color);
  background-color: rgba(2, 132, 199, 0.1);
  font-weight: 600;
}

#layout.dark .nav-link.router-link-active {
  background-color: rgba(79, 193, 255, 0.1);
}

.nav-right {
  display: flex;
  align-items: center;
  gap: 15px;
}

.username-link {
  text-decoration: none;
}

.username {
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--text-color);
}

.auth-btn {
  padding: 8px 16px;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 600;
  transition: all 0.2s;
  border: 1px solid var(--border-color);
  background-color: var(--card-bg);
  color: var(--text-color);
  font-size: 0.9rem;
}

.auth-btn.login {
  background-color: var(--accent-color);
  color: white;
  border: none;
}

.auth-btn.logout {
  background-color: transparent;
}

.auth-btn:hover {
  filter: brightness(1.1);
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.theme-toggle {
  background: none;
  border: 1px solid var(--border-color);
  padding: 8px 12px;
  border-radius: 20px;
  cursor: pointer;
  color: var(--text-color);
  transition: all 0.3s;
}

.theme-toggle:hover {
  background-color: var(--hover-bg);
}
</style>
