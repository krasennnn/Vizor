-- ============================================
-- V14: Fix Video Analytics Cascade Delete
-- ============================================
-- Change foreign key constraint to CASCADE delete
-- When a video is deleted, its analytics should be automatically deleted too
-- ============================================

-- Drop the existing foreign key constraint
ALTER TABLE public.video_analytics
    DROP CONSTRAINT IF EXISTS video_analytics_video_fk;

-- Recreate the foreign key constraint with CASCADE delete
ALTER TABLE public.video_analytics
    ADD CONSTRAINT video_analytics_video_fk
        FOREIGN KEY (video_id)
            REFERENCES public.video(id)
            ON DELETE CASCADE;

