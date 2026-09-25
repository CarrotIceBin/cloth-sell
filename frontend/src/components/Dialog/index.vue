<template>
  <el-dialog :title="title" :model-value="modelValue" @update:model-value="emit('update:modelValue', $event)" width="640px">
    <slot />
    <template #footer><slot name="footer" /></template>
  </el-dialog>
</template>
<script setup lang="ts">
defineProps<{ title: string; modelValue: boolean }>()
const emit = defineEmits(['update:modelValue'])
</script>
