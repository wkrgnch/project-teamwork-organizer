CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- Пользователи: 150 записей.
CREATE TEMP TABLE temp_user_names (
    number INT,
    surname_ru VARCHAR(100),
    surname_login VARCHAR(100),
    first_name VARCHAR(100),
    patronymic VARCHAR(100)
);

INSERT INTO temp_user_names (number, surname_ru, surname_login, first_name, patronymic) VALUES
    (1,  'Иванов',    'ivanov',    'Иван',      'Сергеевич'),
    (2,  'Петров',    'petrov',    'Дмитрий',   'Алексеевич'),
    (3,  'Смирнов',   'smirnov',   'Артём',     'Игоревич'),
    (4,  'Кузнецов',  'kuznetsov', 'Максим',    'Олегович'),
    (5,  'Васильев',  'vasilev',   'Никита',    'Павлович'),
    (6,  'Иванова',   'ivanova',   'Анна',      'Сергеевна'),
    (7,  'Петрова',   'petrova',   'Мария',     'Алексеевна'),
    (8,  'Смирнова',  'smirnova',  'Екатерина', 'Игоревна'),
    (9,  'Кузнецова', 'kuznetsova','Дарья',     'Олеговна'),
    (10, 'Васильева', 'vasileva',  'Полина',    'Павловна'),
    (11, 'Соколова',  'sokolova',  'Алина',     'Дмитриевна'),
    (12, 'Морозов',   'morozov',   'Кирилл',    'Андреевич'),
    (13, 'Новикова',  'novikova',  'Виктория',  'Сергеевна'),
    (14, 'Фёдоров',   'fedorov',   'Роман',     'Владимирович'),
    (15, 'Орлова',    'orlova',    'Софья',     'Максимовна');

INSERT INTO users (
    id,
    full_name,
    email,
    username,
    password_hash,
    created_at,
    system_role_id,
    is_deleted,
    deleted_at
)
SELECT
    10000 + (name.number - 1) * 10 + user_number AS id,
    name.surname_ru || ' ' || name.first_name || ' ' || name.patronymic AS full_name,
    name.surname_login || user_number || '@example.local' AS email,
    name.surname_login || user_number AS username,
    crypt(name.surname_login || user_number, gen_salt('bf', 10)) AS password_hash,
    TIMESTAMP '2026-01-10 09:00:00' + ((name.number - 1) * 10 + user_number) * INTERVAL '3 hours' AS created_at,
    CASE
    WHEN name.number = 1 AND user_number = 1 THEN (SELECT id FROM roles WHERE type = 'GLOBAL_ADMIN')
    ELSE NULL
END AS system_role_id,
    FALSE,
    NULL
FROM temp_user_names name
CROSS JOIN generate_series(1, 10) AS user_number
ON CONFLICT (id) DO NOTHING;

-- Проекты: 40 записей.
INSERT INTO projects (
    id,
    name,
    description,
    public_description,
    methodology,
    status,
    start_date,
    end_date,
    is_public,
    created_by_user_id,
    created_at,
    updated_at
)
SELECT
    20000 + project_number,
    CASE
        WHEN project_number <= 20 THEN 'Разработка веб-сервиса для учебной команды ' || project_number
        ELSE 'Обновление внутреннего портала отдела ' || (project_number - 20)
        END,
    CASE
        WHEN project_number <= 20 THEN 'Проект включает разработку веб-приложения, настройку доступа, создание задач и проверку результата.'
        ELSE 'Проект включает обновление разделов портала, подготовку материалов и контроль выполненных работ.'
        END,
    CASE
        WHEN project_number % 3 = 0 THEN 'Проект открыт для просмотра общей информации о ходе работ.'
        ELSE NULL
        END,
    CASE
        WHEN project_number <= 20 THEN 'SCRUM'::project_methodology_type
        ELSE 'KANBAN'::project_methodology_type
        END,
    CASE
        WHEN project_number % 10 = 0 THEN 'ARCHIVED'::project_status_type
        WHEN project_number % 6 = 0 THEN 'COMPLETED'::project_status_type
        WHEN project_number % 4 = 0 THEN 'PLANNED'::project_status_type
        ELSE 'ACTIVE'::project_status_type
        END,
    DATE '2026-02-01' + project_number * 3,
    DATE '2026-06-01' + project_number * 3,
    project_number % 3 = 0,
    10001 + (((project_number - 1) * 9) % 150),
    TIMESTAMP '2026-02-01 10:00:00' + project_number * INTERVAL '1 day',
    TIMESTAMP '2026-02-10 10:00:00' + project_number * INTERVAL '1 day'
