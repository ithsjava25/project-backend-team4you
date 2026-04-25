package backendlab.team4you.user;

import backendlab.team4you.audit.AuditService;
import backendlab.team4you.dto.UserRegistrationDTO;
import backendlab.team4you.exceptions.DuplicateEmailException;
import backendlab.team4you.exceptions.UserNotFoundException;
import backendlab.team4you.registryaccess.AdminUserCreateDTO;
import jakarta.transaction.Transactional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.webauthn.api.Bytes;
import org.springframework.stereotype.Service;

import java.security.Principal;

import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;

import java.util.List;
import java.util.Optional;


@Service
public class UserService {

    UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final SecureRandom random = new SecureRandom();
    AuditService auditLogService;
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(UserService.class);

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
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
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

        if (dto.name() == null || dto.name().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Username is required");
        }
        if (userRepository.findByName(dto.name().trim()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST ,"Username already exists");
        }

        String cleanEmail = dto.email() != null ? dto.email().trim() : null;

        if (cleanEmail != null && userRepository.findByEmail(cleanEmail).isPresent()) {
            throw new DuplicateEmailException("E-posten är redan tagen");
        }

        UserEntity user = new UserEntity();
        user.setName(dto.name().trim());
        user.setFirstName(dto.firstName());
        user.setLastName(dto.lastName());
        user.setEmail(cleanEmail);
        user.setPhoneNumber(dto.phoneNumber());

        String hashedPw = passwordEncoder.encode(dto.password());
        user.setPasswordHash(hashedPw);

        user.setRole(UserRole.USER);


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

        if(userRepository.findByName(cleanName).isPresent())
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Användarnamnet är redan taget");
        if(userRepository.findByEmail(cleanEmail).isPresent())
            throw new ResponseStatusException(HttpStatus.CONFLICT, "E-posten är redan tagen");

        byte[] idBytes = new byte[32];
        random.nextBytes(idBytes);

        UserEntity userEntity = new UserEntity(
                new Bytes(idBytes),
                cleanName,
                displayName
        );

        userEntity.setEmail(cleanEmail);
        userEntity.setFirstName(firstName);
        userEntity.setLastName(lastName);

        //Every user that register themselves will automatically get the role USER assigned
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
        return userRepository.findByName(name.trim()).orElse(null);
    }

    @Transactional
    public void deleteUser(String id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("User not found");
        }

        userRepository.deleteById(id);
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
        return userRepository.findByRole(UserRole.ADMIN, PageRequest.of(page, size));
    }
    public Optional<UserEntity> findByUsername(String disPlayName) {
        return userRepository.findByDisplayName(disPlayName);
    }

    @Transactional
    public UserEntity getCurrentUser(Principal principal) {
        if (principal == null || principal.getName() == null || principal.getName().isBlank()) {
            throw new UserNotFoundException("No authenticated user found");
        }

        return userRepository.findByName(principal.getName().trim())
                .orElseThrow(() -> new UserNotFoundException("User not found: " + principal.getName()));
    }

    @Transactional
    public void updateRole(Long userId, UserRole newRole) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String actor = (auth != null) ? auth.getName() : "system";

        UserEntity user = userRepository.findById(String.valueOf(userId)).orElseThrow(() -> new UserNotFoundException("User not found: " + userId));

        UserRole oldRole = user.getRole();

        user.setRole(newRole);
        userRepository.save(user);

        auditLogService.log(
                actor,
                "ROLE_UPDATED",
                "USER",
                userId,
                "Changed role from " + oldRole + " to " + newRole,
                "SUCCESS"
        );

        logger.info("User {} role updated from {} to {}", userId, oldRole, newRole);

    }

    public UserEntity updateRole(
            String userId,
            UserRole role
    ) {

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "User not found"
                        )
                );

        user.setRole(role);

        return userRepository.save(user);
    }

    public UserEntity createUserAsAdmin(
            AdminUserCreateDTO dto
    ) {

        if (userRepository.findByEmail(dto.email()).isPresent()) {
            throw new DuplicateEmailException(
                    "Email already exists"
            );
        }

        UserEntity user = new UserEntity();

        user.setEmail(dto.email());
        user.setName(dto.name());
        user.setFirstName(dto.firstName());
        user.setLastName(dto.lastName());
        user.setPhoneNumber(dto.phoneNumber());

        String hashedPw = passwordEncoder.encode(dto.password());
        user.setPasswordHash(hashedPw);

        user.setRole(dto.role());

        return userRepository.save(user);
    }


}
