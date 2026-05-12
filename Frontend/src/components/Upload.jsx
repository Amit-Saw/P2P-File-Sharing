import React, { useState } from 'react'
import Typography from '@mui/material/Typography'
import Button from '@mui/material/Button'
import Box from '@mui/material/Box'
import Alert from '@mui/material/Alert'
import CircularProgress from '@mui/material/CircularProgress'
import TextField from '@mui/material/TextField'
import { uploadFile } from '../api'

const MAX_FILE_SIZE = 500 * 1024 * 1024; // 500MB

export default function Upload(){
  const [file, setFile] = useState(null)
  const [loading, setLoading] = useState(false)
  const [result, setResult] = useState(null)
  const [error, setError] = useState(null)

  const handleFileChange = (e) => {
    const selectedFile = e.target.files[0]
    setError(null)
    
    if(!selectedFile) return
    
    if(selectedFile.size > MAX_FILE_SIZE) {
      setError(`File size exceeds ${MAX_FILE_SIZE / (1024*1024)}MB limit`)
      return
    }
    
    setFile(selectedFile)
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    if(!file) {
      setError('Please select a file')
      return
    }
    setError(null)
    setLoading(true)
    try{
      const data = await uploadFile(file)
      setResult(data)
      setFile(null)
    }catch(err){
      setError(err?.response?.data?.message || err.message || 'Upload failed')
    }finally{
      setLoading(false)
    }
  }

  return (
    <Box>
      <Typography variant="h5" gutterBottom>Upload File</Typography>
      {error && <Alert severity="error" sx={{ mb:2 }}>{error}</Alert>}
      <form onSubmit={handleSubmit}>
        <input 
          type="file" 
          onChange={handleFileChange}
          disabled={loading}
        />
        {file && (
          <Typography variant="body2" sx={{ mt: 1, mb: 1 }}>
            Selected: {file.name} ({(file.size / 1024).toFixed(2)} KB)
          </Typography>
        )}
        <Box sx={{ mt:2 }}>
          <Button variant="contained" type="submit" disabled={loading || !file}>
            {loading ? <><CircularProgress size={20} sx={{ mr: 1 }} /> Uploading...</> : 'Upload'}
          </Button>
        </Box>
      </form>

      {result && (
        <Box sx={{ mt:3 }}>
          <Alert severity="success">
            Uploaded successfully! File: <strong>{result.fileName}</strong><br/>
            Share this port: <strong>{result.port}</strong>
          </Alert>
          <Box sx={{ mt:2 }}>
            <Typography variant="subtitle2" sx={{ mb: 1 }}>Direct download link (copy and share with peers):</Typography>
            <TextField 
              fullWidth
              value={`${window.location.origin}/download?port=${result.port}`}
              InputProps={{
                readOnly: true,
              }}
              onClick={(e) => e.target.select()}
              sx={{ mb: 1 }}
            />
            <a href={`/download?port=${result.port}`} target="_blank" rel="noreferrer">
              <Button variant="outlined" size="small">Open Link</Button>
            </a>
          </Box>
        </Box>
      )}
    </Box>
  )
}
