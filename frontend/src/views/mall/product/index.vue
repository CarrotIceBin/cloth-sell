<template>
  <ContentWrap>
    <el-form class="-mb-15px" :model="queryParams" ref="queryFormRef" :inline="true" label-width="68px">
      <el-form-item label="名称" prop="name">
        <el-input v-model="queryParams.name" placeholder="请输入名称" clearable @keyup.enter="handleQuery" class="!w-240px" />
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
        <el-button type="primary" plain @click="openForm('create')" v-hasPermi="['mall:product:create']">
          <Icon icon="ep:plus" class="mr-5px" /> 新增
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>
  <ContentWrap>
    <div class="table-toolbar mb-10px flex items-center justify-end">
      <el-button size="small" @click="openSettings">列设置</el-button>
    </div>
    <ColumnSettings v-model:modelValue="popoverVisible" :allColumns="allColumns" :columns="visibleColsArray" @update:columns="(v) => (visibleColsArray = v)" @save="() => saveColumns()" />
    <el-table row-key="id" v-loading="loading" :data="list" :stripe="true" :border="true" :show-overflow-tooltip="true" @sort-change="handleSort">
      <el-table-column v-if="isColVisible('name')" label="名称" align="center" prop="name" sortable="custom" />
      <el-table-column v-if="isColVisible('category')" label="分类" align="center" prop="category" />
      <el-table-column v-if="isColVisible('onShelf')" label="上架" align="center" prop="onShelf">
        <template #default="scope">{{ scope.row.onShelf ? '是' : '否' }}</template>
      </el-table-column>
      <el-table-column v-if="isColVisible('minPrice')" label="最低价" align="center" prop="minPrice" />
      <el-table-column v-if="isColVisible('stock')" label="库存" align="center" prop="stock" />
      <el-table-column label="操作" align="center" fixed="right" min-width="120px">
        <template #default="scope">
          <el-button text size="small" @click="openForm('update', scope.row)" v-hasPermi="['mall:product:update']"><Icon icon="ep:edit" /></el-button>
          <el-button text size="small" @click="handleDelete(scope.row.id)" v-hasPermi="['mall:product:delete']"><Icon icon="ep:delete" /></el-button>
        </template>
      </el-table-column>
    </el-table>
    <Pagination :total="total" v-model:page="queryParams.pageNo" v-model:limit="queryParams.pageSize" @pagination="getList" />
  </ContentWrap>
  <ProductForm ref="formRef" @success="getList" />
</template>
<script setup lang="ts">
import { ProductApi, Product } from '@/api/mall/product'
import ProductForm from './ProductForm.vue'
import ContentWrap from '@/components/ContentWrap/index.vue'
import Pagination from '@/components/Pagination/index.vue'
import ColumnSettings from '@/components/ColumnSettings/ColumnSettings.vue'
import Icon from '@/components/Icon/index.vue'
import { useI18n, useMessage } from '@/hooks/web'

defineOptions({ name: 'Product' })
const message = useMessage()
const { t } = useI18n()
const COLS_STORAGE_KEY = 'mall_product_columns_v1'
const allColumns = [
  { prop: 'name', labelText: '名称' },
  { prop: 'category', labelText: '分类' },
  { prop: 'onShelf', labelText: '上架' },
  { prop: 'minPrice', labelText: '最低价' },
  { prop: 'stock', labelText: '库存' }
]
const visibleColsArray = ref<string[]>([])
const popoverVisible = ref(false)
const loadColumns = () => {
  const allProps = allColumns.map((c) => c.prop)
  const raw = localStorage.getItem(COLS_STORAGE_KEY)
  if (!raw) { visibleColsArray.value = allProps; return }
  const parsed = JSON.parse(raw).filter((p: string) => allProps.includes(p))
  visibleColsArray.value = parsed.length ? parsed : allProps
}
const saveColumns = () => {
  localStorage.setItem(COLS_STORAGE_KEY, JSON.stringify(visibleColsArray.value))
  popoverVisible.value = false
  message.success(t('common.updateSuccess'))
}
const isColVisible = (prop: string) => visibleColsArray.value.includes(prop)
const openSettings = () => { popoverVisible.value = true }
const loading = ref(true)
const list = ref<Product[]>([])
const total = ref(0)
const queryParams = reactive({ pageNo: 1, pageSize: 10, name: undefined as string | undefined, sortBy: undefined as string | undefined, sortOrder: undefined as string | undefined })
const queryFormRef = ref()
const getList = async () => {
  loading.value = true
  try {
    const data = await ProductApi.getProductPage(queryParams)
    list.value = data.list
    total.value = data.total
  } finally { loading.value = false }
}
const handleQuery = () => { queryParams.pageNo = 1; getList() }
const resetQuery = () => {
  queryFormRef.value.resetFields()
  queryParams.sortBy = undefined
  queryParams.sortOrder = undefined
  handleQuery()
}
const handleSort = (column: any) => {
  queryParams.pageNo = 1
  if (column.order) {
    queryParams.sortBy = column.prop
    queryParams.sortOrder = column.order === 'ascending' ? 'asc' : 'desc'
  } else {
    queryParams.sortBy = undefined
    queryParams.sortOrder = undefined
  }
  getList()
}
const formRef = ref()
const openForm = (type: string, row?: Product) => formRef.value.open(type, row)
const handleDelete = async (id: number) => {
  try {
    await message.delConfirm()
    await ProductApi.deleteProduct(id)
    message.success(t('common.delSuccess'))
    await getList()
  } catch {}
}
onMounted(() => { loadColumns(); getList() })
</script>
