import request from '@/config/axios'

export interface CartItem {
  id?: number
  skuId?: number
  productId?: number
  name?: string
  coverUrl?: string
  color?: string
  size?: string
  price?: number
  amount?: number
  stock?: number
  qty?: number
}

export interface CartList {
  items: CartItem[]
  freight?: number
  payable?: number
}

export const CartApi = {
  getCartList: async (): Promise<CartList> => request.get({ url: '/mall/client/cart/list', auth: 'user' }),
  createCart: async (data: { skuId: number; qty: number }) => request.post({ url: '/mall/client/cart/create', data, auth: 'user' }),
  updateCart: async (data: { id: number; qty: number }) => request.put({ url: '/mall/client/cart/update', data, auth: 'user' }),
  deleteCart: async (id: number) => request.delete({ url: '/mall/client/cart/delete?id=' + id, auth: 'user' })
}
