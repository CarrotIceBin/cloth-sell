import axios from 'axios'
import { setSession, clearSession } from '@/hooks/web'

const service = axios.create({ baseURL: '', timeout: 15000 })
const raw = axios.create({ baseURL: '', timeout: 15000 })
const refreshing: Record<string, Promise<void> | undefined> = {}

function refreshKey(kind: string) {
  return kind === 'admin' ? 'adminRefresh' : 'userRefresh'
}

function refreshSession(kind: string) {
  if (!refreshing[kind]) {
    refreshing[kind] = raw.post('/mall/auth/refresh', { refreshToken: localStorage.getItem(refreshKey(kind)) })
      .then((res) => {
        const body = res.data
        if (!body || body.code !== 0) throw new Error(body?.msg || '登录已过期')
        const data = body.data
        setSession(kind === 'admin' ? 'admin' : 'user', data.accessToken, data.name, data.permissions, data.refreshToken)
      })
      .catch((error) => {
        clearSession(kind === 'admin' ? 'admin' : 'user')
        throw error
      })
      .finally(() => { refreshing[kind] = undefined })
  }
  return refreshing[kind]
}

service.interceptors.request.use((config) => {
  const kind = config.auth || config._kind
  config._kind = kind
  // axios 的 auth 专用于 Basic 认证，会覆盖 Authorization
  delete config.auth
  const token = localStorage.getItem(kind === 'admin' ? 'adminToken' : 'userToken')
  if (token && kind !== 'none') {
    config.headers.Authorization = 'Bearer ' + token
  }
  return config
})

service.interceptors.response.use((response) => {
  if (response.config.responseType === 'blob') {
    return response.data
  }
  const body = response.data
  if (body && typeof body.code === 'number' && body.code !== 0) {
    return Promise.reject(new Error(body.msg || '请求失败'))
  }
  return body ? body.data : body
}, (error) => {
  const config = error.config
  const status = error.response?.status
  const url = String(config?.url || '')
  if (config && !config._retried && (status === 401 || status === 403) && config._kind && config._kind !== 'none' && !url.includes('/mall/auth/refresh')) {
    config._retried = true
    return refreshSession(config._kind).then(() => service(config))
  }
  const msg = error.response?.data?.msg || error.message || '请求失败'
  return Promise.reject(new Error(msg))
})

const request = {
  get: (option) => service({ ...option, method: 'GET' }),
  post: (option) => service({ ...option, method: 'POST' }),
  put: (option) => service({ ...option, method: 'PUT' }),
  delete: (option) => service({ ...option, method: 'DELETE' })
}

export default request
