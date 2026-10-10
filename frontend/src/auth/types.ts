export type Role = 'ADMIN' | 'SUSTAINABILITY_OFFICER' | 'MANAGER' | 'USER'

export interface UserInfo {
  username: string
  name: string
  role: Role
  siteId: number | null
  siteName: string | null
}

export interface LoginResponse {
  token: string
  expiresInSeconds: number
  user: UserInfo
}