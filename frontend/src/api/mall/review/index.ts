import request from '@/config/axios'

export interface Review {
  id?: number
  productId?: number
  author?: string
  rating?: number
  content?: string
  published?: boolean
  createTime?: string
}

export interface ReviewSummary {
  total: number
  average: number
  stars: Record<string, number>
}

export const ClientReviewApi = {
  getReviewPage: async (params: any) => request.get({ url: '/mall/client/review/page', params, auth: 'none' }),
  getSummary: async (productId: number) => request.get({ url: '/mall/client/review/summary?productId=' + productId, auth: 'none' }),
  createReview: async (data: { productId: number; rating: number; content?: string }) =>
    request.post({ url: '/mall/client/review/create', data, auth: 'user' })
}

export const ReviewApi = {
  getReviewPage: async (params: any) => request.get({ url: '/mall/review/page', params, auth: 'admin' }),
  updatePublished: async (id: number, published: boolean) =>
    request.put({ url: '/mall/review/update-published?id=' + id + '&published=' + published, auth: 'admin' }),
  deleteReview: async (id: number) => request.delete({ url: '/mall/review/delete?id=' + id, auth: 'admin' })
}
