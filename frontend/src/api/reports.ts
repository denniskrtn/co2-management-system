import { apiFetch } from './client'

export type SiteTotal = {
  siteId: number
  siteName: string
  totalKg: number
  roundedKg: number
}

export type TotalsReport = {
  from: string
  to: string
  sites: SiteTotal[]
}

export function fetchTotals(from: string, to: string): Promise<TotalsReport> {
  const params = new URLSearchParams({ from, to })
  return apiFetch<TotalsReport>(`/api/reports/totals?${params}`)
}