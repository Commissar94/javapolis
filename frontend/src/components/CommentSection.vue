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
      <div v-for="comment in comments" :key="comment.id" class="comment-wrapper">
        <div class="comment-item">
          <div class="comment-meta">
            <span class="comment-author">
              <router-link :to="'/profile/' + comment.authorUsername">{{ comment.authorName }}</router-link>
            </span>
            <span class="comment-date">{{ formatDate(comment.createdAt) }}</span>
          </div>
          <div class="comment-content">
            <template v-if="comment.content">
              <div :class="{ 'content-collapsed': !isExpanded(comment.id) && isLong(comment.content) }">
                {{ comment.content }}
              </div>
              <button v-if="isLong(comment.content)" class="expand-btn" @click="toggleExpand(comment.id)">
                {{ isExpanded(comment.id) ? 'Свернуть' : 'Развернуть' }}
              </button>
            </template>
          </div>
          <div v-if="comment.imageUrl" class="comment-image">
            <img :src="comment.imageUrl" alt="Comment image" @click="openImage(comment.imageUrl)">
          </div>
          <div class="comment-actions">
            <button 
              v-if="isAuthenticated && comment.authorUsername !== currentUser?.username" 
              class="like-btn" 
              :class="{ 'already-liked': isAlreadyLiked(comment) }"
              @click="likeComment(comment)"
              :disabled="isAlreadyLiked(comment)"
              :title="getLikersTitle(comment)"
            >
              <i class="fas fa-coins"></i> {{ comment.likes || 0 }}
            </button>
            <span v-else class="likes-count" :title="getLikersTitle(comment)">
              <i class="fas fa-coins"></i> {{ comment.likes || 0 }}
            </span>

            <button 
              v-if="isAuthenticated" 
              class="reply-btn"
              @click="toggleReply(comment.id)"
            >
              Ответить
            </button>
          </div>

    <!-- Форма ответа (вложенная) -->
          <div 
            v-if="replyingTo === comment.id" 
            class="reply-form"
            @dragover.prevent="onDragOver('reply')"
            @dragleave.prevent="onDragLeave('reply')"
            @drop.prevent="onDrop($event, 'reply')"
            :class="{ 'drag-over': isDragOverReply }"
          >
            <textarea 
              v-model="replyContent" 
              placeholder="Напишите ваш ответ... (можно перетащить сюда картинку)" 
              rows="2"
              ref="replyTextarea"
              @input="autoResize($event)"
            ></textarea>
            
            <!-- Загрузка изображения для ответа -->
            <div class="file-upload-container">
              <label :for="'reply-file-' + comment.id" class="file-upload-label">
                <i class="fas fa-image"></i> {{ replyImage ? 'Сменить картинку' : 'Прикрепить картинку' }}
              </label>
              <input 
                :id="'reply-file-' + comment.id" 
                type="file" 
                @change="handleReplyFileChange" 
                accept="image/*" 
                class="file-input"
              >
              <div v-if="replyImage" class="image-preview">
                <img :src="replyImagePreview" alt="Preview">
                <button class="remove-image" @click="removeReplyImage"><i class="fas fa-times"></i></button>
              </div>
            </div>

            <div class="reply-actions">
              <button class="cancel-btn" @click="replyingTo = null">Отмена</button>
              <button 
                class="submit-reply-btn" 
                :disabled="submittingReply || (!replyContent.trim() && !replyImage)"
                @click="submitReply(comment)"
              >
                <span v-if="submittingReply"><i class="fas fa-spinner fa-spin"></i></span>
                <span v-else>Ответить</span>
              </button>
            </div>
          </div>
        </div>

        <!-- Вложенные ответы -->
        <div v-if="comment.replies && comment.replies.length > 0" class="comment-replies">
          <div v-for="reply in comment.replies" :key="reply.id" class="comment-item reply-item">
            <div class="comment-meta">
              <span class="comment-author">
                <router-link :to="'/profile/' + reply.authorUsername">{{ reply.authorName }}</router-link>
              </span>
              <span class="comment-date">{{ formatDate(reply.createdAt) }}</span>
            </div>
            <div class="comment-content">
              <template v-if="reply.content">
                <div :class="{ 'content-collapsed': !isExpanded(reply.id) && isLong(reply.content) }">
                  {{ reply.content }}
                </div>
                <button v-if="isLong(reply.content)" class="expand-btn" @click="toggleExpand(reply.id)">
                  {{ isExpanded(reply.id) ? 'Свернуть' : 'Развернуть' }}
                </button>
              </template>
            </div>
            <div v-if="reply.imageUrl" class="comment-image">
              <img :src="reply.imageUrl" alt="Comment image" @click="openImage(reply.imageUrl)">
            </div>
            <div class="comment-actions">
              <button 
                v-if="isAuthenticated && reply.authorUsername !== currentUser?.username" 
                class="like-btn" 
                :class="{ 'already-liked': isAlreadyLiked(reply) }"
                @click="likeComment(reply, comment)"
                :disabled="isAlreadyLiked(reply)"
                :title="getLikersTitle(reply)"
              >
                <i class="fas fa-coins"></i> {{ reply.likes || 0 }}
              </button>
              <span v-else class="likes-count" :title="getLikersTitle(reply)">
                <i class="fas fa-coins"></i> {{ reply.likes || 0 }}
              </span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Форма добавления комментария -->
    <div class="add-comment-form">
      <div 
        v-if="isAuthenticated"
        @dragover.prevent="onDragOver('new')"
        @dragleave.prevent="onDragLeave('new')"
        @drop.prevent="onDrop($event, 'new')"
        :class="{ 'drag-over': isDragOverNew }"
      >
        <textarea 
          v-model="newComment" 
          placeholder="Ну пиши, чего смотришь?"
          :disabled="submitting"
          rows="3"
          ref="newCommentTextarea"
          @input="autoResize($event)"
        ></textarea>

        <!-- Загрузка изображения для нового комментария -->
        <div class="file-upload-container">
          <label for="new-comment-file" class="file-upload-label">
            <i class="fas fa-image"></i> {{ newCommentImage ? 'Сменить картинку' : 'Прикрепить картинку' }}
          </label>
          <input 
            id="new-comment-file" 
            type="file" 
            @change="handleNewCommentFileChange" 
            accept="image/*" 
            class="file-input"
          >
          <div v-if="newCommentImage" class="image-preview">
            <img :src="newCommentImagePreview" alt="Preview">
            <button class="remove-image" @click="removeNewCommentImage"><i class="fas fa-times"></i></button>
          </div>
        </div>

        <div class="form-actions">
          <button 
            class="submit-btn" 
            :disabled="submitting || (!newComment.trim() && !newCommentImage)" 
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

  <!-- Modal for full size image -->
  <div v-if="fullImageUrl" class="image-modal" @click="fullImageUrl = null">
    <div class="modal-content">
      <img :src="fullImageUrl" alt="Full size">
      <button class="close-modal"><i class="fas fa-times"></i></button>
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
const newCommentImage = ref(null)
const newCommentImagePreview = ref(null)
const replyingTo = ref(null)
const replyContent = ref('')
const replyImage = ref(null)
const replyImagePreview = ref(null)
const submittingReply = ref(false)
const isAuthenticated = ref(false)
const currentUser = ref(null)
const error = ref('')
const fullImageUrl = ref(null)
const isDragOverNew = ref(false)
const isDragOverReply = ref(false)
const expandedComments = ref(new Set())

