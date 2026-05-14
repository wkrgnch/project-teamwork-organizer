ALTER TABLE users
    ADD COLUMN system_role_id INT;

ALTER TABLE users
    ADD CONSTRAINT fk_users_system_role
        FOREIGN KEY (system_role_id)
            REFERENCES roles(id)
            ON DELETE RESTRICT;

UPDATE users u
SET system_role_id = ur.role_id
    FROM users_roles ur
JOIN roles r ON r.id = ur.role_id
WHERE ur.user_id = u.id
  AND r.type = 'GLOBAL_ADMIN';

DROP TABLE users_roles;

ALTER TABLE project_members
    ADD COLUMN role_id INT;

UPDATE project_members pm
SET role_id = r.id
    FROM roles r
WHERE r.type = pm.team_role;

ALTER TABLE project_members
    ALTER COLUMN role_id SET NOT NULL;

ALTER TABLE project_members
    ADD CONSTRAINT fk_project_members_role
        FOREIGN KEY (role_id)
            REFERENCES roles(id)
            ON DELETE RESTRICT;

ALTER TABLE project_members
DROP COLUMN team_role;