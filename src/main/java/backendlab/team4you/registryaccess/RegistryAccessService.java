package backendlab.team4you.registryaccess;

import backendlab.team4you.registry.Registry;
import backendlab.team4you.registry.RegistryRepository;
import backendlab.team4you.user.UserEntity;
import backendlab.team4you.user.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RegistryAccessService {

    private final RegistryAccessRepository registryAccessRepository;
    private final UserRepository userRepository;
    private final RegistryRepository registryRepository;

    public RegistryAccessService(
            RegistryAccessRepository registryAccessRepository,
            UserRepository userRepository,
            RegistryRepository registryRepository
    ) {
        this.registryAccessRepository = registryAccessRepository;
        this.userRepository = userRepository;
        this.registryRepository = registryRepository;
    }

    public boolean canCreateCasesInRegistry(
            String username,
            Long registryId
    ) {
        return registryAccessRepository
                .existsByRegistryIdAndUserUsernameAndCanCreateCasesTrue(
                        registryId,
                        username
                );
    }



}