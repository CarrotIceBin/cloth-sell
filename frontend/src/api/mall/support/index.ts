import request from '@/config/axios'

export interface ChatMessage {
  id: string
  userId: string
  buyerName?: string
  senderRole: string
  senderName: string
  clientId: string
  content: string
  createTime: string
}

export const LiveSupportApi = {
  ticket: (admin: boolean): Promise<{ ticket: string }> =>
    request.post({ url: '/mall/support/ws-ticket', auth: admin ? 'admin' : 'user' }),
  history: (admin: boolean, userId?: string, cursor: { before?: string; after?: string } = {}): Promise<ChatMessage[]> =>
    request.get({ url: admin ? '/mall/admin/support/messages' : '/mall/client/support/messages',
      auth: admin ? 'admin' : 'user', params: { userId, ...cursor } }),
  send: (admin: boolean, userId: string | undefined, clientId: string, content: string): Promise<ChatMessage> =>
    request.post({ url: admin ? '/mall/admin/support/messages' : '/mall/client/support/messages',
      auth: admin ? 'admin' : 'user', data: { userId, clientId, content } }),
  conversations: (page: number): Promise<ChatMessage[]> =>
    request.get({ url: '/mall/admin/support/conversations', auth: 'admin', params: { page } })
}
