import React from 'react'
import { Routes, Route, Link } from 'react-router-dom'
import Container from '@mui/material/Container'
import AppBar from '@mui/material/AppBar'
import Toolbar from '@mui/material/Toolbar'
import Typography from '@mui/material/Typography'
import Button from '@mui/material/Button'

import Dashboard from './components/Dashboard'
import Upload from './components/Upload'
import Download from './components/Download'

export default function App(){
  return (
    <>
      <AppBar position="static">
        <Toolbar>
          <Typography variant="h6" component="div" sx={{ flexGrow: 1 }}>
            P2P File Sharing
          </Typography>
          <Button color="inherit" component={Link} to="/">Home</Button>
          <Button color="inherit" component={Link} to="/upload">Upload</Button>
          <Button color="inherit" component={Link} to="/download">Download</Button>
        </Toolbar>
      </AppBar>
      <Container sx={{ mt: 4 }}>
        <Routes>
          <Route path="/" element={<Dashboard/>} />
          <Route path="/upload" element={<Upload/>} />
          <Route path="/download" element={<Download/>} />
        </Routes>
      </Container>
    </>
  )
}
