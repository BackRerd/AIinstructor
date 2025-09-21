import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import '@/assets/styles/reset.css'

// 完整引入
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'

const app = createApp(App)

app.use(router)
app.use(ElementPlus)
app.mount('#app')
