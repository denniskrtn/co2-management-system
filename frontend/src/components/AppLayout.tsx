import { AppBar, Box, Button, Container, Toolbar, Typography } from '@mui/material'
import { NavLink, Outlet } from 'react-router'
import { navigationFor, roleLabels } from '../auth/roles'
import { useAuth } from '../auth/useAuth'

export function AppLayout() {
    const { user, logout } = useAuth()

    if (!user) {
        return null
    }

    return (
        <>
            <AppBar position="static">
                <Toolbar>
                    <Typography variant="h6" sx={{ mr: 4 }}>
                        CO2-Management-System
                    </Typography>
                    {navigationFor(user.role).map((item) => (
                        <Button
                            key={item.path}
                            component={NavLink}
                            to={item.path}
                            color="inherit"
                            sx={{ '&.active': { textDecoration: 'underline' } }}
                        >
                            {item.label}
                        </Button>
                    ))}
                    <Box sx={{ flexGrow: 1 }} />
                    <Typography sx={{ mr: 2 }}>
                        {user.name} ({roleLabels[user.role]})
                    </Typography>
                    <Button color="inherit" onClick={logout}>
                        Abmelden
                    </Button>
                </Toolbar>
            </AppBar>
            <Container sx={{ mt: 4 }}>
                <Outlet />
            </Container>
        </>
    )
}