import { useState, useEffect } from "react"
import { AccountCards } from "@/components/account/AccountCards"
import { CreateAccountDialog } from "@/components/account/AccountDialogCreate"
import { PageLayout } from "@/components/common/PageLayout"
import { Container } from "@/components/common/Container"
import { getAccounts } from "@/api/accounts"
import type { AccountResponse } from "@/types/Account"
import { useAuth } from "@/contexts/AuthContext"
import { isCreator } from "@/utils/roles"
import { Plus } from "lucide-react"
import { Button } from "@/components/ui/button"
import { useNavigate } from "react-router-dom"

export function AccountsPage() {
  const { user } = useAuth()
  const navigate = useNavigate()
  const [accounts, setAccounts] = useState<AccountResponse[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const userIsCreator = isCreator(user)

  useEffect(() => {
    if (!userIsCreator) {
      navigate("/")
      return
    }
    // Load accounts even if empty - user can still create their first account
    loadAccounts()
  }, [userIsCreator, navigate])

  async function loadAccounts() {
    setLoading(true)
    setError(null)
    try {
      const data = await getAccounts()
      setAccounts(data)
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to load accounts")
    } finally {
      setLoading(false)
    }
  }

  function handleAccountCreated(account: AccountResponse) {
    setAccounts((prev) => [account, ...prev])
  }

  if (!userIsCreator) {
    return null
  }

  return (
    <PageLayout loading={loading} error={error} loadingMessage="Loading accounts...">
      <div className="min-h-screen bg-gradient-to-b from-background to-muted/20">
        <div className="border-b bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60">
          <Container>
            <div className="py-8">
              <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
                <div className="space-y-1">
                  <h1 className="text-3xl font-bold tracking-tight">My Accounts</h1>
                  <p className="text-muted-foreground">
                    Manage your TikTok accounts and their videos
                  </p>
                </div>
                <CreateAccountDialog 
                  onCreated={handleAccountCreated}
                  trigger={
                    <Button size="lg" className="gap-2 w-full sm:w-auto">
                      <Plus className="h-4 w-4" />
                      Add Account
                    </Button>
                  }
                />
              </div>
            </div>
          </Container>
        </div>

        <Container className="py-8">
          <AccountCards accounts={accounts} onAccountCreated={handleAccountCreated} />
        </Container>
      </div>
    </PageLayout>
  )
}

