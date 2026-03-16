import { DatePicker } from "@/components/ui/DatePicker"

type Props = {
  startDate?: Date
  endDate?: Date
  onChange: (range: { startDate?: Date; endDate?: Date }) => void
}

export function DateRangeFilter({ startDate, endDate, onChange }: Props) {
  return (
    <div className="flex items-center gap-2">
      <DatePicker
        label="From"
        value={startDate}
        onChange={((d?: Date) => onChange({ startDate: d ?? undefined, endDate })) as any}
      />
      <DatePicker
        label="To"
        value={endDate}
        onChange={((d?: Date) => onChange({ startDate, endDate: d ?? undefined })) as any}
      />
    </div>
  )
}


