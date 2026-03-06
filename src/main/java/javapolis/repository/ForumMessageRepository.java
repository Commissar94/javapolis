package javapolis.repository;

import javapolis.model.ForumMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ForumMessageRepository extends JpaRepository<ForumMessage, Long> {
    List<ForumMessage> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
