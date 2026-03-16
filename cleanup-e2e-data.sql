-- Cleanup script for E2E test data
-- run this with: cmd /c "type cleanup-e2e-data.sql | docker exec -i pg-vizor psql -U dev -d vizor"

-- 1. Delete video analytics first (child of videos)
DELETE FROM video_analytics 
WHERE video_id IN (
    SELECT id FROM video 
    WHERE contract_id IN (
        SELECT c.id FROM contract c
        INNER JOIN campaign camp ON c.campaign_id = camp.id
        WHERE camp.name LIKE 'Test Campaign%'
           OR c.creator_id IN (
               SELECT id FROM "user" WHERE email LIKE '%@cypress.test'
           )
    )
);

-- 2. Delete videos related to e2e test contracts
DELETE FROM video 
WHERE contract_id IN (
    SELECT c.id FROM contract c
    INNER JOIN campaign camp ON c.campaign_id = camp.id
    WHERE camp.name LIKE 'Test Campaign%'
       OR c.creator_id IN (
           SELECT id FROM "user" WHERE email LIKE '%@cypress.test'
       )
);

-- 3. Delete contracts related to e2e test users/campaigns
DELETE FROM contract 
WHERE campaign_id IN (
    SELECT id FROM campaign 
    WHERE name LIKE 'Test Campaign%'
       OR owner_id IN (
           SELECT id FROM "user" WHERE email LIKE '%@cypress.test'
       )
)
OR creator_id IN (
    SELECT id FROM "user" WHERE email LIKE '%@cypress.test'
);

-- 4. Delete campaigns created by e2e test users
DELETE FROM campaign 
WHERE name LIKE 'Test Campaign%'
   OR owner_id IN (
       SELECT id FROM "user" WHERE email LIKE '%@cypress.test'
   );

-- 5. Delete accounts related to e2e test users
DELETE FROM account 
WHERE creator_id IN (
    SELECT id FROM "user" WHERE email LIKE '%@cypress.test'
);

-- 6. Delete user_role relationships for e2e test users
DELETE FROM user_role 
WHERE user_id IN (
    SELECT id FROM "user" WHERE email LIKE '%@cypress.test'
);

-- 7. Finally, delete the e2e test users
DELETE FROM "user" 
WHERE email LIKE '%@cypress.test'
   OR username LIKE 'testuser%';

-- Verify deletion
SELECT 'Users deleted: ' || COUNT(*) FROM "user" WHERE email LIKE '%@cypress.test' OR username LIKE 'testuser%';
SELECT 'Campaigns deleted: ' || COUNT(*) FROM campaign WHERE name LIKE 'Test Campaign%';
SELECT 'Contracts deleted: ' || COUNT(*) FROM contract WHERE campaign_id IN (SELECT id FROM campaign WHERE name LIKE 'Test Campaign%');
