import { useState, type FormEvent } from 'react'
import { Navigate, useNavigate } from 'react-router'
import { Alert, Box, Button, Paper, TextField, Typography } from '@mui/material'
import { ApiError } from '../api/client'
import { startPathFor } from '../auth/roles'
import { useAuth } from '../auth/useAuth'

export function LoginPage() {
  const { user, login } = useAuth()
  const navigate = useNavigate()
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)

  if (user) {
    return <Navigate to={startPathFor(user.role)} replace />
  }

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setError(null)
    setSubmitting(true)
    try {
      const loggedInUser = await login(username, password)
      navigate(startPathFor(loggedInUser.role), { replace: true })
    } catch (e) {
      setError(
        e instanceof ApiError && e.status === 401
          ? 'Anmeldename oder Passwort ist falsch.'
          : 'Die Anmeldung ist derzeit nicht möglich.',
      )
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <Box sx={{ minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
      <Paper sx={{ p: 4, width: 360 }}>
        <Box component="form" onSubmit={handleSubmit}>
          <Typography variant="h5" gutterBottom>
            CO2-Management-System
          </Typography>
          {error && (
            <Alert severity="error" sx={{ mb: 2 }}>
              {error}
            </Alert>
          )}
          <TextField
            label="Anmeldename"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            fullWidth
            margin="normal"
            autoFocus
            required
          />
          <TextField
            label="Passwort"
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            fullWidth
            margin="normal"
            required
          />
          <Button type="submit" variant="contained" fullWidth sx={{ mt: 2 }} disabled={submitting}>
            Anmelden
          </Button>
        </Box>
      </Paper>
    </Box>
  )
}