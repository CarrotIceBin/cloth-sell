<template>
  <select v-model="locale" class="language-switcher" :aria-label="$t('语言')">
    <option value="zh-CN">简体中文</option>
    <option value="en-US">English</option>
    <option value="ja-JP">日本語</option>
    <option value="zh-TW">繁體中文</option>
  </select>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { i18n, setLocale, type AppLocale } from '@/i18n'

const locale = computed({
  get: () => i18n.global.locale.value,
  set: (value: AppLocale) => setLocale(value)
})
</script>

<style scoped>
.language-switcher {
  border: 1px solid var(--line);
  border-radius: 2px;
  background: #fff;
  color: var(--ink);
  padding: 5px 8px;
  font: inherit;
  font-size: 12px;
  cursor: pointer;
}
</style>
