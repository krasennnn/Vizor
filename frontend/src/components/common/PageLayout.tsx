import type { ReactNode } from "react"
import { Loader2 } from "lucide-react"

type Props = {
  loading?: boolean
  error?: string | null
  loadingMessage?: string
  children: ReactNode
}

/**
 * Reusable page layout component with loading and error states
 * Implements Single Responsibility Principle - handles only layout and state rendering
 */
export function PageLayout({
  loading = false,
  error = null,
  loadingMessage = "Loading...",
  children,
}: Props) {
  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="flex flex-col items-center gap-4">
          <Loader2 className="h-8 w-8 animate-spin text-primary" />
          <p className="text-muted-foreground">{loadingMessage}</p>
        </div>
      </div>
    )
  }

  if (error) {
    return (
      <div className="min-h-screen flex items-center justify-center p-4">
        <div className="text-center space-y-4 max-w-md">
          <div className="text-destructive text-lg font-semibold">Error</div>
          <p className="text-muted-foreground">{error}</p>
        </div>
      </div>
    )
  }

  return <>{children}</>
}
