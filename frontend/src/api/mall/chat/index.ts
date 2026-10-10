import request from '@/config/axios'

export interface ChatIdentity { id: string; role: 'USER' | 'ADMIN' }
export interface ChatRoom {
  id: string
  type: 'DIRECT' | 'GROUP' | 'BROADCAST'
  title: string
  canSend: boolean
  lastContent: string
  lastMessageId: string
  members: (ChatIdentity & { name: string })[]
}
export interface RoomMessage {
  id: string
  roomId: string
  senderRole: string
  senderId: string
  senderName: string
  clientId: string
  content: string
  createTime: string
}
export type RoomEvent = RoomMessage | { roomId: string; created: true }

export function chatApi(admin: boolean) {
  const auth = admin ? 'admin' : 'user'
  return {
    me: (): Promise<ChatIdentity> => request.get({ url: '/mall/chat/me', auth }),
    rooms: (page: number): Promise<ChatRoom[]> => request.get({ url: '/mall/chat/rooms', auth, params: { page } }),
    room: (id: string): Promise<ChatRoom> => request.get({ url: `/mall/chat/rooms/${id}`, auth }),
    create: (type: string, title: string, members: ChatIdentity[]): Promise<ChatRoom[]> =>
      request.post({ url: '/mall/chat/rooms', auth, data: { type, title, members } }),
    history: (id: string, cursor: { before?: string; after?: string } = {}): Promise<RoomMessage[]> =>
      request.get({ url: `/mall/chat/rooms/${id}/messages`, auth, params: cursor }),
    send: (id: string, clientId: string, content: string): Promise<RoomMessage> =>
      request.post({ url: `/mall/chat/rooms/${id}/messages`, auth, data: { clientId, content } })
  }
}
