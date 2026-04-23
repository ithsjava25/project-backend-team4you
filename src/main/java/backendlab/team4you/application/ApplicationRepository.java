package backendlab.team4you.application;

import backendlab.team4you.user.UserEntity;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<ApplicationEntity, Long> {

    Page<ApplicationEntity> findByStatus(ApplicationStatus status, Pageable pageable);


    long countByStatus(ApplicationStatus status);


    Page<ApplicationEntity> findAll(Pageable pageable);

    Page<ApplicationEntity> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String name, String email, Pageable pageable);


    List<ApplicationEntity> findByOwner(UserEntity owner);


    List<ApplicationEntity> findByOwnerUsernameAndStatus(String username, ApplicationStatus status);


    List<ApplicationEntity> findByOwnerUsernameAndStatusNot(String username, ApplicationStatus status);


}
