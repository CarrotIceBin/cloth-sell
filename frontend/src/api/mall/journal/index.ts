import request from '@/config/axios'

export interface Journal {
  id?: number
  tag?: string
  title?: string
  summary?: string
  content?: string
  coverUrl?: string
  published?: boolean
  createTime?: string
}

export const ClientJournalApi = {
  getJournalPage: async (params: any) => request.get({ url: '/mall/client/journal/page', params, auth: 'none' }),
  getJournal: async (id: number) => request.get({ url: '/mall/client/journal/get?id=' + id, auth: 'none' })
}

export const JournalApi = {
  getJournalPage: async (params: any) => request.get({ url: '/mall/journal/page', params, auth: 'admin' }),
  getJournal: async (id: number) => request.get({ url: '/mall/journal/get?id=' + id, auth: 'admin' }),
  createJournal: async (data: Journal) => request.post({ url: '/mall/journal/create', data, auth: 'admin' }),
  updateJournal: async (data: Journal) => request.put({ url: '/mall/journal/update', data, auth: 'admin' }),
  deleteJournal: async (id: number) => request.delete({ url: '/mall/journal/delete?id=' + id, auth: 'admin' }),
  uploadCover: async (file: File) => {
    const data = new FormData()
    data.append('file', file)
    return request.post({ url: '/mall/file/create', data, auth: 'admin' })
  }
}
