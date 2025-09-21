<template>
  <div class="welcome-container">
    <!-- 背景容器：包含渐变、SVG格栅和Canvas粒子 -->
    <div class="welcome-bg" aria-hidden="true">
      <!-- 动态渐变遮罩 -->
      <div class="gradient-overlay"></div>

      <!-- SVG 细线格栅（CSS动画）-->
      <svg class="grid-lines" viewBox="0 0 100 100" preserveAspectRatio="none" xmlns="http://www.w3.org/2000/svg">
        <defs>
          <linearGradient id="lineGrad" x1="0" x2="1">
            <stop offset="0%" stop-color="rgba(255,255,255,0.06)"/>
            <stop offset="100%" stop-color="rgba(255,255,255,0.01)"/>
          </linearGradient>
        </defs>
        <!-- 横线 -->
        <g class="lines">
          <path d="M0 10 H100" stroke="url(#lineGrad)" stroke-width="0.35"/>
          <path d="M0 25 H100" stroke="url(#lineGrad)" stroke-width="0.28"/>
          <path d="M0 40 H100" stroke="url(#lineGrad)" stroke-width="0.2"/>
          <path d="M0 55 H100" stroke="url(#lineGrad)" stroke-width="0.16"/>
          <path d="M0 70 H100" stroke="url(#lineGrad)" stroke-width="0.12"/>
          <path d="M0 85 H100" stroke="url(#lineGrad)" stroke-width="0.08"/>
        </g>
      </svg>

      <!-- Canvas 粒子网络 -->
      <canvas ref="bgCanvas" class="bg-canvas"></canvas>

      <!-- 额外微粒光晕 -->
      <div class="bokeh bokeh-1"></div>
      <div class="bokeh bokeh-2"></div>
    </div>

    <!-- 主内容区 -->
    <main class="welcome-content">
      <div class="logo-section">
        <img src="@/assets/images/logo.png" alt="AI助手Logo" class="app-logo" />
        <h1 class="app-name">智能AI助手</h1>
      </div>

      <div class="hero-section">
        <h2 class="slogan">开启与智能AI的流畅对话</h2>
        <p class="description">体验前沿技术，解答疑问、获取灵感、提升效率，你的智能伙伴已就位。</p>
        <button @click="enterChat" class="cta-button">开始聊天</button>
      </div>

      <ul class="feature-list">
        <li><span class="icon">💬</span> 智能对话</li>
        <li><span class="icon">⚡</span> 快速响应</li>
        <li><span class="icon">🔒</span> 隐私保护</li>
      </ul>
    </main>

    <footer class="welcome-footer">
      <p>© 2025 智能AI助手. 探索无限可能.</p>
    </footer>

    <!-- 粒子调节面板 -->
    <div class="particle-control-panel" :class="{ 'panel-collapsed': isPanelCollapsed }">
      <div class="panel-header" @click="togglePanel">
        <span>粒子调节器</span>
        <svg class="toggle-icon" viewBox="0 0 24 24">
          <path :d="isPanelCollapsed ? 'M7 10l5 5 5-5' : 'M7 14l5-5 5 5'" />
        </svg>
      </div>
      <div class="panel-content">
        <div class="control-group">
          <label>粒子数量: {{ config.particleCount }}</label>
          <input type="range" v-model.number="config.particleCount" min="20" max="300" step="1"
                 @input="initParticles">
        </div>
        <div class="control-group">
          <label>连接距离: {{ config.maxLinkDistance }}</label>
          <input type="range" v-model.number="config.maxLinkDistance" min="50" max="300" step="5">
        </div>
        <div class="control-group">
          <label>粒子大小: {{ config.particleRadius.toFixed(1) }}</label>
          <input type="range" v-model.number="config.particleRadius" min="0.5" max="3" step="0.1">
        </div>
        <div class="control-group">
          <label>移动速度: {{ config.speed.toFixed(2) }}</label>
          <input type="range" v-model.number="config.speed" min="0.1" max="1" step="0.05">
        </div>
        <div class="control-group">
          <label>连线宽度: {{ config.lineWidth.toFixed(1) }}</label>
          <input type="range" v-model.number="config.lineWidth" min="0.2" max="2" step="0.1">
        </div>
        <div class="control-group color-picker">
          <label>粒子颜色</label>
          <input type="color" v-model="particleColorHex" @input="updateParticleColor">
        </div>
        <button class="reset-btn" @click="resetConfig">重置默认</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, computed } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const enterChat = () => router.push('/chat')

// Canvas ref
const bgCanvas = ref(null)
const isPanelCollapsed = ref(false)

const togglePanel = () => {
  isPanelCollapsed.value = !isPanelCollapsed.value
}

