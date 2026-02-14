package javapolis.repository;

import javapolis.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    @Query("SELECT DISTINCT c FROM Comment c LEFT JOIN FETCH c.commentLikes WHERE c.topicPath = :topicPath AND c.page = :page AND c.parent IS NULL ORDER BY c.createdAt DESC")
    List<Comment> findByTopicPathAndPageAndParentIsNullOrderByCreatedAtDesc(@Param("topicPath") String topicPath, @Param("page") int page);
}
