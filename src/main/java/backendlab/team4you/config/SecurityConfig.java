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
import backendlab.team4you.user.UserEntity;
import backendlab.team4you.user.UserService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.config.annotation.web.configurers.WebAuthnConfigurer;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http,
                                            CustomAuthenticationSuccessHandler successHandler) throws Exception {

        return http
                .authorizeHttpRequests(
                        authorizeHttp -> authorizeHttp
                                // Public endpoints
                                .requestMatchers("/static/css/**", "/js/**", "/images/**").permitAll()
                                .requestMatchers( "/","/login", "/login/webauthn", "/signup", "/error").permitAll()
                                .requestMatchers("/webauthn/authenticate/**").permitAll()

                                .requestMatchers("/admin/**").hasRole("ADMIN")
                                .requestMatchers("/dashboard", "/profile/**").hasAnyRole("USER", "ADMIN")
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
                        .successHandler(successHandler)
                )
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
    public UserDetailsService userDetailsService(UserService userService){
        return username -> {
            UserEntity user = userService.findByName(username);
            if (user == null) {
                throw new UsernameNotFoundException("User not found: " + username);
            }

            return User.builder()
                    .username(user.getName())
                    .password(user.getPasswordHash())
                    .roles(user.getRole())
                    .accountLocked(false)
                    .build();
        };
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