// 粒子系统参数（可调整）
const defaultConfig = {
  breakpoint: 768,
  particleCount: 150,
  maxLinkDistance: 180,
  particleRadius: 1.6,
  speed: 0.25,
  lineWidth: 0.6,
  color: '200, 50, 255',
}

const config = ref({...defaultConfig})
const particleColorHex = ref('#c832ff')

const updateParticleColor = () => {
  const hex = particleColorHex.value.replace('#', '')
  const r = parseInt(hex.substring(0, 2), 16)
  const g = parseInt(hex.substring(2, 4), 16)
  const b = parseInt(hex.substring(4, 6), 16)
  config.value.color = `${r}, ${g}, ${b}`
}

const resetConfig = () => {
  config.value = {...defaultConfig}
  particleColorHex.value = '#c832ff'
  initParticles()
}

let rafId = null
let ctx = null
let dpr = 1
let particles = []
let pointer = { x: null, y: null, isActive: false }
let canvasWidth = 0
let canvasHeight = 0

function rand(min, max) {
  return Math.random() * (max - min) + min
}

function distance(a, b) {
  const dx = a.x - b.x
  const dy = a.y - b.y
  return Math.sqrt(dx * dx + dy * dy)
}

class Particle {
  constructor(w, h) {
    this.reset(w, h)
  }
  reset(w, h) {
    this.x = rand(0, w)
    this.y = rand(0, h)
    const speed = config.value.speed * rand(0.2, 1.1)
    const angle = rand(0, Math.PI * 2)
    this.vx = Math.cos(angle) * speed
    this.vy = Math.sin(angle) * speed
    this.r = config.value.particleRadius * rand(0.6, 1.4)
    this.alpha = rand(0.35, 0.95)
  }
  step(w, h) {
    this.x += this.vx
    this.y += this.vy
    if (this.x < -10 || this.x > w + 10) this.vx *= -1
    if (this.y < -10 || this.y > h + 10) this.vy *= -1
  }
}

function setupCanvas(canvas) {
  if (!canvas) return
  dpr = window.devicePixelRatio || 1
  ctx = canvas.getContext('2d')
  resizeCanvas()
  attachListeners(canvas)
}

function resizeCanvas() {
  const canvas = bgCanvas.value
  if (!canvas) return
  const rect = canvas.getBoundingClientRect()
  canvasWidth = Math.max(rect.width, 1)
  canvasHeight = Math.max(rect.height, 1)
  canvas.width = Math.round(canvasWidth * dpr)
  canvas.height = Math.round(canvasHeight * dpr)
  canvas.style.width = `${canvasWidth}px`
  canvas.style.height = `${canvasHeight}px`
  ctx.setTransform(dpr, 0, 0, dpr, 0, 0)
  initParticles()
}

function initParticles() {
  const isMobile = window.innerWidth <= config.value.breakpoint
  const count = isMobile ? Math.floor(config.value.particleCount * 0.6) : config.value.particleCount
  particles = []
  for (let i = 0; i < count; i++) {
    particles.push(new Particle(canvasWidth, canvasHeight))
  }
}

function draw() {
  if (!ctx) return
  ctx.clearRect(0, 0, canvasWidth, canvasHeight)

  for (const p of particles) {
    ctx.beginPath()
    ctx.arc(p.x, p.y, p.r, 0, Math.PI * 2)
    ctx.fillStyle = `rgba(${config.value.color}, ${p.alpha})`
    ctx.fill()
  }

  const maxDist = config.value.maxLinkDistance
  for (let i = 0; i < particles.length; i++) {
    for (let j = i + 1; j < particles.length; j++) {
      const a = particles[i]
      const b = particles[j]
      const dist = distance(a, b)
      if (dist < maxDist) {
        const t = 1 - dist / maxDist
        ctx.beginPath()
        ctx.moveTo(a.x, a.y)
        ctx.lineTo(b.x, b.y)
        ctx.strokeStyle = `rgba(${config.value.color}, ${0.08 * t})`
        ctx.lineWidth = config.value.lineWidth * (0.6 + 0.4 * t)
        ctx.stroke()
      }
    }
  }

  if (pointer.isActive && pointer.x !== null) {
    for (const p of particles) {
      const dist = Math.hypot(p.x - pointer.x, p.y - pointer.y)
      const range = 120
      if (dist < range && dist > 1) {
        const pull = (1 - dist / range) * 0.6
        p.vx += (pointer.x - p.x) * 0.0009 * pull
        p.vy += (pointer.y - p.y) * 0.0009 * pull
      }
    }
  }
}

function update() {
  for (const p of particles) p.step(canvasWidth, canvasHeight)
  draw()
  rafId = requestAnimationFrame(update)
}

