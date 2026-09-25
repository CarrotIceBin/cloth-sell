<template><el-icon class="align-middle"><component :is="icon" /></el-icon></template>
<script setup lang="ts">
import { computed } from 'vue'
import { Search, Refresh, Plus, Edit, Delete } from '@element-plus/icons-vue'
const props = defineProps<{ icon: string }>()
const map: Record<string, unknown> = {
  'ep:search': Search,
  'ep:refresh': Refresh,
  'ep:plus': Plus,
  'ep:edit': Edit,
  'ep:delete': Delete
}
const icon = computed(() => map[props.icon] || Search)
</script>
