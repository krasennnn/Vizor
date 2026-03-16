import * as React from "react"
import { Button } from "@/components/ui/button"
import { Calendar } from "@/components/ui/calendar"
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover"
import { Separator } from "@/components/ui/separator"

type Range = { startDate?: Date; endDate?: Date }

type Props = Range & {
  onChange: (range: Range) => void
}

type PresetType = "today" | "yesterday" | "last7days" | "last30days" | "custom"

export function DateRangeButton({ startDate, endDate, onChange }: Props) {
  const [open, setOpen] = React.useState(false)
  const [draft, setDraft] = React.useState<{ from?: Date; to?: Date }>({
    from: startDate,
    to: endDate,
  })
  const [selectedPreset, setSelectedPreset] = React.useState<PresetType>("custom")

  const today = React.useMemo(() => {
    const d = new Date()
    d.setHours(0, 0, 0, 0)
    return d
  }, [])

  React.useEffect(() => {
    setDraft({ from: startDate, to: endDate })
    
    // Determine which preset is selected based on current dates
    if (!startDate && !endDate) {
      setSelectedPreset("custom")
    } else {
      const from = startDate ? new Date(startDate) : null
      const to = endDate ? new Date(endDate) : null
      
      if (from && to) {
        from.setHours(0, 0, 0, 0)
        to.setHours(0, 0, 0, 0)
        const todayCopy = new Date(today)
        
        if (from.getTime() === todayCopy.getTime() && to.getTime() === todayCopy.getTime()) {
          setSelectedPreset("today")
        } else {
          const yesterday = new Date(todayCopy)
          yesterday.setDate(yesterday.getDate() - 1)
          if (from.getTime() === yesterday.getTime() && to.getTime() === yesterday.getTime()) {
            setSelectedPreset("yesterday")
          } else {
            const last7Start = new Date(todayCopy)
            last7Start.setDate(last7Start.getDate() - 6)
            if (from.getTime() === last7Start.getTime() && to.getTime() === todayCopy.getTime()) {
              setSelectedPreset("last7days")
            } else {
              const last30Start = new Date(todayCopy)
              last30Start.setDate(last30Start.getDate() - 29)
              if (from.getTime() === last30Start.getTime() && to.getTime() === todayCopy.getTime()) {
                setSelectedPreset("last30days")
              } else {
                setSelectedPreset("custom")
              }
            }
          }
        }
      } else {
        setSelectedPreset("custom")
      }
    }
  }, [startDate, endDate, today])

  function formatRange(r: { from?: Date; to?: Date }) {
    if (!r.from && !r.to) return "Select range"
    const fmt = (d?: Date) => (d ? d.toLocaleDateString() : "—")
    return `${fmt(r.from)} - ${fmt(r.to)}`
  }

  const applyPreset = (preset: PresetType) => {
    const todayCopy = new Date(today)
    let newFrom: Date | undefined
    let newTo: Date | undefined

    switch (preset) {
      case "today":
        newFrom = new Date(todayCopy)
        newTo = new Date(todayCopy)
        break
      case "yesterday":
        const yesterday = new Date(todayCopy)
        yesterday.setDate(yesterday.getDate() - 1)
        newFrom = yesterday
        newTo = yesterday
        break
      case "last7days":
        const last7Start = new Date(todayCopy)
        last7Start.setDate(last7Start.getDate() - 6)
        newFrom = last7Start
        newTo = new Date(todayCopy)
        break
      case "last30days":
        const last30Start = new Date(todayCopy)
        last30Start.setDate(last30Start.getDate() - 29)
        newFrom = last30Start
        newTo = new Date(todayCopy)
        break
      case "custom":
        // Don't change dates, just switch to custom mode
        setSelectedPreset("custom")
        return
    }

    setDraft({ from: newFrom, to: newTo })
    setSelectedPreset(preset)
  }

  const apply = () => {
    onChange({ startDate: draft.from, endDate: draft.to })
    setOpen(false)
  }

  const cancel = () => {
    setDraft({ from: startDate, to: endDate })
    // Reset preset based on current dates
    if (!startDate && !endDate) {
      setSelectedPreset("custom")
    } else {
      const from = startDate ? new Date(startDate) : null
      const to = endDate ? new Date(endDate) : null
      
      if (from && to) {
        from.setHours(0, 0, 0, 0)
        to.setHours(0, 0, 0, 0)
        const todayCopy = new Date(today)
        
        if (from.getTime() === todayCopy.getTime() && to.getTime() === todayCopy.getTime()) {
          setSelectedPreset("today")
        } else {
          const yesterday = new Date(todayCopy)
          yesterday.setDate(yesterday.getDate() - 1)
          if (from.getTime() === yesterday.getTime() && to.getTime() === yesterday.getTime()) {
            setSelectedPreset("yesterday")
          } else {
            const last7Start = new Date(todayCopy)
            last7Start.setDate(last7Start.getDate() - 6)
            if (from.getTime() === last7Start.getTime() && to.getTime() === todayCopy.getTime()) {
              setSelectedPreset("last7days")
            } else {
              const last30Start = new Date(todayCopy)
              last30Start.setDate(last30Start.getDate() - 29)
              if (from.getTime() === last30Start.getTime() && to.getTime() === todayCopy.getTime()) {
                setSelectedPreset("last30days")
              } else {
                setSelectedPreset("custom")
              }
            }
          }
        }
      } else {
        setSelectedPreset("custom")
      }
    }
    setOpen(false)
  }

  // Convert draft to DateRange format
  // Note: react-day-picker accepts partial ranges at runtime, but TypeScript requires both
  const selectedRange = draft.from !== undefined || draft.to !== undefined
    ? ({ from: draft.from, to: draft.to } as any)
    : undefined

  // Format preset labels
  const formatPresetLabel = (preset: PresetType): string => {
    switch (preset) {
      case "today":
        return `Today (${today.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })})`
      case "yesterday":
        const yesterday = new Date(today)
        yesterday.setDate(yesterday.getDate() - 1)
        return `Yesterday (${yesterday.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })})`
      case "last7days":
        const last7Start = new Date(today)
        last7Start.setDate(last7Start.getDate() - 6)
        return `Last 7 Days (${last7Start.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })} - ${today.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })})`
      case "last30days":
        const last30Start = new Date(today)
        last30Start.setDate(last30Start.getDate() - 29)
        return `Last 30 Days (${last30Start.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })} - ${today.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })})`
      case "custom":
        return "Custom Date Range"
      default:
        return "Custom Date Range"
    }
  }

  return (
    <Popover open={open} onOpenChange={setOpen}>
      <PopoverTrigger asChild>
        <Button variant="outline" className="justify-between gap-2 w-[280px]">
          {formatRange(draft)}
        </Button>
      </PopoverTrigger>
      <PopoverContent className="w-auto p-0" align="end">
        <div className="flex">
          {/* Preset Options - Left Side */}
          <div className="flex flex-col border-r p-4 min-w-[200px]">
            <div className="text-sm font-semibold mb-3">Preset Dates</div>
            <div className="flex flex-col gap-1">
              <button
                type="button"
                onClick={() => applyPreset("today")}
                className={`text-left px-3 py-2 rounded-md text-sm hover:bg-accent transition-colors ${
                  selectedPreset === "today" ? "bg-accent font-medium" : ""
                }`}
              >
                {formatPresetLabel("today")}
              </button>
              <button
                type="button"
                onClick={() => applyPreset("yesterday")}
                className={`text-left px-3 py-2 rounded-md text-sm hover:bg-accent transition-colors ${
                  selectedPreset === "yesterday" ? "bg-accent font-medium" : ""
                }`}
              >
                {formatPresetLabel("yesterday")}
              </button>
              <button
                type="button"
                onClick={() => applyPreset("last7days")}
                className={`text-left px-3 py-2 rounded-md text-sm hover:bg-accent transition-colors ${
                  selectedPreset === "last7days" ? "bg-accent font-medium" : ""
                }`}
              >
                {formatPresetLabel("last7days")}
              </button>
              <button
                type="button"
                onClick={() => applyPreset("last30days")}
                className={`text-left px-3 py-2 rounded-md text-sm hover:bg-accent transition-colors ${
                  selectedPreset === "last30days" ? "bg-accent font-medium" : ""
                }`}
              >
                {formatPresetLabel("last30days")}
              </button>
              <Separator className="my-2" />
              <button
                type="button"
                onClick={() => {
                  setSelectedPreset("custom")
                }}
                className={`text-left px-3 py-2 rounded-md text-sm hover:bg-accent transition-colors ${
                  selectedPreset === "custom" ? "bg-accent font-medium" : ""
                }`}
              >
                Custom Date Range
              </button>
            </div>
          </div>

          {/* Calendar - Right Side */}
          <div className="flex flex-col gap-3 p-4">
            <Calendar
              mode="range"
              selected={selectedRange}
              onSelect={(range) => {
                if (range) {
                  setDraft({ from: range.from, to: range.to })
                  setSelectedPreset("custom")
                } else {
                  setDraft({ from: undefined, to: undefined })
                }
              }}
              captionLayout="dropdown"
              numberOfMonths={2}
              showOutsideDays={false}
              disabled={(date) => {
                // Disable future dates
                const dateCopy = new Date(date)
                dateCopy.setHours(0, 0, 0, 0)
                return dateCopy > today
              }}
            />
            <div className="flex justify-end gap-2 border-t pt-3">
              <Button variant="ghost" onClick={cancel}>Cancel</Button>
              <Button onClick={apply}>Apply</Button>
            </div>
          </div>
        </div>
      </PopoverContent>
    </Popover>
  )
}


