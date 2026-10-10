import { Navigate, Outlet } from 'react-router'
import { useAuth } from './useAuth'
import { startPathFor } from './roles'
import type { Role } from './types'

export function RequireAuth({ roles }: { roles: Role[] }) {
  const { user, loading } = useAuth()

  if (loading) {
    return null
  }
  if (!user) {
    return <Navigate to="/anmeldung" replace />
  }
  if (!roles.includes(user.role)) {
    return <Navigate to={startPathFor(user.role)} replace />
  }
  return <Outlet />
}