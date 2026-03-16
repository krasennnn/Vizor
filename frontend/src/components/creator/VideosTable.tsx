import { useMemo } from "react"
import { useNavigate } from "react-router-dom"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { Calendar, ArrowUpDown, MessageSquare, Share2, Eye, Heart } from "lucide-react"
import { formatDate } from "@/utils/date"
import type { VideoWithAnalytics } from "@/types/VideoAnalytics"

type SortField = "date" | "views" | "likes" | "comments" | "shares" | "title"
type SortDirection = "asc" | "desc"

type Props = {
  videos: VideoWithAnalytics[]
  sortField: SortField
  sortDirection: SortDirection
  onSort: (field: SortField) => void
}

export function VideosTable({ videos, sortField, sortDirection, onSort }: Props) {
  const navigate = useNavigate()
  
  const sortedVideos = useMemo(() => {
    const sorted = [...videos]
    
    sorted.sort((a, b) => {
      let aValue: number | string = 0
      let bValue: number | string = 0
      
      switch (sortField) {
        case "date":
          aValue = a.video.postedAt ? new Date(a.video.postedAt).getTime() : 0
          bValue = b.video.postedAt ? new Date(b.video.postedAt).getTime() : 0
          break
        case "views":
          aValue = a.latestAnalytics?.viewsCount || 0
          bValue = b.latestAnalytics?.viewsCount || 0
          break
        case "likes":
          aValue = a.latestAnalytics?.likesCount || 0
          bValue = b.latestAnalytics?.likesCount || 0
          break
        case "comments":
          aValue = a.latestAnalytics?.commentsCount || 0
          bValue = b.latestAnalytics?.commentsCount || 0
          break
        case "shares":
          aValue = a.latestAnalytics?.sharesCount || 0
          bValue = b.latestAnalytics?.sharesCount || 0
          break
        case "title":
          aValue = a.video.title || ""
          bValue = b.video.title || ""
          break
      }
      
      if (typeof aValue === "number" && typeof bValue === "number") {
        return sortDirection === "asc" ? aValue - bValue : bValue - aValue
      } else {
        const comparison = String(aValue).localeCompare(String(bValue))
        return sortDirection === "asc" ? comparison : -comparison
      }
    })
    
    return sorted
  }, [videos, sortField, sortDirection])

  return (
    <Card>
      <CardHeader>
        <CardTitle>Top Performing Videos</CardTitle>
        <CardDescription>
          Videos sorted by {sortField} ({sortDirection === "desc" ? "descending" : "ascending"})
        </CardDescription>
      </CardHeader>
      <CardContent>
        {sortedVideos.length === 0 ? (
          <div className="text-center py-12 text-muted-foreground">
            No videos yet
          </div>
        ) : (
          <div className="rounded-lg border overflow-hidden">
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>
                    <button
                      onClick={() => onSort("title")}
                      className="flex items-center gap-1 hover:text-foreground transition-colors"
                    >
                      Title
                      <ArrowUpDown className="h-3 w-3" />
                    </button>
                  </TableHead>
                  <TableHead>
                    <button
                      onClick={() => onSort("date")}
                      className="flex items-center gap-1 hover:text-foreground transition-colors"
                    >
                      <Calendar className="h-3 w-3" />
                      Date
                      <ArrowUpDown className="h-3 w-3" />
                    </button>
                  </TableHead>
                  <TableHead>
                    <button
                      onClick={() => onSort("views")}
                      className="flex items-center gap-1 hover:text-foreground transition-colors"
                    >
                      <Eye className="h-3 w-3" />
                      Views
                      <ArrowUpDown className="h-3 w-3" />
                    </button>
                  </TableHead>
                  <TableHead>
                    <button
                      onClick={() => onSort("likes")}
                      className="flex items-center gap-1 hover:text-foreground transition-colors"
                    >
                      <Heart className="h-3 w-3" />
                      Likes
                      <ArrowUpDown className="h-3 w-3" />
                    </button>
                  </TableHead>
                  <TableHead>
                    <button
                      onClick={() => onSort("comments")}
                      className="flex items-center gap-1 hover:text-foreground transition-colors"
                    >
                      <MessageSquare className="h-3 w-3" />
                      Comments
                      <ArrowUpDown className="h-3 w-3" />
                    </button>
                  </TableHead>
                  <TableHead>
                    <button
                      onClick={() => onSort("shares")}
                      className="flex items-center gap-1 hover:text-foreground transition-colors"
                    >
                      <Share2 className="h-3 w-3" />
                      Shares
                      <ArrowUpDown className="h-3 w-3" />
                    </button>
                  </TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {sortedVideos.map((item) => (
                  <TableRow 
                    key={item.video.id}
                    className="cursor-pointer hover:bg-muted/50"
                    onClick={() => navigate(`/videos/${item.video.id}`)}
                  >
                    <TableCell className="font-medium">
                      {item.video.title || "Untitled Video"}
                    </TableCell>
                    <TableCell>
                      {item.video.postedAt ? (
                        <div className="flex items-center gap-2">
                          <Calendar className="h-4 w-4 text-muted-foreground" />
                          {formatDate(item.video.postedAt)}
                        </div>
                      ) : (
                        "No date"
                      )}
                    </TableCell>
                    <TableCell>
                      {item.latestAnalytics?.viewsCount?.toLocaleString() || "—"}
                    </TableCell>
                    <TableCell>
                      {item.latestAnalytics?.likesCount?.toLocaleString() || "—"}
                    </TableCell>
                    <TableCell>
                      {item.latestAnalytics?.commentsCount?.toLocaleString() || "—"}
                    </TableCell>
                    <TableCell>
                      {item.latestAnalytics?.sharesCount?.toLocaleString() || "—"}
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </div>
        )}
      </CardContent>
    </Card>
  )
}
