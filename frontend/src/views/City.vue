<template>
  <div class="city-container">
    <div class="city-map">
      <!-- Фон города можно будет добавить позже картинкой, пока сделаем стилизованный градиент -->
      <div class="map-overlay"></div>
      
      <div class="buildings-grid">
        <!-- Университет -->
        <div class="building-wrapper university-building" @click="goToUniversity">
          <div class="building-label">Университет</div>
          <div class="building-icon">
            <i class="fas fa-university"></i>
          </div>
          <div class="building-base"></div>
        </div>

        <!-- Музей -->
        <div class="building-wrapper museum-building" @click="router.push('/museum')">
          <div class="building-label">Музей</div>
          <div class="building-icon">
            <i class="fas fa-museum"></i>
          </div>
          <div class="building-base"></div>
        </div>

        <!-- Форум -->
        <div class="building-wrapper forum-building" @click="router.push('/forum')">
          <div class="building-label">Форум разработчиков</div>
          <div class="building-icon">
            <i class="fas fa-comments"></i>
          </div>
          <div class="building-base"></div>
        </div>

        <!-- Бизнес центр -->
        <div class="building-wrapper business-building" @click="router.push('/business')">
          <div class="building-label">Бизнес центр</div>
          <div class="building-icon">
            <i class="fas fa-briefcase"></i>
          </div>
          <div class="building-base"></div>
        </div>

        <!-- Жилой квартал (Заглушка) -->
        <div class="building-wrapper construction" @click="showConstructionNotice('Жилой квартал')">
          <div class="building-label">Жилой квартал</div>
          <div class="building-status">Стройка идет...</div>
          <div class="building-icon">
            <i class="fas fa-home"></i>
            <div class="crane"><i class="fas fa-tools"></i></div>
          </div>
          <div class="building-base"></div>
        </div>

        <!-- Парк (Заглушка) -->
        <div class="building-wrapper construction" @click="showConstructionNotice('Городской парк')">
          <div class="building-label">Парк</div>
          <div class="building-status">Стройка идет...</div>
          <div class="building-icon">
            <i class="fas fa-tree"></i>
          </div>
          <div class="building-base"></div>
        </div>
      </div>
    </div>

    <!-- Уведомление о стройке -->
    <transition name="fade">
      <div v-if="noticeBuilding" class="construction-modal" @click="noticeBuilding = null">
        <div class="modal-content" @click.stop>
          <i class="fas fa-hard-hat"></i>
          <h3>{{ noticeBuilding }}</h3>
          <p>В этом районе сейчас ведутся строительные работы. Возвращайтесь позже, когда здание будет готово к эксплуатации!</p>
          <button @click="noticeBuilding = null" class="close-btn">Понятно</button>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const noticeBuilding = ref(null)

const goToUniversity = () => {
  router.push('/university')
}

const showConstructionNotice = (name) => {
  noticeBuilding.value = name
}
</script>

<style scoped>
.city-container {
  width: 100%;
  height: calc(100vh - 60px);
  background: radial-gradient(circle at center, #2c3e50 0%, #000000 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  position: relative;
}

.city-map {
  width: 100%;
  max-width: 1200px;
  height: 100%;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
}

.map-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image: 
    linear-gradient(rgba(255,255,255,0.05) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255,255,255,0.05) 1px, transparent 1px);
  background-size: 50px 50px;
  pointer-events: none;
}

.buildings-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  grid-template-rows: repeat(2, 1fr);
  gap: 60px;
  width: 100%;
  height: 100%;
  align-items: center;
  justify-items: center;
  z-index: 1;
}

.building-wrapper {
  position: relative;
  width: 200px;
  height: 200px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

.building-wrapper:hover {
  transform: translateY(-10px) scale(1.05);
}

.building-label {
  position: absolute;
  top: -30px;
  background: rgba(0, 0, 0, 0.7);
  color: #fff;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 0.9em;
  font-weight: 600;
  white-space: nowrap;
  border: 1px solid var(--accent-color);
  box-shadow: 0 4px 10px rgba(0,0,0,0.3);
  z-index: 2;
}

.university-building .building-label { border-color: var(--accent-color); }
.museum-building .building-label { border-color: #f39c12; }
.forum-building .building-label { border-color: #42b983; }
.business-building .building-label { border-color: #38bdf8; }

.building-status {
  position: absolute;
  bottom: -25px;
  font-size: 0.75em;
  color: #f39c12;
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 1px;
}

.building-icon {
  width: 120px;
  height: 120px;
  background: var(--card-bg);
  border: 3px solid var(--border-color);
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 4em;
  color: var(--accent-color);
  box-shadow: 0 10px 20px rgba(0,0,0,0.4);
  position: relative;
  z-index: 1;
}

.university-building .building-icon,
.museum-building .building-icon,
.forum-building .building-icon,
.business-building .building-icon {
  background: linear-gradient(135deg, #1e293b 0%, #0f172a 100%);
  border-color: var(--accent-color);
  color: #fff;
}

.museum-building .building-icon {
  border-color: #f39c12;
}

.forum-building .building-icon {
  border-color: #42b983;
}

.business-building .building-icon {
  border-color: #38bdf8;
}

.building-base {
  position: absolute;
  bottom: 20px;
  width: 100px;
  height: 40px;
  background: rgba(0,0,0,0.3);
  filter: blur(10px);
  border-radius: 50%;
  transform: scaleY(0.5);
  z-index: 0;
}

.construction .building-icon {
  filter: grayscale(0.8);
  opacity: 0.7;
}

.crane {
  position: absolute;
  top: -15px;
  right: -15px;
  width: 40px;
  height: 40px;
  background: #f39c12;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.3em;
  color: #000;
  animation: rotate 2s infinite ease-in-out;
}

@keyframes rotate {
  0%, 100% { transform: rotate(-10deg); }
  50% { transform: rotate(10deg); }
}

/* Modal Styles */
.construction-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  backdrop-filter: blur(5px);
}

.modal-content {
  background: var(--card-bg);
  padding: 40px;
  border-radius: 20px;
  max-width: 400px;
  text-align: center;
  border: 1px solid var(--border-color);
  box-shadow: 0 20px 40px rgba(0,0,0,0.5);
}

.modal-content i {
  font-size: 4em;
  color: #f39c12;
  margin-bottom: 20px;
}

.modal-content h3 {
  margin-bottom: 15px;
  font-size: 1.5em;
}

.modal-content p {
  color: var(--text-color);
  opacity: 0.8;
  margin-bottom: 25px;
  line-height: 1.6;
}

.close-btn {
  background: var(--accent-color);
  color: white;
  border: none;
  padding: 10px 30px;
  border-radius: 10px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.close-btn:hover {
  filter: brightness(1.1);
  transform: translateY(-2px);
}

.fade-enter-active, .fade-leave-active {
  transition: opacity 0.3s;
}
.fade-enter-from, .fade-leave-to {
  opacity: 0;
}

@media (max-width: 768px) {
  .buildings-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 40px;
  }
  .building-wrapper {
    width: 150px;
    height: 150px;
  }
  .building-icon {
    width: 90px;
    height: 90px;
    font-size: 2.5em;
  }
}
</style>
