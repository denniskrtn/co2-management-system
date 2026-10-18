import { apiFetch, apiFetchBlob } from './client'

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

export type SiteSeries = {
  siteId: number
  siteName: string
  totalsKg: number[]
  roundedKg: number[]
}

export type MonthlyReport = {
  from: string
  to: string
  months: string[]
  series: SiteSeries[]
}

export function fetchMonthly(from: string, to: string): Promise<MonthlyReport> {
  const params = new URLSearchParams({ from, to })
  return apiFetch<MonthlyReport>(`/api/reports/monthly?${params}`)
}

export function fetchTotals(from: string, to: string): Promise<TotalsReport> {
  const params = new URLSearchParams({ from, to })
  return apiFetch<TotalsReport>(`/api/reports/totals?${params}`)
}

export function fetchTotalsPdf(from: string, to: string): Promise<Blob> {
  const params = new URLSearchParams({ from, to })
  return apiFetchBlob(`/api/reports/totals/pdf?${params}`)
}