const isLong = (content) => {
  return content && content.length > 300
}

const isExpanded = (id) => {
  return expandedComments.value.has(id)
}

const toggleExpand = (id) => {
  if (expandedComments.value.has(id)) {
    expandedComments.value.delete(id)
  } else {
    expandedComments.value.add(id)
  }
}

const newCommentTextarea = ref(null)
const replyTextarea = ref(null)

const autoResize = (event) => {
  const textarea = event.target
  textarea.style.height = 'auto'
  textarea.style.height = textarea.scrollHeight + 'px'
}

const fetchAuthStatus = async () => {
  try {
    const res = await axios.get('/api/auth/status')
    isAuthenticated.value = res.data.authenticated
    if (res.data.authenticated) {
      currentUser.value = {
        username: res.data.username
      }
    }
  } catch (e) {
    isAuthenticated.value = false
    currentUser.value = null
  }
}

const handleNewCommentFileChange = (e) => {
  const file = e.target.files[0]
  if (file) {
    if (file.size > 2 * 1024 * 1024) {
      alert('Размер файла не должен превышать 2МБ')
      e.target.value = ''
      return
    }
    newCommentImage.value = file
    newCommentImagePreview.value = URL.createObjectURL(file)
  }
}

const removeNewCommentImage = () => {
  newCommentImage.value = null
  newCommentImagePreview.value = null
}

const handleReplyFileChange = (e) => {
  const file = e.target.files[0]
  if (file) {
    if (file.size > 2 * 1024 * 1024) {
      alert('Размер файла не должен превышать 2МБ')
      e.target.value = ''
      return
    }
    replyImage.value = file
    replyImagePreview.value = URL.createObjectURL(file)
  }
}

const removeReplyImage = () => {
  replyImage.value = null
  replyImagePreview.value = null
}

const onDragOver = (type) => {
  if (type === 'new') isDragOverNew.value = true
  else isDragOverReply.value = true
}

