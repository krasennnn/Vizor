import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { Button } from "@/components/ui/button"

type Invitation = {
  id: string
  brand: string
  role: string
  sentAt: string
  status: "Pending" | "Accepted" | "Declined"
}

const sampleInvitations: Invitation[] = [
  { id: "1", brand: "BijouBijou", role: "Affiliate", sentAt: "2025-10-01", status: "Pending" },
  { id: "2", brand: "Taily Collagen", role: "Manager", sentAt: "2025-10-10", status: "Pending" },
]

export function InvitationsTable() {
  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold">Invitations</h1>
      </div>
      <div className="rounded-md border">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Brand</TableHead>
              <TableHead>Role</TableHead>
              <TableHead>Sent</TableHead>
              <TableHead>Status</TableHead>
              <TableHead className="text-right">Actions</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {sampleInvitations.map((inv) => (
              <TableRow key={inv.id}>
                <TableCell>{inv.brand}</TableCell>
                <TableCell>{inv.role}</TableCell>
                <TableCell>{inv.sentAt}</TableCell>
                <TableCell>{inv.status}</TableCell>
                <TableCell className="text-right space-x-2">
                  <Button size="sm" variant="default">Accept</Button>
                  <Button size="sm" variant="secondary">Decline</Button>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </div>
    </div>
  )
}


