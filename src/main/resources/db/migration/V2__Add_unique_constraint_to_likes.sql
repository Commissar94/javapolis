-- Add unique constraint to prevent multiple likes from the same user on the same comment
ALTER TABLE comment_likes ADD CONSTRAINT uk_comment_user UNIQUE (comment_id, user_id);
