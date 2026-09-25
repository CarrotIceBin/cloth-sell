import request from '@/config/axios'

export interface OrderLine {
  productName?: string
  color?: string
  size?: string
  price?: number
  qty?: number
}

export interface Order {
  id?: number
  status?: string
  statusText?: string
  receiverName?: string
  receiverPhone?: string
  province?: string
  city?: string
  district?: string
  region?: string
  address?: string
  freight?: number
  totalAmount?: number
  createTime?: string
  lines?: OrderLine[]
}

export const OrderApi = {
  getOrderPage: async (params: any) => request.get({ url: '/mall/order/page', params, auth: 'admin' }),
  updateOrderStatus: async (data: { id: number; status: string }) =>
    request.put({ url: '/mall/order/update-status', data, auth: 'admin' })
}
