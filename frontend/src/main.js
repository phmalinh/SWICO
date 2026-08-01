import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import './style.css'

import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import { createI18n } from './i18n'

const app = createApp(App)
const i18n = createI18n()

app.use(router)
app.use(ElementPlus)
app.use(i18n)

app.mount('#app')