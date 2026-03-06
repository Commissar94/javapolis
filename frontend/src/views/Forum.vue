<template>
  <div class="forum-page">
    <div class="container">
      <div class="header">
        <button class="back-btn" @click="router.push('/city')">
          <i class="fas fa-arrow-left"></i> Назад в город
        </button>
        <h1><i class="fas fa-comments"></i> Форум разработчиков</h1>
        <p>Место, где можно спросить совета или просто пообщаться</p>
      </div>

      <div class="chat-container">
        <div class="messages-list" ref="messagesList">
          <div v-if="loading" class="loading">
            <i class="fas fa-spinner fa-spin"></i> Загрузка сообщений...
          </div>
          <div v-else-if="messages.length === 0" class="empty-chat">
            Пока здесь нет сообщений. Будьте первым!
          </div>
          <div v-for="msg in messages" :key="msg.id" class="message-item" :class="{ 'my-message': msg.username === user?.username }">
            <img :src="msg.avatar" :alt="msg.username" class="message-avatar">
            <div class="message-content">
              <div class="message-header">
                <span class="username">{{ msg.username }}</span>
                <span class="time">{{ formatTime(msg.createdAt) }}</span>
              </div>
              <div class="text">{{ msg.content }}</div>
            </div>
          </div>
        </div>

        <div v-if="user" class="input-area">
          <textarea 
            v-model="newMessage" 
            placeholder="Напишите сообщение..." 
            @keydown.enter.prevent="sendMessage"
          ></textarea>
          <button class="send-btn" @click="sendMessage" :disabled="!newMessage.trim() || sending">
            <i v-if="sending" class="fas fa-spinner fa-spin"></i>
            <i v-else class="fas fa-paper-plane"></i>
          </button>
        </div>
        <div v-else class="auth-notice">
          Пожалуйста, <a @click="openLogin">войдите</a>, чтобы писать сообщения
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, inject, nextTick, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

const router = useRouter()
const user = inject('user')
const openLogin = inject('openLogin')

const messages = ref([])
const loading = ref(true)
const newMessage = ref('')
const sending = ref(false)
const messagesList = ref(null)
let pollInterval = null

const loadMessages = async (scrollDown = false) => {
  try {
    const res = await axios.get('/api/forum/messages')
    messages.value = res.data
    if (scrollDown) {
      scrollToBottom()
    }
  } catch (e) {
    console.error('Failed to load messages', e)
  } finally {
    loading.value = false
  }
}

const sendMessage = async () => {
  if (!newMessage.value.trim() || sending.value) return

  sending.value = true
  try {
    await axios.post('/api/forum/messages', { content: newMessage.value })
    newMessage.value = ''
    await loadMessages(true)
  } catch (e) {
    console.error('Failed to send message', e)
  } finally {
    sending.value = false
  }
}

const scrollToBottom = () => {
  nextTick(() => {
    if (messagesList.value) {
      messagesList.value.scrollTop = messagesList.value.scrollHeight
    }
  })
}

const formatTime = (dateStr) => {
  const date = new Date(dateStr)
  return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
}

onMounted(() => {
  loadMessages(true)
  // Обновление чата каждые 10 секунд (вместо 5), чтобы было меньше фоновых запросов
  pollInterval = setInterval(() => {
    // Обновляем только если вкладка активна
    if (document.visibilityState === 'visible') {
      loadMessages()
    }
  }, 10000)
})

onUnmounted(() => {
  if (pollInterval) clearInterval(pollInterval)
})
</script>

<style scoped>
.forum-page {
  min-height: calc(100vh - 60px);
  background: #0f172a;
  color: #fff;
  padding: 40px 20px;
}

.container {
  max-width: 900px;
  margin: 0 auto;
}

.header {
  text-align: center;
  margin-bottom: 30px;
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
  font-size: 2em;
  color: var(--accent-color);
  margin-bottom: 10px;
}

.chat-container {
  background: #1e293b;
  border-radius: 16px;
  display: flex;
  flex-direction: column;
  height: 600px;
  box-shadow: 0 10px 30px rgba(0,0,0,0.5);
  overflow: hidden;
}

.messages-list {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.message-item {
  display: flex;
  gap: 12px;
  max-width: 80%;
}

.my-message {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.message-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #334155;
}

.message-content {
  background: #334155;
  padding: 12px 16px;
  border-radius: 12px;
  border-top-left-radius: 0;
}

.my-message .message-content {
  background: var(--accent-color);
  border-top-left-radius: 12px;
  border-top-right-radius: 0;
}

.message-header {
  display: flex;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 4px;
  font-size: 0.85em;
}

.username {
  font-weight: 700;
  color: #cbd5e1;
}

.my-message .username {
  color: #fff;
}

.time {
  opacity: 0.5;
}

.text {
  line-height: 1.5;
  word-break: break-word;
}

.input-area {
  padding: 20px;
  background: rgba(0,0,0,0.2);
  display: flex;
  gap: 12px;
}

textarea {
  flex: 1;
  background: #0f172a;
  border: 1px solid #334155;
  color: #fff;
  border-radius: 12px;
  padding: 12px;
  resize: none;
  height: 50px;
  font-family: inherit;
}

textarea:focus {
  outline: none;
  border-color: var(--accent-color);
}

.send-btn {
  width: 50px;
  height: 50px;
  background: var(--accent-color);
  color: white;
  border: none;
  border-radius: 12px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.2em;
  transition: all 0.2s;
}

.send-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.auth-notice {
  padding: 20px;
  text-align: center;
  opacity: 0.7;
}

.auth-notice a {
  color: var(--accent-color);
  cursor: pointer;
  text-decoration: underline;
}

.loading, .empty-chat {
  text-align: center;
  padding: 40px;
  opacity: 0.5;
}

/* Скроллбар */
.messages-list::-webkit-scrollbar {
  width: 6px;
}
.messages-list::-webkit-scrollbar-thumb {
  background: rgba(255,255,255,0.1);
  border-radius: 3px;
}
</style>
