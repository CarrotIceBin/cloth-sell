<template>
  <el-popover :visible="modelValue" placement="bottom" :width="220">
    <el-checkbox-group :model-value="columns" @change="(v: string[]) => emit('update:columns', v)">
      <div v-for="col in allColumns" :key="col.prop">
        <el-checkbox :value="col.prop">{{ col.labelText }}</el-checkbox>
      </div>
    </el-checkbox-group>
    <div class="mt-8px"><el-button size="small" type="primary" @click="emit('save')">保存</el-button></div>
    <template #reference><span></span></template>
  </el-popover>
</template>
<script setup lang="ts">
defineProps<{ modelValue: boolean; allColumns: { prop: string; labelText: string }[]; columns: string[] }>()
const emit = defineEmits(['update:modelValue', 'update:columns', 'save'])
</script>
