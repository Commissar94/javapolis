-- Migration to add Forum and Business Center tables

CREATE TABLE forum_messages (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    content VARCHAR(1000) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_forum_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE business_tasks (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description VARCHAR(500) NOT NULL,
    reward INTEGER NOT NULL,
    icon VARCHAR(255) NOT NULL
);
