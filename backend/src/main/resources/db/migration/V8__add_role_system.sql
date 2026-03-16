-- V8: Add Role System

-- CREATE ROLE TABLE

CREATE TABLE public.role (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

-- CREATE USER_ROLE JUNCTION TABLE

CREATE TABLE public.user_role (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT user_role_user_fk 
        FOREIGN KEY (user_id) 
        REFERENCES public."user"(id) 
        ON DELETE CASCADE,
    CONSTRAINT user_role_role_fk 
        FOREIGN KEY (role_id) 
        REFERENCES public.role(id) 
        ON DELETE CASCADE
);

-- INDEXES FOR PERFORMANCE

CREATE INDEX idx_user_role_user_id 
    ON public.user_role USING btree (user_id);

CREATE INDEX idx_user_role_role_id 
    ON public.user_role USING btree (role_id);

-- SEED INITIAL ROLES

INSERT INTO public.role (name) VALUES 
    ('CREATOR'),
    ('OWNER');

-- MIGRATE EXISTING DATA

-- Migrate users with is_creator = true
INSERT INTO public.user_role (user_id, role_id)
SELECT u.id, r.id 
FROM public."user" u
CROSS JOIN public.role r
WHERE u.is_creator = true 
  AND r.name = 'CREATOR';

-- Migrate users with is_owner = true
INSERT INTO public.user_role (user_id, role_id)
SELECT u.id, r.id 
FROM public."user" u
CROSS JOIN public.role r
WHERE u.is_owner = true 
  AND r.name = 'OWNER';

-- REMOVE OLD CONSTRAINT

-- Drop the constraint that checks boolean flags
ALTER TABLE public."user" 
    DROP CONSTRAINT IF EXISTS user_has_role_check;

