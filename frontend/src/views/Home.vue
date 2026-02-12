<template>
  <div class="home">
    <!-- Hero Section -->
    <header class="hero">
      <div class="hero-content">
        <h1 class="hero-title">Добро пожаловать в Джаваполис</h1>
        <p class="hero-subtitle">
          Джаваполис — это место, где можно начать путь в разработке без страха выглядеть глупо.
        </p>
        <div class="hero-cta">
          <router-link to="/university" class="cta-button primary">Выбрать первый курс</router-link>
          <a href="#about" class="cta-button secondary">Узнать больше</a>
        </div>
      </div>
      <div class="hero-image">
        <i class="fas fa-city"></i>
      </div>
    </header>

    <!-- Emotional Connection Section -->
    <section id="about" class="section empathy">
      <div class="container">
        <div class="empathy-content">
          <p class="quote">Здесь не смеются над вопросами. Не закатывают глаза. Не говорят «это же база».</p>
          <p class="highlight">Здесь помнят, каково быть новичком.</p>
          
          <div class="feelings-grid">
            <h3>Если ты когда-то чувствовал:</h3>
            <ul>
              <li><i class="fas fa-check-circle"></i> что код — это слишком сложно</li>
              <li><i class="fas fa-check-circle"></i> что все вокруг понимают больше тебя</li>
              <li><i class="fas fa-check-circle"></i> что ты “не создан для программирования”</li>
              <li><i class="fas fa-check-circle"></i> что спрашивать стыдно</li>
            </ul>
            <p class="conclusion">— ты в правильном месте.</p>
          </div>
        </div>
      </div>
    </section>

    <!-- Core Philosophy -->
    <section class="section philosophy">
      <div class="container">
        <div class="philosophy-grid">
          <div class="philosophy-card">
            <i class="fas fa-brain"></i>
            <p>В Джаваполисе можно не понимать. Можно ошибаться. Можно задавать вопросы.</p>
          </div>
          <div class="philosophy-card">
            <i class="fas fa-seedling"></i>
            <p>И постепенно — начать понимать.</p>
          </div>
        </div>
      </div>
    </section>

    <!-- Features Section -->
    <section class="section features">
      <div class="container">
        <h2 class="section-title">Что тебя ждёт</h2>
        <p class="section-desc">Каждый курс — это не просто лекции. Это путь от теории к практике.</p>
        
        <div class="features-grid">
          <div class="feature-item">
            <div class="feature-icon"><i class="fas fa-lightbulb"></i></div>
            <h3>Понятные объяснения</h3>
            <p>Без снобизма и сложных терминов на старте.</p>
          </div>
          <div class="feature-item">
            <div class="feature-icon"><i class="fas fa-stairs"></i></div>
            <h3>Шаг за шагом</h3>
            <p>От самого нуля до рабочего проекта.</p>
          </div>
          <div class="feature-item">
            <div class="feature-icon"><i class="fas fa-hammer"></i></div>
            <h3>Реальный результат</h3>
            <p>Практическая задача, которую ты реально завершишь.</p>
          </div>
          <div class="feature-item">
            <div class="feature-icon"><i class="fas fa-trophy"></i></div>
            <h3>Уверенность</h3>
            <p>Ощущение: «Я сделал это сам».</p>
          </div>
        </div>

        <div class="approach">
          <p>Мы не учим ради теории. <strong>Мы строим.</strong></p>
          <div class="steps">
            <span>Первый проект — простой.</span>
            <i class="fas fa-arrow-right"></i>
            <span>Второй — сложнее.</span>
            <i class="fas fa-arrow-right"></i>
            <span>Третий — уже настоящий.</span>
          </div>
          <p>Ты не заметишь, как перестанешь бояться кода.</p>
        </div>
      </div>
    </section>

    <!-- Growth Section -->
    <section class="section growth">
      <div class="container">
        <div class="growth-banner">
          <h2>Джаваполис — это город роста</h2>
          <p>Здесь важен не статус, а прогресс. Не “кто ты сейчас”, а “кем ты становишься”.</p>
          <div class="growth-values">
            <div class="value">Без давления</div>
            <div class="value">Без токсичности</div>
            <div class="value">Без иллюзий</div>
          </div>
        </div>
      </div>
    </section>

    <!-- Why it works Section -->
    <section class="section why">
      <div class="container">
        <h2 class="section-title">Почему это работает</h2>
        <div class="why-grid">
          <div class="why-item">
            <i class="fas fa-list-ul"></i>
            <p>Материал структурирован</p>
          </div>
          <div class="why-item">
            <i class="fas fa-bullseye"></i>
            <p>Каждый курс ведёт к результату</p>
          </div>
          <div class="why-item">
            <i class="fas fa-chart-line"></i>
            <p>Ты видишь свой прогресс</p>
          </div>
          <div class="why-item">
            <i class="fas fa-users"></i>
            <p>И ты не один</p>
          </div>
        </div>
        <p class="why-footer">Мы строим среду, где новичок — это не повод для шуток. Это начало пути.</p>
      </div>
    </section>

    <!-- Final CTA -->
    <section class="section final-cta">
      <div class="container">
        <h2>Готов начать?</h2>
        <div class="cta-box">
          <div class="cta-text">
            <p>Если ты хочешь перестать бояться кода, разобраться, как всё работает и почувствовать уверенность — присоединяйся.</p>
          </div>
          <div class="cta-actions">
            <router-link to="/university" class="cta-button primary large">Присоединиться к Джаваполису</router-link>
            <p class="cta-sub">Создай аккаунт. Выбери первый курс. Сделай первый шаг.</p>
          </div>
        </div>
        <p class="footer-note">Остальное — вопрос времени и практики.</p>
      </div>
    </section>
  </div>
