import { clsx, type ClassValue } from 'clsx'
import { twMerge } from 'tailwind-merge'

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs))
}

export function getMatchColor(percentage: number): string {
  if (percentage >= 80) return 'text-match-high'
  if (percentage >= 60) return 'text-match-medium'
  return 'text-match-low'
}

export function getMatchBgColor(percentage: number): string {
  if (percentage >= 80) return 'bg-green-100 border-green-300'
  if (percentage >= 60) return 'bg-yellow-50 border-yellow-300'
  return 'bg-red-50 border-red-300'
}

export function formatDate(dateStr: string): string {
  return new Date(dateStr).toLocaleDateString('en-US', {
    month: 'short',
    day: 'numeric',
    year: 'numeric',
  })
}

export function formatWorkMode(mode: string): string {
  return mode.charAt(0) + mode.slice(1).toLowerCase()
}
