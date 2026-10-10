import { Button, Container, Typography } from '@mui/material'
import { useAuth } from '../auth/useAuth'

export function PlaceholderPage({ title }: { title: string }) {
  const { user, logout } = useAuth()

  return (
    <Container sx={{ mt: 4 }}>
      <Typography variant="h4" gutterBottom>
        {title}
      </Typography>
      <Typography>Angemeldet als {user?.name}</Typography>
      <Button sx={{ mt: 2 }} onClick={logout}>
        Abmelden
      </Button>
    </Container>
  )
}