</template>

<script setup>
// Компонент главной страницы
</script>

<style scoped>
.home {
  color: var(--text-color);
  line-height: 1.6;
}

.container {
  max-width: 1100px;
  margin: 0 auto;
  padding: 0 20px;
}

.section {
  padding: 80px 0;
}

/* Hero Section */
.hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 100px 5%;
  background: linear-gradient(135deg, var(--nav-bg) 0%, var(--bg-color) 100%);
  min-height: 80vh;
}

.hero-content {
  flex: 1;
  max-width: 600px;
}

.hero-title {
  font-size: 3.5rem;
  font-weight: 800;
  line-height: 1.2;
  margin-bottom: 20px;
  color: var(--header-color);
}

.hero-subtitle {
  font-size: 1.25rem;
  opacity: 0.9;
  margin-bottom: 40px;
}

.hero-cta {
  display: flex;
  gap: 20px;
}

.hero-image {
  flex: 1;
  display: flex;
  justify-content: center;
  font-size: 15rem;
  color: var(--accent-color);
  opacity: 0.15;
}

.cta-button {
  padding: 14px 28px;
  border-radius: 8px;
  font-weight: 600;
  text-decoration: none;
  transition: all 0.2s;
  display: inline-block;
}

.cta-button.primary {
  background: var(--accent-color);
  color: white;
}

.cta-button.primary:hover {
  filter: brightness(1.1);
  transform: translateY(-2px);
}

.cta-button.secondary {
  border: 2px solid var(--border-color);
  color: var(--text-color);
}

.cta-button.secondary:hover {
  background: var(--hover-bg);
}

/* Empathy Section */
.empathy {
  background: var(--sidebar-bg);
  text-align: center;
}

.quote {
  font-size: 1.5rem;
  font-style: italic;
  margin-bottom: 10px;
  opacity: 0.8;
}

.highlight {
  font-size: 1.8rem;
  font-weight: 700;
  color: var(--accent-color);
  margin-bottom: 50px;
}

.feelings-grid {
  background: var(--card-bg);
  padding: 40px;
  border-radius: 20px;
  box-shadow: 0 10px 30px rgba(0,0,0,0.05);
  max-width: 700px;
  margin: 0 auto;
  text-align: left;
}

.feelings-grid h3 {
  margin-top: 0;
  margin-bottom: 25px;
  font-size: 1.4rem;
}

.feelings-grid ul {
  list-style: none;
  padding: 0;
  margin: 0 0 25px 0;
}

.feelings-grid li {
  margin-bottom: 15px;
  display: flex;
  align-items: center;
  gap: 15px;
  font-size: 1.1rem;
}

.feelings-grid li i {
  color: #ef4444;
  opacity: 0.7;
}

.conclusion {
  font-weight: 700;
  font-size: 1.2rem;
  text-align: center;
  margin: 0;
  color: var(--accent-color);
}

/* Philosophy */
.philosophy {
  background: var(--bg-color);
}

.philosophy-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 30px;
}

