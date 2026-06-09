import { create } from 'zustand'
import type { Application, CoverLetter, DashboardStats } from '@/types'
import { api } from '@/lib/api'

interface ApplicationState {
  applications: Application[]
  recentApplications: Application[]
  stats: DashboardStats | null
  currentCoverLetter: CoverLetter | null
  currentApplicationId: number | null
  loading: boolean

  fetchApplications: (userId: number) => Promise<void>
  fetchRecent: (userId: number) => Promise<void>
  fetchStats: (userId: number) => Promise<void>
  generateLetter: (jobId: number, userId: number) => Promise<CoverLetter>
  approveLetter: (coverLetterId: number) => Promise<number>
  regenerateLetter: (jobId: number, userId: number) => Promise<CoverLetter>
  confirmApply: (applicationId: number) => Promise<{ applyUrl: string; coverLetter: string; applicationMode: string }>
  confirmAssistedComplete: (applicationId: number) => Promise<void>
}

export const useApplicationStore = create<ApplicationState>((set) => ({
  applications: [],
  recentApplications: [],
  stats: null,
  currentCoverLetter: null,
  currentApplicationId: null,
  loading: false,

  fetchApplications: async (userId) => {
    const applications = await api.dashboard.applications(userId)
    set({ applications })
  },

  fetchRecent: async (userId) => {
    const recentApplications = await api.dashboard.recent(userId)
    set({ recentApplications })
  },

  fetchStats: async (userId) => {
    const stats = await api.dashboard.stats(userId)
    set({ stats })
  },

  generateLetter: async (jobId, userId) => {
    set({ loading: true })
    const letter = await api.applications.generateLetter(jobId, userId)
    set({ currentCoverLetter: letter, loading: false })
    return letter
  },

  approveLetter: async (coverLetterId) => {
    const result = await api.applications.approveLetter(coverLetterId)
    set({ currentApplicationId: result.applicationId })
    return result.applicationId
  },

  regenerateLetter: async (jobId, userId) => {
    set({ loading: true })
    const letter = await api.applications.regenerateLetter(jobId, userId)
    set({ currentCoverLetter: letter, loading: false })
    return letter
  },

  confirmApply: async (applicationId) => {
    const result = await api.applications.confirmApply(applicationId)
    return {
      applyUrl: result.applyUrl,
      coverLetter: result.coverLetter,
      applicationMode: result.applicationMode,
    }
  },

  confirmAssistedComplete: async (applicationId) => {
    await api.applications.confirmAssistedComplete(applicationId)
  },
}))
