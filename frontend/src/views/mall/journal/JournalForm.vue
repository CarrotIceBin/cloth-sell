<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible" width="760px">
    <el-form ref="formRef" :model="formData" :rules="formRules" label-width="90px" v-loading="formLoading">
      <el-form-item label="标签" prop="tag">
        <el-select v-model="formData.tag" filterable allow-create default-first-option placeholder="选择或输入标签" class="w-full">
          <el-option v-for="tag in tagOptions" :key="tag" :label="tag" :value="tag" />
        </el-select>
      </el-form-item>
      <el-form-item label="标题" prop="title">
        <el-input v-model="formData.title" placeholder="请输入标题" maxlength="120" show-word-limit />
      </el-form-item>
      <el-form-item label="摘要">
        <el-input v-model="formData.summary" type="textarea" :rows="2" maxlength="255" show-word-limit placeholder="列表页显示的一句话摘要" />
      </el-form-item>
      <el-form-item label="正文">
        <el-input v-model="formData.content" type="textarea" :rows="12" placeholder="空行分段，每段独立成段" />
      </el-form-item>
      <el-form-item label="封面">
        <el-upload :show-file-list="false" :http-request="uploadCover">
          <el-button>上传图片</el-button>
        </el-upload>
        <el-image v-if="formData.coverUrl" :src="formData.coverUrl" style="width: 120px; height: 80px; margin-left: 12px" fit="cover" />
        <el-button v-if="formData.coverUrl" text type="danger" class="ml-8px" @click="formData.coverUrl = undefined">清除</el-button>
      </el-form-item>
      <el-form-item label="发布" prop="published">
        <el-switch v-model="formData.published" />
        <span class="tip">关闭后顾客端看不到这篇文章</span>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="submitForm" type="primary" :disabled="formLoading">确 定</el-button>
      <el-button @click="dialogVisible = false">取 消</el-button>
    </template>
  </Dialog>
</template>
<script setup lang="ts">
import { JournalApi, type Journal } from '@/api/mall/journal'
import Dialog from '@/components/Dialog/index.vue'
import { useI18n, useMessage } from '@/hooks/web'

defineOptions({ name: 'JournalForm' })
const { t } = useI18n()
const message = useMessage()
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formLoading = ref(false)
const formType = ref('')
const tagOptions = ['搭配', '面料', '指南']

const empty = (): Journal => ({ id: undefined, tag: '搭配', title: undefined, summary: undefined, content: undefined, coverUrl: undefined, published: true })
const formData = ref<Journal>(empty())
const formRules = reactive({
  tag: [{ required: true, message: '标签不能为空', trigger: 'change' }],
  title: [{ required: true, message: '标题不能为空', trigger: 'blur' }],
  published: [{ required: true, message: '发布状态不能为空', trigger: 'change' }]
})
const formRef = ref()

const open = async (type: string, row?: Journal) => {
  dialogVisible.value = true
  dialogTitle.value = t('action.' + type)
  formType.value = type
  resetForm()
  if (row) formData.value = { ...formData.value, ...row }
  if (type === 'update' && row?.id) {
    formLoading.value = true
    try {
      formData.value = await JournalApi.getJournal(row.id)
    } finally {
      formLoading.value = false
    }
  }
}
defineExpose({ open })

const emit = defineEmits(['success'])

const resetForm = () => {
  formData.value = empty()
  formRef.value?.resetFields()
}

const uploadCover = async (option: any) => {
  try {
    formData.value.coverUrl = await JournalApi.uploadCover(option.file)
  } catch (e: any) {
    message.error(e.message || '上传失败')
  }
}

const submitForm = async () => {
  await formRef.value.validate()
  formLoading.value = true
  try {
    if (formType.value === 'create') {
      await JournalApi.createJournal(formData.value)
      message.success(t('common.createSuccess'))
    } else {
      await JournalApi.updateJournal(formData.value)
      message.success(t('common.updateSuccess'))
    }
    dialogVisible.value = false
    emit('success')
  } catch {
  } finally {
    formLoading.value = false
  }
}
</script>
<style scoped>
.w-full { width: 100%; }
.ml-8px { margin-left: 8px; }
.tip { margin-left: 12px; color: #78716c; font-size: 12px; }
</style>
