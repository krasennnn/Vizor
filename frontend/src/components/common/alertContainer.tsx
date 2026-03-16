import { useEffect, useState } from "react"
import { AlertBanner } from "./alertBanner"

declare global {
  interface Window {
    showAppAlert?: (type: "success" | "error", title: string, message?: string) => void
  }
}

export function AlertContainer() {
  const [alert, setAlert] = useState<{
    type: "success" | "error"
    title: string
    message?: string
  } | null>(null)

  // ✅ Actual alert trigger function
  function showAlert(type: "success" | "error", title: string, message?: string) {
    setAlert({ type, title, message })
    setTimeout(() => setAlert(null), 5000) // auto-dismiss after 5s
  }

  // ✅ Assign globally ONCE when component mounts
  useEffect(() => {
    window.showAppAlert = showAlert
  }, [])

  return alert ? (
    <AlertBanner 
      type={alert.type} 
      title={alert.title} 
      message={alert.message}
      onClose={() => setAlert(null)}
    />
  ) : null
}
