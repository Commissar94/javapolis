<template>
  <div class="comment-section">
    <div class="comments-header">
      <h3><i class="fas fa-comments"></i> Комментарии</h3>
    </div>

    <!-- Список комментариев -->
    <div v-if="loading" class="comments-loading">
      <i class="fas fa-spinner fa-spin"></i> Загрузка комментариев...
    </div>
    <div v-else-if="comments.length === 0" class="no-comments">
      Пока нет комментариев. Будьте первым!
    </div>
    <div v-else class="comments-list">
      <div v-for="comment in comments" :key="comment.id" class="comment-item">
        <div class="comment-meta">
          <span class="comment-author">
            <router-link :to="'/profile/' + comment.authorUsername">{{ comment.authorName }}</router-link>
          </span>
          <span class="comment-date">{{ formatDate(comment.createdAt) }}</span>
        </div>
        <div class="comment-content">
          {{ comment.content }}
        </div>
      </div>
    </div>

    <!-- Форма добавления комментария -->
    <div class="add-comment-form">
      <div v-if="isAuthenticated">
        <textarea 
          v-model="newComment" 
          placeholder="Оставьте ваш комментарий..." 
          :disabled="submitting"
          rows="3"
        ></textarea>
        <div class="form-actions">
          <button 
            class="submit-btn" 
            :disabled="submitting || !newComment.trim()" 
            @click="submitComment"
          >
            <span v-if="submitting"><i class="fas fa-spinner fa-spin"></i> Отправка...</span>
            <span v-else>Отправить</span>
          </button>
        </div>
        <div v-if="error" class="error-msg">{{ error }}</div>
      </div>
      <div v-else class="login-prompt">
        <i class="fas fa-info-circle"></i> 
        Пожалуйста, <router-link to="/login">войдите</router-link>, чтобы оставлять комментарии.
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import axios from 'axios'

const props = defineProps({
  topicPath: {
    type: String,
    required: true
  },
  page: {
    type: Number,
    default: 0
  }
})

const comments = ref([])
const loading = ref(false)
const submitting = ref(false)
const newComment = ref('')
const isAuthenticated = ref(false)
const error = ref('')

const fetchAuthStatus = async () => {
  try {
    const res = await axios.get('/api/auth/status')
    isAuthenticated.value = res.data.authenticated
  } catch (e) {
    isAuthenticated.value = false
  }
}

const fetchComments = async () => {
  if (!props.topicPath) return
  loading.value = true
  try {
    const cleanPath = props.topicPath.replace('.md', '')
    const res = await axios.get('/api/comments', {
      params: { 
        topicPath: cleanPath,
        page: props.page
      }
    })
    comments.value = res.data
  } catch (e) {
    console.error('Failed to fetch comments', e)
  } finally {
    loading.value = false
  }
}

const submitComment = async () => {
  if (!newComment.value.trim()) return
  
  submitting.value = true
  error.value = ''
  try {
    const cleanPath = props.topicPath.replace('.md', '')
    const res = await axios.post('/api/comments', {
      topicPath: cleanPath,
      page: props.page,
      content: newComment.value
    })
    comments.value.unshift(res.data)
    newComment.value = ''
  } catch (e) {
    error.value = e.response?.data || 'Не удалось отправить комментарий'
  } finally {
    submitting.value = false
  }
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleString('ru-RU', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

onMounted(() => {
  fetchAuthStatus()
  fetchComments()
})

watch(() => [props.topicPath, props.page], () => {
  fetchComments()
})
</script>

<style scoped>
.comment-section {
  margin-top: 40px;
  padding-top: 30px;
  border-top: 1px solid var(--border-color);
  text-align: left;
}

.comments-header h3 {
  margin-bottom: 20px;
  color: var(--header-color);
  font-size: 1.5rem;
}

.comments-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
  margin-bottom: 30px;
}

.comment-item {
  background: var(--card-bg);
  padding: 15px;
  border-radius: 8px;
  border-left: 4px solid #4CAF50;
  border-bottom: 1px solid var(--border-color);
}

.comment-meta {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 0.9rem;
}

.comment-author a {
  font-weight: bold;
  color: var(--accent-color);
  text-decoration: none;
}

.comment-author a:hover {
  text-decoration: underline;
}

.comment-author a:visited {
  color: var(--accent-color);
}

.comment-date {
  color: var(--text-color);
  opacity: 0.6;
}

.comment-content {
  line-height: 1.5;
  color: var(--text-color);
  white-space: pre-wrap;
}

.no-comments, .comments-loading {
  text-align: center;
  padding: 20px;
  color: var(--text-color);
  opacity: 0.6;
  font-style: italic;
}

.add-comment-form {
  margin-top: 30px;
  background: var(--card-bg);
  padding: 20px;
  border: 1px solid var(--border-color);
  border-radius: 8px;
}

textarea {
  width: 100%;
  padding: 12px;
  border: 1px solid var(--border-color);
  background: var(--bg-color);
  color: var(--text-color);
  border-radius: 6px;
  resize: vertical;
  margin-bottom: 15px;
  font-family: inherit;
}

textarea:focus {
  outline: none;
  border-color: #4CAF50;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
}

.submit-btn {
  background: #4CAF50;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 500;
  transition: background 0.2s;
}

.submit-btn:hover:not(:disabled) {
  background: #45a049;
}

.submit-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.login-prompt {
  text-align: center;
  padding: 10px;
  color: var(--text-color);
  background: var(--hover-bg);
  border-radius: 6px;
}

.login-prompt a {
  color: #4CAF50;
  text-decoration: none;
  font-weight: bold;
}

.error-msg {
  color: #e74c3c;
  margin-top: 10px;
  font-size: 0.9rem;
}
</style>