FROM generate_series(1, 40) AS project_number
ON CONFLICT (id) DO NOTHING;

-- Участники проектов: 360 записей, по 9 участников на проект.
CREATE TEMP TABLE temp_project_members AS
SELECT
    20000 + project_number AS project_id,
    10001 + (((project_number - 1) * 9 + member_position - 1) % 150) AS user_id,
    member_position
FROM generate_series(1, 40) AS project_number
    CROSS JOIN generate_series(1, 9) AS member_position;

INSERT INTO project_members (project_id, user_id, joined_at)
SELECT
    project_id,
    user_id,
    TIMESTAMP '2026-02-02 10:00:00' + (project_id - 20000) * INTERVAL '1 day' + member_position * INTERVAL '1 hour'
FROM temp_project_members
ON CONFLICT (project_id, user_id) DO NOTHING;

-- Роли участников проектов: 500 записей.
INSERT INTO project_member_roles (project_id, user_id, role_id)
SELECT
    member.project_id,
    member.user_id,
    role.id
FROM temp_project_members member
         JOIN roles role ON role.type = CASE
             WHEN member.member_position = 1 THEN 'PROJECT_ORGANIZATION_ADMIN'
             WHEN member.member_position = 2 THEN 'CONTROL_ADMIN'
             WHEN member.member_position = 3 THEN 'REQUEST_INITIATOR'
             ELSE 'WORK_EXECUTOR'
    END
    ON CONFLICT ON CONSTRAINT pk_project_member_roles DO NOTHING;

INSERT INTO project_member_roles (project_id, user_id, role_id)
SELECT
    member.project_id,
    member.user_id,
    role.id
FROM temp_project_members member
         JOIN roles role ON role.type = 'REQUEST_INITIATOR'
WHERE member.project_id <= 20035
  AND member.member_position BETWEEN 4 AND 7
    ON CONFLICT ON CONSTRAINT pk_project_member_roles DO NOTHING;

-- Этапы проектов: 200 записей, по 5 этапов на проект.
INSERT INTO project_stages (
    id,
    project_id,
    name,
    description,
    start_date,
    end_date,
    status,
    order_number
)
SELECT
    300000 + project_number * 10 + stage_number,
    20000 + project_number,
    CASE
        WHEN project_number <= 20 THEN
            CASE stage_number
                WHEN 1 THEN 'Подготовка бэклога'
                WHEN 2 THEN 'Планирование спринта'
                WHEN 3 THEN 'Выполнение задач'
                WHEN 4 THEN 'Проверка результата'
                ELSE 'Завершение спринта'
                END
        ELSE
            CASE stage_number
                WHEN 1 THEN 'Планирование работ'
                WHEN 2 THEN 'Подготовка задач'
                WHEN 3 THEN 'Выполнение работ'
                WHEN 4 THEN 'Проверка результата'
                ELSE 'Передача результата'
                END
        END,
    'Этап используется для разделения проекта на основные части работ.',
    DATE '2026-02-01' + project_number * 3 + (stage_number - 1) * 14,
    DATE '2026-02-01' + project_number * 3 + stage_number * 14,
    CASE
        WHEN stage_number < 3 THEN 'COMPLETED'::project_stage_status_type
        WHEN stage_number = 3 THEN 'IN_PROGRESS'::project_stage_status_type
        ELSE 'PLANNED'::project_stage_status_type
        END,
    stage_number