const onDragLeave = (type) => {
  if (type === 'new') isDragOverNew.value = false
  else isDragOverReply.value = false
}

const onDrop = (e, type) => {
  onDragLeave(type)
  const file = e.dataTransfer.files[0]
  if (file && file.type.startsWith('image/')) {
    if (file.size > 2 * 1024 * 1024) {
      alert('Размер файла не должен превышать 2МБ')
      return
    }
    if (type === 'new') {
      newCommentImage.value = file
      newCommentImagePreview.value = URL.createObjectURL(file)
    } else {
      replyImage.value = file
      replyImagePreview.value = URL.createObjectURL(file)
    }
  }
}

const uploadImage = async (file) => {
  const formData = new FormData()
  formData.append('file', file)
  const res = await axios.post('/api/upload/image', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
  return res.data.imageUrl
}

const openImage = (url) => {
  fullImageUrl.value = url
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
  if (!newComment.value.trim() && !newCommentImage.value) return
  
  submitting.value = true
  error.value = ''
  try {
    let imageUrl = null
    if (newCommentImage.value) {
      imageUrl = await uploadImage(newCommentImage.value)
    }

    const cleanPath = props.topicPath.replace('.md', '')
    const res = await axios.post('/api/comments', {
      topicPath: cleanPath,
      page: props.page,
      content: newComment.value,
      imageUrl: imageUrl
    })
    // Новый основной комментарий всегда в начало
    comments.value.unshift({
      ...res.data,
      replies: []
    })
    newComment.value = ''
    removeNewCommentImage()
    
    // Сбрасываем высоту поля
    if (newCommentTextarea.value) {
      newCommentTextarea.value.style.height = 'auto'
    }
  } catch (e) {
    error.value = e.response?.data || 'Не удалось отправить комментарий'
  } finally {
    submitting.value = false
  }
}

const toggleReply = (commentId) => {
  if (replyingTo.value === commentId) {
    replyingTo.value = null
  } else {
    replyingTo.value = commentId
    replyContent.value = ''
  }
}

const submitReply = async (parentComment) => {
  if (!replyContent.value.trim() && !replyImage.value) return
  
  submittingReply.value = true
  try {
    let imageUrl = null
    if (replyImage.value) {
      imageUrl = await uploadImage(replyImage.value)
    }

    const cleanPath = props.topicPath.replace('.md', '')
    const res = await axios.post('/api/comments', {
      topicPath: cleanPath,
      page: props.page,
      content: replyContent.value,
      parentId: parentComment.id,
      imageUrl: imageUrl
    })
    
    // Добавляем ответ в список ответов родителя
    if (!parentComment.replies) parentComment.replies = []
    parentComment.replies.push(res.data)
    
    replyContent.value = ''
    replyingTo.value = null
    removeReplyImage()
    
    // Сброс высоты для replyTextarea не требуется здесь явно, так как форма скрывается (v-if),
    // но если бы она оставалась, мы бы сделали так же.
  } catch (e) {
    alert(e.response?.data || 'Не удалось отправить ответ')
  } finally {
    submittingReply.value = false
  }
}

const likeComment = async (comment, parentComment = null) => {
  try {
    const res = await axios.post(`/api/comments/${comment.id}/like`)
    
    if (parentComment) {
      // Обновляем лайк во вложенном ответе
      const index = parentComment.replies.findIndex(r => r.id === comment.id)
      if (index !== -1) {
        parentComment.replies[index].likes = res.data.likes
        parentComment.replies[index].likers = res.data.likers
      }
    } else {
      // Обновляем лайк в основном комментарии
      const index = comments.value.findIndex(c => c.id === comment.id)
      if (index !== -1) {
        comments.value[index].likes = res.data.likes
        comments.value[index].likers = res.data.likers
      }
    }
    
    window.dispatchEvent(new CustomEvent('coins-updated'))
  } catch (e) {
    alert(e.response?.data || 'Не удалось поставить лайк')
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

const getLikersTitle = (comment) => {
  if (isAlreadyLiked(comment)) {
    return 'Вы уже поддержали этот комментарий'
  }
  if (!comment.likers || comment.likers.length === 0) {
    return comment.authorUsername === currentUser.value?.username ? 'Ваш комментарий' : 'Отдать 1 полис'
  }
  const likersList = comment.likers.join(', ')
  return `Полисы от: ${likersList}`
}

const isAlreadyLiked = (comment) => {
  return comment.likers && currentUser.value && comment.likers.includes(currentUser.value.username)
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
  margin-bottom: 10px;
  word-break: break-word;
  overflow-wrap: break-word;
}

.content-collapsed {
  max-height: 100px;
  overflow: hidden;
  position: relative;
  mask-image: linear-gradient(to bottom, black 60%, transparent 100%);
  -webkit-mask-image: linear-gradient(to bottom, black 60%, transparent 100%);
}

.expand-btn {
  background: none;
  border: none;
  color: var(--accent-color);
  font-size: 0.85rem;
  cursor: pointer;
  padding: 5px 0;
  font-weight: 500;
  display: block;
  margin-top: 5px;
}

.expand-btn:hover {
  text-decoration: underline;
}

.comment-image {
  margin-bottom: 15px;
  max-width: 100%;
}

.comment-image img {
  max-width: 300px;
  max-height: 400px;
  border-radius: 8px;
  cursor: pointer;
  transition: transform 0.2s;
  border: 1px solid var(--border-color);
}

.comment-image img:hover {
  transform: scale(1.02);
}

.file-upload-container {
  margin-bottom: 15px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.file-upload-label {
  display: inline-block;
  padding: 6px 12px;
  background: var(--hover-bg);
  border: 1px solid var(--border-color);
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.9rem;
  color: var(--text-color);
  transition: all 0.2s;
  align-self: flex-start;
}

.file-upload-label:hover {
  background: var(--border-color);
}

.file-input {
  display: none;
}

.image-preview {
  position: relative;
  display: inline-block;
  align-self: flex-start;
}

.image-preview img {
  max-width: 150px;
  max-height: 150px;
  border-radius: 4px;
  border: 1px solid var(--border-color);
}

.remove-image {
  position: absolute;
  top: -8px;
  right: -8px;
  background: #e74c3c;
  color: white;
  border: none;
  border-radius: 50%;
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  font-size: 0.7rem;
}

.image-modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.85);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  cursor: pointer;
}

.modal-content {
  position: relative;
  max-width: 90%;
  max-height: 90%;
}

.modal-content img {
  max-width: 100%;
  max-height: 90vh;
  object-fit: contain;
  border-radius: 4px;
}

.close-modal {
  position: absolute;
  top: -40px;
  right: -40px;
  background: none;
  border: none;
  color: white;
  font-size: 2rem;
  cursor: pointer;
}

.comment-actions {
  display: flex;
  align-items: center;
  gap: 15px;
}

.reply-btn {
  background: none;
  border: none;
  color: var(--accent-color);
  font-size: 0.85rem;
  cursor: pointer;
  padding: 0;
  font-weight: 500;
}

.reply-btn:hover {
  text-decoration: underline;
}

.reply-form {
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px dashed var(--border-color);
}

.reply-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.cancel-btn {
  background: none;
  border: 1px solid var(--border-color);
  color: var(--text-color);
  padding: 6px 12px;
  border-radius: 4px;
  cursor: pointer;
}

.submit-reply-btn {
  background: var(--accent-color);
  color: white;
  border: none;
  padding: 6px 15px;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 500;
}

.submit-reply-btn:disabled {
  opacity: 0.6;
}

.comment-replies {
  margin-left: 40px;
  margin-top: 10px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  border-left: 2px solid var(--border-color);
  padding-left: 20px;
}

.reply-item {
  border-left: 3px solid var(--accent-color);
  font-size: 0.95rem;
  padding: 10px 15px;
}

.like-btn {
  background: rgba(255, 215, 0, 0.1);
  border: 1px solid rgba(255, 215, 0, 0.3);
  color: var(--text-color);
  padding: 4px 10px;
  border-radius: 15px;
  cursor: pointer;
  font-size: 0.85rem;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  gap: 5px;
}

.like-btn:hover {
  background: rgba(255, 215, 0, 0.2);
  transform: scale(1.05);
}

.like-btn.already-liked {
  background: rgba(255, 215, 0, 0.3);
  border-color: #ffd700;
  cursor: default;
  transform: none;
}

.like-btn:disabled {
  opacity: 1;
}

.like-btn i {
  color: #ffd700;
}

.likes-count {
  font-size: 0.85rem;
  color: var(--text-color);
  opacity: 0.7;
  display: flex;
  align-items: center;
  gap: 5px;
  padding: 4px 10px;
}

.likes-count i {
  color: #ffd700;
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
  box-sizing: border-box;
}

.drag-over {
  border: 2px dashed var(--accent-color);
  background: var(--hover-bg);
  border-radius: 8px;
  padding: 5px;
}

textarea {
  width: 100%;
  padding: 12px;
  border: 1px solid var(--border-color);
  background: var(--bg-color);
  color: var(--text-color);
  border-radius: 6px;
  resize: none;
  overflow: hidden;
  margin-bottom: 15px;
  font-family: inherit;
  box-sizing: border-box;
  min-height: 45px;
  transition: height 0.1s ease;
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
