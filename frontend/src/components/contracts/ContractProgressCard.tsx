import { useEffect, useState } from "react"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Progress } from "@/components/ui/progress"
import { getVideosByContract } from "@/api/videos"
import type { VideoResponse } from "@/types/Video"
import type { ContractResponse } from "@/types/Contract"

type Props = {
  contract: ContractResponse
  isCreator: boolean
}

export function ContractProgressCard({ contract, isCreator }: Props) {
  const [videos, setVideos] = useState<VideoResponse[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    async function fetchVideos() {
      try {
        setLoading(true)
        const videosData = await getVideosByContract(contract.id)
        setVideos(videosData || [])
      } catch (err: any) {
        // Silently handle "not found" cases - contract may not have videos yet
        if (err?.message?.includes("404") || err?.message?.toLowerCase().includes("not found")) {
          setVideos([])
        } else {
          console.error("Failed to fetch videos:", err)
          setVideos([])
        }
      } finally {
        setLoading(false)
      }
    }

    fetchVideos()
  }, [contract.id])

  const postsDelivered = videos.length
  const expectedPosts = contract.expectedPosts
  const progress = expectedPosts > 0 
    ? Math.min(100, Math.round((postsDelivered / expectedPosts) * 100))
    : 0

  return (
    <Card>
      <CardHeader>
        <CardTitle className="text-lg">Progress</CardTitle>
        <CardDescription>Track your deliverables</CardDescription>
      </CardHeader>
      <CardContent className="space-y-4">
        <div className="space-y-2">
          <div className="flex items-center justify-between text-sm">
            <span className="text-muted-foreground">Posts Delivered</span>
            <span className="font-semibold">
              {loading ? "..." : `${postsDelivered} / ${expectedPosts}`}
            </span>
          </div>
          <Progress value={progress} className="h-2" />
        </div>
        <p className="text-sm text-muted-foreground">
          {isCreator 
            ? "Track your content creation progress here. Mark posts as delivered as you complete them."
            : "Monitor the creator's progress on delivering content for this campaign."}
        </p>
      </CardContent>
    </Card>
  )
}
