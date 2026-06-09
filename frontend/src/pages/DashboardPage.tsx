import { useEffect } from 'react'
import { useAuthStore } from '@/store/authStore'
import { useApplicationStore } from '@/store/applicationStore'
import { useJobStore } from '@/store/jobStore'
import { Briefcase, Clock, TrendingUp } from 'lucide-react'
import { formatDate, formatWorkMode } from '@/lib/utils'

export default function DashboardPage() {
  const { user } = useAuthStore()
  const { stats, recentApplications, fetchStats, fetchRecent } = useApplicationStore()
  const { recentJobs, fetchRecent: fetchRecentJobs } = useJobStore()

  useEffect(() => {
    if (user) {
      fetchStats(user.id)
      fetchRecent(user.id)
      fetchRecentJobs()
    }
  }, [user, fetchStats, fetchRecent, fetchRecentJobs])

  return (
    <div className="space-y-8">
      <div>
        <h2 className="text-2xl font-bold text-gray-900">Dashboard</h2>
        <p className="text-gray-500 mt-1">Your application overview</p>
      </div>

      {/* Stats Cards */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <div className="bg-white rounded-xl border border-gray-200 p-6">
          <div className="flex items-center gap-3">
            <div className="p-2 bg-blue-100 rounded-lg">
              <Briefcase size={20} className="text-brand-600" />
            </div>
            <div>
              <p className="text-2xl font-bold">{stats?.totalApplications ?? 0}</p>
              <p className="text-sm text-gray-500">Total Applications</p>
            </div>
          </div>
        </div>

        <div className="bg-white rounded-xl border border-gray-200 p-6">
          <div className="flex items-center gap-3">
            <div className="p-2 bg-green-100 rounded-lg">
              <Clock size={20} className="text-green-600" />
            </div>
            <div>
              <p className="text-2xl font-bold">{stats?.last24Hours ?? 0}</p>
              <p className="text-sm text-gray-500">Last 24 Hours</p>
            </div>
          </div>
        </div>

        <div className="bg-white rounded-xl border border-gray-200 p-6">
          <div className="flex items-center gap-3">
            <div className="p-2 bg-purple-100 rounded-lg">
              <TrendingUp size={20} className="text-purple-600" />
            </div>
            <div>
              <p className="text-2xl font-bold">{recentJobs.length}</p>
              <p className="text-sm text-gray-500">New Postings Today</p>
            </div>
          </div>
        </div>
      </div>

      {/* Recent Applications */}
      <div className="bg-white rounded-xl border border-gray-200">
        <div className="p-6 border-b border-gray-200">
          <h3 className="text-lg font-semibold">Recent Applications (Last 24h)</h3>
        </div>
        <div className="divide-y divide-gray-100">
          {recentApplications.length === 0 ? (
            <div className="p-6 text-center text-gray-400">No applications in the last 24 hours</div>
          ) : (
            recentApplications.map((app) => (
              <div key={app.id} className="p-4 flex items-center justify-between">
                <div>
                  <p className="font-medium text-gray-900">{app.job.title}</p>
                  <p className="text-sm text-gray-500">{app.job.company} - {app.job.location}</p>
                  <div className="flex gap-2 mt-1">
                    <span className="text-xs px-2 py-0.5 bg-gray-100 rounded-full">
                      {formatWorkMode(app.job.workMode)}
                    </span>
                    <span className="text-xs px-2 py-0.5 bg-blue-50 text-blue-700 rounded-full">
                      {app.applicationMode === 'EASY_APPLY' ? 'Easy Apply' : 'External'}
                    </span>
                  </div>
                </div>
                <div className="text-right">
                  <span className={`text-xs px-2 py-1 rounded-full ${
                    app.status === 'SUBMITTED' ? 'bg-green-100 text-green-700' :
                    app.status === 'PENDING_APPROVAL' ? 'bg-yellow-100 text-yellow-700' :
                    app.status === 'FAILED' ? 'bg-red-100 text-red-700' :
                    'bg-gray-100 text-gray-700'
                  }`}>
                    {app.status.replace('_', ' ')}
                  </span>
                  {app.submittedAt && (
                    <p className="text-xs text-gray-400 mt-1">{formatDate(app.submittedAt)}</p>
                  )}
                </div>
              </div>
            ))
          )}
        </div>
      </div>

      {/* New Job Postings Today */}
      <div className="bg-white rounded-xl border border-gray-200">
        <div className="p-6 border-b border-gray-200">
          <h3 className="text-lg font-semibold">New Postings Today</h3>
        </div>
        <div className="divide-y divide-gray-100">
          {recentJobs.length === 0 ? (
            <div className="p-6 text-center text-gray-400">No new postings discovered today</div>
          ) : (
            recentJobs.slice(0, 10).map((job) => (
              <div key={job.externalId} className="p-4 flex items-center justify-between">
                <div>
                  <p className="font-medium text-gray-900">{job.title}</p>
                  <p className="text-sm text-gray-500">{job.company} - {job.location}</p>
                  <span className="text-xs px-2 py-0.5 bg-gray-100 rounded-full">
                    {formatWorkMode(job.workMode)}
                  </span>
                </div>
                {job.match && (
                  <div className={`text-sm font-bold ${
                    job.match.matchPercentage >= 80 ? 'text-match-high' :
                    job.match.matchPercentage >= 60 ? 'text-match-medium' :
                    'text-match-low'
                  }`}>
                    {job.match.matchPercentage}% match
                  </div>
                )}
              </div>
            ))
          )}
        </div>
      </div>
    </div>
  )
}
