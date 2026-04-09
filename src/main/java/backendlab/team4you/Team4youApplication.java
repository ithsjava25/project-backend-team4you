package backendlab.team4you;

import backendlab.team4you.repository.UserRepository;
import backendlab.team4you.user.UserEntity;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.webauthn.api.Bytes;

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

				UserEntity devUser = new UserEntity(
						Bytes.fromBase64("01"),
						"dev",           // name (username)
						"Developer"      // displayName
				);

				devUser.setPasswordHash(encoder.encode("123456"));
				devUser.setRole("ADMIN");
				devUser.setEmail("dev@team4you.com");

				repository.save(devUser);
			}
		};
	}
}
