const SERIES_COLORS = [
  '#2a78d6',
  '#eb6834',
  '#1baf7a',
  '#eda100',
  '#e87ba4',
  '#008300',
  '#6250d6',
  '#e34948',
]

export function colorForSite(siteId: number): string {
  return SERIES_COLORS[(siteId - 1) % SERIES_COLORS.length]
}