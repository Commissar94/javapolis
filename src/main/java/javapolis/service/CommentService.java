package javapolis.service;

import javapolis.model.Comment;
import javapolis.model.CommentLike;
import javapolis.model.User;
import javapolis.model.dto.CommentDto;
import javapolis.repository.CommentLikeRepository;
import javapolis.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private javapolis.repository.UserRepository userRepository;

    @Autowired
    private CommentLikeRepository commentLikeRepository;

    @Transactional(readOnly = true)
    public List<CommentDto> getComments(String topicPath, int page) {
        return commentRepository.findByTopicPathAndPageAndParentIsNullOrderByCreatedAtDesc(topicPath, page)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public CommentDto addComment(String topicPath, int page, String content, User user, Long parentId) {
        Comment comment = new Comment(topicPath, page, content, user);
        if (parentId != null) {
            Comment parent = commentRepository.findById(parentId)
                    .orElseThrow(() -> new RuntimeException("Родительский комментарий не найден"));
            comment.setParent(parent);
        }
        Comment saved = commentRepository.save(comment);
        return convertToDto(saved);
    }

    @Transactional
    public CommentDto likeComment(Long commentId, User user) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Комментарий не найден"));

        if (comment.getUser().getUsername().equals(user.getUsername())) {
            throw new RuntimeException("Нельзя лайкать собственный комментарий");
        }

        if (commentLikeRepository.existsByCommentAndUser(comment, user)) {
            throw new RuntimeException("Вы уже поддержали этот комментарий");
        }

        if (user.getCoins() < 1) {
            throw new RuntimeException("Недостаточно полисов для лайка");
        }

        // Списываем у лайкающего
        user.setCoins(user.getCoins() - 1);
        userRepository.save(user);

        // Начисляем автору комментария
        User author = comment.getUser();
        author.setCoins(author.getCoins() + 1);
        userRepository.save(author);

        // Увеличиваем счетчик лайков
        comment.setLikes(comment.getLikes() + 1);
        
        // Сохраняем информацию о том, кто лайкнул
        CommentLike commentLike = new CommentLike(comment, user);
        commentLikeRepository.save(commentLike);
        
        Comment saved = commentRepository.save(comment);

        return convertToDto(saved);
    }

    private CommentDto convertToDto(Comment comment) {
        String authorName = comment.getUser().getFirstName() != null && !comment.getUser().getFirstName().isBlank()
                ? comment.getUser().getFirstName() + " " + (comment.getUser().getLastName() != null ? comment.getUser().getLastName() : "")
                : comment.getUser().getUsername();
        
        List<String> likers = comment.getCommentLikes().stream()
                .map(like -> like.getUser().getUsername())
                .collect(Collectors.toList());
        
        List<CommentDto> replies = comment.getReplies().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        
        return new CommentDto(
                comment.getId(),
                comment.getContent(),
                authorName.trim(),
                comment.getUser().getUsername(),
                comment.getCreatedAt(),
                comment.getLikes(),
                likers,
                replies,
                comment.getParent() != null ? comment.getParent().getId() : null
        );
    }
}
