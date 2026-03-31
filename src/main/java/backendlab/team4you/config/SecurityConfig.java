package backendlab.team4you.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.webauthn.management.JdbcPublicKeyCredentialUserEntityRepository;
import org.springframework.security.web.webauthn.management.JdbcUserCredentialRepository;
import org.springframework.security.web.webauthn.management.PublicKeyCredentialUserEntityRepository;
import org.springframework.security.web.webauthn.management.UserCredentialRepository;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        return http
                .authorizeHttpRequests(
                        authorizeHttp -> authorizeHttp
                                // Public endpoints
                                .requestMatchers( "/login", "/signup").permitAll()
                                .anyRequest().authenticated()

                                // Add elevated permissions

                )
                .webAuthn( passkeys -> passkeys
                        .rpId("localhost") //identity of the website
                        .allowedOrigins("http://localhost:8080")
                        .rpName("Passkey team4you")
                )
                .formLogin(form -> form.loginPage("/login"))
                .logout(logout -> logout.logoutSuccessUrl("/").permitAll())
                .build();
    }

    //todo: add jte called add-passkey but in thymelife

    @Bean
    PublicKeyCredentialUserEntityRepository jdbcPublicKeyCredentialRepository(JdbcOperations jdbc) {
        return new JdbcPublicKeyCredentialUserEntityRepository(jdbc);
    }

    @Bean
    UserCredentialRepository userCredentialRepository(JdbcOperations jdbc) {
        return new JdbcUserCredentialRepository(jdbc);
    }

    @Bean
    public UserDetailsService userDetailsService(){
        return username -> User.builder()
                .username(username)
                .password("{noop}") //no password needed, only passkey but idk if i have to change this for something?
                .roles("USER")
                .build();
    }
}
