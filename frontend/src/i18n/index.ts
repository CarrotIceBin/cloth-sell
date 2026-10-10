import { createI18n } from 'vue-i18n'
import zhCN from './locales/zh-CN'
import enUS from './locales/en-US'
import jaJP from './locales/ja-JP'
import zhTW from './locales/zh-TW'

export const supportedLocales = ['zh-CN', 'en-US', 'ja-JP', 'zh-TW'] as const
export type AppLocale = (typeof supportedLocales)[number]

type LocaleMessages = Record<string, unknown>

const messages: Record<AppLocale, LocaleMessages> = {
  'zh-CN': zhCN,
  'en-US': enUS,
  'ja-JP': jaJP,
  'zh-TW': zhTW
}

const savedLocale = localStorage.getItem('locale')
const initialLocale: AppLocale =
  supportedLocales.find((locale) => locale === savedLocale) || 'zh-CN'
document.documentElement.lang = initialLocale

export const i18n = createI18n({
  legacy: false,
  locale: initialLocale,
  fallbackLocale: 'zh-CN',
  messages
})

function createMessageIndex(source: unknown) {
  const result = new Map<string, string>()
  const leafKeys = new Map<string, string[]>()

  function visit(value: unknown, path: string[] = []) {
    if (!value || typeof value !== 'object') return
    for (const [key, child] of Object.entries(value)) {
      const childPath = [...path, key]
      if (typeof child === 'string') {
        result.set(childPath.join('.'), child)
        const paths = leafKeys.get(key) ?? []
        paths.push(childPath.join('.'))
        leafKeys.set(key, paths)
      } else {
        visit(child, childPath)
      }
    }
  }

  visit(source)
  for (const [key, paths] of leafKeys) {
    if (paths.length === 1) {
      const message = result.get(paths[0])
      if (message !== undefined) result.set(key, message)
    }
  }
  return result
}

function createLegacyKeyIndex(source: unknown) {
  const pathsByMessage = new Map<string, string[]>()

  function visit(value: unknown, path: string[] = []) {
    if (!value || typeof value !== 'object') return
    for (const [key, child] of Object.entries(value)) {
      const childPath = [...path, key]
      if (typeof child === 'string') {
        const paths = pathsByMessage.get(child) ?? []
        paths.push(childPath.join('.'))
        pathsByMessage.set(child, paths)
      } else {
        visit(child, childPath)
      }
    }
  }

  visit(source)
  const result = new Map<string, string>()
  for (const [message, paths] of pathsByMessage) {
    if (paths.length === 1) result.set(message, paths[0])
  }
  return result
}

const messageIndexes: Record<AppLocale, Map<string, string>> = {
  'zh-CN': createMessageIndex(zhCN),
  'en-US': createMessageIndex(enUS),
  'ja-JP': createMessageIndex(jaJP),
  'zh-TW': createMessageIndex(zhTW)
}
const legacyKeyIndex = createLegacyKeyIndex(zhCN)

export function setLocale(locale: AppLocale) {
  i18n.global.locale.value = locale
  localStorage.setItem('locale', locale)
  document.documentElement.lang = locale
}

export function translate(key: string, params?: Record<string, unknown>) {
  const locale =
    supportedLocales.find((supportedLocale) => supportedLocale === i18n.global.locale.value) || 'zh-CN'
  const messageKey = legacyKeyIndex.get(key) ?? key
  const message =
    messageIndexes[locale].get(messageKey) ?? messageIndexes['zh-CN'].get(messageKey) ?? key

  return message.replace(/\{([^{}]+)\}/g, (placeholder, name: string) => {
    const value = params?.[name]
    return value === undefined || value === null ? placeholder : String(value)
  })
}
