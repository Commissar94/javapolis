<template>
  <div class="university">
    <div v-if="structure" class="container" :class="{ 'no-sidebar': !currentTopic }">
      <div v-if="currentTopic" class="sidebar">
        <div class="sidebar-header-nav">
          <button class="back-btn" @click="goBackToCourses">
            <i class="fas fa-arrow-left"></i>
            <span>К курсам</span>
          </button>
        </div>
        <div class="sidebar-header">
          <i class="fas fa-graduation-cap"></i>
          <span>Темы курса</span>
        </div>
        <div class="sidebar-content">
          <ul v-if="currentCourseStructure" class="topic-list">
            <TopicTreeItem 
              v-for="item in currentCourseStructure" 
              :key="item.path" 
              :item="item" 
              :current-topic="currentTopic"
            />
          </ul>
        </div>
      </div>
      <div class="content">
        <div class="content-inner">
          <div v-if="currentTopic && user">
            <h2 class="topic-title">{{ currentTopic.name }}</h2>
            
            <!-- Мини-пагинация сверху -->
            <div v-if="totalPages > 1" class="top-pagination">
              <div class="page-numbers">
                <button 
                  v-for="p in totalPages" 
                  :key="p" 
                  class="page-number-btn mini" 
                  :class="{ 
                    'active': currentPage === p - 1,
                    'completed': completedPages.includes(p - 1)
                  }"
                  @click="changePage(p - 1)"
                >
                  <i v-if="completedPages.includes(p - 1) && currentPage !== p - 1" class="fas fa-check"></i>
                  <span v-else>{{ p }}</span>
                </button>
              </div>
            </div>

            <div class="markdown-body" v-html="content"></div>
            
            <!-- Пагинация -->
            <div v-if="totalPages > 1" class="pagination">
              <button 
                class="page-btn nav-btn" 
                :disabled="currentPage === 0" 
                @click="changePage(currentPage - 1)"
              >
                <i class="fas fa-chevron-left"></i>
              </button>
              
              <div class="page-numbers">
                <button 
                  v-for="p in totalPages" 
                  :key="p" 
                  class="page-number-btn" 
                  :class="{ 
                    'active': currentPage === p - 1,
                    'completed': completedPages.includes(p - 1)
                  }"
                  @click="changePage(p - 1)"
                >
                  <i v-if="completedPages.includes(p - 1) && currentPage !== p - 1" class="fas fa-check"></i>
                  <span v-else>{{ p }}</span>
                </button>
              </div>
              
              <button 
                class="page-btn nav-btn" 
                :disabled="currentPage === totalPages - 1" 
                @click="changePage(currentPage + 1)"
              >
                <i class="fas fa-chevron-right"></i>
              </button>
            </div>

            <!-- Секция комментариев -->
            <CommentSection 
              :topic-path="currentTopic.path" 
              :page="currentPage"
            />
          </div>
          <div v-else-if="currentTopic && !user" class="restricted-content">
            <div class="lock-overlay">
              <i class="fas fa-lock"></i>
              <h3>Содержимое заблокировано</h3>
              <p>Пожалуйста, войдите или зарегистрируйтесь, чтобы прочитать эту лекцию.</p>
              <button class="auth-btn" @click="openLogin">Войти / Регистрация</button>
            </div>
          </div>
          <div v-else-if="structure" class="courses-selection">
            <h2 class="selection-title">Выберите курс</h2>
            <div class="courses-grid">
              <CourseCard 
                v-for="item in coursesOnly" 
                :key="item.path" 
                :course="item"
                @click="startCourse(item)"
              />
            </div>
            
            <div v-if="filesOnly.length > 0" class="other-topics">
              <h3 class="selection-subtitle">Другие темы</h3>
              <div class="topics-grid">
                <div 
                  v-for="topic in filesOnly" 
                  :key="topic.path" 
                  class="simple-topic-card"
                  @click="navigateToTopic(topic.path)"
                >
                  <i class="fas fa-file-alt"></i>
                  <span>{{ topic.name }}</span>
                  <i v-if="topic.completed" class="fas fa-check-circle completed-icon"></i>
                </div>
              </div>
            </div>
          </div>
          <div v-else class="empty-state">
            <i class="fas fa-book-open"></i>
            <p>Загрузка структуры обучения...</p>
          </div>
        </div>
      </div>
    </div>
    
    <GuestAccessModal 
      :is-open="isGuestModalOpen" 
      @close="isGuestModalOpen = false" 
      @open-login="openLogin"
    />
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, watch, computed, inject } from 'vue'
import axios from 'axios'
import { useRoute, useRouter } from 'vue-router'
import TopicTreeItem from '../components/TopicTreeItem.vue'
import CommentSection from '../components/CommentSection.vue'
import CourseCard from '../components/CourseCard.vue'
import GuestAccessModal from '../components/GuestAccessModal.vue'

