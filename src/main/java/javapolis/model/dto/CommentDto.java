package javapolis.model.dto;

import java.time.LocalDateTime;

import java.util.List;

public record CommentDto(
    Long id,
    String content,
    String authorName,
    String authorUsername,
    LocalDateTime createdAt,
    int likes,
    List<String> likers,
    List<CommentDto> replies,
    Long parentId,
    String imageUrl
) {}
