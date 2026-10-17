import { apiFetch } from './client'

export type Site = {
  id: number
  code: string
  name: string
}

export function fetchSites(): Promise<Site[]> {
  return apiFetch<Site[]>('/api/sites')
}

export function createSite(code: string, name: string): Promise<Site> {
  return apiFetch<Site>('/api/sites', {
    method: 'POST',
    body: JSON.stringify({ code, name }),
  })
}

export function renameSite(id: number, name: string): Promise<Site> {
  return apiFetch<Site>(`/api/sites/${id}`, {
    method: 'PUT',
    body: JSON.stringify({ name }),
  })
}

export function deleteSite(id: number): Promise<void> {
  return apiFetch<void>(`/api/sites/${id}`, { method: 'DELETE' })
}