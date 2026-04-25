package backendlab.team4you.user;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.util.List;


import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, String> {
    Optional<UserEntity> findByEmail(String email);


    Optional<UserEntity> findByName(String name);

    Page<UserEntity> findAll(Pageable pageable);


    Page<UserEntity> findByRole(UserRole role, Pageable pageable);

    Optional<UserEntity> findByDisplayName(String displayName);

}
