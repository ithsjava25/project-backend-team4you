package backendlab.team4you.registry;

import backendlab.team4you.exceptions.DuplicateRegistryCodeException;
import backendlab.team4you.exceptions.DuplicateRegistryNameException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RegistryService {
    private final RegistryRepository registryRepository;

    public RegistryService(RegistryRepository registryRepository) {
        this.registryRepository = registryRepository;
    }

    public RegistryResponseDto createRegistry(RegistryRequestDto requestDto) {
        String trimmedName = requestDto.name().trim();
        String trimmedCode = requestDto.code().trim();

        if (registryRepository.existsByName(trimmedName)) {
            throw new DuplicateRegistryNameException("registry name already exists: " + trimmedName);
        }

        if (registryRepository.existsByCode(trimmedCode)) {
            throw new DuplicateRegistryCodeException("registry code already exists: " + trimmedCode);
        }

        Registry registry = new Registry(trimmedName, trimmedCode);

        try {
            Registry savedRegistry = registryRepository.save(registry);

            return new RegistryResponseDto(
                    savedRegistry.getId(),
                    savedRegistry.getName(),
                    savedRegistry.getCode()
            );
        } catch (DataIntegrityViolationException e) {
            if (registryRepository.existsByName(trimmedName)) {
                throw new DuplicateRegistryNameException("registry name already exists: " + trimmedName);
            }

            if (registryRepository.existsByCode(trimmedCode)) {
                throw new DuplicateRegistryCodeException("registry code already exists: " + trimmedCode);
            }
            throw e;
        }
    }
}
