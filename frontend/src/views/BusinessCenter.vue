<template>
  <div class="business-page">
    <div class="container">
      <div class="header">
        <button class="back-btn" @click="router.push('/city')">
          <i class="fas fa-arrow-left"></i> Назад в город
        </button>
        <h1><i class="fas fa-briefcase"></i> Бизнес-центр</h1>
        <p>Выполняйте задачи и зарабатывайте полисы для развития своего профиля</p>
      </div>

      <div v-if="loading" class="loading">
        <i class="fas fa-spinner fa-spin"></i> Загрузка задач...
      </div>

      <div v-else class="tasks-grid">
        <div v-for="task in tasks" :key="task.id" class="task-card">
          <div class="task-icon">
            <i :class="task.icon"></i>
          </div>
          <div class="task-info">
            <h3>{{ task.title }}</h3>
            <p>{{ task.description }}</p>
            <div class="reward">
              <i class="fas fa-coins"></i> +{{ task.reward }} полисов
            </div>
          </div>
          <button 
            class="complete-btn" 
            @click="completeTask(task)"
            :disabled="completing === task.id"
          >
            <i v-if="completing === task.id" class="fas fa-spinner fa-spin"></i>
            <span v-else>Выполнить</span>
          </button>
        </div>
      </div>

      <div v-if="successMessage" class="success-toast">
        <i class="fas fa-check-circle"></i> {{ successMessage }}
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, inject } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

const router = useRouter()
const user = inject('user')
const tasks = ref([])
const loading = ref(true)
const completing = ref(null)
const successMessage = ref('')

const loadTasks = async () => {
  try {
    const res = await axios.get('/api/business/tasks')
    tasks.value = res.data
  } catch (e) {
    console.error('Failed to load tasks', e)
  } finally {
    loading.value = false
  }
}

const completeTask = async (task) => {
  if (!user.value) {
    alert('Пожалуйста, войдите в систему')
    return
  }

  completing.value = task.id
  try {
    const res = await axios.post(`/api/business/tasks/${task.id}/complete`)
    // Обновляем монеты пользователя в глобальном состоянии, если оно есть
    if (user.value) {
      user.value.coins = res.data.newCoins
    }
    
    successMessage.value = `Задача "${task.title}" выполнена! +${task.reward} полисов`
    setTimeout(() => {
      successMessage.value = ''
    }, 3000)
  } catch (e) {
    console.error('Failed to complete task', e)
    alert('Ошибка при выполнении задачи')
  } finally {
    completing.value = null
  }
}

onMounted(loadTasks)
</script>

<style scoped>
.business-page {
  min-height: calc(100vh - 60px);
  background: #0f172a;
  color: #fff;
  padding: 40px 20px;
}

.container {
  max-width: 1000px;
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
}

.header h1 {
  font-size: 2.5em;
  color: #38bdf8;
  margin-bottom: 10px;
}

.tasks-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 25px;
}

.task-card {
  background: #1e293b;
  border-radius: 16px;
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 20px;
  border: 1px solid rgba(255,255,255,0.05);
  transition: transform 0.3s;
}

.task-card:hover {
  transform: translateY(-5px);
  border-color: #38bdf8;
}

.task-icon {
  width: 60px;
  height: 60px;
  background: rgba(56, 189, 248, 0.1);
  color: #38bdf8;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5em;
}

.task-info h3 {
  margin: 0 0 8px 0;
  font-size: 1.25em;
}

.task-info p {
  opacity: 0.7;
  font-size: 0.95em;
  line-height: 1.5;
  margin-bottom: 15px;
}

.reward {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: #f39c12;
  font-weight: 700;
  background: rgba(243, 156, 18, 0.1);
  padding: 4px 10px;
  border-radius: 6px;
  font-size: 0.9em;
}

.complete-btn {
  margin-top: auto;
  background: #38bdf8;
  color: #000;
  border: none;
  padding: 12px;
  border-radius: 8px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s;
}

.complete-btn:hover:not(:disabled) {
  background: #7dd3fc;
}

.complete-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.loading {
  text-align: center;
  padding: 50px;
  opacity: 0.5;
}

.success-toast {
  position: fixed;
  bottom: 30px;
  right: 30px;
  background: #10b981;
  color: white;
  padding: 15px 25px;
  border-radius: 12px;
  box-shadow: 0 10px 20px rgba(0,0,0,0.3);
  display: flex;
  align-items: center;
  gap: 12px;
  animation: slideIn 0.3s ease-out;
  z-index: 1000;
}

@keyframes slideIn {
  from { transform: translateX(100%); opacity: 0; }
  to { transform: translateX(0); opacity: 1; }
}
</style>
