import { useState, type FormEvent } from 'react'
import {
  Alert,
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Stack,
  TextField,
} from '@mui/material'
import { ApiError } from '../api/client'
import { createSite, renameSite, type Site } from '../api/sites'

type Props = {
  site: Site | null
  onClose: () => void
  onSaved: (site: Site) => void
}

type FieldErrors = {
  code?: string
  name?: string
}

export function SiteFormDialog({ site, onClose, onSaved }: Props) {
  const [code, setCode] = useState(site?.code ?? '')
  const [name, setName] = useState(site?.name ?? '')
  const [errors, setErrors] = useState<FieldErrors>({})
  const [generalError, setGeneralError] = useState<string | null>(null)
  const [saving, setSaving] = useState(false)

  function validate(): FieldErrors {
    const result: FieldErrors = {}
    if (site === null && code.trim() === '') {
      result.code = 'Bitte eine Kennung eingeben.'
    }
    if (name.trim() === '') {
      result.name = 'Bitte einen Namen eingeben.'
    }
    return result
  }

  async function handleSubmit(event: FormEvent) {
    event.preventDefault()
    const validation = validate()
    setErrors(validation)
    setGeneralError(null)
    if (Object.keys(validation).length > 0) {
      return
    }

    setSaving(true)
    try {
      const saved = site === null
        ? await createSite(code, name)
        : await renameSite(site.id, name)
      onSaved(saved)
    } catch (error) {
      if (site === null && error instanceof ApiError && error.status === 409) {
        setErrors({ code: 'Diese Kennung ist bereits vergeben.' })
      } else {
        setGeneralError('Der Standort konnte nicht gespeichert werden.')
      }
    } finally {
      setSaving(false)
    }
  }

  return (
    <Dialog open onClose={onClose} fullWidth maxWidth="xs">
      <form onSubmit={handleSubmit} noValidate>
        <DialogTitle>{site === null ? 'Standort anlegen' : 'Standort ändern'}</DialogTitle>
        <DialogContent>
          <Stack spacing={2} sx={{ mt: 1 }}>
            {generalError && <Alert severity="error">{generalError}</Alert>}
            <TextField
              label="Kennung"
              value={code}
              onChange={(e) => setCode(e.target.value.toUpperCase())}
              disabled={site !== null}
              required={site === null}
              error={Boolean(errors.code)}
              helperText={errors.code ?? (site === null ? 'Kann später nicht geändert werden' : undefined)}
              slotProps={{ htmlInput: { maxLength: 20 } }}
              autoFocus={site === null}
            />
            <TextField
              label="Name"
              value={name}
              onChange={(e) => setName(e.target.value)}
              required
              error={Boolean(errors.name)}
              helperText={errors.name}
              slotProps={{ htmlInput: { maxLength: 100 } }}
              autoFocus={site !== null}
            />
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={onClose}>Abbrechen</Button>
          <Button type="submit" variant="contained" disabled={saving}>
            Speichern
          </Button>
        </DialogActions>
      </form>
    </Dialog>
  )
}