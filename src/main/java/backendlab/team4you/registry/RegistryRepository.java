package backendlab.team4you.registry;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RegistryRepository extends JpaRepository<Registry,Long> {
    boolean existsByName(String name);
    boolean existsByCode(String code);
    Optional<Registry> findByCode(String code);
}
