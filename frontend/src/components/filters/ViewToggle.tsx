import { Button } from "@/components/ui/button"
import { List, LayoutGrid } from "lucide-react"

type Props = {
  value: "table" | "cards"
  onChange: (val: "table" | "cards") => void
}

export function ViewToggle({ value, onChange }: Props) {
  return (
    <div className="inline-flex items-center rounded-md border p-1">
      <Button size="sm" variant={value === "table" ? "default" : "ghost"} onClick={() => onChange("table")} className="gap-2">
        <List className="h-4 w-4" />
        Table
      </Button>
      <Button size="sm" variant={value === "cards" ? "default" : "ghost"} onClick={() => onChange("cards")} className="gap-2">
        <LayoutGrid className="h-4 w-4" />
        Cards
      </Button>
    </div>
  )
}


