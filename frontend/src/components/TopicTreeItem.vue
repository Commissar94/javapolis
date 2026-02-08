<template>
  <li v-if="item.type === 'folder'" class="folder-item" :class="{ 'expanded': item.expanded }">
    <div class="item-title" @click="item.expanded = !item.expanded">
      <i class="fas" :class="item.expanded ? 'fa-chevron-down' : 'fa-chevron-right'"></i>
      <i class="fas fa-folder"></i>
      <span>{{ item.name }}</span>
    </div>
    <ul v-show="item.expanded" class="sub-list">
      <TopicTreeItem 
        v-for="child in item.children" 
        :key="child.path" 
        :item="child" 
        :current-topic="currentTopic"
        @load-topic="$emit('load-topic', $event)"
      />
    </ul>
  </li>
  <li v-else class="file-item" 
      :class="{ 'active': currentTopic && currentTopic.path === item.path }"
      @click="navigateToTopic(item.path)">
    <i class="far fa-file-alt"></i>
    <span>{{ item.name }}</span>
  </li>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

defineProps({
  item: Object,
  currentTopic: Object
})

defineEmits(['load-topic'])

const navigateToTopic = (path) => {
  const cleanPath = path.replace('.md', '')
  router.push(`/university/${cleanPath}`)
}
</script>

<style scoped>
.folder-item, .file-item {
  list-style: none;
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

.sub-list {
  padding-left: 20px;
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
</style>
