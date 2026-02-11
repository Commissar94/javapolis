<template>
  <div class="profile-page">
    <div v-if="loading" class="loading">
      <i class="fas fa-spinner fa-spin"></i> Загрузка профиля...
    </div>
    <div v-else-if="error" class="error">
      {{ error }}
    </div>
    <div v-else-if="profile" class="profile-container">
      <div class="profile-header">
        <div class="profile-avatar">
          <i class="fas fa-user-circle"></i>
        </div>
        <div class="profile-info">
          <h1>{{ profile.firstName && profile.lastName ? `${profile.firstName} ${profile.lastName}` : profile.username }}</h1>
          <p class="username">@{{ profile.username }}</p>
          <p class="join-date">На сайте с {{ formatDate(profile.createdAt) }}</p>
        </div>
      </div>

      <div class="stats-grid">
        <div class="stat-card">
          <div class="stat-value">{{ profile.completedTopics }}</div>
          <div class="stat-label">Пройдено лекций</div>
        </div>
        <div class="stat-card">
          <div class="stat-value">{{ profile.totalTopics }}</div>
          <div class="stat-label">Всего лекций</div>
        </div>
        <div class="stat-card">
          <div class="stat-value">{{ profile.completionPercentage }}%</div>
          <div class="stat-label">Прогресс курса</div>
        </div>
      </div>

      <div class="progress-section">
        <h3>Прогресс обучения</h3>
        <div class="progress-bar-container">
          <div class="progress-bar" :style="{ width: profile.completionPercentage + '%' }"></div>
        </div>
        <div class="progress-text">
          Завершено {{ profile.completedTopics }} из {{ profile.totalTopics }} материалов
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import axios from 'axios'

const route = useRoute()
const profile = ref(null)
const loading = ref(true)
const error = ref(null)

const fetchProfile = async () => {
  loading.value = true
  error.value = null
  try {
    const username = route.params.username
    const res = await axios.get(`/api/profile/${username}`)
    profile.value = res.data
  } catch (e) {
    console.error('Failed to fetch profile', e)
    error.value = e.response?.status === 404 
      ? 'Пользователь не найден' 
      : 'Произошла ошибка при загрузке профиля'
  } finally {
    loading.value = false
  }
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleDateString('ru-RU', {
    day: 'numeric',
    month: 'long',
    year: 'numeric'
  })
}

onMounted(fetchProfile)

watch(() => route.params.username, fetchProfile)
</script>

<style scoped>
.profile-page {
  max-width: 800px;
  margin: 40px auto;
  padding: 0 20px;
}

.profile-container {
  background: var(--card-bg);
  border: 1px solid var(--border-color);
  border-radius: 12px;
  padding: 40px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.profile-header {
  display: flex;
  align-items: center;
  gap: 30px;
  margin-bottom: 40px;
}

.profile-avatar {
  font-size: 80px;
  color: var(--accent-color);
}

.profile-info h1 {
  margin: 0 0 5px 0;
  font-size: 2rem;
  color: var(--header-color);
}

.username {
  color: var(--accent-color);
  font-weight: 600;
  margin: 0 0 10px 0;
}

.join-date {
  font-size: 0.9rem;
  color: var(--text-color);
  opacity: 0.8;
  margin: 0;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  margin-bottom: 40px;
}

.stat-card {
  background: var(--bg-color);
  border: 1px solid var(--border-color);
  border-radius: 10px;
  padding: 20px;
  text-align: center;
}

.stat-value {
  font-size: 2rem;
  font-weight: 700;
  color: var(--accent-color);
  margin-bottom: 5px;
}

.stat-label {
  font-size: 0.9rem;
  color: var(--text-color);
  opacity: 0.9;
}

.progress-section h3 {
  margin-bottom: 15px;
  color: var(--header-color);
}

.progress-bar-container {
  height: 12px;
  background: var(--bg-color);
  border: 1px solid var(--border-color);
  border-radius: 6px;
  overflow: hidden;
  margin-bottom: 10px;
}

.progress-bar {
  height: 100%;
  background: #4CAF50;
  transition: width 0.5s ease;
}

.progress-text {
  font-size: 0.9rem;
  color: var(--text-color);
  opacity: 0.9;
  text-align: right;
}

.loading, .error {
  text-align: center;
  padding: 40px;
  font-size: 1.2rem;
}

.error {
  color: #e74c3c;
}

@media (max-width: 600px) {
  .profile-header {
    flex-direction: column;
    text-align: center;
  }
  
  .stats-grid {
    grid-template-columns: 1fr;
  }
}
</style>
