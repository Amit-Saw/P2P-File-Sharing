import React from 'react'
import { createRoot } from 'react-dom/client'
import { createBrowserRouter, RouterProvider } from 'react-router-dom'
import App from './App'
import Dashboard from './components/Dashboard'
import Upload from './components/Upload'
import Download from './components/Download'
import './styles.css'

const router = createBrowserRouter(
  [
    {
      path: '/',
      element: <App />,
      children: [
        { index: true, element: <Dashboard /> },
        { path: 'upload', element: <Upload /> },
        { path: 'download', element: <Download /> },
      ],
    },
  ],
  {
    future: {
      v7_startTransition: true,
      v7_relativeSplatPath: true,
    },
  }
)

createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <RouterProvider router={router} />
  </React.StrictMode>
)
