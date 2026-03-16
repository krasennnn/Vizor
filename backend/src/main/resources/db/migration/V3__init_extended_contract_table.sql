-- Drop the old redundant column
ALTER TABLE contract
DROP COLUMN IF EXISTS complete_is;

-- Add the new approval flag
ALTER TABLE contract
    ADD COLUMN approved_by_owner BOOLEAN DEFAULT FALSE;

-- Add the new start_at timestamp
ALTER TABLE contract
    ADD COLUMN start_at TIMESTAMPTZ NULL;