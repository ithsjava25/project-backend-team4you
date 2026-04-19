package backendlab.team4you.log;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LogRepository extends JpaRepository<LogEntity, Long> {


    Page<LogEntity> findByActionContainingIgnoreCaseOrDetailsContainingIgnoreCase(
            String action,
            String details,
            Pageable pageable
    );
}
