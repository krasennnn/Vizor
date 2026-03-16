import { z } from "zod"
import { useForm } from "react-hook-form"
import { zodResolver } from "@hookform/resolvers/zod"

import {
  Form,
  FormField,
  FormItem,
  FormLabel,
  FormControl,
  FormMessage,
} from "@/components/ui/form"
import { Input } from "@/components/ui/input"
import { Button } from "@/components/ui/button"
import { DatePicker } from "../ui/DatePicker"

// ✅ Validation schema
const campaignSchema = z.object({
  name: z.string().min(1, "Campaign name is required"),
  startDate: z.date(),
  endDate: z.date().optional(),
})

export type CampaignFormValues = z.infer<typeof campaignSchema>

interface CampaignFormProps {
  defaultValues?: Partial<CampaignFormValues>
  onSubmit: (values: CampaignFormValues) => void | Promise<void>
  isStartDateReadOnly?: boolean
}

export function CampaignForm({ defaultValues, onSubmit, isStartDateReadOnly = false }: CampaignFormProps) {
  const form = useForm<CampaignFormValues>({
    resolver: zodResolver(campaignSchema),
    defaultValues: {
      name: defaultValues?.name ?? "",
      startDate: defaultValues?.startDate ?? (() => { const d = new Date(); d.setHours(0,0,0,0); return d })(),
      endDate: defaultValues?.endDate ?? undefined,
    },
  })

  const handleFormSubmit = async (values: CampaignFormValues) => {
    try {
      await onSubmit(values)
    } catch (error) {
      // Let react-hook-form handle the error
      throw error
    }
  }

  return (
    <Form {...form}>
      <form
        onSubmit={form.handleSubmit(handleFormSubmit)}
        className="space-y-6"
      >
        {/* Campaign Name */}
        <FormField
          control={form.control}
          name="name"
          render={({ field }) => (
            <FormItem>
              <FormLabel>Campaign Name</FormLabel>
              <FormControl>
                <Input placeholder="e.g. Summer Launch" {...field} />
              </FormControl>
              <FormMessage />
            </FormItem>
          )}
        />

        <FormField
          control={form.control}
          name="startDate"
          render={({ field }) => (
            <FormItem>
              <FormLabel>Start Date</FormLabel>
              <FormControl>
                <DatePicker 
                  value={field.value} 
                  onChange={field.onChange}
                  disabled={isStartDateReadOnly}
                />
              </FormControl>
              {isStartDateReadOnly && (
                <p className="text-sm text-muted-foreground">
                  Start date cannot be changed after campaign creation
                </p>
              )}
              <FormMessage />
            </FormItem>
          )}
        />

        <FormField
          control={form.control}
          name="endDate"
          render={({ field }) => (
            <FormItem>
              <FormLabel>End Date</FormLabel>
              <FormControl>
                <DatePicker value={field.value} onChange={field.onChange} />
              </FormControl>
              <FormMessage />
            </FormItem>
          )}
        />
        

        {/* Submit Button */}
        <Button type="submit" className="w-full">
          {defaultValues ? "Update Campaign" : "Create Campaign"}
        </Button>
      </form>
    </Form>
  )
}
