import { create } from 'zustand'
import type { JobWithMatch, MatchResult, WorkMode } from '@/types'
import { api } from '@/lib/api'

interface JobFilters {
  title: string
  location: string
  workModes: WorkMode[]
}

interface JobState {
  searchResults: JobWithMatch[]
  selectedJobIds: Set<number>
  recentJobs: JobWithMatch[]
  filters: JobFilters
  loading: boolean
  error: string | null

  setFilters: (filters: Partial<JobFilters>) => void
  resetFilters: () => void
  search: (userId: number) => Promise<void>
  fetchRecent: () => Promise<void>
  scoreMatch: (jobId: number, userId: number) => Promise<MatchResult>
  selectJob: (jobId: number) => void
  deselectJob: (jobId: number) => void
  clearSelection: () => void
}

const defaultFilters: JobFilters = {
  title: '',
  location: '',
  workModes: ['REMOTE', 'HYBRID', 'ONSITE'],
}

export const useJobStore = create<JobState>((set, get) => ({
  searchResults: [],
  selectedJobIds: new Set(),
  recentJobs: [],
  filters: { ...defaultFilters },
  loading: false,
  error: null,

  setFilters: (partial) =>
    set((state) => ({ filters: { ...state.filters, ...partial } })),

  resetFilters: () => set({ filters: { ...defaultFilters } }),

  search: async (userId: number) => {
    const { filters } = get()
    set({ loading: true, error: null })
    try {
      const results = await api.jobs.search(filters.title, filters.location, filters.workModes)
      const jobsWithMatch: JobWithMatch[] = results.map((job) => ({ ...job }))
      set({ searchResults: jobsWithMatch, loading: false })

      // Score matches in background
      for (const job of jobsWithMatch) {
        if (job.id) {
          try {
            const match = await api.jobs.scoreMatch(job.id, userId)
            set((state) => ({
              searchResults: state.searchResults.map((j) =>
                j.id === job.id ? { ...j, match } : j
              ),
            }))
          } catch {
            // Scoring failure is non-critical
          }
        }
      }
    } catch (e) {
      set({ error: (e as Error).message, loading: false })
    }
  },

  fetchRecent: async () => {
    try {
      const jobs = await api.jobs.getRecent()
      set({ recentJobs: jobs.map((job) => ({ ...job })) })
    } catch {
      // Silent fail for recent jobs fetch
    }
  },

  scoreMatch: async (jobId: number, userId: number) => {
    const match = await api.jobs.scoreMatch(jobId, userId)
    set((state) => ({
      searchResults: state.searchResults.map((j) =>
        j.id === jobId ? { ...j, match } : j
      ),
    }))
    return match
  },

  selectJob: (jobId: number) =>
    set((state) => {
      const newSet = new Set(state.selectedJobIds)
      newSet.add(jobId)
      return { selectedJobIds: newSet }
    }),

  deselectJob: (jobId: number) =>
    set((state) => {
      const newSet = new Set(state.selectedJobIds)
      newSet.delete(jobId)
      return { selectedJobIds: newSet }
    }),

  clearSelection: () => set({ selectedJobIds: new Set() }),
}))
