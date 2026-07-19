package com.hrms.config;

import com.hrms.auth.JwtAuthenticationFilter;
import com.hrms.auth.UserDetailsServiceImpl;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity          // enables @PreAuthorize on individual methods
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsServiceImpl userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF disabled — REST APIs use JWT, not session cookies
                // CSRF protects against cookie-based session hijacking
                // With stateless JWT there are no sessions to hijack
                .csrf(AbstractHttpConfigurer::disable)

                // Request authorisation rules — order matters, first match wins
                .authorizeHttpRequests(auth -> auth

                        // Public endpoints — no token required
                        .requestMatchers("/api/v1/auth/**").permitAll()

                        // Swagger UI — accessible without auth in development
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()

                        // Department reads — any authenticated user
                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/departments/**").authenticated()

                        // Department writes — HR and ADMIN only
                        .requestMatchers(HttpMethod.POST,
                                "/api/v1/departments/**").hasAnyRole("ADMIN", "HR")
                        .requestMatchers(HttpMethod.PUT,
                                "/api/v1/departments/**").hasAnyRole("ADMIN", "HR")
                        .requestMatchers(HttpMethod.DELETE,
                                "/api/v1/departments/**").hasRole("ADMIN")

                        // Employee reads — any authenticated user
                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/employees/**").authenticated()

                        // Employee writes — HR and ADMIN only
                        .requestMatchers(HttpMethod.POST,
                                "/api/v1/employees/**").hasAnyRole("ADMIN", "HR")
                        .requestMatchers(HttpMethod.PUT,
                                "/api/v1/employees/**").hasAnyRole("ADMIN", "HR")
                        .requestMatchers(HttpMethod.DELETE,
                                "/api/v1/employees/**").hasAnyRole("ADMIN", "HR")

                        // Leave — employees can view their own, HR/Admin can manage all
                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/leave/**").authenticated()
                        .requestMatchers(HttpMethod.POST,
                                "/api/v1/leave/**").authenticated()
                        .requestMatchers("/api/v1/leave/pending").hasAnyRole("ADMIN", "HR")
                        .requestMatchers(HttpMethod.PUT,
                                "/api/v1/leave/*/approve").hasAnyRole("ADMIN", "HR")
                        .requestMatchers(HttpMethod.PUT,
                                "/api/v1/leave/*/reject").hasAnyRole("ADMIN", "HR")

                        // Everything else requires authentication
                        .anyRequest().authenticated()
                )

                // Stateless — no HTTP session, no cookies
                // Every request must carry its own JWT — the server remembers nothing
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            // WHY: this fires when an unauthenticated request hits a secured endpoint
                            // Without this, Spring returns 403 for both "no token" and "wrong role"
                            // With this, we correctly return 401 for "no token" and 403 for "wrong role"
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json");
                            response.getWriter().write(
                                    "{\"success\":false,\"message\":\"Authentication required" +
                                            " — please login to access this resource\",\"errors\":" +
                                            "[\"No authentication token provided\"]}"
                            );
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            // WHY: this fires when an authenticated user lacks the required role
                            // Returns 403 with a clear message instead of Spring's default HTML error
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType("application/json");
                            response.getWriter().write(
                                    "{\"success\":false,\"message\":\"Access denied" +
                                            " — you do not have permission to perform this action\",\"errors\":" +
                                            "[\"Insufficient role\"]}"
                            );
                        })
                )

                // Register our DaoAuthenticationProvider
                .authenticationProvider(authenticationProvider())

                // Insert our JWT filter BEFORE Spring's default login filter
                // WHY before UsernamePasswordAuthenticationFilter:
                // Our filter runs first, sets authentication in SecurityContextHolder
                // The default filter then sees auth is already set and does nothing
                .addFilterBefore(jwtAuthFilter,
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        // DaoAuthenticationProvider connects Spring Security to our database
        // It uses UserDetailsService to load the user and PasswordEncoder to
        // verify the password hash
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        // AuthenticationManager is what AuthService calls to verify credentials
        // Spring Boot auto-configures this — we just expose it as a Bean
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCrypt with strength 12 — 2^12 = 4096 iterations
        // Each hash takes ~250ms on modern hardware — fast enough for users,
        // slow enough to make brute force attacks impractical
        // Strength 10 = default, 12 = recommended for production
        return new BCryptPasswordEncoder(12);
    }
}