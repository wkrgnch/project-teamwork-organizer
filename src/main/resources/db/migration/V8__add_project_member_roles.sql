CREATE TABLE project_member_roles (
    project_id INT NOT NULL,
    user_id INT NOT NULL,
    role_id INT NOT NULL,

    CONSTRAINT pk_project_member_roles
        PRIMARY KEY (project_id, user_id, role_id),

    CONSTRAINT fk_project_member_roles_member
        FOREIGN KEY (project_id, user_id)
        REFERENCES project_members(project_id, user_id)
        ON DELETE CASCADE,

    CONSTRAINT fk_project_member_roles_role
        FOREIGN KEY (role_id)
        REFERENCES roles(id)
        ON DELETE RESTRICT
);

INSERT INTO project_member_roles (project_id, user_id, role_id)
SELECT project_id, user_id, role_id
FROM project_members
WHERE role_id IS NOT NULL;

ALTER TABLE project_members
DROP CONSTRAINT IF EXISTS fk_project_members_role;

ALTER TABLE project_members
DROP COLUMN role_id;