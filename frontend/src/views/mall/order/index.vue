<template>
  <ContentWrap>
    <el-form :model="queryParams" ref="queryFormRef" :inline="true">
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" clearable class="!w-160px">
          <el-option label="待付款" value="PENDING" />
          <el-option label="已付款" value="PAID" />
          <el-option label="已发货" value="SHIPPED" />
          <el-option label="已完成" value="DONE" />
          <el-option label="已取消" value="CANCELLED" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery">搜索</el-button>
        <el-button @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>
  <ContentWrap>
    <el-table v-loading="loading" :data="list" border stripe @sort-change="handleSort">
      <el-table-column label="编号" prop="id" width="80" sortable="custom" />
      <el-table-column label="状态" prop="statusText" />
      <el-table-column label="金额" prop="totalAmount" sortable="custom" />
      <el-table-column label="收货人" prop="receiverName" />
      <el-table-column label="手机" prop="receiverPhone" min-width="130" />
      <el-table-column label="地址" min-width="180" show-overflow-tooltip>
        <template #default="scope">{{ scope.row.province ? scope.row.province + (scope.row.city || '') + (scope.row.district || '') : (scope.row.region || '') }} {{ scope.row.address }}</template>
      </el-table-column>
      <el-table-column label="创建时间" prop="createTime" sortable="custom" min-width="160" />
      <el-table-column label="操作" width="96">
        <template #default="scope">
          <div class="order-actions">
            <el-tooltip v-for="item in actions(scope.row.status)" :key="item.status" :content="item.label" placement="top">
              <el-button circle size="small" :type="item.type" :plain="item.plain" :icon="item.icon" @click="change(scope.row.id, item.status)" v-hasPermi="['mall:order:update']" />
            </el-tooltip>
          </div>
        </template>
      </el-table-column>
    </el-table>
    <Pagination :total="total" v-model:page="queryParams.pageNo" v-model:limit="queryParams.pageSize" @pagination="getList" />
  </ContentWrap>
</template>
<script setup lang="ts">
import { OrderApi, Order } from '@/api/mall/order'
import ContentWrap from '@/components/ContentWrap/index.vue'
import Pagination from '@/components/Pagination/index.vue'
import { useMessage } from '@/hooks/web'
import { CircleCheck, CircleClose, Van, Wallet } from '@element-plus/icons-vue'
defineOptions({ name: 'Order' })
const message = useMessage()
const loading = ref(true)
const list = ref<Order[]>([])
const total = ref(0)
const queryParams = reactive({ pageNo: 1, pageSize: 10, status: undefined as string | undefined, sortBy: undefined as string | undefined, sortOrder: undefined as string | undefined })
const queryFormRef = ref()
const getList = async () => {
  loading.value = true
  try {
    const data = await OrderApi.getOrderPage(queryParams)
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
const actions = (status?: string) => {
  const cancel = { status: 'CANCELLED', label: '取消并回库存', type: 'danger' as const, plain: true, icon: CircleClose }
  if (status === 'PENDING') return [{ status: 'PAID', label: '标为已付款', type: 'primary' as const, icon: Wallet }, cancel]
  if (status === 'PAID') return [{ status: 'SHIPPED', label: '标为已发货', type: 'primary' as const, icon: Van }, cancel]
  if (status === 'SHIPPED') return [{ status: 'DONE', label: '标为已完成', type: 'primary' as const, icon: CircleCheck }]
  return []
}
const change = async (id: number, status: string) => {
  try {
    await OrderApi.updateOrderStatus({ id, status })
    message.success('已更新')
    await getList()
  } catch (e: any) { message.error(e.message) }
}
onMounted(getList)
</script>
<style scoped>
.order-actions { display: flex; gap: 6px; }
</style>