const route = useRoute()
const router = useRouter()

const user = inject('user')
const openLogin = inject('openLogin')

const isGuestModalOpen = ref(false)

const structure = ref(null)
const currentTopic = ref(null)
const content = ref('')
const currentPage = ref(0)
const totalPages = ref(1)
const completedPages = ref([])

const coursesOnly = computed(() => {
  return structure.value ? structure.value.filter(item => item.type === 'folder') : []
})

const filesOnly = computed(() => {
  return structure.value ? structure.value.filter(item => item.type === 'file') : []
})

const currentCourseStructure = computed(() => {
  if (!currentTopic.value || !structure.value) return null
  
  // Определяем корневую папку (курс) текущей темы
  const pathParts = currentTopic.value.path.split('/')
  if (pathParts.length === 1) {
    // Если тема в корне (не в папке), то это файл, показываем его в списке "Другие темы"
    // Но в сайдбаре мы можем показать просто этот файл или ничего.
    // Обычно "Другие темы" не имеют древовидной навигации в сайдбаре, 
    // но если пользователь хочет, можем показать список всех корневых файлов.
    return structure.value.filter(item => item.type === 'file')
  }
  
  const courseRootName = pathParts[0]
  const course = structure.value.find(item => item.name === courseRootName && item.type === 'folder')
  
  return course ? course.children : []
})

onMounted(async () => {
  try {
    const res = await axios.get('/api/university/structure')
    structure.value = res.data
    
    // Если в URL уже есть путь к топику, загружаем его
    const path = route.params.pathMatch
    if (path) {
      const page = parseInt(route.query.page) || 0
      if (!user.value) {
        isGuestModalOpen.value = true
        currentTopic.value = { name: 'Доступ ограничен', path: Array.isArray(path) ? path.join('/') : path }
      } else {
        loadTopic(path, page)
      }
    }
  } catch (e) {
    console.error('Failed to load structure', e)
  }
})

// Следим за изменением параметров маршрута для поддержки навигации браузера
watch(() => route.params.pathMatch, (newPath) => {
  if (newPath) {
    const page = parseInt(route.query.page) || 0
    loadTopic(newPath, page)
  }
})

watch(() => user.value, (newUser) => {
  if (newUser) {
    const path = route.params.pathMatch
    if (path) {
      const page = parseInt(route.query.page) || 0
      loadTopic(path, page)
    }
  } else {
    // Если разлогинились, сбрасываем контент
    content.value = ''
  }
})

watch(() => route.query.page, (newPage) => {
  const path = route.params.pathMatch
  if (path) {
    loadTopic(path, parseInt(newPage) || 0)
  }
})

const loadTopic = async (path, page = 0) => {
  if (!path) return
  
  // Приводим путь к строке, если это массив (бывает в pathMatch)
  const stringPath = Array.isArray(path) ? path.join('/') : path
  
  if (!user.value) {
    isGuestModalOpen.value = true
    // Мы всё равно можем захотеть загрузить структуру темы (имена и т.д.), 
    // но контент не покажем. Или можем вообще не загружать.
    // Покажем модалку и заблокируем отображение.
    currentTopic.value = { name: 'Загрузка...', path: stringPath }
    content.value = ''
    return
  }
  
  try {
    // В API путь ожидается без .md и после /api/university/topic/
    const cleanPath = stringPath.replace('.md', '')
    const res = await axios.get(`/api/university/topic/${cleanPath}`, {
      params: { page }
    })
    currentTopic.value = res.data.currentTopic
    content.value = res.data.content
    currentPage.value = res.data.currentPage
    totalPages.value = res.data.totalPages
    completedPages.value = res.data.completedPages || []
    
    nextTick(() => {
      // Автоматически раскрываем папки по пути к текущему топику
      if (structure.value && currentTopic.value) {
        expandFoldersToPath(structure.value, currentTopic.value.path);
      }
    
      if (window.Prism) {
        window.Prism.highlightAll()
      }
      setupCollapsibles()
      setupQuizzes()
      setupCodeTasks()
      
      // Скроллим вверх при смене страницы
      const contentEl = document.querySelector('.content')
      if (contentEl) contentEl.scrollTop = 0
    })

    // Отмечаем прогресс
    markPageAsCompleted(cleanPath, page, res.data.totalPages);
  } catch (e) {
    console.error('Failed to load topic', e)
  }
}

