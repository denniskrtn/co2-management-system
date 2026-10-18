const GERMAN_DECIMAL = /^(\d{1,3}(\.\d{3})+|\d+)(,\d+)?$/

export type ParsedDecimal = {
  value: number
  fractionDigits: number
}

export function parseGermanDecimal(input: string): ParsedDecimal | null {
  const text = input.trim()
  if (!GERMAN_DECIMAL.test(text)) {
    return null
  }
  const [integerPart, fractionPart = ''] = text.replace(/\./g, '').split(',')
  return {
    value: Number(`${integerPart}.${fractionPart || '0'}`),
    fractionDigits: fractionPart.length,
  }
}

export function formatDecimal(value: number, maximumFractionDigits = 3): string {
  return new Intl.NumberFormat('de-DE', { maximumFractionDigits }).format(value)
}

export function formatKg(value: number): string {
  return new Intl.NumberFormat('de-DE', { minimumFractionDigits: 2, maximumFractionDigits: 2 }).format(value)
}