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
						"dev",
						"admin"
				);

				devAdmin.setPasswordHash(encoder.encode("123456"));
				devAdmin.setRole(UserRole.ADMIN);
				devAdmin.setEmail("devadmin@gmail.com");

				repository.save(devAdmin);
				System.out.println("✅ Admin created");

				UserEntity devUser = new UserEntity(
						Bytes.fromBase64("dXNlcg=="),
						"user",
						"user"
				);

				devUser.setPasswordHash(encoder.encode("1234"));
				devUser.setRole(UserRole.USER);
				devUser.setEmail("devuser@gmail.com");

				repository.save(devUser);
				System.out.println("✅ User created");

				UserEntity devCaseOfficer = new UserEntity(
						Bytes.fromBase64("YLRtaW5="),
						"officer",
						"caseofficer"
				);

				devCaseOfficer.setPasswordHash(encoder.encode("abcd"));
				devCaseOfficer.setRole(UserRole.CASE_OFFICER);
				devCaseOfficer.setEmail("devcaseofficer@gmail.com");

				repository.save(devCaseOfficer);
				System.out.println("✅ Case officer created");
			}
		};
	}
}
