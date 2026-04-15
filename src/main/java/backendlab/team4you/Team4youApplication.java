package backendlab.team4you;

import backendlab.team4you.user.UserRepository;
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

				UserEntity devAdmin = new UserEntity(
						Bytes.fromBase64("01"),
						"dev",           // name (username)
						"Developer"      // displayName
				);

				devAdmin.setPasswordHash(encoder.encode("123456"));
				devAdmin.setRole("ADMIN");
				devAdmin.setEmail("devadmin@gmail.com");

				repository.save(devAdmin);

				UserEntity devUser = new UserEntity(
						Bytes.fromBase64("02"),
						"user",           // name (username)
						"Developer"      // displayName
				);

				devUser.setPasswordHash(encoder.encode("1234"));
				devUser.setRole("USER");
				devUser.setEmail("devuser@gmail.com");

				repository.save(devUser);
			}
		};
	}
}
