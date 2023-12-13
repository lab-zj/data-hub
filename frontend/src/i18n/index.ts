/**
 * i18n 国际化
 */
import { createI18n } from 'vue-i18n'
import zh from './zh'
import en from './en'


const i18nMessage = {
  en,
  zh,
}

const i18n = createI18n({
  locale: 'zh', // 默认locale
  fallbackLocale: 'zh', // 首选语言缺少翻译时使用的locale
  messages: i18nMessage,
  legacy: false,
})

export default i18n
