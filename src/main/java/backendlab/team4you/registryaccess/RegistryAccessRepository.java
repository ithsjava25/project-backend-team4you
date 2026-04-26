package backendlab.team4you.registryaccess;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RegistryAccessRepository
        extends JpaRepository<RegistryAccessEntity, Long> {

    boolean existsByRegistryIdAndUserNameAndCanCreateCasesTrue(
            Long registryId,
            String name
    );

    Optional<RegistryAccessEntity> findByRegistryIdAndUserName(
            Long registryId,
            String name
    );

    List<RegistryAccessEntity> findByUserName(
            String name
    );
}