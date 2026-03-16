import { useState, useEffect } from "react"
import { useParams } from "react-router-dom"
import { VideoCards } from "@/components/video/VideoCards"
import { CreateVideoDialog } from "@/components/video/VideoDialogCreate"
import { PageLayout } from "@/components/common/PageLayout"
import { Container } from "@/components/common/Container"
import { getAccount } from "@/api/accounts"
import { getVideosByAccount } from "@/api/videos"
import type { AccountResponse } from "@/types/Account"
import type { VideoResponse } from "@/types/Video"
import { useAuth } from "@/contexts/AuthContext"
import { isCreator } from "@/utils/roles"
import { ArrowLeft, Plus, ExternalLink } from "lucide-react"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { useNavigate } from "react-router-dom"

export function AccountDetailPage() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const { user } = useAuth()
  const [account, setAccount] = useState<AccountResponse | null>(null)
  const [videos, setVideos] = useState<VideoResponse[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const userIsCreator = isCreator(user)
  const canEdit = userIsCreator && account?.creatorId === user?.userId

  useEffect(() => {
    if (id) {
      loadData()
    }
  }, [id])

  async function loadData() {
    if (!id) return
    
    setLoading(true)
    setError(null)
    try {
      const [accountData, videosData] = await Promise.all([
        getAccount(Number(id)),
        getVideosByAccount(Number(id))
      ])
      setAccount(accountData)
      setVideos(videosData)
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to load account")
    } finally {
      setLoading(false)
    }
  }

  function handleVideoCreated(video: VideoResponse) {
    setVideos((prev) => [video, ...prev])
  }

  if (!account) {
    return (
      <PageLayout loading={loading} error={error} loadingMessage="Loading account...">
        <div></div>
      </PageLayout>
    )
  }

  return (
    <PageLayout loading={loading} error={error} loadingMessage="Loading account...">
      <div className="min-h-screen bg-gradient-to-b from-background to-muted/20">
        <div className="border-b bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60">
          <Container>
            <div className="py-8">
              <Button
                variant="ghost"
                onClick={() => navigate("/accounts")}
                className="mb-4 gap-2"
              >
                <ArrowLeft className="h-4 w-4" />
                Back to Accounts
              </Button>
              <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
                <div className="space-y-1">
                  <h1 className="text-3xl font-bold tracking-tight">
                    {account.displayName || account.platformUsername}
                  </h1>
                  <p className="text-muted-foreground">@{account.platformUsername}</p>
                </div>
                {canEdit && (
                  <CreateVideoDialog 
                    accountId={account.id}
                    onCreated={handleVideoCreated}
                    trigger={
                      <Button size="lg" className="gap-2 w-full sm:w-auto">
                        <Plus className="h-4 w-4" />
                        Add Video
                      </Button>
                    }
                  />
                )}
              </div>
            </div>
          </Container>
        </div>

        <Container className="py-8">
          <div className="space-y-8">
            <Card>
              <CardHeader>
                <CardTitle>Account Information</CardTitle>
                <CardDescription>Details about this TikTok account</CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div>
                    <p className="text-sm font-medium text-muted-foreground">Platform User ID</p>
                    <p className="text-sm">{account.platformUserId}</p>
                  </div>
                  <div>
                    <p className="text-sm font-medium text-muted-foreground">Status</p>
                    <Badge variant={account.isActive ? "default" : "secondary"}>
                      {account.isActive ? "Active" : "Inactive"}
                    </Badge>
                  </div>
                  {account.profileLink && (
                    <div>
                      <p className="text-sm font-medium text-muted-foreground">Profile Link</p>
                      <a 
                        href={account.profileLink} 
                        target="_blank" 
                        rel="noopener noreferrer"
                        className="flex items-center gap-1 text-primary hover:underline text-sm"
                      >
                        <ExternalLink className="h-3 w-3" />
                        View Profile
                      </a>
                    </div>
                  )}
                </div>
              </CardContent>
            </Card>

            <div>
              <h2 className="text-2xl font-bold mb-4">Videos</h2>
              <VideoCards 
                videos={videos} 
                accountId={account.id}
                onVideoCreated={handleVideoCreated}
                canEdit={canEdit}
              />
            </div>
          </div>
        </Container>
      </div>
    </PageLayout>
  )
}


