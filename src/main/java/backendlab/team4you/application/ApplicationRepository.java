package backendlab.team4you.application;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApplicationRepository extends JpaRepository<ApplicationEntity, Long> {


    Page<ApplicationEntity> findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
            String name,
            String email,
            Pageable pageable
    );

    Page<ApplicationEntity> findAll(Pageable pageable);

    Page<ApplicationEntity> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String name, String email, Pageable pageable);
}
