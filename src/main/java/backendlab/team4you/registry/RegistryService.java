package backendlab.team4you.registry;

import backendlab.team4you.exceptions.DuplicateRegistryCodeException;
import backendlab.team4you.exceptions.DuplicateRegistryNameException;
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
        if (registryRepository.existsByName(requestDto.name().trim())) {
            throw new DuplicateRegistryNameException("registry name already exists: " + requestDto.name().trim());
        }

        if (registryRepository.existsByCode(requestDto.code().trim())) {
            throw new DuplicateRegistryCodeException("registry code already exists: " + requestDto.code().trim());
        }

        Registry registry = new Registry(
                requestDto.name().trim(),
                requestDto.code().trim()
        );

        Registry savedRegistry = registryRepository.save(registry);

        return new RegistryResponseDto(
                savedRegistry.getId(),
                savedRegistry.getName(),
                savedRegistry.getCode()
        );
    }


}
