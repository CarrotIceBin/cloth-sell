<template>
  <el-pagination
    class="pager"
    background
    layout="total, sizes, prev, pager, next"
    :total="total"
    :current-page="page"
    :page-size="limit"
    :page-sizes="[10, 15, 20]"
    @current-change="onPage"
    @size-change="onSize"
  />
</template>
<script setup lang="ts">
defineProps<{ total: number; page: number; limit: number }>()
const emit = defineEmits(['update:page', 'update:limit', 'pagination'])

let resizing = false

function onPage(p: number) {
  if (resizing) return
  emit('update:page', p)
  emit('pagination')
}

function onSize(size: number) {
  resizing = true
  emit('update:limit', size)
  emit('update:page', 1)
  emit('pagination')
  setTimeout(() => { resizing = false }, 0)
}
</script>
<style scoped>
.pager {
  display: flex;
  width: 100%;
  margin-top: 20px;
  justify-content: flex-start;
}
</style>