const changePage = (newPage) => {
  const path = route.params.pathMatch
  const stringPath = Array.isArray(path) ? path.join('/') : (path || '')
  
  router.push({
    path: `/university/${stringPath}`,
    query: { ...route.query, page: newPage }
  })
}

const startCourse = (course) => {
  if (!user.value) {
    isGuestModalOpen.value = true
    return
  }
  const firstTopic = findFirstTopic(course)
  if (firstTopic) {
    navigateToTopic(firstTopic.path)
  }
}

const findFirstTopic = (item) => {
  if (item.type === 'file') return item
  if (item.children && item.children.length > 0) {
    for (const child of item.children) {
      const found = findFirstTopic(child)
      if (found) return found
    }
  }
  return null
}

const navigateToTopic = (path) => {
  if (!user.value) {
    isGuestModalOpen.value = true
    return
  }
  const cleanPath = path.replace('.md', '')
  router.push(`/university/${cleanPath}`)
}

const goBackToCourses = () => {
  currentTopic.value = null
  router.push('/university')
}

const markPageAsCompleted = async (topicPath, page, totalPages) => {
  try {
    await axios.post('/api/university/progress', {
      topicPath,
      page,
      totalPages
    });
    
    // После сохранения прогресса, если это была новая страница, 
    // обновим структуру, чтобы увидеть галочку в сайдбаре
    if (!completedPages.value.includes(page)) {
      completedPages.value.push(page);
      const res = await axios.get('/api/university/structure');
      structure.value = res.data;
    }
  } catch (e) {
    console.error('Failed to save progress', e);
  }
}

const setupCollapsibles = () => {
  const headers = document.querySelectorAll('.collapsible-header');
  headers.forEach(header => {
    // Удаляем старый обработчик, если он был, чтобы избежать дублирования
    header.onclick = null;
    header.onclick = (e) => {
      e.stopPropagation();
      const parent = header.parentElement;
      const content = header.nextElementSibling;
      
      const isOpen = parent.classList.contains('open');
      
      if (!isOpen) {
        content.style.display = 'block';
        parent.classList.add('open');
      } else {
        content.style.display = 'none';
        parent.classList.remove('open');
      }
    };
  });
}

const setupQuizzes = () => {
  const quizzes = document.querySelectorAll('.quiz-block');
  quizzes.forEach(quiz => {
    const type = quiz.dataset.type;
    const options = quiz.querySelectorAll('.quiz-option');
    const checkBtn = quiz.querySelector('.quiz-check-btn');
    const feedback = quiz.querySelector('.quiz-feedback');
    
    options.forEach(option => {
      option.onclick = () => {
        if (quiz.classList.contains('checked')) return;
        
        if (type === 'single') {
          options.forEach(o => o.classList.remove('selected'));
          option.classList.add('selected');
        } else {
          option.classList.toggle('selected');
        }
        feedback.style.display = 'none';
      };
    });
    
    checkBtn.onclick = () => {
      if (quiz.classList.contains('checked')) {
        // Сброс
        quiz.classList.remove('checked');
        options.forEach(o => {
          o.classList.remove('selected', 'correct', 'incorrect');
        });
        feedback.style.display = 'none';
        checkBtn.textContent = 'Проверить ответ';
        return;
      }
      
      const selected = quiz.querySelectorAll('.quiz-option.selected');
      if (selected.length === 0) {
        feedback.textContent = 'Пожалуйста, выберите хотя бы один вариант';
        feedback.className = 'quiz-feedback warning-msg';
        feedback.style.display = 'block';
        return;
      }
      
      let allCorrect = true;
      options.forEach(option => {
        const isCorrect = option.dataset.correct === 'true';
        const isSelected = option.classList.contains('selected');
        
        if (isCorrect) {
          option.classList.add('correct');
          if (!isSelected) allCorrect = false;
        } else if (isSelected) {
          option.classList.add('incorrect');
          allCorrect = false;
        }
      });
      
      quiz.classList.add('checked');
      if (allCorrect) {
        feedback.textContent = 'Верно! Отличная работа.';
        feedback.className = 'quiz-feedback success-msg';
      } else {
        feedback.textContent = 'Не совсем так. Посмотрите правильные ответы.';
        feedback.className = 'quiz-feedback error-msg';
      }
      feedback.style.display = 'block';
      checkBtn.textContent = 'Попробовать снова';
    };
  });
}

