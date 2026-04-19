package backendlab.team4you.user;


import backendlab.team4you.webauthn.WebAuthnCredential;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.util.List;


import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByEmail(String email);


    Page<UserEntity> findAll(Pageable pageable);




    Page<UserEntity> findByRole(String admin, Pageable pageable);

    UserEntity findByDisplayName(String DisplayName);

    Page<UserEntity> findByDisplayNameContainingIgnoreCaseOrEmailContainingIgnoreCase(String search, String search1, Pageable pageable);

    Optional<UserEntity> findByUsername(String username);


}
