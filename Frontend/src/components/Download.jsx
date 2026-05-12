import React, { useState } from 'react'
import Typography from '@mui/material/Typography'
import Button from '@mui/material/Button'
import Box from '@mui/material/Box'
import TextField from '@mui/material/TextField'

export default function Download(){
  const [port, setPort] = useState('')

  const handleDownload = () => {
    if(!port) return
    window.location.href = `/download?port=${encodeURIComponent(port)}`
  }

  return (
    <Box>
      <Typography variant="h5" gutterBottom>Download by Port</Typography>
      <TextField label="Port" value={port} onChange={e => setPort(e.target.value)} sx={{ mr:2 }} />
      <Button variant="contained" onClick={handleDownload}>Download</Button>
      <Box sx={{ mt:2 }}>
        <Typography variant="body2">If you received a port from a peer, paste it above and press Download.</Typography>
      </Box>
    </Box>
  )
}
