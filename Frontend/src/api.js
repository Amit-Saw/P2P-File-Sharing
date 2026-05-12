import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 60000,
})

export async function uploadFile(file){
  const fd = new FormData()
  fd.append('file', file)
  const resp = await api.post('/upload', fd, { headers: { 'Content-Type': 'multipart/form-data' } })
  return resp.data
}

export async function downloadByPort(port){
  // returns a full URL to download via browser
  return `/api/download?port=${encodeURIComponent(port)}`
}

export default api
