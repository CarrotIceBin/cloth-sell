import request from '@/config/axios'
import type { Product } from '@/api/mall/product'

export const ClientProductApi = {
  getProductPage: async (params: any) => request.get({ url: '/mall/client/product/page', params, auth: 'none' }),
  getProduct: async (id: number) => request.get({ url: '/mall/client/product/get?id=' + id, auth: 'none' })
}

export type { Product }
