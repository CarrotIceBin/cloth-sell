import 'vue'

declare module 'vue' {
  interface ComponentCustomProperties {
    $t(key: string, params?: Record<string, unknown>): string
  }
}
