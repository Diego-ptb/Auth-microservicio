-- Create roles table
CREATE TABLE IF NOT EXISTS roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);

-- Create users table
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    rut VARCHAR(12) NOT NULL UNIQUE,
    role_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    FOREIGN KEY (role_id) REFERENCES roles(id)
);

-- Add rut column if it was created before this column was introduced
ALTER TABLE users ADD COLUMN IF NOT EXISTS rut VARCHAR(12);
CREATE UNIQUE INDEX IF NOT EXISTS idx_users_rut ON users(rut);

-- Create index on username for faster queries
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);

-- Solicitudes de rol veterinaria
CREATE TABLE IF NOT EXISTS vet_requests (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    clinic_name VARCHAR(200) NOT NULL,
    address VARCHAR(300),
    phone VARCHAR(30),
    rut_clinica VARCHAR(12),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    notes VARCHAR(500),
    created_at TIMESTAMP NOT NULL
);

ALTER TABLE vet_requests ADD COLUMN IF NOT EXISTS latitude DOUBLE PRECISION;
ALTER TABLE vet_requests ADD COLUMN IF NOT EXISTS longitude DOUBLE PRECISION;

CREATE INDEX IF NOT EXISTS idx_vet_requests_user_id ON vet_requests(user_id);
CREATE INDEX IF NOT EXISTS idx_vet_requests_status ON vet_requests(status);

CREATE UNIQUE INDEX IF NOT EXISTS uq_vet_requests_user_id ON vet_requests(user_id);
