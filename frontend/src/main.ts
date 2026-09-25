import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import 'element-plus/dist/index.css'
import './styles.css'
import App from './App.vue'
import router from './router'
import { getPermissions } from './hooks/web'

const app = createApp(App)
app.use(ElementPlus, { locale: zhCn })
app.use(router)
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
