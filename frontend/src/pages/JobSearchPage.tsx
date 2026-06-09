import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuthStore } from '@/store/authStore'
import { useJobStore } from '@/store/jobStore'
import { Search, RotateCcw, MapPin, Briefcase } from 'lucide-react'
import { cn, getMatchBgColor, formatWorkMode } from '@/lib/utils'
import type { WorkMode } from '@/types'

export default function JobSearchPage() {
  const { user } = useAuthStore()
  const {
    searchResults, filters, selectedJobIds, loading,
    setFilters, resetFilters, search, selectJob, deselectJob,
  } = useJobStore()
  const navigate = useNavigate()
  const [searchExecuted, setSearchExecuted] = useState(false)

  const handleSearch = async () => {
    if (!user) return
    await search(user.id)
    setSearchExecuted(true)
  }

  const toggleWorkMode = (mode: WorkMode) => {
    const current = filters.workModes
    if (current.includes(mode)) {
      setFilters({ workModes: current.filter((m) => m !== mode) })
    } else {
      setFilters({ workModes: [...current, mode] })
    }
  }

  const handleReset = () => {
    resetFilters()
    setSearchExecuted(false)
  }

  const handleProceed = () => {
    if (selectedJobIds.size === 0) return
    const firstJobId = Array.from(selectedJobIds)[0]
    navigate(`/approve/${firstJobId}`)
  }

  const workModeOptions: WorkMode[] = ['REMOTE', 'HYBRID', 'ONSITE']

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-bold text-gray-900">Job Search</h2>
          <p className="text-gray-500 mt-1">Find and match jobs to your resume</p>
        </div>
        <button
          onClick={handleReset}
          className="flex items-center gap-2 px-4 py-2 text-sm text-gray-600 border border-gray-300 rounded-lg hover:bg-gray-50"
        >
          <RotateCcw size={16} />
          Reset Filters
        </button>
      </div>

      {/* Filters */}
      <div className="bg-white rounded-xl border border-gray-200 p-6 space-y-4">
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Job Title</label>
            <div className="relative">
              <Briefcase size={16} className="absolute left-3 top-3 text-gray-400" />
              <input
                type="text"
                value={filters.title}
                onChange={(e) => setFilters({ title: e.target.value })}
                placeholder="e.g., Software Engineer"
                className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-brand-500 focus:border-transparent"
              />
            </div>
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Location</label>
            <div className="relative">
              <MapPin size={16} className="absolute left-3 top-3 text-gray-400" />
              <input
                type="text"
                value={filters.location}
                onChange={(e) => setFilters({ location: e.target.value })}
                placeholder="e.g., San Francisco, CA"
                className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-brand-500 focus:border-transparent"
              />
            </div>
          </div>
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">Work Mode</label>
          <div className="flex gap-3">
            {workModeOptions.map((mode) => (
              <button
                key={mode}
                onClick={() => toggleWorkMode(mode)}
                className={cn(
                  'px-4 py-2 rounded-lg text-sm font-medium border transition-colors',
                  filters.workModes.includes(mode)
                    ? 'bg-brand-50 border-brand-300 text-brand-700'
                    : 'bg-white border-gray-300 text-gray-500 hover:bg-gray-50'
                )}
              >
                {formatWorkMode(mode)}
              </button>
            ))}
          </div>
        </div>

        <button
          onClick={handleSearch}
          disabled={loading || !filters.title}
          className="flex items-center gap-2 px-6 py-2.5 bg-brand-600 text-white rounded-lg font-medium hover:bg-brand-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
        >
          <Search size={16} />
          {loading ? 'Searching...' : 'Search Jobs'}
        </button>
      </div>

      {/* Results */}
      {searchExecuted && (
        <div className="space-y-4">
          <div className="flex items-center justify-between">
            <p className="text-sm text-gray-500">{searchResults.length} results found</p>
            {selectedJobIds.size > 0 && (
              <button
                onClick={handleProceed}
                className="px-6 py-2.5 bg-green-600 text-white rounded-lg font-medium hover:bg-green-700 transition-colors"
              >
                Generate Cover Letters ({selectedJobIds.size} selected)
              </button>
            )}
          </div>

          <div className="space-y-3">
            {searchResults.map((job) => {
              const isEligible = job.match ? job.match.matchPercentage >= 80 : false
              const isSelected = job.id ? selectedJobIds.has(job.id) : false

              return (
                <div
                  key={job.externalId}
                  className={cn(
                    'bg-white rounded-xl border p-5 transition-all',
                    job.match ? getMatchBgColor(job.match.matchPercentage) : 'border-gray-200',
                    isSelected && 'ring-2 ring-brand-500',
                    !isEligible && job.match && 'opacity-60'
                  )}
                >
                  <div className="flex items-start justify-between">
                    <div className="flex-1">
                      <div className="flex items-center gap-3">
                        {job.id && (
                          <input
                            type="checkbox"
                            checked={isSelected}
                            disabled={!isEligible}
                            onChange={() => isSelected ? deselectJob(job.id!) : selectJob(job.id!)}
                            className="w-4 h-4 rounded border-gray-300 text-brand-600 focus:ring-brand-500 disabled:opacity-50"
                          />
                        )}
                        <h3 className="font-semibold text-gray-900">{job.title}</h3>
                      </div>
                      <p className="text-sm text-gray-600 mt-1">{job.company} - {job.location}</p>
                      <div className="flex gap-2 mt-2">
                        <span className="text-xs px-2 py-0.5 bg-gray-100 rounded-full">
                          {formatWorkMode(job.workMode)}
                        </span>
                        <span className="text-xs px-2 py-0.5 bg-blue-50 text-blue-700 rounded-full">
                          {job.applicationMode === 'EASY_APPLY' ? 'Easy Apply' : 'External'}
                        </span>
                      </div>
                      {job.match && (
                        <div className="mt-3">
                          <p className="text-xs text-gray-500">{job.match.summary}</p>
                          {job.match.matchedSkills.length > 0 && (
                            <div className="flex flex-wrap gap-1 mt-1">
                              {job.match.matchedSkills.map((skill) => (
                                <span key={skill} className="text-xs px-2 py-0.5 bg-green-100 text-green-700 rounded-full">
                                  {skill}
                                </span>
                              ))}
                            </div>
                          )}
                          {job.match.missingSkills.length > 0 && (
                            <div className="flex flex-wrap gap-1 mt-1">
                              {job.match.missingSkills.map((skill) => (
                                <span key={skill} className="text-xs px-2 py-0.5 bg-red-50 text-red-600 rounded-full">
                                  {skill}
                                </span>
                              ))}
                            </div>
                          )}
                        </div>
                      )}
                    </div>

                    {/* Match percentage badge */}
                    {job.match ? (
                      <div className={cn(
                        'text-lg font-bold px-3 py-1 rounded-lg',
                        job.match.matchPercentage >= 80 ? 'text-green-700 bg-green-100' :
                        job.match.matchPercentage >= 60 ? 'text-yellow-700 bg-yellow-100' :
                        'text-red-700 bg-red-100'
                      )}>
                        {job.match.matchPercentage}%
                      </div>
                    ) : (
                      <div className="text-sm text-gray-400 animate-pulse">Scoring...</div>
                    )}
                  </div>
                </div>
              )
            })}
          </div>
        </div>
      )}
    </div>
  )
}
