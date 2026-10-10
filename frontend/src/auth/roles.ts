import type { Role } from './types'

export const roleLabels: Record<Role, string> = {
  ADMIN: 'Administration',
  SUSTAINABILITY_OFFICER: 'Nachhaltigkeitsbeauftragte',
  MANAGER: 'Führungskraft',
  USER: 'Benutzer',
}

export function startPathFor(role: Role): string {
  return role === 'ADMIN' ? '/standortverwaltung' : '/auswertung'
}