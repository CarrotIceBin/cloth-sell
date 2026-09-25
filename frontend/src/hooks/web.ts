import { ElMessage, ElMessageBox } from 'element-plus'

export function useMessage() {
  return {
    success(text: string) {
      ElMessage.success(text)
    },
    error(text: string) {
      ElMessage.error(text)
    },
    async delConfirm() {
      await ElMessageBox.confirm('确认删除？', '提示')
    }
  }
}

const texts: Record<string, string> = {
  'common.delSuccess': '删除成功',
  'common.createSuccess': '创建成功',
  'common.updateSuccess': '修改成功',
  'action.create': '新增',
  'action.update': '编辑'
}

export function useI18n() {
  return { t: (key: string) => texts[key] || key }
}

const ADMIN_PERMS = [
  'mall:product:query', 'mall:product:create', 'mall:product:update', 'mall:product:delete',
  'mall:order:query', 'mall:order:update'
]

function readList(key: string) {
  try {
    const value = JSON.parse(localStorage.getItem(key) || '[]')
    return Array.isArray(value) ? value as string[] : []
  } catch {
    return []
  }
}

export function getPermissions() {
  const list = [
    ...readList('adminPermissions'),
    ...readList('userPermissions'),
    ...readList('permissions')
  ]
  if (localStorage.getItem('adminToken')) list.push(...ADMIN_PERMS)
  return list
}

export function setSession(kind: 'admin' | 'user', accessToken: string, name: string, permissions: string[], refreshToken: string) {
  localStorage.setItem(kind === 'admin' ? 'adminToken' : 'userToken', accessToken)
  localStorage.setItem(kind === 'admin' ? 'adminRefresh' : 'userRefresh', refreshToken)
  localStorage.setItem(kind === 'admin' ? 'adminName' : 'userName', name)
  localStorage.setItem(kind === 'admin' ? 'adminPermissions' : 'userPermissions', JSON.stringify(permissions || []))
}

export function clearSession(kind: 'admin' | 'user') {
  localStorage.removeItem(kind === 'admin' ? 'adminToken' : 'userToken')
  localStorage.removeItem(kind === 'admin' ? 'adminRefresh' : 'userRefresh')
  localStorage.removeItem(kind === 'admin' ? 'adminName' : 'userName')
  localStorage.removeItem(kind === 'admin' ? 'adminPermissions' : 'userPermissions')
}
