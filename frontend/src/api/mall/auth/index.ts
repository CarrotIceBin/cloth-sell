import request from '@/config/axios'

export const AuthApi = {
  register: async (data: { phone: string; password: string }) => request.post({ url: '/mall/auth/register', data, auth: 'none' }),
  login: async (data: { phone: string; password: string }) => request.post({ url: '/mall/auth/login', data, auth: 'none' }),
  adminLogin: async (data: { username: string; password: string }) => request.post({ url: '/mall/auth/admin-login', data, auth: 'none' }),
  logout: async (refreshToken: string) => request.post({ url: '/mall/auth/logout', data: { refreshToken }, auth: 'none' })
}
