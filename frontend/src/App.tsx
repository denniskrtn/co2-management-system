import { Navigate, Route, Routes } from 'react-router'
import { RequireAuth } from './auth/RequireAuth'
import { allRoles } from './auth/roles'
import { AppLayout } from './components/AppLayout'
import { LoginPage } from './pages/LoginPage'
import { PlaceholderPage } from './pages/PlaceholderPage'

function App() {
  return (
    <Routes>
      <Route path="/anmeldung" element={<LoginPage />} />

      <Route element={<RequireAuth roles={allRoles} />}>
        <Route element={<AppLayout />}>
          <Route element={<RequireAuth roles={['SUSTAINABILITY_OFFICER', 'MANAGER', 'USER']} />}>
            <Route path="/auswertung" element={<PlaceholderPage title="Auswertung" />} />
          </Route>

          <Route element={<RequireAuth roles={['SUSTAINABILITY_OFFICER']} />}>
            <Route path="/erfassung" element={<PlaceholderPage title="Erfassung" />} />
          </Route>

          <Route element={<RequireAuth roles={['ADMIN']} />}>
            <Route path="/standortverwaltung" element={<PlaceholderPage title="Standortverwaltung" />} />
            <Route path="/benutzerverwaltung" element={<PlaceholderPage title="Benutzerverwaltung" />} />
          </Route>
        </Route>
      </Route>

      <Route path="*" element={<Navigate to="/anmeldung" replace />} />
    </Routes>
  )
}

export default App