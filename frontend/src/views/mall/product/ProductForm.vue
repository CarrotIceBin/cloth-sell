<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible">
    <el-form ref="formRef" :model="formData" :rules="formRules" label-width="90px" v-loading="formLoading">
      <el-form-item label="名称" prop="name">
        <el-input v-model="formData.name" placeholder="请输入名称" />
      </el-form-item>
      <el-form-item label="封面">
        <el-upload :show-file-list="false" :http-request="uploadCover">
          <el-button>上传图片</el-button>
        </el-upload>
        <el-image v-if="formData.coverUrl" :src="formData.coverUrl" style="width: 80px; height: 80px; margin-left: 12px" fit="cover" />
      </el-form-item>
      <el-form-item label="上架" prop="onShelf">
        <el-switch v-model="formData.onShelf" />
      </el-form-item>
      <el-form-item label="规格">
        <div v-for="(sku, index) in formData.skus" :key="index" class="sku-row">
          <el-input v-model="sku.color" placeholder="颜色" />
          <el-input v-model="sku.size" placeholder="尺码" />
          <el-input v-model="sku.price" placeholder="价格" />
          <el-input v-model="sku.stock" placeholder="库存" />
          <el-button text @click="formData.skus.splice(index, 1)">删除</el-button>
        </div>
        <el-button @click="formData.skus.push({ color: '', size: '', price: undefined, stock: undefined })">加规格</el-button>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="submitForm" type="primary" :disabled="formLoading">确 定</el-button>
      <el-button @click="dialogVisible = false">取 消</el-button>
    </template>
  </Dialog>
</template>
<script setup lang="ts">
import { ProductApi, Product, Sku } from '@/api/mall/product'
import Dialog from '@/components/Dialog/index.vue'
import { useI18n, useMessage } from '@/hooks/web'

defineOptions({ name: 'ProductForm' })
const { t } = useI18n()
const message = useMessage()
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formLoading = ref(false)
const formType = ref('')
const empty = (): Product => ({ id: undefined, name: undefined, coverUrl: undefined, onShelf: true, skus: [{ color: '', size: '' }] })
const formData = ref<Product>(empty())
const formRules = reactive({
  name: [{ required: true, message: '名称不能为空', trigger: 'blur' }],
  onShelf: [{ required: true, message: '上架状态不能为空', trigger: 'change' }]
})
const formRef = ref()

const open = async (type: string, row?: Product) => {
  dialogVisible.value = true
  dialogTitle.value = t('action.' + type)
  formType.value = type
  resetForm()
  if (row) formData.value = { ...formData.value, ...row, skus: row.skus ? row.skus.map((s: Sku) => ({ ...s })) : [] }
  if (type === 'update' && row?.id) {
    formLoading.value = true
    try {
      formData.value = await ProductApi.getProduct(row.id)
    } finally {
      formLoading.value = false
    }
  }
}
defineExpose({ open })
const emit = defineEmits(['success'])

const uploadCover = async (option: any) => {
  formData.value.coverUrl = URL.createObjectURL(option.file)
  try {
    formData.value.coverUrl = await ProductApi.uploadCover(option.file)
  } catch (e: any) {
    formData.value.coverUrl = undefined
    message.error(e.message || '上传失败')
  }
}

const submitForm = async () => {
  await formRef.value.validate()
  formLoading.value = true
  try {
    const data = formData.value as Product
    if (formType.value === 'create') {
      await ProductApi.createProduct(data)
      message.success(t('common.createSuccess'))
    } else {
      await ProductApi.updateProduct(data)
      message.success(t('common.updateSuccess'))
    }
    dialogVisible.value = false
    emit('success')
  } catch (e: any) {
    message.error(e.message)
  } finally {
    formLoading.value = false
  }
}
const resetForm = () => {
  formData.value = empty()
  formRef.value?.resetFields()
}
</script>
<style scoped>
.sku-row { display: flex; gap: 8px; margin-bottom: 8px; }
</style>
