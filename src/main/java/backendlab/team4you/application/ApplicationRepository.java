package backendlab.team4you.application;

import backendlab.team4you.user.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<ApplicationEntity, Long> {


    Page<ApplicationEntity> findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
            String name,
            String email,
            Pageable pageable
    );

    Page<ApplicationEntity> findAll(Pageable pageable);

    Page<ApplicationEntity> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String name, String email, Pageable pageable);
//
//    Optional<UserEntity> findByUsername(String username);

    List<ApplicationEntity> findByOwner(UserEntity owner);

    List<ApplicationEntity> findByOwnerUsernameAndStatus(String username, ApplicationStatus status);


}
