import request from '@/config/axios'
import type { Order } from '@/api/mall/order'

export const ClientOrderApi = {
  getOrderPage: async (params: any) => request.get({ url: '/mall/client/order/page', params, auth: 'user' }),
  createOrder: async (data: { receiverName: string; receiverPhone: string; province: string; city: string; district: string; address: string }) =>
    request.post({ url: '/mall/client/order/create', data, auth: 'user' }),
  payOrder: async (id: number) => request.put({ url: '/mall/client/order/pay?id=' + id, auth: 'user' })
}

export type { Order }
