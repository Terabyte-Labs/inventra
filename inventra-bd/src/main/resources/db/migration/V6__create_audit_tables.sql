CREATE TABLE audit.audit_log
(
    id          UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    entity_name VARCHAR(120) NOT NULL,
    entity_id   UUID,
    action      VARCHAR(50)  NOT NULL,
    old_data    JSONB,
    new_data    JSONB,
    created_by  UUID REFERENCES auth.users (id),
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);