package backendlab.team4you;

import backendlab.team4you.user.UserRepository;
import backendlab.team4you.user.UserEntity;
import backendlab.team4you.user.UserRole;
import org.springframework.boot.CommandLineRunner;
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
	@Profile("dev")
	@Bean
	CommandLineRunner commandLineRunner(UserRepository repository, BCryptPasswordEncoder encoder) {
		return args -> {
			seedUser(
					repository,
					encoder,
					"dev",
					"dev",
					"dev",
					"dev@gmail.com",
					UserRole.USER,
					"ZGV2"
			);

			seedUser(
					repository,
					encoder,
					"admin",
					"admin",
					"admin",
					"admin@gmail.com",
					UserRole.ADMIN,
					"YWRtaW4="
			);

			seedUser(
					repository,
					encoder,
					"officer1",
					"officer1",
					"caseofficer1",
					"devcaseofficer@gmail.com",
					UserRole.CASE_OFFICER,
					"YLRtaW5="
			);

			seedUser(
					repository,
					encoder,
					"officer2",
					"officer2",
					"caseofficer2",
					"devcaseofficer2@gmail.com",
					UserRole.CASE_OFFICER,
					"YLRtaG6="
			);
		};
	}

	private void seedUser(
			UserRepository repository,
			BCryptPasswordEncoder encoder,
			String username,
			String password,
			String displayName,
			String email,
			UserRole role,
			String base64Id
	) {
		if (repository.existsByName(username)) {
			return;
		}

		UserEntity user = new UserEntity(
				Bytes.fromBase64(base64Id),
				username,
				displayName
		);

		user.setPasswordHash(encoder.encode(password));
		user.setRole(role);
		user.setEmail(email);

		repository.save(user);

		System.out.println("✅ User created: " + username + " (" + role + ")");
	}
}
