import { z } from "zod"
import { useForm } from "react-hook-form"
import { zodResolver } from "@hookform/resolvers/zod"
import { useState } from "react"

import {
  Form,
  FormField,
  FormItem,
  FormLabel,
  FormControl,
  FormMessage,
  FormDescription,
} from "@/components/ui/form"
import { Input } from "@/components/ui/input"
import { Button } from "@/components/ui/button"

// Validation schema
const contractApplicationSchema = z.object({
  retainerCents: z
    .number()
    .min(0, "Retainer must be positive")
    .optional(),
  expectedPosts: z
    .number()
    .min(1, "Expected posts must be at least 1")
    .int("Expected posts must be a whole number"),
})

export type ContractApplicationFormValues = z.infer<typeof contractApplicationSchema>

interface ContractApplicationFormProps {
  defaultValues?: Partial<ContractApplicationFormValues>
  onSubmit: (values: ContractApplicationFormValues) => void
}

const DEFAULT_EXPECTED_POSTS = 30

export function ContractApplicationForm({ 
  defaultValues, 
  onSubmit 
}: ContractApplicationFormProps) {
  // Local state for retainer input (in dollars as string for better UX)
  const [retainerInput, setRetainerInput] = useState<string>("")
  
  const form = useForm<ContractApplicationFormValues>({
    resolver: zodResolver(contractApplicationSchema),
    defaultValues: {
      retainerCents: defaultValues?.retainerCents ?? undefined,
      expectedPosts: defaultValues?.expectedPosts ?? DEFAULT_EXPECTED_POSTS,
    },
  })

  const handleSubmit = (values: ContractApplicationFormValues) => {
    // Convert retainer input string to cents
    const retainerCents = retainerInput.trim() 
      ? Math.round(parseFloat(retainerInput) * 100) 
      : undefined
    
    onSubmit({
      ...values,
      retainerCents: retainerCents && retainerCents > 0 ? retainerCents : undefined,
    })
  }

  return (
    <Form {...form}>
      <form
        onSubmit={form.handleSubmit(handleSubmit)}
        className="space-y-6"
      >
        {/* Retainer (in dollars, converted to cents) */}
        <FormItem>
          <FormLabel>Asking Retainer (USD)</FormLabel>
          <FormControl>
            <Input
              type="text"
              inputMode="decimal"
              placeholder="0.00"
              value={retainerInput}
              onChange={(e) => {
                // Allow typing numbers and decimal point
                const value = e.target.value
                // Only allow numbers and one decimal point
                if (value === "" || /^\d*\.?\d*$/.test(value)) {
                  setRetainerInput(value)
                }
              }}
              onBlur={() => {
                // Validate and format on blur
                const num = parseFloat(retainerInput)
                if (isNaN(num) || num < 0) {
                  setRetainerInput("")
                } else {
                  setRetainerInput(num.toFixed(2))
                }
              }}
            />
          </FormControl>
          <FormDescription>
            Optional: The amount you're requesting for this contract
          </FormDescription>
        </FormItem>

        {/* Expected Posts */}
        <FormField
          control={form.control}
          name="expectedPosts"
          render={({ field }) => {
            const [localValue, setLocalValue] = useState<string>(
              field.value ? String(field.value) : String(DEFAULT_EXPECTED_POSTS)
            )

            return (
              <FormItem>
                <FormLabel>Expected Posts</FormLabel>
                <FormControl>
                  <Input
                    type="text"
                    inputMode="numeric"
                    placeholder={String(DEFAULT_EXPECTED_POSTS)}
                    value={localValue}
                    onChange={(e) => {
                      const value = e.target.value
                      // Allow empty or numbers only
                      if (value === "" || /^\d+$/.test(value)) {
                        setLocalValue(value)
                        if (value === "") {
                          // Don't update form field when empty, let it be handled on blur
                          return
                        }
                        const num = parseInt(value)
                        if (!isNaN(num) && num > 0) {
                          field.onChange(num)
                        }
                      }
                    }}
                    onBlur={() => {
                      // If empty or invalid, set to default
                      const num = parseInt(localValue)
                      if (isNaN(num) || num < 1) {
                        setLocalValue(String(DEFAULT_EXPECTED_POSTS))
                        field.onChange(DEFAULT_EXPECTED_POSTS)
                      } else {
                        setLocalValue(String(num))
                        field.onChange(num)
                      }
                    }}
                  />
                </FormControl>
                <FormDescription>
                  Number of posts you plan to create for this campaign (default: {DEFAULT_EXPECTED_POSTS})
                </FormDescription>
                <FormMessage />
              </FormItem>
            )
          }}
        />

        {/* Submit Button */}
        <Button type="submit" className="w-full">
          Send Application
        </Button>
      </form>
    </Form>
  )
}

