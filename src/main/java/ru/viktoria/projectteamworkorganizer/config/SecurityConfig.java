package ru.viktoria.projectteamworkorganizer.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.util.function.Supplier;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        configureAuthorization(http);
        configureLogin(http);
        configureLogout(http);

        return http.build();
    }

    private void configureAuthorization(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/public/**", "/login", "/register", "/css/**", "/js/**", "/images/**").permitAll()
                .requestMatchers("/admin", "/admin/**").hasRole("GLOBAL_ADMIN")
                .requestMatchers("/logs").hasRole("GLOBAL_ADMIN")
                .requestMatchers("/work-types", "/work-types/**").hasRole("GLOBAL_ADMIN")

                .requestMatchers(HttpMethod.GET, "/projects", "/projects/**")
                .access((authentication, context) -> authenticatedNotGlobalAdmin(authentication))

                .requestMatchers(HttpMethod.POST, "/projects", "/projects/**")
                .access((authentication, context) -> authenticatedNotGlobalAdmin(authentication))

                .requestMatchers("/tasks/**")
                .access((authentication, context) -> authenticatedNotGlobalAdmin(authentication))

                .anyRequest().authenticated()
        );
    }

    private AuthorizationDecision authenticatedNotGlobalAdmin(Supplier<? extends Authentication> authenticationSupplier) {
        Authentication authentication = authenticationSupplier.get();

        if (authentication == null || !authentication.isAuthenticated()) {
            return new AuthorizationDecision(false);
        }

        boolean anonymous = authentication.getAuthorities()
                .stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ANONYMOUS"));

        if (anonymous) {
            return new AuthorizationDecision(false);
        }

        boolean globalAdmin = authentication.getAuthorities()
                .stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_GLOBAL_ADMIN"));

        return new AuthorizationDecision(!globalAdmin);
    }

    private void configureLogin(HttpSecurity http) throws Exception {
        http.formLogin(form -> form
                .loginPage("/login")
                .successHandler((request, response, authentication) -> {
                    boolean globalAdmin = authentication.getAuthorities()
                            .stream()
                            .anyMatch(authority -> authority.getAuthority().equals("ROLE_GLOBAL_ADMIN"));

                    if (globalAdmin) {
                        response.sendRedirect("/admin");
                    } else {
                        response.sendRedirect("/projects");
                    }
                })
                .permitAll()
        );
    }

    private void configureLogout(HttpSecurity http) throws Exception {
        http.logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
                .permitAll()
        );
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