const setupCodeTasks = () => {
  const tasks = document.querySelectorAll('.code-task');
  tasks.forEach(task => {
    const runBtn = task.querySelector('.code-run-btn');
    const output = task.querySelector('.code-output');
    const textarea = task.querySelector('.code-editor');
    const taskId = task.dataset.id || '';
    const language = task.dataset.language || 'java';

    // Простейший шаблон по умолчанию для Java
    if (language === 'java' && (!textarea.value || textarea.value.trim() === '')) {
      textarea.value = `class Solution {\n    static int sum(int a, int b) {\n        // TODO: реализуйте\n        return a + b;\n    }\n}`;
    }

    runBtn.onclick = async () => {
      runBtn.disabled = true;
      runBtn.textContent = 'Запуск...';
      output.style.display = 'block';
      output.innerHTML = '⏳ Выполняется...';
      try {
        const res = await axios.post('/api/judge/solutions', {
          taskId,
          language,
          code: textarea.value
        });
        const data = res.data;
        const status = data.status || 'UNKNOWN';
        
        let html = `<div class="output-header">
          <span class="status-badge ${status.toLowerCase()}">${status}</span>
          <span class="exit-code">Exit: ${data.exitCode}</span>
        </div>`;
        
        if (data.stdout) {
          html += `<div class="output-section">
            <div class="section-label">Результат:</div>
            <pre class="stdout">${data.stdout}</pre>
          </div>`;
        }
        
        if (data.stderr) {
          html += `<div class="output-section error">
            <div class="section-label">Ошибки:</div>
            <pre class="stderr">${data.stderr}</pre>
          </div>`;
        }
        
        output.innerHTML = html;
        output.className = 'code-output ' + (status === 'OK' ? 'ok' : 'fail');
      } catch (e) {
        output.textContent = 'Ошибка запроса: ' + (e?.response?.data?.message || e.message);
        output.className = 'code-output fail';
      } finally {
        runBtn.disabled = false;
        runBtn.textContent = 'Запустить тесты';
      }
    };
  });
}

const expandFoldersToPath = (items, targetPath) => {
  let found = false;
  for (const item of items) {
    if (item.type === 'folder') {
      if (expandFoldersToPath(item.children, targetPath)) {
        item.expanded = true;
        found = true;
      }
    } else if (item.path === targetPath) {
      found = true;
    }
  }
  return found;
}
</script>

<style scoped>
.container {
  display: flex;
  min-height: calc(100vh - 64px); /* Высота за вычетом навбара, если он есть */
}

.container.no-sidebar {
  display: block;
}

.sidebar {
  width: 300px;
  display: flex;
  flex-direction: column;
  background: var(--sidebar-bg);
  border-right: 1px solid var(--border-color);
  position: sticky;
  top: 0;
  height: calc(100vh - 64px);
}

.sidebar-header {
  padding: 20px;
  font-weight: 700;
  color: var(--text-color);
  display: flex;
  align-items: center;
  gap: 10px;
  border-bottom: 1px solid var(--border-color);
  font-size: 1.1em;
}

.sidebar-header-nav {
  padding: 10px 15px;
  border-bottom: 1px solid var(--border-color);
  background: var(--content-bg);
}

.back-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  background: none;
  border: 1px solid var(--border-color);
  padding: 6px 12px;
  border-radius: 6px;
  color: var(--text-color);
  cursor: pointer;
  font-size: 0.9em;
  font-weight: 500;
  transition: all 0.2s;
  width: 100%;
}

