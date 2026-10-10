import request from '@/config/axios'

export interface SupportMessage {
  role: 'user' | 'assistant'
  content: string
}

export interface SupportChatReply {
  reply: string
  aiEnabled: boolean
}

export const SupportApi = {
  getStatus: async (): Promise<{ configured: boolean; model: string }> =>
    request.get({ url: '/mall/client/support/status', auth: 'none' }),
  chat: async (messages: SupportMessage[]): Promise<SupportChatReply> =>
    request.post({ url: '/mall/client/support/chat', data: { messages }, auth: 'none' })
}