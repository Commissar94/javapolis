<template>
  <div class="city">
    <h1>Город</h1>
    <p>{{ message }}</p>
    <div class="locations">
      <div class="location-card">Университет</div>
      <div class="location-card">Библиотека</div>
      <div class="location-card">Мэрия</div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'

const message = ref('Загрузка...')

onMounted(async () => {
  try {
    const response = await axios.get('/api/city')
    message.value = response.data.message
  } catch (e) {
    message.value = 'Ошибка загрузки данных из API'
  }
})
</script>

<style scoped>
.locations {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-top: 20px;
}
.location-card {
  padding: 20px;
  border: 1px solid var(--border-color);
  background-color: var(--card-bg);
  color: var(--text-color);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}
.location-card:hover {
  background-color: var(--hover-bg);
  transform: translateY(-2px);
}
</style>
