package backendlab.team4you;

import backendlab.team4you.dto.UserDTO;
import backendlab.team4you.dto.UserUpdateDTO;
import backendlab.team4you.mapper.UserMapper;
import backendlab.team4you.user.UserEntity;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.User;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class UserMapperTest {


    @Test
    void shouldReturnNullWhenEntityIsNull() {
        assertNull(UserMapper.toDto(null));
    }

    @Test
    void shouldMapEntityToDTO() {

        UserEntity user = new UserEntity();
        user.setEmail("test@test.com");
        user.setFirstName("firstName");
        user.setLastName("lastName");
        user.setPhoneNumber("0701234567");
        

        UserDTO dto = UserMapper.toDto(user);

        assertEquals(user.getEmail(), dto.email());
        assertEquals(user.getFirstName(), dto.firstName());
        assertEquals(user.getLastName(), dto.lastName());
        assertEquals("firstName lastName", dto.fullName());
    }
}
