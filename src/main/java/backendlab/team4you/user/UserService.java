package backendlab.team4you.user;

import backendlab.team4you.dto.UserRegistrationDTO;
import backendlab.team4you.exceptions.DuplicateEmailException;
import backendlab.team4you.exceptions.UserNotFoundException;
import backendlab.team4you.log.LogService;
import jakarta.transaction.Transactional;

import org.jspecify.annotations.Nullable;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.webauthn.api.Bytes;
import org.springframework.stereotype.Service;

import org.springframework.web.bind.annotation.DeleteMapping;


import java.security.Principal;

import org.springframework.web.server.ResponseStatusException;


import java.security.SecureRandom;

import java.util.List;


@Service
public class UserService {



    UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final SecureRandom random = new SecureRandom();
    private final LogService logService;

    public UserService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder, LogService logService){



        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.logService = logService;
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
    public UserEntity findById(Long id){
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    @Transactional
    public void deleteById(Long id){
        if(userRepository.findById(id).isEmpty()){
            throw new DuplicateEmailException("E-posten är redan taken");
        }
        userRepository.deleteById(id);
    }
    public UserEntity update(UserEntity userEntity){
        return userRepository.save(userEntity);
    }


    public void registerUser(UserRegistrationDTO dto) {

        if (dto.name() == null || dto.name().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Username is required");
        }
        if (userRepository.findByUsername(dto.name().trim()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST ,"Username already exists");
        }

        String cleanEmail = dto.email() != null ? dto.email().trim() : null;

        if (cleanEmail != null && userRepository.findByEmail(cleanEmail).isPresent()) {
            throw new DuplicateEmailException("E-posten är redan tagen");
        }

        UserEntity user = new UserEntity();
        user.setFirstName(dto.firstName());
        user.setLastName(dto.lastName());
        user.setEmail(cleanEmail);
        user.setPhoneNumber(dto.phoneNumber());


        String hashedPw = passwordEncoder.encode(dto.password());
        user.setPasswordHash(hashedPw);


        userRepository.save(user);
    }

    public UserEntity registerWebAuthnUser(String username, String displayName, String email, String firstName, String lastName){

        if (username == null || username.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username is required");
        }
        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is required");
        }
        String cleanName = username.trim();
        String cleanEmail = email.trim();

        if(userRepository.findByUsername(cleanName).isPresent())
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Användarnamnet är redan taget");
        if(userRepository.findByEmail(cleanEmail).isPresent())
            throw new ResponseStatusException(HttpStatus.CONFLICT, "E-posten är redan tagen");

        byte[] idBytes = new byte[32];
        random.nextBytes(idBytes);

        UserEntity userEntity = new UserEntity(
        );

        userEntity.setEmail(cleanEmail);
        userEntity.setFirstName(firstName);
        userEntity.setLastName(lastName);


        String assignedRole = "USER";
        userEntity.setRole(UserRole.USER);

        try {
            return userRepository.save(userEntity);
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username or email already taken");
        }
    }

    public UserEntity findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    public UserEntity findByName(String name){
        return userRepository.findByUsername(name.trim()).orElse(null);
    }

    @Transactional
    public void deleteUser(Long id, String adminUsername) {

        userRepository.deleteById(id);

        logService.log(
                "USER_DELETED",
                adminUsername,
                "Deleted user with id: " + id
        );
    }

    @Transactional
    public String deleteByEmail(String email) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(
                        "User not found with email: " + email
                ));

        userRepository.delete(user);

        return email;
    }


    public String search(String searchTerm) {


        return searchTerm;

    }
    public Page<UserEntity> getUsers(int page, int size) {
        return userRepository.findAll(PageRequest.of(page, size));
    }
    public Page<UserEntity> getAdmins(int page, int size) {
        return userRepository.findByRole("ADMIN", PageRequest.of(page, size));
    }
    public UserEntity findByUsername(String disPlayName) {
        return userRepository.findByDisplayName(disPlayName);
    }
}