FROM generate_series(1, 40) AS project_number
         CROSS JOIN generate_series(1, 5) AS stage_number
    ON CONFLICT (id) DO NOTHING;

-- Спринты: 60 записей, по 3 спринта для каждого Scrum-проекта.
INSERT INTO sprints (id, project_id, name, start_date, end_date, goal)
SELECT
    400000 + project_number * 10 + sprint_number,
    20000 + project_number,
    'Спринт ' || sprint_number,
    DATE '2026-02-01' + project_number * 3 + (sprint_number - 1) * 14,
    DATE '2026-02-01' + project_number * 3 + sprint_number * 14 - 1,
    CASE sprint_number
        WHEN 1 THEN 'Подготовить базовые страницы и основные формы.'
        WHEN 2 THEN 'Реализовать работу с задачами и участниками.'
        ELSE 'Проверить результат и исправить замечания.'
        END
FROM generate_series(1, 20) AS project_number
         CROSS JOIN generate_series(1, 3) AS sprint_number
    ON CONFLICT (id) DO NOTHING;

-- Заявки: 260 записей.
INSERT INTO requests (
    id,
    project_id,
    author_id,
    title,
    description,
    goal,
    expected_result,
    desired_deadline,
    material_url,
    clarification,
    status,
    created_at,
    updated_at
)
SELECT
    500000 + request_number,
    20000 + ((request_number - 1) % 40) + 1,
    10001 + ((((request_number - 1) % 40) * 9 + 2) % 150),
    CASE request_number % 5
    WHEN 0 THEN 'Добавить поиск задач по названию'
    WHEN 1 THEN 'Добавить фильтрацию задач на странице проекта'
    WHEN 2 THEN 'Подготовить экспорт списка задач'
    WHEN 3 THEN 'Добавить страницу профиля пользователя'
    ELSE 'Улучшить отображение карточки задачи'
END,
    CASE request_number % 5
        WHEN 0 THEN 'Необходимо добавить поиск задач по части названия на странице проекта.'
        WHEN 1 THEN 'Необходимо добавить фильтры по статусу, исполнителю, приоритету и этапу проекта.'
        WHEN 2 THEN 'Необходимо подготовить выгрузку списка задач проекта в отдельный файл.'
        WHEN 3 THEN 'Необходимо добавить страницу профиля с основными данными пользователя.'
        ELSE 'Необходимо улучшить отображение карточки задачи на доске и странице просмотра.'
END,
    CASE request_number % 5
        WHEN 0 THEN 'Сделать поиск задач быстрее и удобнее для участников проекта.'
        WHEN 1 THEN 'Упростить контроль большого количества задач в проекте.'
        WHEN 2 THEN 'Упростить подготовку отчётов по задачам проекта.'
        WHEN 3 THEN 'Дать пользователю доступ к просмотру и изменению данных профиля.'
        ELSE 'Сделать интерфейс карточки задачи более понятным.'
END,
    CASE request_number % 5
        WHEN 0 THEN 'На странице проекта появляется поле поиска задач по названию.'
        WHEN 1 THEN 'На странице проекта появляется блок фильтрации задач.'
        WHEN 2 THEN 'Пользователь получает файл со списком задач проекта.'
        WHEN 3 THEN 'Пользователь может открыть страницу профиля через верхнее меню.'
        ELSE 'Карточка задачи содержит основные данные и удобные кнопки действий.'
END,
    DATE '2026-04-01' + request_number % 90,
    CASE
        WHEN request_number % 4 = 0 THEN 'https://example.local/materials/request-' || request_number
        ELSE NULL
END,
    CASE
        WHEN request_number % 7 = 0 THEN 'Нужно уточнить состав полей и ограничения по доступу.'
        ELSE NULL
END,
    CASE
        WHEN request_number <= 100 THEN 'CONVERTED_TO_TASK'::request_status_type
        WHEN request_number % 10 = 0 THEN 'CANCELED'::request_status_type
        WHEN request_number % 8 = 0 THEN 'REJECTED'::request_status_type
        WHEN request_number % 7 = 0 THEN 'NEEDS_CLARIFICATION'::request_status_type
        WHEN request_number % 5 = 0 THEN 'ACCEPTED'::request_status_type
        ELSE 'NEW'::request_status_type
