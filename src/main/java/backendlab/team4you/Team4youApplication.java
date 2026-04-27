package backendlab.team4you;

import backendlab.team4you.user.UserRepository;
import backendlab.team4you.user.UserEntity;
import backendlab.team4you.user.UserRole;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
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

				UserEntity officer1 = new UserEntity(
						Bytes.fromBase64("YLRtaW5="),
						"officer1",
						"caseofficer1"
				);

				officer1.setPasswordHash(encoder.encode("officer1"));
				officer1.setRole(UserRole.CASE_OFFICER);
				officer1.setEmail("devcaseofficer@gmail.com");

				repository.save(officer1);
				System.out.println("✅ Case officer 1 created");

				UserEntity officer2 = new UserEntity(
						Bytes.fromBase64("YLRtaG6="),
						"officer2",
						"caseofficer2"
				);

				officer2.setPasswordHash(encoder.encode("officer2"));
				officer2.setRole(UserRole.CASE_OFFICER);
				officer2.setEmail("devcaseofficer2@gmail.com");

				repository.save(officer2);
				System.out.println("✅ Case officer 2 created");
			}
		};
	}
}
