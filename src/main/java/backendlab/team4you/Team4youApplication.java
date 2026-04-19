package backendlab.team4you;

import backendlab.team4you.user.UserRepository;
import backendlab.team4you.user.UserEntity;
import backendlab.team4you.user.UserRole;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.webauthn.api.Bytes;

import java.time.ZonedDateTime;

@SpringBootApplication
public class Team4youApplication {

	public static void main(String[] args) {
		SpringApplication.run(Team4youApplication.class, args);
	}

    @Bean
    @Profile("dev")
    ApplicationRunner init(UserRepository repository, BCryptPasswordEncoder encoder) {
        return args -> {
            if (repository.count() == 0) {

                UserEntity devUser = new UserEntity();

                devUser.setUsername("dev");
                devUser.setDisplayName("Developer");
                devUser.setEmail("dev@gmail.com");
                devUser.setPasswordHash(encoder.encode("123456"));
                devUser.setRole(UserRole.USER);
                devUser.setCreatedAt(ZonedDateTime.now());

                repository.save(devUser);
            }
        };
    }
}