END,
    TIMESTAMP '2026-03-01 09:00:00' + request_number * INTERVAL '5 hours',
    CASE
        WHEN request_number % 3 = 0 THEN TIMESTAMP '2026-03-03 09:00:00' + request_number * INTERVAL '5 hours'
        ELSE NULL
END
FROM generate_series(1, 260) AS request_number
ON CONFLICT (id) DO NOTHING;

-- Задачи: 1000 записей.
INSERT INTO tasks (
    id,
    project_id,
    stage_id,
    sprint_id,
    request_id,
    title,
    description,
    work_type_id,
    priority,
    status,
    assignee_id,
    created_by_user_id,
    deadline,
    completed_at,
    closed_at,
    result_description,
    result_url,
    created_at,
    updated_at
)
SELECT
    600000 + task_number,
    20000 + ((task_number - 1) % 40) + 1,
    300000 + (((task_number - 1) % 40) + 1) * 10 + ((task_number - 1) % 5) + 1,
    CASE
    WHEN ((task_number - 1) % 40) + 1 <= 20
    THEN 400000 + (((task_number - 1) % 40) + 1) * 10 + ((task_number - 1) % 3) + 1
    ELSE NULL
END,
    CASE
        WHEN task_number <= 100 THEN 500000 + task_number
        ELSE NULL
END,
    CASE task_number % 6
        WHEN 0 THEN 'Разработать форму ввода данных'
        WHEN 1 THEN 'Настроить проверку прав доступа'
        WHEN 2 THEN 'Подготовить страницу просмотра данных'
        WHEN 3 THEN 'Проверить отображение интерфейса'
        WHEN 4 THEN 'Обновить описание функциональности'
        ELSE 'Исправить замечания по задаче'
END || ' #' || task_number,
    CASE task_number % 6
        WHEN 0 THEN 'Нужно подготовить форму, проверить обязательные поля и сохранить введённые данные.'
        WHEN 1 THEN 'Нужно настроить доступ к странице с учётом роли пользователя в проекте.'
        WHEN 2 THEN 'Нужно вывести данные на странице и добавить переходы к связанным разделам.'
        WHEN 3 THEN 'Нужно проверить страницу на разных размерах экрана и исправить найденные проблемы.'
        WHEN 4 THEN 'Нужно обновить текстовое описание функции и привести его к текущей реализации.'
        ELSE 'Нужно исправить замечания после проверки и повторно отправить результат.'
END,
    (SELECT id FROM work_types ORDER BY id LIMIT 1 OFFSET ((task_number - 1) % 5)),
    CASE task_number % 4
        WHEN 0 THEN 'LOW'::task_priority_type
        WHEN 1 THEN 'MEDIUM'::task_priority_type
        WHEN 2 THEN 'HIGH'::task_priority_type
        ELSE 'URGENT'::task_priority_type
END,
    CASE task_number % 7
        WHEN 0 THEN 'TO_DO'::task_status_type
        WHEN 1 THEN 'IN_PROGRESS'::task_status_type
        WHEN 2 THEN 'ON_REVIEW'::task_status_type
        WHEN 3 THEN 'DONE'::task_status_type
        WHEN 4 THEN 'NEEDS_REVISION'::task_status_type
        WHEN 5 THEN 'CLOSED'::task_status_type
        ELSE 'NEEDS_CLARIFICATION'::task_status_type
END,
    CASE
        WHEN task_number % 7 = 0 THEN NULL
        ELSE 10001 + ((((task_number - 1) % 40) * 9 + 3 + (task_number % 6)) % 150)
END,
    10001 + ((((task_number - 1) % 40) * 9) % 150),
    DATE '2026-04-01' + task_number % 120,
    CASE
        WHEN task_number % 7 IN (3, 5) THEN TIMESTAMP '2026-05-01 18:00:00' + task_number * INTERVAL '2 hours'
        ELSE NULL
