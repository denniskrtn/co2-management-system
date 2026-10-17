import { apiFetch } from './client'

export type EmissionSource = {
  id: number
  name: string
  unit: string
  scope: string
}

export type NewEntry = {
  siteId: number
  emissionSourceId: number
  quantity: number
  entryDate: string
}

export type EntryInfo = {
  id: number
  siteName: string
  emissionSourceName: string
  unit: string
  quantity: number
  entryDate: string
  factorValue: number
  co2eKg: number
}

export function fetchEmissionSources(): Promise<EmissionSource[]> {
  return apiFetch<EmissionSource[]>('/api/emission-sources')
}

export function createEntry(entry: NewEntry): Promise<EntryInfo> {
  return apiFetch<EntryInfo>('/api/entries', {
    method: 'POST',
    body: JSON.stringify(entry),
  })
}