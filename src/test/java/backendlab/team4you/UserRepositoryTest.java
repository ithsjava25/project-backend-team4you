//package backendlab.team4you;
//
//import backendlab.team4you.user.UserEntity;
//import org.junit.jupiter.api.Test;
//import org.springframework.security.web.webauthn.api.Bytes;
//
//import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
//
//
//
//import backendlab.team4you.controller.UserController;
//import backendlab.team4you.user.UserEntity;
//import backendlab.team4you.user.UserRepository;
//import jakarta.persistence.EntityManager;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
//import org.springframework.security.web.webauthn.api.Bytes;
//
//import java.util.List;
//import java.util.Optional;
//import java.util.UUID;
//
//import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
//
//
//public class UserRepositoryTest {
//
//    UserRepository userRepository;
//
//
//    @Test
//    void shouldFindUsers() {
//        UserEntity user = new UserEntity();
//        user.setId(Bytes.fromBase64(UUID.randomUUID().toString()));
//        user.setEmail("test1@hotmail.com");
//        user.setFirstName("firstName");
//        user.setLastName("lastName");
//        user.setPhoneNumber("0701234567");
//
//
//        List<UserEntity> users = userRepository.findAll();
//
//        assertThat(users).isNotEmpty();
//
//    }
//
//    @Test
//    void shouldSaveAndFindUser() {
//        UserEntity user = new UserEntity();
//        user.setId(Bytes.fromBase64(UUID.randomUUID().toString()));
//        user.setEmail("test2@mail.com");
//        user.setFirstName("firstName");
//        user.setLastName("lastName");
//        user.setPhoneNumber("0701234567");
//
//        UserEntity savedUser = userRepository.save(user);
//
//        Optional<UserEntity> found = userRepository.findById(String.valueOf(savedUser.getId()));
//
//        assertThat(found).isPresent();
//        assertThat(found.get().getFirstName()).isEqualTo("firstName");
//    }
//
//
//}
