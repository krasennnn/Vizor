import { useState } from "react"
import { useForm } from "react-hook-form"
import { zodResolver } from "@hookform/resolvers/zod"
import { z } from "zod"
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog"
import { Button } from "@/components/ui/button"
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from "@/components/ui/form"
import { Input } from "@/components/ui/input"
import { syncAccount } from "@/api/accounts"
import type { AccountResponse, AccountSyncRequest } from "@/types/Account"

const accountSchema = z.object({
  platformUserId: z.string().min(1, "Platform User ID is required"),
  platformUsername: z.string().min(1, "Platform Username is required"),
  profileLink: z.string().url("Must be a valid URL").optional().or(z.literal("")),
  displayName: z.string().optional(),
  isActive: z.boolean(),
})

type AccountFormValues = z.infer<typeof accountSchema>

interface CreateAccountDialogProps {
  onCreated?: (account: AccountResponse) => void
  trigger?: React.ReactNode
}

export function CreateAccountDialog({ onCreated, trigger }: CreateAccountDialogProps) {
  const [open, setOpen] = useState(false)
  const form = useForm<AccountFormValues>({
    resolver: zodResolver(accountSchema),
    defaultValues: {
      platformUserId: "",
      platformUsername: "",
      profileLink: "",
      displayName: "",
      isActive: true,
    },
  })

  async function handleSubmit(values: AccountFormValues) {
    const request: AccountSyncRequest = {
      platformUserId: values.platformUserId,
      platformUsername: values.platformUsername,
      profileLink: values.profileLink || undefined,
      displayName: values.displayName || undefined,
      isActive: values.isActive,
    }

    try {
      const response = await syncAccount(request)
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
        {trigger || <Button>Add Account</Button>}
      </DialogTrigger>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Add TikTok Account</DialogTitle>
        </DialogHeader>
        <Form {...form}>
          <form onSubmit={form.handleSubmit(handleSubmit)} className="space-y-4">
            <FormField
              control={form.control}
              name="platformUserId"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Platform User ID *</FormLabel>
                  <FormControl>
                    <Input placeholder="Enter TikTok user ID" {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <FormField
              control={form.control}
              name="platformUsername"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Platform Username *</FormLabel>
                  <FormControl>
                    <Input placeholder="@username" {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <FormField
              control={form.control}
              name="displayName"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Display Name</FormLabel>
                  <FormControl>
                    <Input placeholder="Optional display name" {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <FormField
              control={form.control}
              name="profileLink"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Profile Link</FormLabel>
                  <FormControl>
                    <Input placeholder="https://tiktok.com/@username" {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <div className="flex justify-end gap-2">
              <Button type="button" variant="outline" onClick={() => setOpen(false)}>
                Cancel
              </Button>
              <Button type="submit">Add Account</Button>
            </div>
          </form>
        </Form>
      </DialogContent>
    </Dialog>
  )
}
