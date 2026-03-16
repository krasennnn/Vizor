"use client"

import { Alert, AlertTitle, AlertDescription } from "@/components/ui/alert"
import { AlertCircle, CheckCircle2, XIcon } from "lucide-react"
import { Button } from "@/components/ui/button"

interface AlertBannerProps {
  type: "success" | "error"
  title: string
  message?: string
  onClose?: () => void
}

export function AlertBanner({ type, title, message, onClose }: AlertBannerProps) {
  const isError = type === "error"

  return (
    <div className="fixed top-4 right-4 z-[100] animate-in fade-in slide-in-from-top-2 group">
      <Alert variant={isError ? "destructive" : "default"} className="shadow-lg w-96 relative">
        {isError ? (
          <AlertCircle className="h-4 w-4" />
        ) : (
          <CheckCircle2 className="h-4 w-4" />
        )}
        <AlertTitle>{title}</AlertTitle>
        {message && <AlertDescription>{message}</AlertDescription>}
        {onClose && (
          <Button
            variant="ghost"
            size="icon"
            className="absolute top-2 right-2 h-6 w-6 opacity-0 group-hover:opacity-100 transition-opacity"
            onClick={onClose}
          >
            <XIcon className="h-4 w-4" />
            <span className="sr-only">Close</span>
          </Button>
        )}
      </Alert>
    </div>
  )
}
