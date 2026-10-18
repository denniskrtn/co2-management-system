import { useEffect, useState, type FormEvent } from 'react'

import {
  Alert,
  Button,
  Paper,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Typography,
} from '@mui/material'
import { DatePicker } from '@mui/x-date-pickers/DatePicker'
import { LineChart } from '@mui/x-charts/LineChart'
import dayjs, { type Dayjs } from 'dayjs'
import { fetchMonthly, fetchTotals, fetchTotalsPdf, type MonthlyReport, type TotalsReport } from '../api/reports'
import { useAuth } from '../auth/useAuth'
import { colorForSite } from '../utils/chartColors'
import { formatKg } from '../utils/numbers'

const ISO_DATE = 'YYYY-MM-DD'
const axisNumber = new Intl.NumberFormat('de-DE', { maximumFractionDigits: 0 })

type FieldErrors = {
  from?: string
  to?: string
}

function validateRange(from: Dayjs | null, to: Dayjs | null): FieldErrors {
  const errors: FieldErrors = {}
  if (from === null || !from.isValid()) {
    errors.from = 'Bitte ein gültiges Startdatum eingeben.'
  }
  if (to === null || !to.isValid()) {
    errors.to = 'Bitte ein gültiges Enddatum eingeben.'
  }
  if (!errors.from && !errors.to && from !== null && to !== null && from.isAfter(to, 'day')) {
    errors.to = 'Das Enddatum liegt vor dem Startdatum.'
  }
  return errors
}

function formatMonth(month: string): string {
  return `${month.slice(5, 7)}/${month.slice(0, 4)}`
}

export function ReportPage() {
  const { user } = useAuth()
  const showChart = user?.role === 'SUSTAINABILITY_OFFICER' || user?.role === 'MANAGER'

  const [from, setFrom] = useState<Dayjs | null>(() => dayjs().startOf('year'))
  const [to, setTo] = useState<Dayjs | null>(() => dayjs().endOf('year'))
  const [errors, setErrors] = useState<FieldErrors>({})
  const [report, setReport] = useState<TotalsReport | null>(null)
  const [monthly, setMonthly] = useState<MonthlyReport | null>(null)
  const [loadError, setLoadError] = useState<string | null>(null)
  const [pdfError, setPdfError] = useState<string | null>(null)

  useEffect(() => {
    let active = true
    const fromIso = dayjs().startOf('year').format(ISO_DATE)
    const toIso = dayjs().endOf('year').format(ISO_DATE)
    Promise.all([
      fetchTotals(fromIso, toIso),
      showChart ? fetchMonthly(fromIso, toIso) : Promise.resolve(null),
    ])
      .then(([totals, monthlyData]) => {
        if (active) {
          setReport(totals)
          setMonthly(monthlyData)
        }
      })
      .catch(() => {
        if (active) setLoadError('Die Auswertung konnte nicht geladen werden.')
      })
    return () => {
      active = false
    }
  }, [showChart])

  async function handleSubmit(event: FormEvent) {
    event.preventDefault()
    const validation = validateRange(from, to)
    setErrors(validation)
    setLoadError(null)
    if (Object.keys(validation).length > 0 || from === null || to === null) {
      return
    }
    const fromIso = from.format(ISO_DATE)
    const toIso = to.format(ISO_DATE)
    try {
      const [totals, monthlyData] = await Promise.all([
        fetchTotals(fromIso, toIso),
        showChart ? fetchMonthly(fromIso, toIso) : Promise.resolve(null),
      ])
      setReport(totals)
      setMonthly(monthlyData)
    } catch {
      setLoadError('Die Auswertung konnte nicht geladen werden.')
    }
  }

  async function handlePdfExport() {
    if (report === null) {
      return
    }
    setPdfError(null)
    try {
      const blob = await fetchTotalsPdf(report.from, report.to)
      const url = URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = `emissionsbericht_${report.from}_${report.to}.pdf`
      link.click()
      setTimeout(() => URL.revokeObjectURL(url), 1000)
    } catch {
      setPdfError('Der Bericht konnte nicht erstellt werden.')
    }
  }

  return (
    <Stack spacing={3}>
      <Typography variant="h5" component="h1">
        Auswertung
      </Typography>

      <Paper sx={{ p: 2 }}>
        <form onSubmit={handleSubmit} noValidate>
          <Stack direction="row" spacing={2} sx={{ alignItems: 'flex-start' }}>
            <DatePicker
              label="Startdatum"
              value={from}
              onChange={(value) => setFrom(value)}
              slotProps={{
                textField: { required: true, error: Boolean(errors.from), helperText: errors.from },
              }}
            />
            <DatePicker
              label="Enddatum"
              value={to}
              onChange={(value) => setTo(value)}
              slotProps={{
                textField: { required: true, error: Boolean(errors.to), helperText: errors.to },
              }}
            />
            <Button type="submit" variant="contained" sx={{ height: 56 }}>
              Anzeigen
            </Button>
          </Stack>
        </form>
      </Paper>

      {loadError && <Alert severity="error">{loadError}</Alert>}

      {report && (
        <Stack spacing={1}>
          <Typography variant="h6" component="h2">
            Gesamtemissionen
          </Typography>
          <Typography variant="body2" sx={{ color: 'text.secondary' }}>
            Zeitraum {dayjs(report.from).format('DD.MM.YYYY')} bis {dayjs(report.to).format('DD.MM.YYYY')}
          </Typography>
          <TableContainer component={Paper}>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>Standort</TableCell>
                  <TableCell align="right">Gesamtemissionen (kg CO₂e)</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {report.sites.map((site) => (
                  <TableRow key={site.siteId}>
                    <TableCell>{site.siteName}</TableCell>
                    <TableCell align="right">{formatKg(site.roundedKg)}</TableCell>
                  </TableRow>
                ))}
                {report.sites.length === 0 && (
                  <TableRow>
                    <TableCell colSpan={2}>Keine Standorte vorhanden.</TableCell>
                  </TableRow>
                )}
              </TableBody>
            </Table>
          </TableContainer>
        </Stack>
      )}

      {showChart && monthly && (
        <Stack spacing={1}>
          <Typography variant="h6" component="h2">
            Emissionsentwicklung
          </Typography>
          <Typography variant="body2" sx={{ color: 'text.secondary' }}>
            Monatssummen in kg CO₂e
          </Typography>
          <Paper sx={{ p: 2 }}>
            <LineChart
              height={320}
              xAxis={[{ scaleType: 'point', data: monthly.months.map(formatMonth) }]}
              yAxis={[{ valueFormatter: (value: number) => axisNumber.format(value) }]}
              series={monthly.series.map((series) => ({
                id: `site-${series.siteId}`,
                label: series.siteName,
                data: series.roundedKg,
                color: colorForSite(series.siteId),
                valueFormatter: (value: number | null) =>
                  value === null ? '' : `${formatKg(value)} kg CO₂e`,
              }))}
              grid={{ horizontal: true }}
            />
          </Paper>
        </Stack>
      )}

      {report && (
        <Stack spacing={1} sx={{ alignItems: 'flex-start' }}>
          {pdfError && <Alert severity="error">{pdfError}</Alert>}
          <Button variant="outlined" onClick={handlePdfExport}>
            PDF exportieren
          </Button>
        </Stack>
      )}
    </Stack>
  )
}