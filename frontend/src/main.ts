import { createApp } from 'vue'
import { createPinia } from 'pinia'
import piniaPersist from 'pinia-plugin-persistedstate'
import './style.css'
import App from './App.vue'
import ElementPlus from 'element-plus'
import router from './router'
import i18n from './i18n'
// import 'element-plus/dist/index.css'
import './styles/jianwei-theme.scss'
import '@/assets/iconfont.css'
import * as ELIcons from '@element-plus/icons-vue'
import fetchIconFont from '@/components/iconfont/iconfont' // 获取iconfont文件
import IconFont from '@/components/iconfont/IconFont.vue' // 封装的iconfont svg 组件，用法 <icon-font type="icon-xxx" />
import dragWidth from '@/directives/dragWidth.ts'
import loadMore from '@/directives/loadMore.ts'

// createApp(App).mount('#app')
const app = createApp(App)

for (const iconName in ELIcons) {
  app.component(iconName, ELIcons[iconName])
}

fetchIconFont({
  scriptUrl: '//at.alicdn.com/t/c/font_4068394_nbbwqpmhqgs.js'
})
app.component('IconFont', IconFont) // 注册iconfont 组件
const pinia = createPinia()
pinia.use(piniaPersist)

app.use(pinia)
app.use(ElementPlus)
app.use(i18n)
app.use(router)
app.use(dragWidth)
app.use(loadMore)

app.mount('#app')

// app.directive('focus', {
//   mounted:(el: any) => {el.focus() }
// })
