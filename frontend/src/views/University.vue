<template>
  <div class="university">
    <h1>Университет</h1>
    <div class="container">
      <div class="sidebar">
        <div class="sidebar-header">
          <i class="fas fa-graduation-cap"></i>
          <span>Темы курса</span>
        </div>
        <div class="sidebar-content">
          <ul v-if="structure" class="topic-list">
            <TopicTreeItem 
              v-for="item in structure" 
              :key="item.path" 
              :item="item" 
              :current-topic="currentTopic"
              @load-topic="loadTopic"
            />
          </ul>
        </div>
      </div>
      <div class="content">
        <div class="content-inner">
          <div v-if="currentTopic">
            <h2 class="topic-title">{{ currentTopic.name }}</h2>
            <div class="markdown-body" v-html="content"></div>
          </div>
          <div v-else class="empty-state">
            <i class="fas fa-book-open"></i>
            <p>Выберите тему из списка слева, чтобы начать обучение</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import axios from 'axios'
import TopicTreeItem from '../components/TopicTreeItem.vue'

const structure = ref(null)
const currentTopic = ref(null)
const content = ref('')

onMounted(async () => {
  try {
    const res = await axios.get('/api/university/structure')
    structure.value = res.data
  } catch (e) {
    console.error('Failed to load structure', e)
  }
})

const loadTopic = async (path) => {
  try {
    // В API путь ожидается без .md и после /api/university/topic/
    const cleanPath = path.replace('.md', '')
    const res = await axios.get(`/api/university/topic/${cleanPath}`)
    currentTopic.value = res.data.currentTopic
    content.value = res.data.content
    
    nextTick(() => {
      // Автоматически раскрываем папки по пути к текущему топику
      if (structure.value && currentTopic.value) {
        expandFoldersToPath(structure.value, currentTopic.value.path);
      }
    
      if (window.Prism) {
        window.Prism.highlightAll()
      }
      setupCollapsibles()
    })
  } catch (e) {
    console.error('Failed to load topic', e)
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
  height: calc(100vh - 120px);
  max-width: 1400px;
  margin: 0 auto;
  background: var(--content-bg);
  box-shadow: 0 0 20px rgba(0,0,0,0.1);
  border-radius: 8px;
  overflow: hidden;
}

.sidebar {
  width: 300px;
  display: flex;
  flex-direction: column;
  background: var(--sidebar-bg);
  border-right: 1px solid var(--border-color);
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
  background: var(--content-bg);
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
</style>
