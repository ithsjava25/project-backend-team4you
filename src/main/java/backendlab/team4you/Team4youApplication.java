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
						Bytes.fromBase64("YWRtaW4="),
						"dev",           // name (username)
						"admin"      // displayName
				);

				devAdmin.setPasswordHash(encoder.encode("123456"));
				devAdmin.setRole("ROLE_ADMIN");
				devAdmin.setEmail("devadmin@gmail.com");

				repository.save(devAdmin);
				System.out.println("✅ Admin created");

				UserEntity devUser = new UserEntity(
						Bytes.fromBase64("dXNlcg=="),
						"user",           // name (username)
						"user"      // displayName
				);

				devUser.setPasswordHash(encoder.encode("1234"));
				devUser.setRole("ROLE_USER");
				devUser.setEmail("devuser@gmail.com");

				repository.save(devUser);
				System.out.println("✅ User created");
			}
		};
	}
}
