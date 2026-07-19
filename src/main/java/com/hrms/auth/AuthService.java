package com.hrms.auth;

import com.hrms.auth.dto.AuthResponse;
import com.hrms.auth.dto.LoginRequest;
import com.hrms.auth.dto.RegisterRequest;
import com.hrms.common.exception.EmployeeNotFoundException;
import com.hrms.employee.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Value("${application.security.jwt.expiration}")
    private long jwtExpiration;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Registering new user: {}", request.email());

        if (userRepository.existsByEmail(request.email())) {
            throw new RuntimeException("Email already registered: " + request.email());
        }

        User.UserBuilder userBuilder = User.builder()
                .email(request.email())
                // BCrypt hashes the password — we NEVER store plain text passwords
                // BCrypt is intentionally slow — makes brute force attacks expensive
                .password(passwordEncoder.encode(request.password()))
                .role(request.role())
                .isActive(true);

        // If employeeId provided, link to employee record
        if (request.employeeId() != null) {
            var employee = employeeRepository.findById(request.employeeId())
                    .orElseThrow(() -> new EmployeeNotFoundException(request.employeeId()));
            userBuilder.employee(employee);
        }

        User saved = userRepository.save(userBuilder.build());
        log.info("User registered with id: {}", saved.getId());

        String accessToken = jwtService.generateToken(saved);
        String refreshToken = jwtService.generateRefreshToken(saved);

        return AuthResponse.of(accessToken, refreshToken,
                saved.getEmail(), saved.getRole().name(), jwtExpiration);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for: {}", request.email());

        // AuthenticationManager does two things:
        // 1. Calls UserDetailsService.loadUserByUsername(email)
        // 2. Compares the provided password against the stored BCrypt hash
        // If either fails, it throws BadCredentialsException → 401
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        // If we reach here, credentials are valid
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("User not found after auth"));

        if (!user.isActive()) {
            throw new RuntimeException("Account is deactivated");
        }

        String accessToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        log.info("Login successful for: {}", request.email());
        return AuthResponse.of(accessToken, refreshToken,
                user.getEmail(), user.getRole().name(), jwtExpiration);
    }
}