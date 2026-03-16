import { useState, useEffect } from "react"
import { useForm } from "react-hook-form"
import { zodResolver } from "@hookform/resolvers/zod"
import { z } from "zod"
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog"
import { Button } from "@/components/ui/button"
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage, FormDescription } from "@/components/ui/form"
import { Input } from "@/components/ui/input"
import { syncVideo } from "@/api/videos"
import type { VideoSyncRequest } from "@/types/Video"
import { getContracts } from "@/api/contracts"
import type { VideoResponse } from "@/types/Video"
import type { ContractResponse } from "@/types/Contract"
import { useAuth } from "@/contexts/AuthContext"
// Using native select for simplicity

const videoSchema = z.object({
  contractId: z.number().min(1, "Contract is required"),
  accountId: z.number().min(1, "Account is required"),
  platformVideoId: z.string().min(1, "Platform Video ID is required"),
  platformVideoLink: z.string().url("Must be a valid URL").optional().or(z.literal("")),
  title: z.string().optional(),
  description: z.string().optional(),
  location: z.string().optional(),
  duration: z.string().optional(),
  postedAt: z.string().optional(),
})

type VideoFormValues = z.infer<typeof videoSchema>

interface CreateVideoDialogProps {
  accountId: number
  onCreated?: (video: VideoResponse) => void
  trigger?: React.ReactNode
}

export function CreateVideoDialog({ accountId, onCreated, trigger }: CreateVideoDialogProps) {
  const { user } = useAuth()
  const [open, setOpen] = useState(false)
  const [contracts, setContracts] = useState<ContractResponse[]>([])
  const [loading, setLoading] = useState(false)

  const form = useForm<VideoFormValues>({
    resolver: zodResolver(videoSchema),
    defaultValues: {
      contractId: 0,
      accountId: accountId,
      platformVideoId: "",
      platformVideoLink: "",
      title: "",
      description: "",
      location: "",
      duration: "",
      postedAt: "",
    },
  })

  useEffect(() => {
    if (open) {
      loadContracts()
    }
  }, [open, user?.userId])

  async function loadContracts() {
    setLoading(true)
    try {
      const data = await getContracts()
      // Filter to only show active contracts where the current user is the creator
      // Active = approved, started, and not completed
      const activeCreatorContracts = user?.userId
        ? data.filter((contract) => 
            contract.creatorId === user.userId &&
            contract.approvedByOwner &&
            contract.startAt !== null &&
            contract.completedAt === null &&
            contract.deletedAt === null
          )
        : []
      setContracts(activeCreatorContracts)
    } catch (err) {
      // Error already handled in API
    } finally {
      setLoading(false)
    }
  }

  async function handleSubmit(values: VideoFormValues) {
    const request: VideoSyncRequest = {
      contractId: values.contractId,
      accountId: values.accountId,
      platformVideoId: values.platformVideoId,
      platformVideoLink: values.platformVideoLink || undefined,
      title: values.title || undefined,
      description: values.description || undefined,
      location: values.location || undefined,
      duration: values.duration || undefined,
      postedAt: values.postedAt || undefined,
    }

    try {
      const response = await syncVideo(request)
      setOpen(false)
      form.reset()
      onCreated?.(response)
    } catch (err) {
      throw err
    }
  }

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger asChild>
        {trigger || <Button>Add Video</Button>}
      </DialogTrigger>
      <DialogContent className="max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle>Add Video</DialogTitle>
        </DialogHeader>
        <Form {...form}>
          <form onSubmit={form.handleSubmit(handleSubmit)} className="space-y-4">
            <FormField
              control={form.control}
              name="contractId"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Contract *</FormLabel>
                  <FormControl>
                    <select
                      {...field}
                      value={field.value || ""}
                      onChange={(e) => field.onChange(Number(e.target.value))}
                      disabled={loading}
                      className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background file:border-0 file:bg-transparent file:text-sm file:font-medium placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
                    >
                      <option value="">Select a contract</option>
                      {contracts.map((contract) => (
                        <option key={contract.id} value={contract.id}>
                          Contract #{contract.id} - Campaign: {contract.campaign?.name || "Unknown"}
                        </option>
                      ))}
                    </select>
                  </FormControl>
                  <FormDescription>
                    Select the contract this video is associated with
                  </FormDescription>
                  <FormMessage />
                </FormItem>
              )}
            />
            <FormField
              control={form.control}
              name="platformVideoId"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Platform Video ID *</FormLabel>
                  <FormControl>
                    <Input placeholder="Enter TikTok video ID" {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <FormField
              control={form.control}
              name="platformVideoLink"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Video Link</FormLabel>
                  <FormControl>
                    <Input placeholder="https://tiktok.com/@username/video/..." {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <FormField
              control={form.control}
              name="title"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Title</FormLabel>
                  <FormControl>
                    <Input placeholder="Video title" {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <FormField
              control={form.control}
              name="description"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Description</FormLabel>
                  <FormControl>
                    <Input placeholder="Video description" {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <div className="flex justify-end gap-2">
              <Button type="button" variant="outline" onClick={() => setOpen(false)}>
                Cancel
              </Button>
              <Button type="submit" disabled={loading}>Add Video</Button>
            </div>
          </form>
        </Form>
      </DialogContent>
    </Dialog>
  )
}

