-- Add deleted_at column to contract table for soft delete support
ALTER TABLE contract
ADD COLUMN deleted_at TIMESTAMP WITH TIME ZONE;

-- Add deleted_at column to campaign table for soft delete support
ALTER TABLE campaign
ADD COLUMN deleted_at TIMESTAMP WITH TIME ZONE;

-- Create index on deleted_at for better query performance
CREATE INDEX idx_contract_deleted_at ON contract(deleted_at) WHERE deleted_at IS NULL;
CREATE INDEX idx_campaign_deleted_at ON campaign(deleted_at) WHERE campaign.deleted_at IS NULL;

