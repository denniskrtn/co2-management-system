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
import dayjs, { type Dayjs } from 'dayjs'
import { fetchTotals, type TotalsReport } from '../api/reports'
import { formatKg } from '../utils/numbers'

const ISO_DATE = 'YYYY-MM-DD'

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

export function ReportPage() {
  const [from, setFrom] = useState<Dayjs | null>(() => dayjs().startOf('year'))
  const [to, setTo] = useState<Dayjs | null>(() => dayjs().endOf('year'))
  const [errors, setErrors] = useState<FieldErrors>({})
  const [report, setReport] = useState<TotalsReport | null>(null)
  const [loadError, setLoadError] = useState<string | null>(null)

  useEffect(() => {
    let active = true
    fetchTotals(dayjs().startOf('year').format(ISO_DATE), dayjs().endOf('year').format(ISO_DATE))
      .then((data) => {
        if (active) setReport(data)
      })
      .catch(() => {
        if (active) setLoadError('Die Auswertung konnte nicht geladen werden.')
      })
    return () => {
      active = false
    }
  }, [])

  async function handleSubmit(event: FormEvent) {
    event.preventDefault()
    const validation = validateRange(from, to)
    setErrors(validation)
    setLoadError(null)
    if (Object.keys(validation).length > 0 || from === null || to === null) {
      return
    }
    try {
      setReport(await fetchTotals(from.format(ISO_DATE), to.format(ISO_DATE)))
    } catch {
      setLoadError('Die Auswertung konnte nicht geladen werden.')
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
    </Stack>
  )
}