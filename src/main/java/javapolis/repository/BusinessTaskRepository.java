package javapolis.repository;

import javapolis.model.BusinessTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BusinessTaskRepository extends JpaRepository<BusinessTask, Long> {
}
