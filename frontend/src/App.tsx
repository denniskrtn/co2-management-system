import { Navigate, Route, Routes } from 'react-router'
import { RequireAuth } from './auth/RequireAuth'
import { allRoles } from './auth/roles'
import { AppLayout } from './components/AppLayout'
import { LoginPage } from './pages/LoginPage'
import { SitesPage } from './pages/SitesPage'
import { UsersPage } from './pages/UsersPage'
import { EntryPage } from './pages/EntryPage'
import { ReportPage } from './pages/ReportPage'

function App() {
  return (
    <Routes>
      <Route path="/anmeldung" element={<LoginPage />} />

      <Route element={<RequireAuth roles={allRoles} />}>
        <Route element={<AppLayout />}>
          <Route element={<RequireAuth roles={['SUSTAINABILITY_OFFICER', 'MANAGER', 'USER']} />}>
            <Route path="/auswertung" element={<ReportPage />} />
          </Route>

          <Route element={<RequireAuth roles={['SUSTAINABILITY_OFFICER']} />}>
            <Route path="/erfassung" element={<EntryPage />} />
          </Route>

          <Route element={<RequireAuth roles={['ADMIN']} />}>
            <Route path="/standortverwaltung" element={<SitesPage />} />
            <Route path="/benutzerverwaltung" element={<UsersPage />} />
          </Route>
        </Route>
      </Route>

      <Route path="*" element={<Navigate to="/anmeldung" replace />} />
    </Routes>
  )
}

export default App