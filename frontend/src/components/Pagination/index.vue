<template>
  <el-pagination
    class="pager"
    background
    layout="total, sizes, prev, pager, next"
    :total="total"
    :current-page="page"
    :page-size="limit"
    :page-sizes="pageSizes"
    @current-change="onPage"
    @size-change="onSize"
  />
</template>
<script setup lang="ts">
const props = defineProps<{ total: number; page: number; limit: number }>()
const emit = defineEmits(['update:page', 'update:limit', 'pagination'])

// 选项里始终包含当前每页条数，否则下拉框会显示一个选不回来的值
const pageSizes = computed(() => {
  const sizes = new Set([10, 12, 20, 40])
  const current = Number(props.limit)
  if (current > 0) sizes.add(current)
  return [...sizes].sort((a, b) => a - b)
})

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
