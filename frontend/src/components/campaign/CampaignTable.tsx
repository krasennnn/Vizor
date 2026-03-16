import { Button } from "@/components/ui/button"
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"
import { Link } from "react-router-dom"
import { MoreHorizontal } from "lucide-react"
import type { CampaignResponse } from "@/types/Campaign"
import { UpdateCampaignDialog } from "./CampaignDialogUpdate"
import { DeleteConfirmDialog } from "@/components/common/DeleteConfirmDialog"
import { CreateCampaignDialog } from "./CampaignDialogCreate"
import { useAuth } from "@/contexts/AuthContext"
import { deleteCampaign } from "@/api/campaigns"

type CampaignTableProps = {
  campaigns: CampaignResponse[]
  onCampaignUpdated: (updatedCampaign: CampaignResponse) => void
  onCampaignDeleted: (campaignId: number) => void
  onCampaignCreated?: (campaign: CampaignResponse) => void
}

export function CampaignTable({ 
  campaigns, 
  onCampaignUpdated, 
  onCampaignDeleted, 
  onCampaignCreated 
}: CampaignTableProps) {
  const { user } = useAuth()
  const isOwner = user?.roles?.includes("OWNER") ?? false

  async function handleDelete(campaignId: number) {
    try {
      await deleteCampaign(campaignId)
      onCampaignDeleted(campaignId) // Update local state immediately
    } catch (err) {
      // Error will be shown by the global error handler
      console.error("Failed to delete campaign:", err)
    }
  }

  if (campaigns.length === 0) {
    return (
      <div className="rounded-md border bg-background">
        <div className="text-center py-12 space-y-4">
          {isOwner ? (
            <>
              <p className="text-muted-foreground">You don't have any campaigns yet.</p>
              <CreateCampaignDialog onCreated={onCampaignCreated || (() => {})} />
            </>
          ) : (
            <p className="text-muted-foreground">No campaigns found</p>
          )}
        </div>
      </div>
    )
  }

  return (
    <div className="rounded-md border bg-background">
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead className="w-[100px]">Logo</TableHead>
            <TableHead>Campaign Name</TableHead>
            <TableHead>Active Contracts</TableHead>
            <TableHead className="text-right">Actions</TableHead>
          </TableRow>
        </TableHeader>

        <TableBody>
          {campaigns.map((campaign) => (
              <TableRow key={campaign.id}>
                <TableCell>
                  <img
                    src={"/vite.svg"}
                    alt={campaign.name}
                    className="h-10 w-10 rounded-md object-contain"
                  />
                </TableCell>
                <TableCell className="font-medium">{campaign.name}</TableCell>
                <TableCell>{0}</TableCell>
                <TableCell className="text-right">
                  <DropdownMenu>
                    <DropdownMenuTrigger asChild>
                      <Button variant="ghost" className="h-8 w-8 p-0">
                        <span className="sr-only">Open menu</span>
                        <MoreHorizontal className="h-4 w-4" />
                      </Button>
                    </DropdownMenuTrigger>
                    <DropdownMenuContent align="end">
                      <DropdownMenuLabel>Actions</DropdownMenuLabel>
                      <DropdownMenuItem asChild>
                        <Link to={`/campaign/${campaign.id}`}>
                          View Details
                        </Link>
                      </DropdownMenuItem>
                      <DropdownMenuSeparator />
                      <UpdateCampaignDialog 
                        campaign={campaign} 
                        onUpdated={onCampaignUpdated}
                        trigger={
                          <DropdownMenuItem onSelect={(e) => e.preventDefault()}>
                            Edit Campaign
                          </DropdownMenuItem>
                        }
                      />
                      <DeleteConfirmDialog
                        trigger={
                          <DropdownMenuItem 
                            onSelect={(e) => e.preventDefault()}
                            className="text-destructive focus:text-destructive"
                          >
                            Delete Campaign
                          </DropdownMenuItem>
                        }
                        onConfirm={() => handleDelete(campaign.id)}
                        title="Delete Campaign"
                        description={`Are you sure you want to delete "${campaign.name}"? This action cannot be undone.`}
                      />
                    </DropdownMenuContent>
                  </DropdownMenu>
                </TableCell>
              </TableRow>
            ))}
        </TableBody>
      </Table>
    </div>
  )
}