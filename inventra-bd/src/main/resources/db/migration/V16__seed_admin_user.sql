INSERT INTO auth.users (
    id,
    username,
    email,
    password_hash,
    full_name,
    active,
    created_at
)
VALUES (
           gen_random_uuid(),
           'admin',
           'admin@inventra.local',
           '$2a$10$7EqJtq98hPqEX7fNZaFWoOhiZwqnyu.c0/jhX8ktC2zU2f0W4H4W6',
           'Inventra Admin',
           true,
           now()
       );

INSERT INTO auth.user_roles (user_id, role_id)
SELECT u.id, r.id
FROM auth.users u
         JOIN auth.roles r ON r.name = 'ADMIN'
WHERE u.username = 'admin';