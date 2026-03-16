import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { CheckCircle2, Clock } from "lucide-react"

type ContractWorkflowStep = "pending" | "accepted" | "active" | "completed"

type Props = {
  workflowStep: ContractWorkflowStep
  isCreator: boolean
}

export function ContractWorkflow({ workflowStep, isCreator }: Props) {
  return (
    <Card>
      <CardHeader>
        <CardTitle className="text-lg">Contract Status</CardTitle>
        <CardDescription>Track your contract progress</CardDescription>
      </CardHeader>
      <CardContent>
        <div className="space-y-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2">
              <div className={`w-8 h-8 rounded-full flex items-center justify-center ${
                workflowStep !== "pending" ? "bg-primary text-primary-foreground" : "bg-muted"
              }`}>
                {workflowStep !== "pending" ? (
                  <CheckCircle2 className="h-4 w-4" />
                ) : (
                  <Clock className="h-4 w-4" />
                )}
              </div>
              <span className={workflowStep !== "pending" ? "font-medium" : ""}>
                {isCreator ? "Application Sent" : "Invitation Sent"}
              </span>
            </div>
            {workflowStep === "pending" && (
              <Badge variant="outline">Current</Badge>
            )}
          </div>

          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2">
              <div className={`w-8 h-8 rounded-full flex items-center justify-center ${
                workflowStep === "accepted" || workflowStep === "active" || workflowStep === "completed"
                  ? "bg-primary text-primary-foreground" : "bg-muted"
              }`}>
                {workflowStep === "accepted" || workflowStep === "active" || workflowStep === "completed" ? (
                  <CheckCircle2 className="h-4 w-4" />
                ) : (
                  <Clock className="h-4 w-4" />
                )}
              </div>
              <span className={workflowStep === "accepted" || workflowStep === "active" || workflowStep === "completed" ? "font-medium" : ""}>
                {isCreator ? "Application Accepted" : "Invitation Accepted"}
              </span>
            </div>
            {workflowStep === "accepted" && (
              <Badge variant="outline">Current</Badge>
            )}
          </div>

          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2">
              <div className={`w-8 h-8 rounded-full flex items-center justify-center ${
                workflowStep === "active" || workflowStep === "completed"
                  ? "bg-primary text-primary-foreground" : "bg-muted"
              }`}>
                {workflowStep === "active" || workflowStep === "completed" ? (
                  <CheckCircle2 className="h-4 w-4" />
                ) : (
                  <Clock className="h-4 w-4" />
                )}
              </div>
              <span className={workflowStep === "active" || workflowStep === "completed" ? "font-medium" : ""}>
                Work in Progress
              </span>
            </div>
            {workflowStep === "active" && (
              <Badge variant="outline">Current</Badge>
            )}
          </div>

          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2">
              <div className={`w-8 h-8 rounded-full flex items-center justify-center ${
                workflowStep === "completed"
                  ? "bg-primary text-primary-foreground" : "bg-muted"
              }`}>
                {workflowStep === "completed" ? (
                  <CheckCircle2 className="h-4 w-4" />
                ) : (
                  <Clock className="h-4 w-4" />
                )}
              </div>
              <span className={workflowStep === "completed" ? "font-medium" : ""}>
                Completed
              </span>
            </div>
            {workflowStep === "completed" && (
              <Badge variant="outline">Current</Badge>
            )}
          </div>
        </div>
      </CardContent>
    </Card>
  )
}
