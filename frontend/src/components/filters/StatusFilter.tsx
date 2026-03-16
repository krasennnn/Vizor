import { Button } from "@/components/ui/button"
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger } from "@/components/ui/dropdown-menu"

type Props = {
  value: string
  onChange: (val: string) => void
}

const statuses = ["All", "Active", "Upcoming", "Ended"]

export function StatusFilter({ value, onChange }: Props) {
  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <Button variant="outline" className="gap-2">
          + Status: {value}
        </Button>
      </DropdownMenuTrigger>
      <DropdownMenuContent align="start">
        {statuses.map((s) => (
          <DropdownMenuItem key={s} onClick={() => onChange(s)}>
            {s}
          </DropdownMenuItem>
        ))}
      </DropdownMenuContent>
    </DropdownMenu>
  )
}


