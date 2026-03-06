<template>
  <div class="museum-page">
    <div class="container">
      <div class="header">
        <button class="back-btn" @click="router.push('/city')">
          <i class="fas fa-arrow-left"></i> Назад в город
        </button>
        <h1><i class="fas fa-museum"></i> Музей Славы</h1>
        <p>Здесь собраны имена самых выдающихся жителей Джаваполиса</p>
      </div>

      <div v-if="loading" class="loading">
        <i class="fas fa-spinner fa-spin"></i> Загрузка лидеров...
      </div>

      <div v-else class="leaders-table-container">
        <table class="leaders-table">
          <thead>
            <tr>
              <th>Место</th>
              <th>Игрок</th>
              <th>Полисы</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(player, index) in players" :key="player.username" :class="{ 'top-three': index < 3 }">
              <td class="rank">
                <span v-if="index === 0" class="medal gold"><i class="fas fa-trophy"></i></span>
                <span v-else-if="index === 1" class="medal silver"><i class="fas fa-medal"></i></span>
                <span v-else-if="index === 2" class="medal bronze"><i class="fas fa-medal"></i></span>
                <span v-else>{{ index + 1 }}</span>
              </td>
              <td class="player-info">
                <img :src="player.avatar" :alt="player.username" class="avatar">
                <span class="username">{{ player.username }}</span>
              </td>
              <td class="coins">
                <i class="fas fa-coins"></i> {{ player.coins }}
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

const router = useRouter()
const players = ref([])
const loading = ref(true)

onMounted(async () => {
  try {
    const res = await axios.get('/api/museum/top-players')
    players.value = res.data
  } catch (e) {
    console.error('Failed to load top players', e)
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.museum-page {
  min-height: calc(100vh - 60px);
  background: #0f172a;
  color: #fff;
  padding: 40px 20px;
}

.container {
  max-width: 800px;
  margin: 0 auto;
}

.header {
  text-align: center;
  margin-bottom: 40px;
}

.back-btn {
  background: transparent;
  border: 1px solid rgba(255,255,255,0.2);
  color: #fff;
  padding: 8px 16px;
  border-radius: 8px;
  cursor: pointer;
  margin-bottom: 20px;
  transition: all 0.2s;
}

.back-btn:hover {
  background: rgba(255,255,255,0.1);
}

.header h1 {
  font-size: 2.5em;
  margin-bottom: 10px;
  color: #f39c12;
}

.header p {
  opacity: 0.7;
}

.leaders-table-container {
  background: #1e293b;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 10px 30px rgba(0,0,0,0.5);
}

.leaders-table {
  width: 100%;
  border-collapse: collapse;
}

.leaders-table th {
  text-align: left;
  padding: 20px;
  background: rgba(0,0,0,0.2);
  text-transform: uppercase;
  font-size: 0.8em;
  letter-spacing: 1px;
  opacity: 0.6;
}

.leaders-table td {
  padding: 15px 20px;
  border-bottom: 1px solid rgba(255,255,255,0.05);
}

.rank {
  font-weight: 700;
  width: 80px;
}

.medal {
  font-size: 1.2em;
}

.gold { color: #f1c40f; }
.silver { color: #bdc3c7; }
.bronze { color: #cd7f32; }

.player-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #334155;
}

.username {
  font-weight: 600;
}

.coins {
  color: #f39c12;
  font-weight: 700;
  text-align: right;
}

.top-three {
  background: rgba(243, 156, 18, 0.05);
}

.loading {
  text-align: center;
  padding: 50px;
  font-size: 1.2em;
  opacity: 0.7;
}
</style>