END,
    CASE
        WHEN task_number % 7 = 5 THEN TIMESTAMP '2026-05-03 18:00:00' + task_number * INTERVAL '2 hours'
        ELSE NULL
END,
    CASE
        WHEN task_number % 7 IN (2, 3, 5) THEN 'Работа выполнена, результат передан на проверку.'
        ELSE NULL
END,
    CASE
        WHEN task_number % 7 IN (2, 3, 5) THEN 'https://example.local/results/task-' || task_number
        ELSE NULL
END,
    TIMESTAMP '2026-03-10 09:00:00' + task_number * INTERVAL '3 hours',
    CASE
        WHEN task_number % 2 = 0 THEN TIMESTAMP '2026-03-12 09:00:00' + task_number * INTERVAL '3 hours'
        ELSE NULL
END
FROM generate_series(1, 1000) AS task_number
ON CONFLICT (id) DO NOTHING;

-- Комментарии к задачам: 470 записей.
INSERT INTO task_comments (id, task_id, author_id, comment_type, text, created_at)
SELECT
    700000 + comment_number,
    600000 + ((comment_number - 1) % 1000) + 1,
    CASE
    WHEN comment_number % 3 = 0 THEN 10001 + ((((comment_number - 1) % 40) * 9 + 1) % 150)
    ELSE 10001 + ((((comment_number - 1) % 40) * 9 + 3 + (comment_number % 6)) % 150)
END,
    CASE comment_number % 6
        WHEN 0 THEN 'COMMON'::task_comment_type
        WHEN 1 THEN 'PROGRESS'::task_comment_type
        WHEN 2 THEN 'CONTROL'::task_comment_type
        WHEN 3 THEN 'PROBLEM'::task_comment_type
        WHEN 4 THEN 'CLARIFICATION_REQUEST'::task_comment_type
        ELSE 'REVISION_COMMENT'::task_comment_type
END,
    CASE comment_number % 6
        WHEN 0 THEN 'Описание задачи понятно, можно продолжать работу.'
        WHEN 1 THEN 'Работа начата, подготовлена основная часть реализации.'
        WHEN 2 THEN 'Результат проверен, критических замечаний нет.'
        WHEN 3 THEN 'Обнаружена проблема, требуется дополнительная проверка.'
        WHEN 4 THEN 'Нужно уточнить требования перед продолжением работы.'
        ELSE 'Необходимо исправить замечания и повторно отправить результат.'
END,
    TIMESTAMP '2026-03-15 12:00:00' + comment_number * INTERVAL '4 hours'
FROM generate_series(1, 470) AS comment_number
ON CONFLICT (id) DO NOTHING;

-- Обновление последовательностей после вставки явных ID.
SELECT setval(pg_get_serial_sequence('users', 'id'), GREATEST((SELECT max(id) FROM users), 1));
SELECT setval(pg_get_serial_sequence('roles', 'id'), GREATEST((SELECT max(id) FROM roles), 1));
SELECT setval(pg_get_serial_sequence('work_types', 'id'), GREATEST((SELECT max(id) FROM work_types), 1));
SELECT setval(pg_get_serial_sequence('projects', 'id'), GREATEST((SELECT max(id) FROM projects), 1));
SELECT setval(pg_get_serial_sequence('project_stages', 'id'), GREATEST((SELECT max(id) FROM project_stages), 1));
SELECT setval(pg_get_serial_sequence('sprints', 'id'), GREATEST((SELECT max(id) FROM sprints), 1));
SELECT setval(pg_get_serial_sequence('requests', 'id'), GREATEST((SELECT max(id) FROM requests), 1));
SELECT setval(pg_get_serial_sequence('tasks', 'id'), GREATEST((SELECT max(id) FROM tasks), 1));
SELECT setval(pg_get_serial_sequence('task_comments', 'id'), GREATEST((SELECT max(id) FROM task_comments), 1));
