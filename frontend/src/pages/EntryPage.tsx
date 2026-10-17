import { useEffect, useState, type FormEvent } from 'react'
import {
  Alert,
  Button,
  InputAdornment,
  MenuItem,
  Paper,
  Stack,
  TextField,
  Typography,
} from '@mui/material'
import { DatePicker } from '@mui/x-date-pickers/DatePicker'
import dayjs, { type Dayjs } from 'dayjs'
import { ApiError } from '../api/client'
import { createEntry, fetchEmissionSources, type EmissionSource, type EntryInfo } from '../api/entries'
import { fetchSites, type Site } from '../api/sites'
import { formatDecimal, parseGermanDecimal } from '../utils/numbers'

type FieldErrors = {
  siteId?: string
  sourceId?: string
  quantity?: string
  date?: string
}

export function EntryPage() {
  const [sites, setSites] = useState<Site[]>([])
  const [sources, setSources] = useState<EmissionSource[]>([])
  const [siteId, setSiteId] = useState('')
  const [sourceId, setSourceId] = useState('')
  const [quantity, setQuantity] = useState('')
  const [date, setDate] = useState<Dayjs | null>(null)
  const [errors, setErrors] = useState<FieldErrors>({})
  const [generalError, setGeneralError] = useState<string | null>(null)
  const [saved, setSaved] = useState<EntryInfo | null>(null)
  const [saving, setSaving] = useState(false)

  useEffect(() => {
    let active = true
    Promise.all([fetchSites(), fetchEmissionSources()])
      .then(([siteData, sourceData]) => {
        if (active) {
          setSites(siteData)
          setSources(sourceData)
        }
      })
      .catch(() => {
        if (active) setGeneralError('Standorte und Emissionsquellen konnten nicht geladen werden.')
      })
    return () => {
      active = false
    }
  }, [])

  const selectedSource = sources.find((s) => String(s.id) === sourceId)

  function validate(): FieldErrors {
    const result: FieldErrors = {}
    if (siteId === '') {
      result.siteId = 'Bitte einen Standort wählen.'
    }
    if (sourceId === '') {
      result.sourceId = 'Bitte eine Emissionsquelle wählen.'
    }
    const parsed = parseGermanDecimal(quantity)
    if (parsed === null) {
      result.quantity = 'Bitte eine Zahl im Format 1.200,5 eingeben.'
    } else if (parsed.value <= 0) {
      result.quantity = 'Die Menge muss größer als null sein.'
    } else if (parsed.fractionDigits > 3) {
      result.quantity = 'Höchstens drei Nachkommastellen.'
    }
    if (date === null || !date.isValid()) {
      result.date = 'Bitte ein gültiges Datum eingeben.'
    } else if (date.isAfter(dayjs(), 'day')) {
      result.date = 'Das Datum darf nicht in der Zukunft liegen.'
    }
    return result
  }

  function resetForm() {
    setSiteId('')
    setSourceId('')
    setQuantity('')
    setDate(null)
  }

  async function handleSubmit(event: FormEvent) {
    event.preventDefault()
    const validation = validate()
    setErrors(validation)
    setGeneralError(null)
    setSaved(null)
    const parsed = parseGermanDecimal(quantity)
    if (Object.keys(validation).length > 0 || parsed === null || date === null) {
      return
    }

    setSaving(true)
    try {
      const entry = await createEntry({
        siteId: Number(siteId),
        emissionSourceId: Number(sourceId),
        quantity: parsed.value,
        entryDate: date.format('YYYY-MM-DD'),
      })
      setSaved(entry)
      resetForm()
    } catch (error) {
      if (error instanceof ApiError && error.status === 409) {
        setErrors({
          date: `Für ${selectedSource?.name ?? 'diese Emissionsquelle'} ist für ${date.year()} kein Emissionsfaktor hinterlegt.`,
        })
      } else {
        setGeneralError('Der Eintrag konnte nicht gespeichert werden.')
      }
    } finally {
      setSaving(false)
    }
  }

  return (
    <Paper sx={{ p: 3, maxWidth: 560 }}>
      <form onSubmit={handleSubmit} noValidate>
        <Stack spacing={2}>
          <Typography variant="h5" component="h1">
            Emissionsdaten erfassen
          </Typography>

          {saved && (
            <Alert severity="success" onClose={() => setSaved(null)}>
              Gespeichert: {formatDecimal(saved.quantity)} {saved.unit} {saved.emissionSourceName} am{' '}
              {dayjs(saved.entryDate).format('DD.MM.YYYY')} für {saved.siteName}. Ergebnis:{' '}
              <strong>{formatDecimal(saved.co2eKg)} kg CO₂e</strong> (Faktor {formatDecimal(saved.factorValue, 6)}).
            </Alert>
          )}
          {generalError && <Alert severity="error">{generalError}</Alert>}

          <TextField
            select
            label="Standort"
            value={siteId}
            onChange={(e) => setSiteId(e.target.value)}
            required
            error={Boolean(errors.siteId)}
            helperText={errors.siteId}
          >
            {sites.map((s) => (
              <MenuItem key={s.id} value={String(s.id)}>
                {s.name}
              </MenuItem>
            ))}
          </TextField>

          <TextField
            select
            label="Emissionsquelle"
            value={sourceId}
            onChange={(e) => setSourceId(e.target.value)}
            required
            error={Boolean(errors.sourceId)}
            helperText={errors.sourceId}
          >
            {sources.map((s) => (
              <MenuItem key={s.id} value={String(s.id)}>
                {s.name}
              </MenuItem>
            ))}
          </TextField>

          <TextField
            label="Menge"
            value={quantity}
            onChange={(e) => setQuantity(e.target.value)}
            required
            error={Boolean(errors.quantity)}
            helperText={errors.quantity ?? 'Deutsches Format, z. B. 1.200,5'}
            slotProps={{
              input: {
                endAdornment: selectedSource ? (
                  <InputAdornment position="end">{selectedSource.unit}</InputAdornment>
                ) : undefined,
              },
              htmlInput: { inputMode: 'decimal' },
            }}
          />

          <DatePicker
            label="Datum"
            value={date}
            onChange={(value) => setDate(value)}
            disableFuture
            slotProps={{
              textField: {
                required: true,
                error: Boolean(errors.date),
                helperText: errors.date,
              },
            }}
          />

          <Stack direction="row" sx={{ justifyContent: 'flex-end' }}>
            <Button type="submit" variant="contained" disabled={saving}>
              Speichern
            </Button>
          </Stack>
        </Stack>
      </form>
    </Paper>
  )
}