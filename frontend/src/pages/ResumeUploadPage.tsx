import { useState, useCallback } from 'react'
import { useAuthStore } from '@/store/authStore'
import { api } from '@/lib/api'
import type { Resume } from '@/types'
import { Upload, FileText, Check } from 'lucide-react'

export default function ResumeUploadPage() {
  const { user } = useAuthStore()
  const [resume, setResume] = useState<Resume | null>(null)
  const [uploading, setUploading] = useState(false)
  const [dragActive, setDragActive] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const loadActiveResume = useCallback(async () => {
    if (!user) return
    try {
      const active = await api.resumes.getActive(user.id)
      setResume(active)
    } catch {
      // No active resume
    }
  }, [user])

  useState(() => { loadActiveResume() })

  const handleUpload = async (file: File) => {
    if (!user) return
    if (file.type !== 'application/pdf') {
      setError('Only PDF files are supported')
      return
    }
    setUploading(true)
    setError(null)
    try {
      const result = await api.resumes.upload(user.id, file)
      setResume(result)
    } catch (e) {
      setError((e as Error).message)
    } finally {
      setUploading(false)
    }
  }

  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault()
    setDragActive(false)
    const file = e.dataTransfer.files[0]
    if (file) handleUpload(file)
  }

  const handleFileInput = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0]
    if (file) handleUpload(file)
  }

  return (
    <div className="space-y-6 max-w-3xl">
      <div>
        <h2 className="text-2xl font-bold text-gray-900">Resume</h2>
        <p className="text-gray-500 mt-1">Upload your resume for job matching and cover letter generation</p>
      </div>

      {/* Upload Zone */}
      <div
        onDragOver={(e) => { e.preventDefault(); setDragActive(true) }}
        onDragLeave={() => setDragActive(false)}
        onDrop={handleDrop}
        className={`border-2 border-dashed rounded-xl p-12 text-center transition-colors ${
          dragActive ? 'border-brand-500 bg-brand-50' : 'border-gray-300 bg-white'
        }`}
      >
        <Upload size={40} className="mx-auto text-gray-400 mb-4" />
        <p className="text-gray-600 mb-2">Drag & drop your PDF resume here</p>
        <p className="text-sm text-gray-400 mb-4">or</p>
        <label className="inline-block px-6 py-2.5 bg-brand-600 text-white rounded-lg font-medium cursor-pointer hover:bg-brand-700 transition-colors">
          Browse Files
          <input type="file" accept=".pdf" onChange={handleFileInput} className="hidden" />
        </label>
        {uploading && <p className="text-sm text-brand-600 mt-4">Uploading and parsing...</p>}
        {error && <p className="text-sm text-red-500 mt-4">{error}</p>}
      </div>

      {/* Current Resume */}
      {resume && (
        <div className="bg-white rounded-xl border border-gray-200 p-6">
          <div className="flex items-center gap-3 mb-4">
            <div className="p-2 bg-green-100 rounded-lg">
              <Check size={20} className="text-green-600" />
            </div>
            <div>
              <p className="font-medium text-gray-900">Active Resume</p>
              <p className="text-sm text-gray-500">{resume.fileName}</p>
            </div>
          </div>

          {resume.skills && resume.skills.length > 0 && (
            <div className="mb-4">
              <p className="text-sm font-medium text-gray-700 mb-2">Detected Skills</p>
              <div className="flex flex-wrap gap-2">
                {resume.skills.map((skill) => (
                  <span key={skill} className="text-xs px-3 py-1 bg-blue-50 text-blue-700 rounded-full">
                    {skill}
                  </span>
                ))}
              </div>
            </div>
          )}

          {resume.parsedText && (
            <div>
              <p className="text-sm font-medium text-gray-700 mb-2">Parsed Content Preview</p>
              <pre className="text-xs text-gray-500 bg-gray-50 p-4 rounded-lg max-h-64 overflow-y-auto whitespace-pre-wrap">
                {resume.parsedText.substring(0, 2000)}
                {resume.parsedText.length > 2000 && '...'}
              </pre>
            </div>
          )}
        </div>
      )}
    </div>
  )
}
