CREATE TYPE project_methodology_type AS ENUM (
    'SCRUM',
    'KANBAN'
);

CREATE TYPE project_status_type AS ENUM (
    'PLANNED',
    'ACTIVE',
    'COMPLETED',
    'ARCHIVED'
);

CREATE TYPE project_stage_status_type AS ENUM (
    'PLANNED',
    'IN_PROGRESS',
    'COMPLETED',
    'DELAYED'
);

CREATE TYPE request_status_type AS ENUM (
    'NEW',
    'REVIEWED',
    'ACCEPTED',
    'IN_WORK',
    'ON_REVIEW',
    'COMPLETED',
    'REJECTED',
    'CANCELED'
);

CREATE TYPE task_priority_type AS ENUM (
    'LOW',
    'MEDIUM',
    'HIGH',
    'URGENT'
);

CREATE TYPE task_status_type AS ENUM (
    'TO_DO',
    'IN_PROGRESS',
    'NEEDS_CLARIFICATION',
    'ON_REVIEW',
    'DONE',
    'NEEDS_REVISION',
    'CLOSED'
);

CREATE TYPE task_comment_type AS ENUM (
    'COMMON',
    'PROGRESS',
    'CONTROL',
    'PROBLEM',
    'CLARIFICATION_REQUEST',
    'REVISION_COMMENT'
);

CREATE TYPE user_action_type AS ENUM (
    'LOGIN',
    'LOGOUT',
    'CREATE_PROJECT',
    'UPDATE_PROJECT',
    'PUBLISH_PROJECT',
    'HIDE_PROJECT',
    'CREATE_REQUEST',
    'UPDATE_REQUEST',
    'CREATE_TASK',
    'UPDATE_TASK',
    'CHANGE_TASK_STATUS',
    'ASSIGN_EXECUTOR',
    'ADD_COMMENT',
    'UPDATE_PROFILE',
    'CHANGE_PASSWORD'
);

CREATE TYPE action_object_type AS ENUM (
    'SYSTEM',
    'USER',
    'PROJECT',
    'REQUEST',
    'TASK',
    'COMMENT'
);