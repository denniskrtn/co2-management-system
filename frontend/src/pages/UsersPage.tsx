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
import { fetchSites, type Site } from '../api/sites'
import { deleteUser, fetchUsers } from '../api/users'
import { roleLabels } from '../auth/roles'
import type { UserInfo } from '../auth/types'
import { useAuth } from '../auth/useAuth'
import { ConfirmDialog } from '../components/ConfirmDialog'
import { UserFormDialog } from './UserFormDialog'

function byName(a: UserInfo, b: UserInfo) {
  return a.name.localeCompare(b.name, 'de')
}

export function UsersPage() {
  const { user: currentUser } = useAuth()
  const [users, setUsers] = useState<UserInfo[]>([])
  const [sites, setSites] = useState<Site[]>([])
  const [error, setError] = useState<string | null>(null)
  const [formOpen, setFormOpen] = useState(false)
  const [editing, setEditing] = useState<UserInfo | null>(null)
  const [deleting, setDeleting] = useState<UserInfo | null>(null)

  useEffect(() => {
    let active = true
    Promise.all([fetchUsers(), fetchSites()])
      .then(([userData, siteData]) => {
        if (active) {
          setUsers(userData)
          setSites(siteData)
        }
      })
      .catch(() => {
        if (active) setError('Die Benutzer konnten nicht geladen werden.')
      })
    return () => {
      active = false
    }
  }, [])

  function openCreate() {
    setEditing(null)
    setFormOpen(true)
  }

  function openEdit(user: UserInfo) {
    setEditing(user)
    setFormOpen(true)
  }

  function handleSaved(saved: UserInfo) {
    setUsers((current) =>
      [...current.filter((u) => u.username !== saved.username), saved].sort(byName),
    )
    setFormOpen(false)
  }

  async function handleDelete() {
    if (deleting === null) {
      return
    }
    const user = deleting
    setDeleting(null)
    setError(null)
    try {
      await deleteUser(user.username)
      setUsers((current) => current.filter((u) => u.username !== user.username))
    } catch {
      setError(`Das Konto „${user.name}“ konnte nicht gelöscht werden.`)
    }
  }

  return (
    <Stack spacing={2}>
      <Stack direction="row" sx={{ justifyContent: 'space-between', alignItems: 'center' }}>
        <Typography variant="h5" component="h1">
          Benutzer
        </Typography>
        <Button variant="contained" onClick={openCreate}>
          Benutzer anlegen
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
              <TableCell>Name</TableCell>
              <TableCell>Anmeldename</TableCell>
              <TableCell>Rolle</TableCell>
              <TableCell>Standort</TableCell>
              <TableCell align="right">Aktionen</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {users.map((u) => (
              <TableRow key={u.username}>
                <TableCell>{u.name}</TableCell>
                <TableCell>{u.username}</TableCell>
                <TableCell>{roleLabels[u.role]}</TableCell>
                <TableCell>{u.siteName ?? '–'}</TableCell>
                <TableCell align="right">
                  {u.username === currentUser?.username ? (
                    <Typography variant="body2" sx={{ color: 'text.secondary' }}>
                      Eigenes Konto
                    </Typography>
                  ) : (
                    <>
                      <Button size="small" onClick={() => openEdit(u)}>
                        Ändern
                      </Button>
                      <Button size="small" color="error" onClick={() => setDeleting(u)}>
                        Löschen
                      </Button>
                    </>
                  )}
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>

      {formOpen && (
        <UserFormDialog
          user={editing}
          sites={sites}
          onClose={() => setFormOpen(false)}
          onSaved={handleSaved}
        />
      )}

      <ConfirmDialog
        open={deleting !== null}
        title="Benutzer löschen?"
        message={deleting ? `Soll das Konto „${deleting.name}“ (${deleting.username}) wirklich gelöscht werden?` : ''}
        confirmLabel="Löschen"
        onConfirm={handleDelete}
        onCancel={() => setDeleting(null)}
      />
    </Stack>
  )
}