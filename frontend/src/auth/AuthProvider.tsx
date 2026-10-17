import { useCallback, useEffect, useState, type ReactNode } from 'react'
import { apiFetch, getToken, setToken, setUnauthorizedHandler } from '../api/client'
import { AuthContext } from './AuthContext'
import type { LoginResponse, UserInfo } from './types'

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<UserInfo | null>(null)
  const [loading, setLoading] = useState(() => getToken() !== null)


  const logout = useCallback(() => {
    setToken(null)
    setUser(null)
  }, [])

  useEffect(() => {
    setUnauthorizedHandler(logout)

    if (!getToken()) {
      return
    }
    apiFetch<UserInfo>('/api/me')
      .then(setUser)
      .catch(() => logout())
      .finally(() => setLoading(false))
  }, [logout])

  const login = async (username: string, password: string): Promise<UserInfo> => {
    const response = await apiFetch<LoginResponse>('/api/auth/login', {
      method: 'POST',
      body: JSON.stringify({ username, password }),
    })
    setToken(response.token)
    setUser(response.user)
    return response.user
  }

  return (
    <AuthContext.Provider value={{ user, loading, login, logout }}>
      {children}
    </AuthContext.Provider>
  )
}