.back-btn:hover {
  background: var(--hover-bg);
  border-color: var(--accent-color);
  color: var(--accent-color);
}

.back-btn i {
  font-size: 0.85em;
}

.sidebar-content {
  flex: 1;
  overflow-y: auto;
  padding: 10px 0;
}

.topic-list, .sub-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.item-title, .file-item {
  padding: 8px 20px;
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  transition: all 0.2s;
  color: var(--text-color);
  opacity: 0.8;
  font-size: 0.95em;
  user-select: none;
}

.item-title:hover, .file-item:hover {
  background: var(--hover-bg);
  color: var(--text-color);
  opacity: 1;
}

.file-item.active {
  background: var(--hover-bg);
  color: var(--accent-color);
  font-weight: 600;
  border-right: 3px solid var(--accent-color);
}

.folder-item .item-title {
  font-weight: 600;
  color: var(--text-color);
  opacity: 1;
}

.sub-list .file-item {
  padding-left: 45px;
}

.item-title i.fa-chevron-right, 
.item-title i.fa-chevron-down {
  width: 12px;
  font-size: 0.8em;
  color: #94a3b8;
}

.item-title i.fa-folder {
  color: #64748b;
}

.file-item i.fa-file-alt {
  color: #94a3b8;
}

.file-item.active i.fa-file-alt {
  color: #0284c7;
}

.content {
  flex: 1;
  padding: 0;
  overflow-y: auto;
  background: var(--bg-color);
}

.content-inner {
  padding: 40px;
}

/* Стили для Markdown и кастомных блоков */
.markdown-body {
  box-sizing: border-box;
  min-width: 200px;
  max-width: 900px;
  margin: 0 auto;
  padding: 20px 0;
  text-align: left;
  color: var(--text-color);
  background-color: transparent !important;
}

.topic-title {
  font-size: 2.5em;
  font-weight: 800;
  color: var(--text-color);
  margin-bottom: 30px;
  border-bottom: 1px solid var(--border-color);
  padding-bottom: 20px;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 400px;
  color: #94a3b8;
}

.empty-state i {
  font-size: 4em;
  margin-bottom: 20px;
  color: var(--border-color);
}

.empty-state p {
  font-size: 1.2em;
}

.pagination {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 40px;
  padding-top: 20px;
  border-top: 1px solid var(--border-color);
  max-width: 900px;
  margin-left: auto;
  margin-right: auto;
}

.page-btn {
  padding: 10px 15px;
  background: var(--sidebar-bg);
  border: 1px solid var(--border-color);
  border-radius: 6px;
  color: var(--text-color);
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-weight: 600;
  min-width: 40px;
}

.page-numbers {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  justify-content: center;
}

.page-number-btn {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--sidebar-bg);
  border: 1px solid var(--border-color);
  border-radius: 6px;
  color: var(--text-color);
  cursor: pointer;
  transition: all 0.2s;
  font-weight: 500;
}

.page-number-btn.mini {
  width: 28px;
  height: 28px;
  font-size: 0.85em;
}

.top-pagination {
  margin-bottom: 20px;
  display: flex;
  justify-content: flex-start;
  max-width: 900px;
  margin-left: auto;
  margin-right: auto;
}

.page-number-btn:hover:not(.active) {
  background: var(--hover-bg);
  border-color: var(--accent-color);
  color: var(--accent-color);
}

.page-number-btn.active {
  background: var(--accent-color);
  border-color: var(--accent-color);
  color: white;
  font-weight: 700;
}

.page-number-btn.completed:not(.active) {
  border-color: #10b981;
  color: #10b981;
}

.page-number-btn.completed i {
  font-size: 0.8em;
}

