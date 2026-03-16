import { Link } from "react-router-dom"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import type { VideoResponse } from "@/types/Video"
import { CreateVideoDialog } from "./VideoDialogCreate"
import { Video, ExternalLink, Plus } from "lucide-react"
import { format } from "date-fns"

type Props = {
  videos: VideoResponse[]
  accountId?: number
  onVideoCreated?: (video: VideoResponse) => void
  canEdit?: boolean
}

export function VideoCards({ videos, accountId, onVideoCreated, canEdit = false }: Props) {
  if (videos.length === 0) {
    return (
      <div className="flex flex-col items-center justify-center py-16 text-center">
        <div className="rounded-full bg-muted p-6 mb-6">
          <Video className="h-16 w-16 text-muted-foreground" />
        </div>
        <h3 className="text-2xl font-semibold mb-3">No videos found</h3>
        <p className="text-muted-foreground mb-6 max-w-md">
          {canEdit 
            ? "You don't have any videos for this account yet. Add your first video to start tracking analytics and performance."
            : "No videos have been added to this account yet."}
        </p>
        {canEdit && accountId && (
          <CreateVideoDialog 
            accountId={accountId}
            onCreated={onVideoCreated}
            trigger={
              <Button size="lg" className="gap-2">
                <Plus className="h-5 w-5" />
                Add Video
              </Button>
            }
          />
        )}
      </div>
    )
  }

  return (
    <div className="grid gap-6 sm:grid-cols-2 xl:grid-cols-3">
      {videos.map((video) => (
        <Card key={video.id} className="hover:shadow-lg transition-shadow">
          <CardHeader>
            <div className="flex items-start justify-between">
              <div className="space-y-1 flex-1">
                <CardTitle className="text-lg line-clamp-2">
                  {video.title || "Untitled Video"}
                </CardTitle>
                <CardDescription>
                  {video.postedAt ? (() => {
                    try {
                      return format(new Date(video.postedAt), "MMM d, yyyy")
                    } catch {
                      return video.postedAt
                    }
                  })() : "No date"}
                </CardDescription>
              </div>
            </div>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              <div className="text-sm text-muted-foreground space-y-1">
                <p>Video ID: {video.platformVideoId}</p>
                {video.platformVideoLink && (
                  <a 
                    href={video.platformVideoLink} 
                    target="_blank" 
                    rel="noopener noreferrer"
                    className="flex items-center gap-1 text-primary hover:underline"
                  >
                    <ExternalLink className="h-3 w-3" />
                    View Video
                  </a>
                )}
              </div>
              <Button asChild variant="outline" className="w-full">
                <Link to={`/videos/${video.id}`}>
                  View Analytics
                </Link>
              </Button>
            </div>
          </CardContent>
        </Card>
      ))}
    </div>
  )
}