function attachListeners(canvas) {
  if (!canvas) return

  const getCanvasRect = () => canvas.getBoundingClientRect()

  const onPointerMove = (e) => {
    if (!e || typeof e.clientX === 'undefined') return
    const rect = getCanvasRect()
    pointer.isActive = true
    pointer.x = e.clientX - rect.left
    pointer.y = e.clientY - rect.top
  }

  const onPointerDown = (e) => {
    onPointerMove(e)
    pointer.isActive = true
  }

  const onPointerUp = () => {
    pointer.isActive = false
    pointer.x = null
    pointer.y = null
  }

  const onPointerLeave = (e) => {
    pointer.isActive = false
    pointer.x = null
    pointer.y = null
  }

  const onResize = () => {
    resizeCanvas()
  }

  window.addEventListener('pointermove', onPointerMove, { passive: true })
  window.addEventListener('pointerdown', onPointerDown, { passive: true })
  window.addEventListener('pointerup', onPointerUp, { passive: true })
  window.addEventListener('pointercancel', onPointerLeave, { passive: true })
  window.addEventListener('pointerleave', onPointerLeave, { passive: true })
  window.addEventListener('resize', onResize, { passive: true })
  window.addEventListener('blur', onPointerLeave)

  attachListeners._cleanup = () => {
    window.removeEventListener('pointermove', onPointerMove)
    window.removeEventListener('pointerdown', onPointerDown)
    window.removeEventListener('pointerup', onPointerUp)
    window.removeEventListener('pointercancel', onPointerLeave)
    window.removeEventListener('pointerleave', onPointerLeave)
    window.removeEventListener('resize', onResize)
    window.removeEventListener('blur', onPointerLeave)
  }
}

onMounted(() => {
  const canvas = bgCanvas.value
  setupCanvas(canvas)
  rafId = requestAnimationFrame(update)
})

onBeforeUnmount(() => {
  if (rafId) cancelAnimationFrame(rafId)
  if (attachListeners._cleanup) attachListeners._cleanup()
})
</script>

<style scoped>
/* 新增的粒子控制面板样式 */
.particle-control-panel {
  position: fixed;
  right: 20px;
  bottom: 20px;
  width: 280px;
  background: rgba(15, 23, 36, 0.85);
  border-radius: 12px;
  backdrop-filter: blur(10px);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
  border: 1px solid rgba(255, 255, 255, 0.08);
  overflow: hidden;
  transition: all 0.3s ease;
  z-index: 100;
}

.panel-collapsed {
  height: 40px;
}

.panel-header {
  padding: 12px 16px;
  background: rgba(123, 58, 255, 0.15);
  color: #e6eefc;
  font-weight: 600;
  display: flex;
  justify-content: space-between;
  align-items: center;
  cursor: pointer;
  user-select: none;
}

.panel-header:hover {
  background: rgba(123, 58, 255, 0.25);
}

.toggle-icon {
  width: 20px;
  height: 20px;
  fill: currentColor;
  transition: transform 0.2s ease;
}

.panel-collapsed .toggle-icon {
  transform: rotate(180deg);
}

.panel-content {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.control-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.control-group label {
  color: #c9d6f6;
  font-size: 0.85rem;
  font-weight: 500;
}

.control-group input[type="range"] {
  width: 100%;
  height: 6px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 3px;
  outline: none;
  -webkit-appearance: none;
}

.control-group input[type="range"]::-webkit-slider-thumb {
  -webkit-appearance: none;
  width: 16px;
  height: 16px;
  border-radius: 50%;
  background: #7b3aff;
  cursor: pointer;
}

.color-picker {
  margin-top: 8px;
}

.color-picker input[type="color"] {
  width: 100%;
  height: 32px;
  border: none;
  background: transparent;
  cursor: pointer;
}

.reset-btn {
  margin-top: 8px;
  padding: 8px 12px;
  background: rgba(255, 255, 255, 0.1);
  color: #e6eefc;
  border: none;
  border-radius: 6px;
  font-size: 0.85rem;
  cursor: pointer;
  transition: background 0.2s ease;
}

.reset-btn:hover {
  background: rgba(255, 255, 255, 0.2);
}

/* 响应式调整 */
@media (max-width: 420px) {
  .particle-control-panel {
    width: 260px;
    right: 10px;
    bottom: 10px;
  }
}

/* 原有的其他样式保持不变... */
.welcome-bg {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  overflow: hidden;
  z-index: -1;
  pointer-events: none;
  background: #0f1724;
}

.gradient-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(120deg,
  rgba(100,26,207,0.08) 0%,
  rgba(58,160,255,0.06) 28%,
  rgba(170,120,255,0.05) 60%,
  rgba(20,30,60,0.06) 100%);
  mix-blend-mode: screen;
  filter: blur(36px);
  transform: translateZ(0);
  animation: slowShift 12s linear infinite;
  opacity: 0.95;
}

