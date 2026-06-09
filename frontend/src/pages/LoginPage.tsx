import { Linkedin } from 'lucide-react'

export default function LoginPage() {
  const handleLogin = () => {
    window.location.href = '/api/auth/linkedin'
  }

  return (
    <div className="flex items-center justify-center min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100">
      <div className="bg-white rounded-2xl shadow-xl p-8 max-w-md w-full text-center">
        <h1 className="text-3xl font-bold text-gray-900 mb-2">ResumeRadar</h1>
        <p className="text-gray-500 mb-8">
          Smart job matching, automated applications, and AI-powered cover letters.
        </p>

        <button
          onClick={handleLogin}
          className="flex items-center justify-center gap-3 w-full px-6 py-3 bg-[#0A66C2] text-white rounded-lg font-medium hover:bg-[#004182] transition-colors"
        >
          <Linkedin size={20} />
          Sign in with LinkedIn
        </button>

        <p className="text-xs text-gray-400 mt-6">
          We'll search for jobs, score matches against your resume, and help you apply — with your approval at every step.
        </p>
      </div>
    </div>
  )
}
