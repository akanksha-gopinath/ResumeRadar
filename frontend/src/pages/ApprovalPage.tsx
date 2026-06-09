import { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useAuthStore } from '@/store/authStore'
import { useApplicationStore } from '@/store/applicationStore'
import { RefreshCw, Check, ExternalLink, Copy } from 'lucide-react'

type Stage = 'generating' | 'review' | 'confirming' | 'submitted' | 'assisted'

export default function ApprovalPage() {
  const { jobId } = useParams<{ jobId: string }>()
  const { user } = useAuthStore()
  const {
    currentCoverLetter, currentApplicationId, loading,
    generateLetter, approveLetter, regenerateLetter, confirmApply,
  } = useApplicationStore()
  const navigate = useNavigate()

  const [stage, setStage] = useState<Stage>('generating')
  const [applyUrl, setApplyUrl] = useState('')
  const [coverLetterText, setCoverLetterText] = useState('')

  useEffect(() => {
    if (!user || !jobId) return
    generateLetter(Number(jobId), user.id).then((letter) => {
      setCoverLetterText(letter.content)
      setStage('review')
    })
  }, [user, jobId, generateLetter])

  const handleRegenerate = async () => {
    if (!user || !jobId) return
    setStage('generating')
    const letter = await regenerateLetter(Number(jobId), user.id)
    setCoverLetterText(letter.content)
    setStage('review')
  }

  const handleApprove = async () => {
    if (!currentCoverLetter) return
    const applicationId = await approveLetter(currentCoverLetter.coverLetterId)
    setStage('confirming')
  }

  const handleConfirmApply = async () => {
    if (!currentApplicationId) return
    const result = await confirmApply(currentApplicationId)

    if (result.applicationMode === 'EXTERNAL_ASSISTED') {
      setApplyUrl(result.applyUrl)
      setCoverLetterText(result.coverLetter)
      setStage('assisted')
    } else {
      setStage('submitted')
    }
  }

  const handleCopyLetter = () => {
    navigator.clipboard.writeText(coverLetterText)
  }

  const handleOpenCareerPage = () => {
    window.open(applyUrl, '_blank')
  }

  if (stage === 'generating' || loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="text-center">
          <RefreshCw size={32} className="mx-auto text-brand-500 animate-spin mb-4" />
          <p className="text-gray-600">Generating your custom cover letter...</p>
        </div>
      </div>
    )
  }

  if (stage === 'submitted') {
    return (
      <div className="max-w-2xl mx-auto text-center py-16">
        <div className="p-4 bg-green-100 rounded-full inline-block mb-6">
          <Check size={40} className="text-green-600" />
        </div>
        <h2 className="text-2xl font-bold text-gray-900 mb-2">Application Submitted!</h2>
        <p className="text-gray-500 mb-8">Your application has been submitted via Easy Apply.</p>
        <button
          onClick={() => navigate('/dashboard')}
          className="px-6 py-2.5 bg-brand-600 text-white rounded-lg font-medium hover:bg-brand-700"
        >
          Back to Dashboard
        </button>
      </div>
    )
  }

  if (stage === 'assisted') {
    return (
      <div className="max-w-2xl mx-auto space-y-6">
        <div className="bg-yellow-50 border border-yellow-200 rounded-xl p-6">
          <h2 className="text-xl font-bold text-gray-900 mb-2">External Application</h2>
          <p className="text-gray-600 mb-4">
            This job requires applying on the company's career page. We've prepared your cover letter — copy it and complete the application manually.
          </p>

          <div className="flex gap-3">
            <button
              onClick={handleCopyLetter}
              className="flex items-center gap-2 px-4 py-2 bg-white border border-gray-300 rounded-lg text-sm font-medium hover:bg-gray-50"
            >
              <Copy size={16} />
              Copy Cover Letter
            </button>
            <button
              onClick={handleOpenCareerPage}
              className="flex items-center gap-2 px-4 py-2 bg-brand-600 text-white rounded-lg text-sm font-medium hover:bg-brand-700"
            >
              <ExternalLink size={16} />
              Open Career Page
            </button>
          </div>
        </div>

        <div className="bg-white rounded-xl border border-gray-200 p-6">
          <h3 className="font-medium text-gray-700 mb-3">Your Cover Letter</h3>
          <pre className="text-sm text-gray-600 whitespace-pre-wrap bg-gray-50 p-4 rounded-lg">
            {coverLetterText}
          </pre>
        </div>

        <button
          onClick={() => navigate('/dashboard')}
          className="px-6 py-2.5 bg-green-600 text-white rounded-lg font-medium hover:bg-green-700"
        >
          I've Submitted — Mark as Complete
        </button>
      </div>
    )
  }

  return (
    <div className="max-w-2xl mx-auto space-y-6">
      {/* Stepper */}
      <div className="flex items-center gap-2 text-sm">
        <span className="px-3 py-1 bg-green-100 text-green-700 rounded-full font-medium">1. Match Verified</span>
        <span className="text-gray-300">→</span>
        <span className={`px-3 py-1 rounded-full font-medium ${
          stage === 'review' ? 'bg-brand-100 text-brand-700' : 'bg-green-100 text-green-700'
        }`}>2. Review Letter</span>
        <span className="text-gray-300">→</span>
        <span className={`px-3 py-1 rounded-full font-medium ${
          stage === 'confirming' ? 'bg-brand-100 text-brand-700' : 'bg-gray-100 text-gray-500'
        }`}>3. Confirm Apply</span>
      </div>

      {stage === 'review' && (
        <>
          <div className="bg-white rounded-xl border border-gray-200 p-6">
            <h3 className="font-semibold text-gray-900 mb-4">Generated Cover Letter</h3>
            <textarea
              value={coverLetterText}
              onChange={(e) => setCoverLetterText(e.target.value)}
              className="w-full h-64 p-4 border border-gray-200 rounded-lg text-sm text-gray-700 resize-none focus:ring-2 focus:ring-brand-500 focus:border-transparent"
            />
          </div>

          <div className="flex gap-3">
            <button
              onClick={handleRegenerate}
              className="flex items-center gap-2 px-4 py-2.5 border border-gray-300 rounded-lg text-sm font-medium hover:bg-gray-50"
            >
              <RefreshCw size={16} />
              Regenerate
            </button>
            <button
              onClick={handleApprove}
              className="flex items-center gap-2 px-6 py-2.5 bg-brand-600 text-white rounded-lg font-medium hover:bg-brand-700"
            >
              <Check size={16} />
              Approve Letter
            </button>
          </div>
        </>
      )}

      {stage === 'confirming' && (
        <div className="bg-white rounded-xl border border-gray-200 p-8 text-center">
          <h3 className="text-xl font-bold text-gray-900 mb-3">Ready to Apply?</h3>
          <p className="text-gray-500 mb-6">
            Your cover letter is approved. Click below to submit your application.
          </p>
          <div className="flex justify-center gap-3">
            <button
              onClick={() => setStage('review')}
              className="px-4 py-2.5 border border-gray-300 rounded-lg text-sm font-medium hover:bg-gray-50"
            >
              Go Back
            </button>
            <button
              onClick={handleConfirmApply}
              className="px-8 py-2.5 bg-green-600 text-white rounded-lg font-medium hover:bg-green-700 text-lg"
            >
              Apply Now
            </button>
          </div>
        </div>
      )}
    </div>
  )
}
