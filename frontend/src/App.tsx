import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { useEffect } from 'react'
import { useAuthStore } from '@/store/authStore'
import LoginPage from '@/pages/LoginPage'
import DashboardPage from '@/pages/DashboardPage'
import JobSearchPage from '@/pages/JobSearchPage'
import ResumeUploadPage from '@/pages/ResumeUploadPage'
import ApprovalPage from '@/pages/ApprovalPage'
import AppShell from '@/components/layout/AppShell'

export default function App() {
  const { isAuthenticated, loading, fetchUser } = useAuthStore()

  useEffect(() => {
    fetchUser()
  }, [fetchUser])

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="text-lg text-gray-500">Loading...</div>
      </div>
    )
  }

  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={!isAuthenticated ? <LoginPage /> : <Navigate to="/dashboard" />} />
        <Route element={isAuthenticated ? <AppShell /> : <Navigate to="/login" />}>
          <Route path="/dashboard" element={<DashboardPage />} />
          <Route path="/search" element={<JobSearchPage />} />
          <Route path="/resume" element={<ResumeUploadPage />} />
          <Route path="/approve/:jobId" element={<ApprovalPage />} />
          <Route path="/" element={<Navigate to="/dashboard" />} />
        </Route>
      </Routes>
    </BrowserRouter>
  )
}
