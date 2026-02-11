package javapolis.service;

import javapolis.model.Comment;
import javapolis.model.User;
import javapolis.model.dto.CommentDto;
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

    public List<CommentDto> getComments(String topicPath, int page) {
        return commentRepository.findByTopicPathAndPageOrderByCreatedAtDesc(topicPath, page)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public CommentDto addComment(String topicPath, int page, String content, User user) {
        Comment comment = new Comment(topicPath, page, content, user);
        Comment saved = commentRepository.save(comment);
        return convertToDto(saved);
    }

    private CommentDto convertToDto(Comment comment) {
        String authorName = comment.getUser().getFirstName() != null && !comment.getUser().getFirstName().isBlank()
                ? comment.getUser().getFirstName() + " " + (comment.getUser().getLastName() != null ? comment.getUser().getLastName() : "")
                : comment.getUser().getUsername();
        
        return new CommentDto(
                comment.getId(),
                comment.getContent(),
                authorName.trim(),
                comment.getUser().getUsername(),
                comment.getCreatedAt()
        );
    }
}
