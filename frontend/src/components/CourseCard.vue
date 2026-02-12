<template>
  <div class="course-card" @click="$emit('click')">
    <div class="course-icon">
      <i class="fas fa-graduation-cap"></i>
    </div>
    <div class="course-info">
      <h3 class="course-name">{{ course.name }}</h3>
      <div class="course-meta">
        <span class="topic-count">
          <i class="fas fa-book"></i> {{ topicCount }} тем
        </span>
        <div v-if="course.completed" class="completed-badge">
          <i class="fas fa-check-circle"></i> Завершен
        </div>
      </div>
      <div class="progress-container">
        <div class="progress-bar">
          <div class="progress-fill" :style="{ width: progressPercentage + '%' }"></div>
        </div>
        <span class="progress-text">{{ progressPercentage }}%</span>
      </div>
    </div>
    <div class="course-action">
      <button class="start-btn">Начать обучение</button>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  course: {
    type: Object,
    required: true
  }
})

defineEmits(['click'])

const topicCount = computed(() => {
  return countFiles(props.course.children)
})

const progressPercentage = computed(() => {
  const total = countFiles(props.course.children)
  if (total === 0) return 0
  const completed = countCompletedFiles(props.course.children)
  return Math.round((completed / total) * 100)
})

function countFiles(items) {
  let count = 0
  if (!items) return 0
  for (const item of items) {
    if (item.type === 'file') count++
    else if (item.type === 'folder') count += countFiles(item.children)
  }
  return count
}

function countCompletedFiles(items) {
  let count = 0
  if (!items) return 0
  for (const item of items) {
    if (item.type === 'file' && item.completed) count++
    else if (item.type === 'folder') count += countCompletedFiles(item.children)
  }
  return count
}
</script>

<style scoped>
.course-card {
  background: var(--sidebar-bg);
  border: 1px solid var(--border-color);
  border-radius: 12px;
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  height: 100%;
}

.course-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0,0,0,0.1);
  border-color: var(--accent-color);
}

.course-icon {
  width: 50px;
  height: 50px;
  background: rgba(2, 132, 199, 0.1);
  color: var(--accent-color);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5em;
}

.course-info {
  flex: 1;
}

.course-name {
  margin: 0 0 10px 0;
  font-size: 1.25em;
  font-weight: 700;
  color: var(--text-color);
}

.course-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  font-size: 0.9em;
  color: #64748b;
}

.completed-badge {
  color: #10b981;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 5px;
}

.progress-container {
  display: flex;
  align-items: center;
  gap: 12px;
}

.progress-bar {
  flex: 1;
  height: 8px;
  background: var(--border-color);
  border-radius: 4px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: var(--accent-color);
  transition: width 0.5s ease;
}

.progress-text {
  font-size: 0.85em;
  font-weight: 600;
  color: var(--text-color);
  min-width: 35px;
}

.start-btn {
  width: 100%;
  padding: 10px;
  background: transparent;
  border: 2px solid var(--accent-color);
  color: var(--accent-color);
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.course-card:hover .start-btn {
  background: var(--accent-color);
  color: white;
}
</style>
