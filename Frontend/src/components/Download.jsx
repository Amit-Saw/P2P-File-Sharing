import React, { useState } from 'react'
import Typography from '@mui/material/Typography'
import Button from '@mui/material/Button'
import Box from '@mui/material/Box'
import TextField from '@mui/material/TextField'
import Alert from '@mui/material/Alert'

export default function Download(){
  const [port, setPort] = useState('')
  const [error, setError] = useState(null)

  const handleDownload = () => {
    setError(null)
    if(!port || port.trim() === '') {
      setError('Please enter a port number')
      return
    }
    const portNum = parseInt(port)
    if(isNaN(portNum) || portNum < 1 || portNum > 65535) {
      setError('Port must be a number between 1 and 65535')
      return
    }
    window.location.href = `/download?port=${encodeURIComponent(portNum)}`
  }

  const handleKeyPress = (e) => {
    if(e.key === 'Enter') {
      handleDownload()
    }
  }

  return (
    <Box>
      <Typography variant="h5" gutterBottom>Download by Port</Typography>
      {error && <Alert severity="error" sx={{ mb:2 }}>{error}</Alert>}
      <Box sx={{ display: 'flex', gap: 2, alignItems: 'flex-start' }}>
        <TextField 
          label="Port" 
          type="number"
          inputProps={{ min: 1, max: 65535 }}
          value={port} 
          onChange={e => setPort(e.target.value)}
          onKeyPress={handleKeyPress}
          placeholder="e.g., 54321"
        />
        <Button variant="contained" onClick={handleDownload} sx={{ mt: 1 }}>Download</Button>
      </Box>
      <Box sx={{ mt:2 }}>
        <Typography variant="body2">If you received a port from a peer, paste it above and press Download.</Typography>
      </Box>
    </Box>
  )
}
