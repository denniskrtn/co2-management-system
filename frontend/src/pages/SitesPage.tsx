import { useEffect, useState } from 'react'
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
import { ApiError } from '../api/client'
import { deleteSite, fetchSites, type Site } from '../api/sites'
import { ConfirmDialog } from '../components/ConfirmDialog'
import { SiteFormDialog } from './SiteFormDialog'

function byCode(a: Site, b: Site) {
  return a.code.localeCompare(b.code)
}

export function SitesPage() {
  const [sites, setSites] = useState<Site[]>([])
  const [error, setError] = useState<string | null>(null)
  const [formOpen, setFormOpen] = useState(false)
  const [editing, setEditing] = useState<Site | null>(null)
  const [deleting, setDeleting] = useState<Site | null>(null)

  useEffect(() => {
    let active = true
    fetchSites()
      .then((data) => {
        if (active) setSites(data)
      })
      .catch(() => {
        if (active) setError('Die Standorte konnten nicht geladen werden.')
      })
    return () => {
      active = false
    }
  }, [])

  function openCreate() {
    setEditing(null)
    setFormOpen(true)
  }

  function openEdit(site: Site) {
    setEditing(site)
    setFormOpen(true)
  }

  function handleSaved(saved: Site) {
    setSites((current) => [...current.filter((s) => s.id !== saved.id), saved].sort(byCode))
    setFormOpen(false)
  }

  async function handleDelete() {
    if (deleting === null) {
      return
    }
    const site = deleting
    setDeleting(null)
    setError(null)
    try {
      await deleteSite(site.id)
      setSites((current) => current.filter((s) => s.id !== site.id))
    } catch (err) {
      if (err instanceof ApiError && err.status === 409) {
        setError(`Der Standort „${site.name}“ hat zugeordnete Benutzer oder Einträge und kann nicht gelöscht werden.`)
      } else {
        setError('Der Standort konnte nicht gelöscht werden.')
      }
    }
  }

  return (
    <Stack spacing={2}>
      <Stack direction="row" sx={{ justifyContent: 'space-between', alignItems: 'center' }}>
        <Typography variant="h5" component="h1">
          Standorte
        </Typography>
        <Button variant="contained" onClick={openCreate}>
          Standort anlegen
        </Button>
      </Stack>

      {error && (
        <Alert severity="error" onClose={() => setError(null)}>
          {error}
        </Alert>
      )}

      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Kennung</TableCell>
              <TableCell>Name</TableCell>
              <TableCell align="right">Aktionen</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {sites.map((site) => (
              <TableRow key={site.id}>
                <TableCell>{site.code}</TableCell>
                <TableCell>{site.name}</TableCell>
                <TableCell align="right">
                  <Button size="small" onClick={() => openEdit(site)}>
                    Ändern
                  </Button>
                  <Button size="small" color="error" onClick={() => setDeleting(site)}>
                    Löschen
                  </Button>
                </TableCell>
              </TableRow>
            ))}
            {sites.length === 0 && (
              <TableRow>
                <TableCell colSpan={3}>Keine Standorte vorhanden.</TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </TableContainer>

      {formOpen && (
        <SiteFormDialog site={editing} onClose={() => setFormOpen(false)} onSaved={handleSaved} />
      )}

      <ConfirmDialog
        open={deleting !== null}
        title="Standort löschen?"
        message={deleting ? `Soll der Standort „${deleting.name}“ (${deleting.code}) wirklich gelöscht werden?` : ''}
        confirmLabel="Löschen"
        onConfirm={handleDelete}
        onCancel={() => setDeleting(null)}
      />
    </Stack>
  )
}