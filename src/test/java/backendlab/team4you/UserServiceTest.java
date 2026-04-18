//package backendlab.team4you;
//
//
//import backendlab.team4you.mapper.UserMapper;
//import backendlab.team4you.user.UserEntity;
//import backendlab.team4you.user.UserRepository;
//import backendlab.team4you.user.UserService;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.Spy;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//public class UserServiceTest {
//
//    @Spy
//    UserMapper userMapper;
//
//    @Mock
//    UserRepository userRepository;
//
//    @InjectMocks
//    UserService userService;
//
//
//    @Test
//    void shouldFindUserWhenExists(){
//        UserEntity user = new UserEntity();
//        user.setId("id");
//        user.setEmail("email");
//
//        when(userRepository.findById("id")).thenReturn(Optional.of(user));
//
//        UserEntity result = userService.findById("id");
//
//        assertNotNull(result);
//        assertEquals("email", result.getEmail());
//        verify(userRepository, times(1)).findById("id");
//    }
//}