@keyframes slowShift {
  0% { transform: translate3d(-6%, -2%, 0) scale(1.03); }
  50% { transform: translate3d(6%, 2%, 0) scale(1.02); }
  100% { transform: translate3d(-6%, -2%, 0) scale(1.03); }
}

.grid-lines {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  transform: translateZ(0);
  opacity: 0.65;
  mix-blend-mode: overlay;
  pointer-events: none;
}

.grid-lines .lines {
  animation: gridFloat 18s linear infinite;
  transform-origin: center;
}

@keyframes gridFloat {
  0% { transform: translateY(-1.5%); opacity: .85; }
  50% { transform: translateY(1.5%); opacity: .65; }
  100% { transform: translateY(-1.5%); opacity: .85; }
}

.bg-canvas {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  display: block;
  z-index: 0;
  mix-blend-mode: screen;
  opacity: 0.95;
}

.bokeh {
  position: absolute;
  width: 360px;
  height: 360px;
  border-radius: 50%;
  filter: blur(60px);
  opacity: 0.12;
  pointer-events: none;
  mix-blend-mode: screen;
}
.bokeh-1 {
  background: radial-gradient(circle at 30% 30%, rgba(100,26,207,0.9), rgba(100,26,207,0.06) 40%, transparent 60%);
  top: 6%;
  left: 6%;
  transform: translateZ(0);
}
.bokeh-2 {
  background: radial-gradient(circle at 70% 70%, rgba(58,160,255,0.9), rgba(58,160,255,0.05) 38%, transparent 62%);
  bottom: 8%;
  right: 6%;
}

.welcome-container {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  position: relative;
  justify-content: space-between;
  align-items: center;
  text-align: center;
  overflow: hidden;
}

.welcome-content {
  padding: 2rem;
  margin-top: 8.5rem;
  z-index: 1;
  max-width: 900px;
  width: 100%;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1.5rem;
  color: #e6eefc;
}

.logo-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  width: 100%;
}

.app-logo {
  display: block;
  width: clamp(56px, 12vw, 96px);
  height: auto;
  margin: 0;
  object-fit: contain;
  filter: drop-shadow(0 6px 20px rgba(100,26,207,0.18));
}

.app-name {
  font-size: clamp(1.5rem, 3.5vw, 2.5rem);
  font-weight: 700;
  color: #f0f6ff;
  margin: 0;
  text-shadow: 0 2px 12px rgba(20,10,50,0.35);
}

.hero-section {
  margin-bottom: 1rem;
  max-width: 720px;
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.75rem;
}

.slogan {
  font-size: clamp(1.25rem, 3.25vw, 2rem);
  font-weight: 600;
  color: #cfd4ff;
  margin-bottom: 0;
}

.description {
  font-size: clamp(0.95rem, 2.2vw, 1.1rem);
  color: #c9d6f6;
  margin: 0;
  line-height: 1.6;
  padding: 0 1rem;
}

.cta-button {
  background: linear-gradient(90deg, #7b3aff 0%, #3aa0ff 100%);
  color: white;
  border: none;
  padding: 1rem 2.5rem;
  font-size: 1.05rem;
  font-weight: 600;
  border-radius: 50px;
  cursor: pointer;
  transition: transform .22s ease, box-shadow .22s ease;
  box-shadow: 0 8px 30px rgba(60,20,110,0.25);
  margin-top: 0.5rem;
  min-width: 160px;
}

.cta-button:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 40px rgba(60,20,110,0.34);
}

.feature-list {
  display: flex;
  justify-content: center;
  gap: 2rem;
  list-style: none;
  padding: 0;
  margin: 0;
  color: #cbd6e8;
  flex-wrap: wrap;
}

.feature-list li {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.25rem 0.5rem;
}

.welcome-footer {
  padding: 1.25rem;
  color: #9fb0d9;
  width: 100%;
  text-align: center;
}

@media (max-width: 768px) {
  .welcome-content { padding: 1.25rem; margin-top: 2rem; }
  .slogan { font-size: 1.5rem; }
  .cta-button { padding: 0.9rem 1.8rem; font-size: 1rem; min-width: 140px; }
  .feature-list { gap: 1rem; }
}

@media (max-width: 420px) {
  .bokeh { display: none; }
  .grid-lines { opacity: 0.35; }
  .app-logo { width: clamp(48px, 18vw, 72px); }
  .app-name { font-size: 1.35rem; }
}
</style>