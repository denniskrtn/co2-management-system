import { Typography } from '@mui/material'

export function PlaceholderPage({ title }: { title: string }) {
  return (
    <>
      <Typography variant="h4" gutterBottom>
        {title}
      </Typography>
      <Typography>Diese Seite folgt in einem späteren Issue.</Typography>
    </>
  )
}