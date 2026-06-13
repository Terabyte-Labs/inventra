-- V23__auth_roles_seed_fix.sql
-- Fix for databases where auth.roles does not have USER/OPERATOR.
-- Required by UserService default role resolution.

INSERT INTO auth.roles (id, name, description, created_at)
VALUES
    ('00000000-0000-0000-0000-000000000101', 'ADMIN', 'Administrador del sistema', NOW()),
    ('00000000-0000-0000-0000-000000000102', 'USER', 'Usuario operativo', NOW()),
    ('00000000-0000-0000-0000-000000000103', 'OPERATOR', 'Operador de inventario y manufactura', NOW())
ON CONFLICT (name) DO NOTHING;

-- Make sure admin keeps ADMIN role.
INSERT INTO auth.user_roles (user_id, role_id)
SELECT u.id, r.id
FROM auth.users u
JOIN auth.roles r ON r.name = 'ADMIN'
WHERE u.username = 'admin'
ON CONFLICT DO NOTHING;
