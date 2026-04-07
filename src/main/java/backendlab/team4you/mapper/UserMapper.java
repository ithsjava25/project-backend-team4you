package backendlab.team4you.mapper;

import backendlab.team4you.dto.UserDTO;
import backendlab.team4you.dto.UserRegistrationDTO;
import backendlab.team4you.user.UserEntity;

import java.util.UUID;

public class UserMapper {


    public static UserEntity toEntity(UserRegistrationDTO dto) {
        if (dto == null) return null;

        UserEntity entity = new UserEntity();
        entity.setFirstName(dto.firstName());
        entity.setLastName(dto.lastName());
        entity.setEmail(dto.email());
        entity.setPhoneNumber(dto.phoneNumber());

        return entity;
    }


    public static UserDTO toDto(UserEntity entity) {
        if (entity == null) return null;

        return new UserDTO(
                entity.getId(),
                entity.getFirstName(),
                entity.getLastName(),
                entity.getEmail(),
                entity.getFirstName() + " " + entity.getLastName()
        );
    }


}

