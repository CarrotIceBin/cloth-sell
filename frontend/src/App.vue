<template>
  <el-config-provider :locale="elementLocale">
    <router-view />
  </el-config-provider>
</template>

<script setup>
import { computed } from 'vue'
import en from 'element-plus/es/locale/lang/en'
import ja from 'element-plus/es/locale/lang/ja'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import zhTw from 'element-plus/es/locale/lang/zh-tw'
import { i18n } from '@/i18n'

const elementLocales = {
  'zh-CN': zhCn,
  'en-US': en,
  'ja-JP': ja,
  'zh-TW': zhTw
}
const elementLocale = computed(() => elementLocales[i18n.global.locale.value])
</script>
