-- ============================================
-- V2: Add Constraints and Indexes
-- ============================================
-- This migration adds:
-- - Foreign key relationships
-- - Unique constraints for data integrity
-- - Performance indexes
-- - Default values and NOT NULL constraints
-- ============================================

-- ==========================================
-- DATA CLEANUP (if needed)
-- ==========================================

-- Update any existing NULL values in is_complete to false
UPDATE public.contract
SET is_complete = false
WHERE is_complete IS NULL;


-- ==========================================
-- CONSTRAINTS
-- ==========================================

-- Contract: Foreign key to campaign
-- Ensures referential integrity
ALTER TABLE public.contract
    ADD CONSTRAINT contract_campaign_fk
        FOREIGN KEY (campaign_id)
            REFERENCES public.campaign(id)
            ON DELETE RESTRICT;


-- Contract: Set default value for is_complete
-- New contracts start as incomplete
ALTER TABLE public.contract
    ALTER COLUMN is_complete SET DEFAULT false;


-- Contract: Make is_complete NOT NULL
ALTER TABLE public.contract
    ALTER COLUMN is_complete SET NOT NULL;


-- ==========================================
-- INDEXES FOR PERFORMANCE
-- ==========================================

-- Campaign: Index on owner_id
-- Speeds up queries like findByOwnerId()
CREATE INDEX idx_campaign_owner_id
    ON public.campaign USING btree (owner_id);


-- Contract: Index on campaign_id
-- Speeds up foreign key lookups and queries
CREATE INDEX idx_contract_campaign_id
    ON public.contract USING btree (campaign_id);


-- Contract: Index on creator_id
-- If you query contracts by creator, this helps
CREATE INDEX idx_contract_creator_id
    ON public.contract USING btree (creator_id);


-- Contract: Index on is_complete
-- If you frequently filter by completion status
CREATE INDEX idx_contract_is_complete
    ON public.contract USING btree (is_complete);