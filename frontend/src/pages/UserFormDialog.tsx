import { useState, type FormEvent } from 'react'
import {
  Alert,
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  MenuItem,
  Stack,
  TextField,
} from '@mui/material'
import { ApiError } from '../api/client'
import type { Site } from '../api/sites'
import { createUser, updateUser } from '../api/users'
import { allRoles, roleLabels } from '../auth/roles'
import type { Role, UserInfo } from '../auth/types'

type Props = {
  user: UserInfo | null
  sites: Site[]
  onClose: () => void
  onSaved: (user: UserInfo) => void
}

type FieldErrors = {
  name?: string
  username?: string
  password?: string
  role?: string
  siteId?: string
}

function requiresSite(role: Role | ''): boolean {
  return role === 'MANAGER' || role === 'USER'
}

export function UserFormDialog({ user, sites, onClose, onSaved }: Props) {
  const isNew = user === null
  const [name, setName] = useState(user?.name ?? '')
  const [username, setUsername] = useState(user?.username ?? '')
  const [password, setPassword] = useState('')
  const [role, setRole] = useState<Role | ''>(user?.role ?? '')
  const [siteId, setSiteId] = useState(user?.siteId != null ? String(user.siteId) : '')
  const [errors, setErrors] = useState<FieldErrors>({})
  const [generalError, setGeneralError] = useState<string | null>(null)
  const [saving, setSaving] = useState(false)

  function handleRoleChange(value: Role) {
    setRole(value)
    if (!requiresSite(value)) {
      setSiteId('')
    }
  }

  function validate(): FieldErrors {
    const result: FieldErrors = {}
    if (isNew) {
      if (name.trim() === '') {
        result.name = 'Bitte einen Namen eingeben.'
      }
      if (username === '') {
        result.username = 'Bitte einen Anmeldenamen eingeben.'
      } else if (!/^[a-z0-9._-]+$/.test(username)) {
        result.username = 'Nur Kleinbuchstaben, Ziffern sowie . _ - erlaubt.'
      }
      if (password.length < 8) {
        result.password = 'Das Passwort braucht mindestens 8 Zeichen.'
      }
    }
    if (role === '') {
      result.role = 'Bitte eine Rolle wählen.'
    }
    if (requiresSite(role) && siteId === '') {
      result.siteId = 'Für diese Rolle ist ein Standort Pflicht.'
    }
    return result
  }

  async function handleSubmit(event: FormEvent) {
    event.preventDefault()
    const validation = validate()
    setErrors(validation)
    setGeneralError(null)
    if (Object.keys(validation).length > 0 || role === '') {
      return
    }

    const selectedSiteId = siteId === '' ? null : Number(siteId)
    setSaving(true)
    try {
      const saved = user === null
        ? await createUser({ name, username, password, role, siteId: selectedSiteId })
        : await updateUser(user.username, role, selectedSiteId)
      onSaved(saved)
    } catch (error) {
      if (user === null && error instanceof ApiError && error.status === 409) {
        setErrors({ username: 'Dieser Anmeldename ist bereits vergeben.' })
      } else {
        setGeneralError('Das Konto konnte nicht gespeichert werden.')
      }
    } finally {
      setSaving(false)
    }
  }

  return (
    <Dialog open onClose={onClose} fullWidth maxWidth="xs">
      <form onSubmit={handleSubmit} noValidate>
        <DialogTitle>{isNew ? 'Benutzer anlegen' : 'Benutzer ändern'}</DialogTitle>
        <DialogContent>
          <Stack spacing={2} sx={{ mt: 1 }}>
            {generalError && <Alert severity="error">{generalError}</Alert>}
            <TextField
              label="Name"
              value={name}
              onChange={(e) => setName(e.target.value)}
              disabled={!isNew}
              required={isNew}
              error={Boolean(errors.name)}
              helperText={errors.name}
              slotProps={{ htmlInput: { maxLength: 100 } }}
              autoFocus={isNew}
            />
            <TextField
              label="Anmeldename"
              value={username}
              onChange={(e) => setUsername(e.target.value.toLowerCase())}
              disabled={!isNew}
              required={isNew}
              error={Boolean(errors.username)}
              helperText={errors.username ?? (isNew ? 'Kleinbuchstaben, Ziffern sowie . _ -' : undefined)}
              slotProps={{ htmlInput: { maxLength: 50 } }}
            />
            {isNew && (
              <TextField
                label="Passwort"
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
                error={Boolean(errors.password)}
                helperText={errors.password ?? 'Mindestens 8 Zeichen'}
                autoComplete="new-password"
                slotProps={{ htmlInput: { maxLength: 72 } }}
              />
            )}
            <TextField
              select
              label="Rolle"
              value={role}
              onChange={(e) => handleRoleChange(e.target.value as Role)}
              required
              error={Boolean(errors.role)}
              helperText={errors.role}
            >
              {allRoles.map((r) => (
                <MenuItem key={r} value={r}>
                  {roleLabels[r]}
                </MenuItem>
              ))}
            </TextField>
            <TextField
              select
              label="Standort"
              value={siteId}
              onChange={(e) => setSiteId(e.target.value)}
              disabled={!requiresSite(role)}
              required={requiresSite(role)}
              error={Boolean(errors.siteId)}
              helperText={errors.siteId ?? (requiresSite(role) ? undefined : 'Nur für Führungskraft und Benutzer')}
            >
              {sites.map((s) => (
                <MenuItem key={s.id} value={String(s.id)}>
                  {s.name}
                </MenuItem>
              ))}
            </TextField>
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