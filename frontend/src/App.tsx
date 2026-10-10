import { useState } from 'react'
import { Button, Container, Typography } from '@mui/material'

function App() {
  const [status, setStatus] = useState('')

  const checkBackend = async () => {
    const response = await fetch('/api/me')
    setStatus(`Antwort vom Backend: ${response.status}`)
  }

  return (
    <Container sx={{ mt: 4 }}>
      <Typography variant="h4" gutterBottom>
        CO2-Management-System
      </Typography>
      <Button variant="contained" onClick={checkBackend}>
        Backend prüfen
      </Button>
      <Typography sx={{ mt: 2 }}>{status}</Typography>
    </Container>
  )
}

export default App