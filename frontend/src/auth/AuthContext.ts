import { createContext } from 'react'
import type { UserInfo } from './types'

export interface AuthContextValue {
  user: UserInfo | null
  loading: boolean
  login: (username: string, password: string) => Promise<UserInfo>
  logout: () => void
}

export const AuthContext = createContext<AuthContextValue | null>(null)