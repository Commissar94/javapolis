-- Initial schema for JavaPolis

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    role VARCHAR(50) DEFAULT 'USER',
    email_verified BOOLEAN DEFAULT FALSE,
    verification_token VARCHAR(255),
    verification_token_expiry TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    last_login TIMESTAMP,
    enabled BOOLEAN DEFAULT TRUE,
    coins INTEGER NOT NULL DEFAULT 10
);

CREATE TABLE comments (
    id BIGSERIAL PRIMARY KEY,
    topic_path VARCHAR(255) NOT NULL,
    page INTEGER NOT NULL,
    content TEXT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    likes INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT fk_comments_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE comment_likes (
    id BIGSERIAL PRIMARY KEY,
    comment_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_likes_comment FOREIGN KEY (comment_id) REFERENCES comments (id) ON DELETE CASCADE,
    CONSTRAINT fk_likes_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE user_progress (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    topic_path VARCHAR(255) NOT NULL,
    completed_pages TEXT NOT NULL,
    is_completed BOOLEAN NOT NULL DEFAULT FALSE,
    updated_at TIMESTAMP,
    CONSTRAINT fk_progress_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT uk_user_topic UNIQUE (user_id, topic_path)
);
