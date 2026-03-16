import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { Button } from "@/components/ui/button"
import { Link } from "react-router-dom"
import type { ContractResponse } from "@/types/Contract"
import { approveContract, rejectContract } from "@/api/contracts"
import { ContractStatusBadge } from "./ContractStatusBadge"
import { DeadlineBadge } from "./DeadlineBadge"
import type { ContractDisplay } from "@/utils/contracts"

type Props = {
  items: ContractDisplay[]
  onContractUpdated?: (contract: ContractResponse) => void
}

export function ContractsTable({ items, onContractUpdated }: Props) {
  async function handleApprove(contractId: number) {
    try {
      const updated = await approveContract(contractId)
      onContractUpdated?.(updated)
    } catch (err) {
      // Error is handled by the API function
    }
  }

  async function handleReject(contractId: number) {
    try {
      const updated = await rejectContract(contractId)
      onContractUpdated?.(updated)
    } catch (err) {
      // Error is handled by the API function
    }
  }

  return (
    <div className="rounded-md border">
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead>Sender</TableHead>
            <TableHead>Campaign</TableHead>
            <TableHead>Status</TableHead>
            <TableHead>Deadline</TableHead>
            <TableHead>Direction</TableHead>
            <TableHead className="text-right">Actions</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          {items.length === 0 ? (
            <TableRow>
              <TableCell colSpan={6} className="text-center text-muted-foreground py-6">No results.</TableCell>
            </TableRow>
          ) : (
            items.map((c) => (
              <TableRow key={c.id}>
                <TableCell className="font-medium">{c.organization}</TableCell>
                <TableCell>{c.campaign ?? "—"}</TableCell>
                <TableCell>
                  <ContractStatusBadge status={c.status} />
                </TableCell>
                <TableCell>
                  {c.deadlineAt ? (
                    <DeadlineBadge deadlineAt={c.deadlineAt} completedAt={c.completedAt} />
                  ) : (
                    <span className="text-sm text-muted-foreground">No deadline</span>
                  )}
                </TableCell>
                <TableCell>{c.direction}</TableCell>
                <TableCell className="text-right space-x-2">
                  {c.direction === "Received" && c.status === "Pending" ? (
                    <>
                      <Button size="sm" onClick={() => handleApprove(c.contractId)}>Accept</Button>
                      <Button size="sm" variant="secondary" onClick={() => handleReject(c.contractId)}>Reject</Button>
                    </>
                  ) : (
                    <Button size="sm" variant="outline" asChild>
                      <Link to={`/contracts/${c.contractId}`}>View</Link>
                    </Button>
                  )}
                </TableCell>
              </TableRow>
            ))
          )}
        </TableBody>
      </Table>
    </div>
  )
}


