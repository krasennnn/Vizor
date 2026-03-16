-- ============================================
-- V11: Add soft delete support to account table
-- ============================================

ALTER TABLE public.account
    ADD COLUMN deleted_at timestamp(6) with time zone;

