import { Link } from "react-router-dom"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import type { AccountResponse } from "@/types/Account"
import { CreateAccountDialog } from "./AccountDialogCreate"
import { User, ExternalLink, Plus } from "lucide-react"

type Props = {
  accounts: AccountResponse[]
  onAccountCreated?: (account: AccountResponse) => void
}

export function AccountCards({ accounts, onAccountCreated }: Props) {
  if (accounts.length === 0) {
    return (
      <div className="flex flex-col items-center justify-center py-16 text-center">
        <div className="rounded-full bg-muted p-6 mb-6">
          <User className="h-16 w-16 text-muted-foreground" />
        </div>
        <h3 className="text-2xl font-semibold mb-3">No accounts connected</h3>
        <p className="text-muted-foreground mb-6 max-w-md">
          You don't have any TikTok accounts connected yet. Connect your first account to start managing your videos and analytics.
        </p>
        <CreateAccountDialog 
          onCreated={onAccountCreated}
          trigger={
            <Button size="lg" className="gap-2">
              <Plus className="h-5 w-5" />
              Connect Account
            </Button>
          }
        />
      </div>
    )
  }

  return (
    <div className="grid gap-6 sm:grid-cols-2 xl:grid-cols-3">
      {accounts.map((account) => (
        <Card key={account.id} className="hover:shadow-lg transition-shadow">
          <CardHeader>
            <div className="flex items-start justify-between">
              <div className="space-y-1">
                <CardTitle className="text-lg">{account.displayName || account.platformUsername}</CardTitle>
                <CardDescription>@{account.platformUsername}</CardDescription>
              </div>
              <Badge variant={account.isActive ? "default" : "secondary"}>
                {account.isActive ? "Active" : "Inactive"}
              </Badge>
            </div>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              <div className="text-sm text-muted-foreground">
                <p>Platform ID: {account.platformUserId}</p>
                {account.profileLink && (
                  <a 
                    href={account.profileLink} 
                    target="_blank" 
                    rel="noopener noreferrer"
                    className="flex items-center gap-1 text-primary hover:underline mt-2"
                  >
                    <ExternalLink className="h-3 w-3" />
                    View Profile
                  </a>
                )}
              </div>
              <Button asChild variant="outline" className="w-full">
                <Link to={`/accounts/${account.id}`}>
                  View Details
                </Link>
              </Button>
            </div>
          </CardContent>
        </Card>
      ))}
    </div>
  )
}

