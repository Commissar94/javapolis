package javapolis.model.dto;

import java.time.LocalDateTime;

public record CommentDto(
    Long id,
    String content,
    String authorName,
    String authorUsername,
    LocalDateTime createdAt
) {}
