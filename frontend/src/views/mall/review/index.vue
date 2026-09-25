<template>
  <ContentWrap>
    <el-form class="-mb-15px" :model="queryParams" ref="queryFormRef" :inline="true" label-width="68px">
      <el-form-item label="商品" prop="productId">
        <el-input v-model="queryParams.productId" placeholder="商品编号" clearable @keyup.enter="handleQuery" class="w-id" />
      </el-form-item>
      <el-form-item label="评分" prop="rating">
        <el-select v-model="queryParams.rating" clearable placeholder="全部" class="w-rating">
          <el-option v-for="star in [5, 4, 3, 2, 1]" :key="star" :label="star + ' 星'" :value="star" />
        </el-select>
      </el-form-item>
      <el-form-item label="显示" prop="published">
        <el-select v-model="queryParams.published" clearable placeholder="全部" class="w-pub">
          <el-option label="显示中" :value="true" />
          <el-option label="已隐藏" :value="false" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>
  <ContentWrap>
    <el-table row-key="id" v-loading="loading" :data="list" :stripe="true" :border="true" :show-overflow-tooltip="true">
      <el-table-column label="编号" align="center" prop="id" width="80px" />
      <el-table-column label="商品" align="center" prop="productId" width="90px" />
      <el-table-column label="评价人" align="center" prop="author" width="130px" />
      <el-table-column label="评分" align="center" prop="rating" width="120px">
        <template #default="scope"><span class="stars">{{ starsText(scope.row.rating) }}</span></template>
      </el-table-column>
      <el-table-column label="内容" align="left" prop="content" min-width="240px" />
      <el-table-column label="显示" align="center" prop="published" width="90px">
        <template #default="scope">
          <el-tag :type="scope.row.published ? 'success' : 'info'" size="small">
            {{ scope.row.published ? '显示中' : '已隐藏' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="评价时间" align="center" prop="createTime" width="170px">
        <template #default="scope">{{ fmtTime(scope.row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" align="center" fixed="right" width="150px">
        <template #default="scope">
          <el-button text size="small" @click="toggle(scope.row)" v-hasPermi="['mall:review:update']">
            {{ scope.row.published ? '隐藏' : '显示' }}
          </el-button>
          <el-button text size="small" @click="handleDelete(scope.row.id)" v-hasPermi="['mall:review:delete']">
            <Icon icon="ep:delete" />
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <Pagination :total="total" v-model:page="queryParams.pageNo" v-model:limit="queryParams.pageSize" @pagination="getList" />
  </ContentWrap>
</template>
<script setup lang="ts">
import { ReviewApi, type Review } from '@/api/mall/review'
import ContentWrap from '@/components/ContentWrap/index.vue'
import Pagination from '@/components/Pagination/index.vue'
import Icon from '@/components/Icon/index.vue'
import { useI18n, useMessage } from '@/hooks/web'

defineOptions({ name: 'Review' })
const message = useMessage()
const { t } = useI18n()

const loading = ref(true)
const list = ref<Review[]>([])
const total = ref(0)
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  productId: undefined as string | undefined,
  rating: undefined as number | undefined,
  published: undefined as boolean | undefined
})
const queryFormRef = ref()

const starsText = (value?: number) => '★'.repeat(value || 0) + '☆'.repeat(5 - (value || 0))
const fmtTime = (value?: string) => (value ? value.replace('T', ' ').slice(0, 16) : '')

const getList = async () => {
  loading.value = true
  try {
    const params = { ...queryParams, productId: queryParams.productId ? Number(queryParams.productId) : undefined }
    const data = await ReviewApi.getReviewPage(params)
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

const handleQuery = () => { queryParams.pageNo = 1; getList() }

const resetQuery = () => {
  queryFormRef.value.resetFields()
  handleQuery()
}

const toggle = async (row: Review) => {
  try {
    await ReviewApi.updatePublished(row.id!, !row.published)
    message.success(row.published ? '已隐藏' : '已显示')
    await getList()
  } catch (e: any) {
    message.error(e.message)
  }
}

const handleDelete = async (id: number) => {
  try {
    await message.delConfirm()
    await ReviewApi.deleteReview(id)
    message.success(t('common.delSuccess'))
    await getList()
  } catch {}
}

onMounted(getList)
</script>
<style scoped>
.w-id { width: 140px; }
.w-rating { width: 120px; }
.w-pub { width: 120px; }
.stars { color: #b8975a; letter-spacing: 0.08em; }
</style>
