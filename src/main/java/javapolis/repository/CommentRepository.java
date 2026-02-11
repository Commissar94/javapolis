package javapolis.repository;

import javapolis.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByTopicPathAndPageOrderByCreatedAtDesc(String topicPath, int page);
}
