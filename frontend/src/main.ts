import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import './styles.css'
import App from './App.vue'
import router from './router'
import { getPermissions } from './hooks/web'
import { i18n, translate } from './i18n'

const app = createApp(App)
app.use(ElementPlus)
app.use(i18n)
app.use(router)
app.config.globalProperties.$t = translate
app.directive('hasPermi', {
  mounted(el, binding) {
    const need = binding.value as string[]
    const owned = getPermissions()
    if (!need.some((item) => owned.includes(item))) {
      el.style.display = 'none'
    }
  }
})
app.mount('#app')
