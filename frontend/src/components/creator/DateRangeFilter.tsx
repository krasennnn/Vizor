import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { DateRangeButton } from "@/components/filters/DateRangeButton"

type Props = {
  startDate?: Date
  endDate?: Date
  onDateChange: (range: { startDate?: Date; endDate?: Date }) => void
  onClear: () => void
}

export function DateRangeFilter({ startDate, endDate, onDateChange, onClear }: Props) {
  return (
    <Card>
      <CardHeader>
        <CardTitle>Filter by Date Range</CardTitle>
        <CardDescription>Select a date range to filter analytics data</CardDescription>
      </CardHeader>
      <CardContent>
        <div className="flex items-center gap-4">
          <DateRangeButton
            startDate={startDate}
            endDate={endDate}
            onChange={onDateChange}
          />
          {(startDate || endDate) && (
            <Button
              variant="outline"
              size="sm"
              onClick={onClear}
            >
              Clear Filter
            </Button>
          )}
        </div>
      </CardContent>
    </Card>
  )
}
