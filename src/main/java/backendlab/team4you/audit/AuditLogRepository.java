package backendlab.team4you.audit;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    void deleteByTimestampBefore(ZonedDateTime limit);
    List<AuditLog> findAllByOrderByTimestampDesc();

}
