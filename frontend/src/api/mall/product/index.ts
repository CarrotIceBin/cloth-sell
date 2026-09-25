import request from '@/config/axios'

export interface Sku {
  id?: number
  color?: string
  size?: string
  price?: number
  stock?: number
}

export interface Product {
  id?: number
  name?: string
  category?: string
  coverUrl?: string
  onShelf?: boolean
  minPrice?: number
  stock?: number
  createTime?: string
  skus?: Sku[]
}

export const ProductApi = {
  getProductPage: async (params: any) => request.get({ url: '/mall/product/page', params, auth: 'admin' }),
  getProduct: async (id: number) => request.get({ url: '/mall/product/get?id=' + id, auth: 'admin' }),
  createProduct: async (data: Product) => request.post({ url: '/mall/product/create', data, auth: 'admin' }),
  updateProduct: async (data: Product) => request.put({ url: '/mall/product/update', data, auth: 'admin' }),
  deleteProduct: async (id: number) => request.delete({ url: '/mall/product/delete?id=' + id, auth: 'admin' }),
  uploadCover: async (file: File) => {
    const data = new FormData()
    data.append('file', file)
    return request.post({ url: '/mall/file/create', data, auth: 'admin' })
  }
}
