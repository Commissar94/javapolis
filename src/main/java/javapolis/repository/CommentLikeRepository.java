package javapolis.repository;

import javapolis.model.Comment;
import javapolis.model.CommentLike;
import javapolis.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {
    boolean existsByCommentAndUser(Comment comment, User user);
}
