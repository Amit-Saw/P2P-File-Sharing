import React from 'react'
import Typography from '@mui/material/Typography'
import Box from '@mui/material/Box'

export default function Dashboard(){
  return (
    <Box>
      <Typography variant="h4" gutterBottom>Welcome to P2P File Sharing</Typography>
      <Typography sx={{ mb:2 }}>Use the Upload page to share a file. After upload, you will receive a port which other peers can use on the Download page.</Typography>
      <Typography variant="h6">How it works</Typography>
      <ol>
        <li>Upload a file via <strong>Upload</strong>.</li>
        <li>Server returns a `port` for that file.</li>
        <li>Share the `port` to peers; they use <strong>Download</strong> to fetch the file.</li>
      </ol>
      <Typography sx={{ mt:2 }}>* For local testing the site proxies API calls to the backend on `localhost:8080`.</Typography>
    </Box>
  )
}
