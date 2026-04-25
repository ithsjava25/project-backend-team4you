package backendlab.team4you.registryaccess;


import backendlab.team4you.registry.Registry;
import backendlab.team4you.registry.RegistryRepository;
import backendlab.team4you.user.UserEntity;
import backendlab.team4you.user.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RegistryAccessAdminService {

    private final RegistryAccessRepository registryAccessRepository;
    private final RegistryRepository registryRepository;
    private final UserRepository userRepository;
    public RegistryAccessAdminService(RegistryAccessRepository registryAccessRepository, RegistryRepository registryRepository, UserRepository userRepository) {

        this.registryAccessRepository = registryAccessRepository;

        this.registryRepository = registryRepository;
        this.userRepository = userRepository;
    }



    public RegistryAccessEntity revokeCaseCreationAccess(
            Long registryId,
            String username
    ) {

        RegistryAccessEntity access = registryAccessRepository
                .findByRegistryIdAndUserName(registryId, username)
                .orElseThrow();

        access.setCanCreateCases(false);

        return registryAccessRepository.save(access);
    }

    public List<RegistryAccessEntity> getRegistryPermissionsForUser(
            String username
    ) {
        return registryAccessRepository.findByUserName(username);
    }


    public boolean grantCaseCreationAccess(
            Long registryId,
            String username
    ) {

        RegistryAccessEntity access = registryAccessRepository
                .findByRegistryIdAndUserName(registryId, username)
                .orElse(null);

        if (access != null) {
            access.setCanCreateCases(true);
            registryAccessRepository.save(access);
            return true;
        }

        Registry registry = registryRepository.findById(registryId)
                .orElseThrow();

        UserEntity user = userRepository.findByName(username)
                .orElseThrow();

        RegistryAccessEntity newAccess = new RegistryAccessEntity(
                registry,
                user,
                true
        );

        registryAccessRepository.save(newAccess);

        return true;
    }
}
