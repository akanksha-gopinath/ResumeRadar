import type { Application, CoverLetter, DashboardStats, Job, JobWithMatch, MatchResult, Resume, SearchPreferences, WorkMode } from '@/types'

const BASE = '/api'

async function fetchJson<T>(url: string, options?: RequestInit): Promise<T> {
  const res = await fetch(`${BASE}${url}`, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...options?.headers,
    },
    credentials: 'include',
  })
  if (!res.ok) {
    const error = await res.json().catch(() => ({ error: res.statusText }))
    throw new Error(error.error || res.statusText)
  }
  return res.json()
}

export const api = {
  auth: {
    me: () => fetchJson<{ id: number; email: string; displayName: string }>('/auth/me'),
    logout: () => fetchJson<void>('/auth/logout', { method: 'POST' }),
  },

  resumes: {
    upload: async (userId: number, file: File): Promise<Resume> => {
      const formData = new FormData()
      formData.append('file', file)
      formData.append('userId', userId.toString())
      const res = await fetch(`${BASE}/resumes/upload`, {
        method: 'POST',
        body: formData,
        credentials: 'include',
      })
      if (!res.ok) throw new Error('Upload failed')
      return res.json()
    },
    getActive: (userId: number) => fetchJson<Resume>(`/resumes/active?userId=${userId}`),
  },

  jobs: {
    search: (title: string, location: string, workModes: WorkMode[]) =>
      fetchJson<Job[]>('/jobs/search', {
        method: 'POST',
        body: JSON.stringify({ title, location, workModes }),
      }),
    getRecent: () => fetchJson<Job[]>('/jobs/recent'),
    scoreMatch: (jobId: number, userId: number) =>
      fetchJson<MatchResult>(`/jobs/${jobId}/match?userId=${userId}`, { method: 'POST' }),
    batchMatch: (userId: number, jobIds: number[]) =>
      fetchJson<MatchResult[]>('/jobs/batch-match', {
        method: 'POST',
        body: JSON.stringify({ userId, jobIds }),
      }),
  },

  applications: {
    selectJobs: (userId: number, jobIds: number[]) =>
      fetchJson<{ selectedJobs: number[]; nextStep: string }>('/applications/select-jobs', {
        method: 'POST',
        body: JSON.stringify({ userId, jobIds }),
      }),
    generateLetter: (jobId: number, userId: number) =>
      fetchJson<CoverLetter>(`/applications/${jobId}/generate-letter?userId=${userId}`, { method: 'POST' }),
    approveLetter: (coverLetterId: number) =>
      fetchJson<{ applicationId: number; status: string; applicationMode: string }>(`/applications/${coverLetterId}/approve-letter`, { method: 'PUT' }),
    regenerateLetter: (jobId: number, userId: number) =>
      fetchJson<CoverLetter>(`/applications/${jobId}/regenerate-letter?userId=${userId}`, { method: 'PUT' }),
    confirmApply: (applicationId: number) =>
      fetchJson<{ applicationId: number; status: string; applicationMode: string; applyUrl: string; coverLetter: string }>(`/applications/${applicationId}/confirm-apply`, { method: 'POST' }),
    confirmAssistedComplete: (applicationId: number) =>
      fetchJson<{ applicationId: number; status: string }>(`/applications/${applicationId}/confirm-assisted-complete`, { method: 'POST' }),
  },

  dashboard: {
    stats: (userId: number) => fetchJson<DashboardStats>(`/dashboard/stats?userId=${userId}`),
    applications: (userId: number) => fetchJson<Application[]>(`/dashboard/applications?userId=${userId}`),
    recent: (userId: number) => fetchJson<Application[]>(`/dashboard/recent?userId=${userId}`),
  },

  preferences: {
    get: (userId: number) => fetchJson<SearchPreferences>(`/preferences?userId=${userId}`),
    update: (userId: number, prefs: Partial<SearchPreferences>) =>
      fetchJson<SearchPreferences>(`/preferences?userId=${userId}`, {
        method: 'PUT',
        body: JSON.stringify(prefs),
      }),
    reset: (userId: number) =>
      fetchJson<SearchPreferences>(`/preferences/reset?userId=${userId}`, { method: 'POST' }),
  },
}
