<template>
  <ContentWrap>
    <el-form class="-mb-15px" :model="queryParams" ref="queryFormRef" :inline="true" label-width="68px">
      <el-form-item label="标题" prop="title">
        <el-input v-model="queryParams.title" placeholder="请输入标题" clearable @keyup.enter="handleQuery" class="w-title" />
      </el-form-item>
      <el-form-item label="标签" prop="tag">
        <el-select v-model="queryParams.tag" clearable placeholder="全部标签" class="w-tag">
          <el-option v-for="tag in tagOptions" :key="tag" :label="tag" :value="tag" />
        </el-select>
      </el-form-item>
      <el-form-item label="发布" prop="published">
        <el-select v-model="queryParams.published" clearable placeholder="全部" class="w-pub">
          <el-option label="已发布" :value="true" />
          <el-option label="未发布" :value="false" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
        <el-button type="primary" plain @click="openForm('create')" v-hasPermi="['mall:journal:create']">
          <Icon icon="ep:plus" class="mr-5px" /> 新增
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>
  <ContentWrap>
    <el-table row-key="id" v-loading="loading" :data="list" :stripe="true" :border="true" :show-overflow-tooltip="true" @sort-change="handleSort">
      <el-table-column label="编号" align="center" prop="id" width="80px" sortable="custom" />
      <el-table-column label="标签" align="center" prop="tag" width="100px" />
      <el-table-column label="标题" align="left" prop="title" min-width="200px" />
      <el-table-column label="摘要" align="left" prop="summary" min-width="240px" />
      <el-table-column label="发布" align="center" prop="published" width="90px">
        <template #default="scope">
          <el-tag :type="scope.row.published ? 'success' : 'info'" size="small">
            {{ scope.row.published ? '已发布' : '未发布' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="170px" sortable="custom">
        <template #default="scope">{{ fmtTime(scope.row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" align="center" fixed="right" width="130px">
        <template #default="scope">
          <el-button text size="small" @click="openForm('update', scope.row)" v-hasPermi="['mall:journal:update']"><Icon icon="ep:edit" /></el-button>
          <el-button text size="small" @click="handleDelete(scope.row.id)" v-hasPermi="['mall:journal:delete']"><Icon icon="ep:delete" /></el-button>
        </template>
      </el-table-column>
    </el-table>
    <Pagination :total="total" v-model:page="queryParams.pageNo" v-model:limit="queryParams.pageSize" @pagination="getList" />
  </ContentWrap>
  <JournalForm ref="formRef" @success="getList" />
</template>
<script setup lang="ts">
import { JournalApi, type Journal } from '@/api/mall/journal'
import JournalForm from './JournalForm.vue'
import ContentWrap from '@/components/ContentWrap/index.vue'
import Pagination from '@/components/Pagination/index.vue'
import Icon from '@/components/Icon/index.vue'
import { useI18n, useMessage } from '@/hooks/web'

defineOptions({ name: 'Journal' })
const message = useMessage()
const { t } = useI18n()
const tagOptions = ['搭配', '面料', '指南']

const loading = ref(true)
const list = ref<Journal[]>([])
const total = ref(0)
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  title: undefined as string | undefined,
  tag: undefined as string | undefined,
  published: undefined as boolean | undefined,
  sortBy: undefined as string | undefined,
  sortOrder: undefined as string | undefined
})
const queryFormRef = ref()
const fmtTime = (value?: string) => (value ? value.replace('T', ' ').slice(0, 16) : '')

const getList = async () => {
  loading.value = true
  try {
    const data = await JournalApi.getJournalPage(queryParams)
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
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
const openForm = (type: string, row?: Journal) => formRef.value.open(type, row)

const handleDelete = async (id: number) => {
  try {
    await message.delConfirm()
    await JournalApi.deleteJournal(id)
    message.success(t('common.delSuccess'))
    await getList()
  } catch {}
}

onMounted(getList)
</script>
<style scoped>
.w-title { width: 240px; }
.w-tag { width: 160px; }
.w-pub { width: 120px; }
</style>