.page-btn:hover:not(:disabled) {
  background: var(--hover-bg);
  border-color: var(--accent-color);
  color: var(--accent-color);
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* Стили для квизов */
.markdown-body :deep(.quiz-block) {
  background: var(--sidebar-bg);
  border: 1px solid var(--border-color);
  border-radius: 12px;
  padding: 25px;
  margin: 40px 0;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
}

.markdown-body :deep(.quiz-header) {
  font-weight: 700;
  font-size: 0.9em;
  text-transform: uppercase;
  color: #64748b;
  margin-bottom: 15px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.markdown-body :deep(.quiz-question) {
  font-size: 1.2em;
  font-weight: 600;
  margin-bottom: 20px;
  color: var(--text-color);
}

.markdown-body :deep(.quiz-options) {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 25px;
}

.markdown-body :deep(.quiz-option) {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 12px 18px;
  border: 2px solid var(--border-color);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  background: var(--content-bg);
}

.markdown-body :deep(.quiz-option:hover) {
  border-color: var(--accent-color);
  background: var(--hover-bg);
}

.markdown-body :deep(.quiz-option.selected) {
  border-color: var(--accent-color);
  background: rgba(2, 132, 199, 0.05);
}

.markdown-body :deep(.quiz-option-checkbox) {
  width: 20px;
  height: 20px;
  border: 2px solid #cbd5e1;
  border-radius: 4px;
  flex-shrink: 0;
  position: relative;
  transition: all 0.2s;
}

.markdown-body :deep(.quiz-block[data-type="single"] .quiz-option-checkbox) {
  border-radius: 50%;
}

.markdown-body :deep(.quiz-option.selected .quiz-option-checkbox) {
  border-color: var(--accent-color);
  background: var(--accent-color);
}

.markdown-body :deep(.quiz-option.selected .quiz-option-checkbox::after) {
  content: '\f00c';
  font-family: 'Font Awesome 5 Free';
  font-weight: 900;
  color: white;
  font-size: 10px;
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
}

.markdown-body :deep(.quiz-option.correct) {
  border-color: #10b981;
  background: rgba(16, 185, 129, 0.05);
}

.markdown-body :deep(.quiz-option.correct .quiz-option-checkbox) {
  border-color: #10b981;
  background: #10b981;
}

.markdown-body :deep(.quiz-option.incorrect) {
  border-color: #ef4444;
  background: rgba(239, 68, 68, 0.05);
}

.markdown-body :deep(.quiz-option.incorrect .quiz-option-checkbox) {
  border-color: #ef4444;
  background: #ef4444;
}

.markdown-body :deep(.quiz-check-btn) {
  padding: 12px 24px;
  background: var(--accent-color);
  color: white;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.markdown-body :deep(.quiz-check-btn:hover) {
  opacity: 0.9;
  transform: translateY(-1px);
}

.markdown-body :deep(.quiz-feedback) {
  margin-top: 20px;
  padding: 12px 18px;
  border-radius: 8px;
  display: none;
  font-weight: 500;
}

.markdown-body :deep(.success-msg) {
  background: rgba(16, 185, 129, 0.1);
  color: #059669;
  border-left: 4px solid #10b981;
}

.markdown-body :deep(.error-msg) {
  background: rgba(239, 68, 68, 0.1);
  color: #dc2626;
  border-left: 4px solid #ef4444;
}

.markdown-body :deep(.warning-msg) {
  background: rgba(245, 158, 11, 0.1);
  color: #d97706;
  border-left: 4px solid #f59e0b;
}

/* Стили для code-task */
.markdown-body :deep(.code-task) {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  background: var(--sidebar-bg);
  border: 1px solid var(--border-color);
  border-radius: 12px;
  padding: 20px;
  margin: 30px 0;
}

.markdown-body :deep(.code-task-left) {
  padding-right: 10px;
  border-right: 1px dashed var(--border-color);
}

.markdown-body :deep(.code-task-right) {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.markdown-body :deep(.code-editor) {
  width: 100%;
  height: 220px;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, "Liberation Mono", "Courier New", monospace;
  font-size: 13px;
  line-height: 1.4;
  color: #e5e7eb;
  background: #1e1e1e;
  border: 1px solid #2d2d2d;
  border-radius: 8px;
  padding: 12px;
}

.markdown-body :deep(.code-task-actions) {
  display: flex;
  gap: 8px;
}

.markdown-body :deep(.code-run-btn) {
  padding: 10px 16px;
  background: var(--accent-color);
  color: white;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
}

.markdown-body :deep(.code-output) {
  background: #0b1220;
  color: #e5e7eb;
  border: 1px solid #1f2937;
  border-radius: 8px;
  padding: 0;
  overflow: hidden;
}

.markdown-body :deep(.output-header) {
  background: #1e293b;
  padding: 8px 12px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #334155;
}

.markdown-body :deep(.status-badge) {
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 700;
  text-transform: uppercase;
}

.markdown-body :deep(.status-badge.ok) { background: #065f46; color: #34d399; }
.markdown-body :deep(.status-badge.fail), 
.markdown-body :deep(.status-badge.build_error), 
.markdown-body :deep(.status-badge.error) { background: #7f1d1d; color: #f87171; }

.markdown-body :deep(.exit-code) {
  font-size: 11px;
  color: #94a3b8;
  font-family: monospace;
}

.markdown-body :deep(.output-section) {
  padding: 12px;
}

.markdown-body :deep(.section-label) {
  font-size: 11px;
  font-weight: 600;
  color: #64748b;
  text-transform: uppercase;
  margin-bottom: 6px;
}

.markdown-body :deep(.stdout), .markdown-body :deep(.stderr) {
  margin: 0;
  padding: 0;
  background: transparent !important;
  border: none !important;
  font-size: 13px;
  line-height: 1.5;
  white-space: pre-wrap;
  color: #e2e8f0;
}

.markdown-body :deep(.output-section.error) {
  border-top: 1px solid #311;
  background: rgba(127, 29, 29, 0.1);
}

.markdown-body :deep(.stderr) {
  color: #f87171;
}

.markdown-body :deep(.code-output.ok) { border-left: 4px solid #10b981; }
.markdown-body :deep(.code-output.fail) { border-left: 4px solid #ef4444; }

@media (max-width: 1024px) {
  .markdown-body :deep(.code-task) {
    grid-template-columns: 1fr;
  }
}

.page-info {
  font-weight: 500;
  color: #94a3b8;
}

@media (max-width: 767px) {
  .markdown-body {
    padding: 15px;
  }
}

.markdown-body :deep(h1), 
.markdown-body :deep(h2), 
.markdown-body :deep(h3), 
.markdown-body :deep(h4), 
.markdown-body :deep(h5), 
.markdown-body :deep(h6) {
  color: var(--header-color) !important;
  border-bottom-color: var(--border-color) !important;
}

.markdown-body :deep(hr) {
  background-color: var(--border-color) !important;
}

.markdown-body :deep(code) {
  background-color: var(--hover-bg) !important;
  color: var(--accent-color) !important;
}

.markdown-body :deep(pre) {
  background-color: #1e1e1e !important; /* Prism Tomorrow theme usually handles this, but let's be sure */
}

.markdown-body :deep(blockquote) {
  border-left-color: var(--border-color) !important;
  color: #64748b !important;
}

.markdown-body :deep(p) {
  margin-top: 0;
  margin-bottom: 16px;
}

.markdown-body :deep(ul), .markdown-body :deep(ol) {
  padding-left: 2em;
  margin-bottom: 16px;
  color: var(--text-color);
}

.markdown-body :deep(li) {
  margin-bottom: 4px;
  cursor: default; /* Overriding my global li:hover if any */
}

.markdown-body :deep(li:hover) {
  color: var(--text-color); /* Overriding my global li:hover */
}

.markdown-body :deep(table) {
  border-collapse: collapse;
  width: 100%;
  margin-bottom: 16px;
}

.markdown-body :deep(table th), .markdown-body :deep(table td) {
  border: 1px solid var(--border-color);
  padding: 8px 12px;
}

.markdown-body :deep(table th) {
  background-color: var(--sidebar-bg);
}

.markdown-body :deep(img) {
  max-width: 100%;
  height: auto;
  border-radius: 8px;
  margin: 16px 0;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.markdown-body :deep(.tip), .markdown-body :deep(.warning), .markdown-body :deep(.info), .markdown-body :deep(.success) {
  padding: 12px 16px;
  margin: 16px 0;
  border-left: 4px solid var(--border-color);
  border-radius: 4px;
  background-color: var(--sidebar-bg);
  font-size: 0.95em;
  line-height: 1.5;
  display: flex;
  align-items: flex-start;
}

.markdown-body :deep(.tip i), .markdown-body :deep(.warning i), .markdown-body :deep(.info i), .markdown-body :deep(.success i) {
  margin-right: 12px;
  margin-top: 2px;
  font-size: 1.2em;
  flex-shrink: 0;
}

.markdown-body :deep(.admonition-content) {
  flex: 1;
}

.markdown-body :deep(.tip) {
  border-left-color: #34a853;
  background-color: rgba(52, 168, 83, 0.1);
}

.markdown-body :deep(.warning) {
  border-left-color: #f9ab00;
  background-color: rgba(249, 171, 0, 0.1);
}

.markdown-body :deep(.info) {
  border-left-color: #4285f4;
  background-color: rgba(66, 133, 244, 0.1);
}

.markdown-body :deep(.success) {
  border-left-color: #34a853;
  background-color: rgba(52, 168, 83, 0.1);
}

.markdown-body :deep(.collapsible) {
  border: 1px solid var(--border-color);
  margin: 16px 0;
  border-radius: 4px;
  overflow: hidden;
}

.markdown-body :deep(.collapsible-header) {
  background: var(--sidebar-bg);
  padding: 12px 16px;
  cursor: pointer;
  font-weight: 600;
  display: flex;
  align-items: center;
  user-select: none;
  border-bottom: 1px solid transparent;
  transition: all 0.2s;
  color: var(--text-color);
}

.markdown-body :deep(.collapsible-header:hover) {
  background: var(--hover-bg);
  color: var(--accent-color);
}

.markdown-body :deep(.collapsible-header::before) {
  content: "▶";
  display: inline-block;
  margin-right: 8px;
  font-size: 0.8em;
  transition: transform 0.2s, color 0.2s;
  color: #64748b;
}

.markdown-body :deep(.collapsible.open > .collapsible-header::before) {
  transform: rotate(90deg);
  color: var(--accent-color);
}

.markdown-body :deep(.collapsible.open > .collapsible-header) {
  border-bottom-color: var(--border-color);
}

.markdown-body :deep(.collapsible-content) {
  padding: 16px;
  display: none;
  background: var(--content-bg);
}

.markdown-body :deep(.collapsible.open > .collapsible-content) {
  display: block;
}
ul {
  list-style: none;
  padding-left: 15px;
}
li {
  cursor: pointer;
  margin-bottom: 5px;
}
li:hover {
  color: #42b983;
}
/* Стили для выбора курсов */
.courses-selection {
  max-width: 1000px;
  margin: 0 auto;
}

.selection-title {
  font-size: 2em;
  margin-bottom: 30px;
  color: var(--text-color);
}

.courses-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 25px;
  margin-bottom: 50px;
}

.other-topics {
  margin-top: 40px;
}

.selection-subtitle {
  font-size: 1.5em;
  margin-bottom: 20px;
  color: var(--text-color);
  opacity: 0.8;
}

.topics-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 15px;
}

.simple-topic-card {
  background: var(--sidebar-bg);
  border: 1px solid var(--border-color);
  border-radius: 8px;
  padding: 15px;
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  transition: all 0.2s;
  color: var(--text-color);
}

.simple-topic-card:hover {
  border-color: var(--accent-color);
  background: var(--hover-bg);
}

.simple-topic-card i.fa-file-alt {
  color: #94a3b8;
}

.completed-icon {
  margin-left: auto;
  color: #10b981;
  font-size: 0.9em;
}

.restricted-content {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
  background: var(--sidebar-bg);
  border-radius: 12px;
  border: 1px dashed var(--border-color);
  margin: 20px 0;
}

.lock-overlay {
  text-align: center;
  padding: 40px;
}

.lock-overlay i {
  font-size: 4rem;
  color: var(--accent-color);
  margin-bottom: 20px;
}

.lock-overlay h3 {
  font-size: 1.8rem;
  margin-bottom: 10px;
  color: var(--header-color);
}

.lock-overlay p {
  font-size: 1.1rem;
  margin-bottom: 24px;
  opacity: 0.8;
}

.lock-overlay .auth-btn {
  background: var(--accent-color);
  color: white;
  border: none;
  padding: 12px 24px;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.lock-overlay .auth-btn:hover {
  transform: translateY(-2px);
  filter: brightness(1.1);
}

@media (max-width: 767px) {
  .courses-grid {
    grid-template-columns: 1fr;
  }
}
</style>
