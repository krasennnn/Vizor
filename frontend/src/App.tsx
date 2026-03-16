import { Navbar } from "./components/layout/Navbar"
import { BrowserRouter } from "react-router-dom"
import { AlertContainer } from "@/components/common/alertContainer"
import { AppRoutes } from "@/routes/AppRoutes"
import { AuthProvider } from "@/contexts/AuthContext"

function App() {
  return (
    <>
      <AlertContainer />
      <BrowserRouter>
        <AuthProvider>
          <Navbar />
          <AppRoutes />
        </AuthProvider>
      </BrowserRouter>
    </>
  )
}

export default App