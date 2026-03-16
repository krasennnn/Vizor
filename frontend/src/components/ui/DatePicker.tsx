import * as React from "react"
import { ChevronDownIcon } from "lucide-react"

import { Button } from "@/components/ui/button"
import { Calendar } from "@/components/ui/calendar"
import { Label } from "@/components/ui/label"
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from "@/components/ui/popover"

type DatePickerProps = React.HTMLAttributes<HTMLDivElement> & {
  label?: string | undefined
  value?: Date | undefined
  onChange?: (date?: Date) => void
  disabled?: boolean
}

export function DatePicker({ label, value, onChange, disabled = false, ...rest }: DatePickerProps) {
  const [open, setOpen] = React.useState(false)
  const [localDate, setLocalDate] = React.useState<Date | undefined>(value)

  React.useEffect(() => {
    // normalize incoming value to start of day to avoid time mismatches
    if (value) {
      const nd = new Date(value)
      nd.setHours(0, 0, 0, 0)
      setLocalDate(nd)
    } else {
      setLocalDate(undefined)
    }
  }, [value])

  const selected = value ?? localDate

  return (
    <div className="flex flex-col gap-3" {...rest}>
      {label && (
        <Label htmlFor="date" className="px-1">
          {label}
        </Label>
      )}
      <Popover open={disabled ? false : open} onOpenChange={disabled ? undefined : setOpen}>
        <PopoverTrigger asChild>
          <Button
            variant="outline"
            id="date"
            className="w-48 justify-between font-normal"
            disabled={disabled}
          >
            {selected ? selected.toLocaleDateString() : "Select date"}
            <ChevronDownIcon />
          </Button>
        </PopoverTrigger>
        <PopoverContent className="w-auto overflow-hidden p-0" align="start">
          <Calendar
            mode="single"
            selected={selected}
            captionLayout="dropdown"
            onSelect={(date) => {
              // normalize to start of day so comparisons ignore time
              const normalize = (d?: Date) => {
                if (!d) return undefined
                const nd = new Date(d)
                nd.setHours(0, 0, 0, 0)
                return nd
              }

              const nd = normalize(date)
              if (onChange) {
                onChange(nd)
              } else {
                setLocalDate(nd)
              }
              setOpen(false)
            }}
          />
        </PopoverContent>
      </Popover>
    </div>
  )
}
