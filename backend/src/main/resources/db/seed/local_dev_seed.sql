-- Deterministic local/dev identities. Password for all seed users is the literal: password
-- Hash is BCrypt (not a production secret). Flyway applies the same inserts in V4__local_dev_seed.sql.

INSERT INTO products (id, name, created_at) VALUES
    ('00000000-0000-0000-0000-000000000001', 'Support', TIMESTAMPTZ '2026-01-01 00:00:00+00'),
    ('00000000-0000-0000-0000-000000000002', 'Other', TIMESTAMPTZ '2026-01-01 00:00:00+00')
ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name;

INSERT INTO users (id, username, password, email, display_name, created_at) VALUES
    ('00000000-0000-0000-0000-0000000000a1', 'alice', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'alice@example.com', 'Alice', TIMESTAMPTZ '2026-01-01 00:00:00+00'),
    ('00000000-0000-0000-0000-0000000000b2', 'bob', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'bob@example.com', 'Bob', TIMESTAMPTZ '2026-01-01 00:00:00+00'),
    ('00000000-0000-0000-0000-0000000000d3', 'dana', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'dana@example.com', 'Dana', TIMESTAMPTZ '2026-01-01 00:00:00+00')
ON CONFLICT (id) DO UPDATE SET
    username = EXCLUDED.username,
    password = EXCLUDED.password,
    email = EXCLUDED.email,
    display_name = EXCLUDED.display_name;

INSERT INTO user_product (user_id, product_id, role) VALUES
    ('00000000-0000-0000-0000-0000000000a1', '00000000-0000-0000-0000-000000000001', 'PRODUCT_OWNER'),
    ('00000000-0000-0000-0000-0000000000b2', '00000000-0000-0000-0000-000000000001', 'DEVELOPER'),
    ('00000000-0000-0000-0000-0000000000a1', '00000000-0000-0000-0000-000000000002', 'PRODUCT_MANAGER'),
    ('00000000-0000-0000-0000-0000000000d3', '00000000-0000-0000-0000-000000000002', 'QA')
ON CONFLICT (user_id, product_id) DO UPDATE SET role = EXCLUDED.role;
