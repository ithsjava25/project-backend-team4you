package backendlab.team4you.user;

import backendlab.team4you.dto.UserRegistrationDTO;
import backendlab.team4you.exceptions.DuplicateEmailException;
import backendlab.team4you.exceptions.UserNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;


import java.util.List;


@Service
public class UserService {


    UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder){


        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }


    @Transactional
    public void save(UserEntity userEntity){
        userRepository.save(userEntity);
    }

    @Transactional
    public List<UserEntity> findAll(){
        return userRepository.findAll();
    }

    @Transactional
    public UserEntity findById(String id){
        return userRepository.findById(id).orElse(null);
    }

    @Transactional
    public void deleteById(String id){
        if(userRepository.findById(id).isEmpty()){
            throw new DuplicateEmailException("E-posten är redan taken");
        }
        userRepository.deleteById(id);
    }
    public UserEntity update(UserEntity userEntity){
        return userRepository.save(userEntity);
    }


    public void registerUser(UserRegistrationDTO dto) {

        if (userRepository.findByEmail(dto.email()).isPresent()) {
            throw new DuplicateEmailException("E-posten är redan tagen");
        }

        UserEntity user = new UserEntity();
        user.setName(dto.name());
        user.setFirstName(dto.firstName());
        user.setLastName(dto.lastName());
        user.setEmail(dto.email());
        user.setPhoneNumber(dto.phoneNumber());


        String hashedPw = passwordEncoder.encode(dto.password());
        user.setPasswordHash(hashedPw);


        userRepository.save(user);
    }

    public UserEntity findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    public UserEntity findByName(String name){
        return userRepository.findByName(name).orElse(null);
    }

    @Transactional
    public void deleteUser(String id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("User not found with id: " + id);
        }

        userRepository.deleteById(id);
    }
}
