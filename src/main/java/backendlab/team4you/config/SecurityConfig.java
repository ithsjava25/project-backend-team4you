package backendlab.team4you.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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
                                .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                                .requestMatchers( "/","/login", "login/webauthn", "/signup", "/error").permitAll()
                                .requestMatchers("/webauthn/authenticate/**").permitAll()

                                .requestMatchers("/profile", "/logout").authenticated()

                                .requestMatchers("/admin/**").hasRole("ADMIN")
                                .requestMatchers("/dashboard/**", "/profile/**").hasAnyRole("USER", "ADMIN")
                                .requestMatchers("/add-passkey").hasAnyRole("USER", "ADMIN")
                                .requestMatchers("/webauthn/register/**").hasAnyRole("USER", "ADMIN")

                                .anyRequest().authenticated()
                )
                .webAuthn( passkeys -> passkeys
                        .rpId("localhost") //identity of the website
                        .allowedOrigins("http://localhost:8080")
                        .rpName("Passkey team4you")
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/dashboard", true))
                .logout(logout -> logout.logoutSuccessUrl("/").permitAll())
                .build();
    }

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
        return username -> {
            String role = username.equals("admin@team4you.com") ? "ADMIN" : "USER";

            return User.builder()
                    .username(username)
                    .password("{noop}!LOCKED!") // Non-empty impossible-to-match password
                    .roles(role)
                    .accountLocked(true) // Prevent password-based login
                    .build();
        };
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
