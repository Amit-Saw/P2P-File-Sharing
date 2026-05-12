import React, { useState } from 'react'
import Typography from '@mui/material/Typography'
import Button from '@mui/material/Button'
import Box from '@mui/material/Box'
import Alert from '@mui/material/Alert'
import CircularProgress from '@mui/material/CircularProgress'
import { uploadFile } from '../api'

export default function Upload(){
  const [file, setFile] = useState(null)
  const [loading, setLoading] = useState(false)
  const [result, setResult] = useState(null)
  const [error, setError] = useState(null)

  const handleSubmit = async (e) => {
    e.preventDefault()
    if(!file) return setError('Please select a file')
    setError(null)
    setLoading(true)
    try{
      const data = await uploadFile(file)
      setResult(data)
    }catch(err){
      setError(err?.response?.data || err.message)
    }finally{
      setLoading(false)
    }
  }

  return (
    <Box>
      <Typography variant="h5" gutterBottom>Upload File</Typography>
      {error && <Alert severity="error" sx={{ mb:2 }}>{error}</Alert>}
      <form onSubmit={handleSubmit}>
        <input type="file" onChange={e => setFile(e.target.files[0])} />
        <Box sx={{ mt:2 }}>
          <Button variant="contained" type="submit" disabled={loading}>
            {loading ? <CircularProgress size={20} /> : 'Upload'}
          </Button>
        </Box>
      </form>

      {result && (
        <Box sx={{ mt:3 }}>
          <Alert severity="success">Uploaded. Share this port: <strong>{result.port}</strong></Alert>
          <Box sx={{ mt:1 }}>
            <Typography>Direct download link (share with peers):</Typography>
            <a href={"/download?port="+result.port} target="_blank" rel="noreferrer">/download?port={result.port}</a>
          </Box>
        </Box>
      )}
    </Box>
  )
}