.philosophy-card {
  padding: 40px;
  background: var(--sidebar-bg);
  border-radius: 15px;
  text-align: center;
  border-bottom: 4px solid var(--accent-color);
}

.philosophy-card i {
  font-size: 3rem;
  color: var(--accent-color);
  margin-bottom: 20px;
}

.philosophy-card p {
  font-size: 1.2rem;
  margin: 0;
}

/* Features */
.features {
  background: var(--sidebar-bg);
  text-align: center;
}

.section-title {
  font-size: 2.5rem;
  margin-bottom: 10px;
}

.section-desc {
  font-size: 1.2rem;
  opacity: 0.7;
  margin-bottom: 60px;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 40px;
  margin-bottom: 80px;
}

.feature-item {
  text-align: center;
}

.feature-icon {
  width: 70px;
  height: 70px;
  background: rgba(79, 193, 255, 0.1);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 2rem;
  color: var(--accent-color);
  margin: 0 auto 20px;
}

.feature-item h3 {
  margin-bottom: 15px;
}

.feature-item p {
  opacity: 0.8;
}

.approach {
  background: var(--card-bg);
  padding: 40px;
  border-radius: 20px;
  display: inline-block;
  width: 100%;
}

.steps {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 20px;
  margin: 30px 0;
  font-weight: 700;
  font-size: 1.1rem;
}

.steps i {
  color: var(--accent-color);
}

/* Growth */
.growth {
  background: linear-gradient(rgba(0,0,0,0.7), rgba(0,0,0,0.7)), url('https://images.unsplash.com/photo-1480714378408-67cf0d13bc1b?ixlib=rb-1.2.1&auto=format&fit=crop&w=1350&q=80');
  background-size: cover;
  background-position: center;
  color: white;
  text-align: center;
  padding: 120px 0;
}

.growth h2 {
  font-size: 2.5rem;
  margin-bottom: 20px;
}

.growth p {
  font-size: 1.3rem;
  margin-bottom: 40px;
  max-width: 800px;
  margin-left: auto;
  margin-right: auto;
}

.growth-values {
  display: flex;
  justify-content: center;
  gap: 40px;
}

.value {
  padding: 10px 25px;
  background: rgba(255,255,255,0.1);
  border-radius: 30px;
  backdrop-filter: blur(5px);
  font-weight: 600;
}

/* Why Section */
.why {
  text-align: center;
}

.why-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 30px;
  margin-bottom: 50px;
}

.why-item {
  padding: 30px;
  background: var(--sidebar-bg);
  border-radius: 12px;
  transition: transform 0.3s;
}

.why-item:hover {
  transform: translateY(-5px);
}

.why-item i {
  font-size: 2.5rem;
  color: var(--accent-color);
  margin-bottom: 15px;
}

.why-item p {
  font-weight: 600;
  margin: 0;
}

.why-footer {
  font-size: 1.2rem;
  font-weight: 500;
  color: var(--accent-color);
}

/* Final CTA */
.final-cta {
  background: var(--sidebar-bg);
  text-align: center;
}

.final-cta h2 {
  font-size: 2.5rem;
  margin-bottom: 40px;
}

.cta-box {
  background: var(--card-bg);
  padding: 60px 40px;
  border-radius: 30px;
  box-shadow: 0 20px 50px rgba(0,0,0,0.1);
  max-width: 900px;
  margin: 0 auto;
}

.cta-text p {
  font-size: 1.4rem;
  margin-bottom: 40px;
}

.cta-button.large {
  font-size: 1.2rem;
  padding: 18px 40px;
}

.cta-sub {
  margin-top: 20px;
  opacity: 0.7;
  font-weight: 500;
}

.footer-note {
  margin-top: 40px;
  font-style: italic;
  opacity: 0.6;
}

@media (max-width: 768px) {
  .hero {
    flex-direction: column;
    text-align: center;
    padding-top: 60px;
  }
  
  .hero-image {
    display: none;
  }
  
  .hero-title {
    font-size: 2.5rem;
  }
  
  .hero-cta {
    justify-content: center;
    flex-direction: column;
  }
  
  .philosophy-grid {
    grid-template-columns: 1fr;
  }
  
  .steps {
    flex-direction: column;
  }
  
  .steps i {
    transform: rotate(90deg);
  }
  
  .growth-values {
    flex-direction: column;
    gap: 15px;
  }
}
</style>
