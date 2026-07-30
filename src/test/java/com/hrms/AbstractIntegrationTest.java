package com.hrms;

import com.hrms.auth.UserRepository;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

// All integration tests extend this class
// @Testcontainers activates automatic container lifecycle management
// @SpringBootTest starts the full Spring context on a random port
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class AbstractIntegrationTest {

    // Static = one container shared across ALL tests in the class
    // WHY static: starting a MySQL container takes ~5 seconds
    // If non-static, every test method gets a new container = very slow
    @Container
    static final MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("hrms_test_db")
            .withUsername("hrms_user")
            .withPassword("hrms_password")
            .withEnv("TZ", "UTC");

    // @DynamicPropertySource overrides application.yml datasource settings
    // WITH the TestContainers MySQL URL instead of localhost:3306
    // WHY dynamic: the container port is randomly assigned — not fixed
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () ->
                mysql.getJdbcUrl() + "?useSSL=false&serverTimezone=UTC" +
                        "&allowPublicKeyRetrieval=true&characterEncoding=UTF-8");
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
    }

    // The port Spring started on — used to build request URLs
    @LocalServerPort
    protected int port;

    protected String baseUrl() {
        return "http://localhost:" + port;
    }

    @BeforeAll
    static void fixAdminPassword(
            @Autowired UserRepository userRepository,
            @Autowired PasswordEncoder passwordEncoder) {
        userRepository.findByEmail("admin@hrms.com").ifPresent(user -> {
            user.setPassword(passwordEncoder.encode("admin123"));
            userRepository.save(user);
        });
    }
}