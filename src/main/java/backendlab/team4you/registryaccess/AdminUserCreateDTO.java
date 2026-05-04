package backendlab.team4you.registryaccess;

import backendlab.team4you.user.UserRole;

public record AdminUserCreateDTO(

        String email,
        String name,
        String firstName,
        String lastName,
        String phoneNumber,
        String password,
        UserRole role

) {
}
