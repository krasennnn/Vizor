import { Card, CardContent } from "@/components/ui/card"
import { Button } from "@/components/ui/button"

type Props = {
  onComplete: () => void
  completing: boolean
}

export function ContractActionsCard({ onComplete, completing }: Props) {
  return (
    <Card>
      <CardContent className="pt-6">
        <div className="flex items-center justify-between">
          <div>
            <h3 className="font-semibold mb-1">Mark Contract as Complete</h3>
            <p className="text-sm text-muted-foreground">
              Once you've finished all deliverables, mark this contract as complete.
            </p>
          </div>
          <Button 
            onClick={onComplete} 
            disabled={completing}
            size="lg"
          >
            {completing ? "Completing..." : "Mark Complete"}
          </Button>
        </div>
      </CardContent>
    </Card>
  )
}
