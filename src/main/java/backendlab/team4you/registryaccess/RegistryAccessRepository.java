package backendlab.team4you.registryaccess;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RegistryAccessRepository extends JpaRepository<RegistryAccessEntity, Long> {

    boolean existsByRegistryIdAndUserUsernameAndCanCreateCasesTrue(
            Long registryId,
            String username
    );

    Optional<RegistryAccessEntity> findByRegistryIdAndUserUsername(
            Long registryId,
            String username
    );

    List<RegistryAccessEntity> findByUserUsername(String username);

    Optional<RegistryAccessEntity> findByRegistryIdAndUserName(Long registryId, String username);

    List<RegistryAccessEntity> findByUserName(String username);
}
