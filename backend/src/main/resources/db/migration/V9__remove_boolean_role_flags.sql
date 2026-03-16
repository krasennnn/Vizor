-- ============================================
-- V9: Remove Boolean Role Flags
-- ============================================
-- This migration removes the deprecated 
-- is_creator and is_owner boolean columns
-- after role system migration is complete
-- 
-- Note: User role validation is enforced at 
-- the application level, as PostgreSQL CHECK
-- constraints cannot use subqueries.
-- ============================================

-- ==========================================
-- REMOVE BOOLEAN COLUMNS
-- ==========================================

ALTER TABLE public."user" 
    DROP COLUMN IF EXISTS is_creator;

ALTER TABLE public."user" 
    DROP COLUMN IF EXISTS is_owner;

