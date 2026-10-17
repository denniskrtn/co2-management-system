import { apiFetch } from './client'
import type { Role, UserInfo } from '../auth/types'

export type NewUser = {
  name: string
  username: string
  password: string
  role: Role
  siteId: number | null
}

export function fetchUsers(): Promise<UserInfo[]> {
  return apiFetch<UserInfo[]>('/api/users')
}

export function createUser(user: NewUser): Promise<UserInfo> {
  return apiFetch<UserInfo>('/api/users', {
    method: 'POST',
    body: JSON.stringify(user),
  })
}

export function updateUser(username: string, role: Role, siteId: number | null): Promise<UserInfo> {
  return apiFetch<UserInfo>(`/api/users/${encodeURIComponent(username)}`, {
    method: 'PUT',
    body: JSON.stringify({ role, siteId }),
  })
}

export function deleteUser(username: string): Promise<void> {
  return apiFetch<void>(`/api/users/${encodeURIComponent(username)}`, { method: 'DELETE' })
}