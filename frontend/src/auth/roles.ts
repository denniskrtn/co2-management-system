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

export const allRoles: Role[] = ['ADMIN', 'SUSTAINABILITY_OFFICER', 'MANAGER', 'USER']

export interface NavItem {
  label: string
  path: string
}

const navigation: Record<Role, NavItem[]> = {
  ADMIN: [
    { label: 'Standortverwaltung', path: '/standortverwaltung' },
    { label: 'Benutzerverwaltung', path: '/benutzerverwaltung' },
  ],
  SUSTAINABILITY_OFFICER: [
    { label: 'Auswertung', path: '/auswertung' },
    { label: 'Erfassung', path: '/erfassung' },
  ],
  MANAGER: [{ label: 'Auswertung', path: '/auswertung' }],
  USER: [{ label: 'Auswertung', path: '/auswertung' }],
}

export function navigationFor(role: Role): NavItem[] {
  return navigation[role]
}