export type WorkMode = 'REMOTE' | 'HYBRID' | 'ONSITE'
export type ApplicationMode = 'EASY_APPLY' | 'EXTERNAL_ASSISTED'
export type ApplicationStatus = 'DRAFT' | 'PENDING_APPROVAL' | 'SUBMITTED' | 'ASSISTED_PENDING' | 'ASSISTED_CONFIRMED' | 'FAILED'
export type ApprovalStage = 'SEARCH_RESULTS_PENDING' | 'MATCH_SCORED' | 'JOBS_SELECTED' | 'COVER_LETTER_PENDING' | 'COVER_LETTER_REVIEWED' | 'APPLY_CONFIRMED' | 'APPLIED'

export interface User {
  id: number
  email: string
  displayName: string
}

export interface Job {
  id?: number
  externalId: string
  title: string
  company: string
  location: string
  workMode: WorkMode
  description: string
  platform: string
  applicationMode: ApplicationMode
  postedAt: string
  applyUrl: string
}

export interface MatchResult {
  matchPercentage: number
  matchedSkills: string[]
  missingSkills: string[]
  summary: string
}

export interface JobWithMatch extends Job {
  match?: MatchResult
}

export interface Resume {
  id: number
  fileName: string
  parsedText: string
  skills: string[]
  uploadedAt: string
}

export interface CoverLetter {
  coverLetterId: number
  content: string
  generatedAt: string
}

export interface Application {
  id: number
  job: Job
  coverLetter?: { content: string }
  currentStage: ApprovalStage
  status: ApplicationStatus
  applicationMode: ApplicationMode
  createdAt: string
  submittedAt?: string
}

export interface SearchPreferences {
  id?: number
  jobTitle: string | null
  location: string | null
  remoteEnabled: boolean
  hybridEnabled: boolean
  onsiteEnabled: boolean
  updatedAt?: string
}

export interface DashboardStats {
  totalApplications: number
  last24Hours: number
}
