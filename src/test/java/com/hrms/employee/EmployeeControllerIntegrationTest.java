package com.hrms.employee;

import com.hrms.AbstractIntegrationTest;
import com.hrms.auth.dto.LoginRequest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import org.springframework.http.HttpStatus;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

// Naming convention: ClassUnderTest + IntegrationTest
// This tests the full HTTP stack: request → controller → service → real DB → response
@DisplayName("Employee API — integration tests")
class EmployeeControllerIntegrationTest extends AbstractIntegrationTest {

    private String adminToken;

    @BeforeEach
    void setUp() {
        // RestAssured base URI — points to our running test server
        RestAssured.baseURI = baseUrl();

        // Get a JWT token for all authenticated tests
        // The admin user is seeded by V5 migration which Flyway runs on the test DB
        adminToken = given()
                .contentType(ContentType.JSON)
                .body(new LoginRequest("admin@hrms.com", "admin123"))
                .when()
                .post("/api/v1/auth/login")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .path("data.accessToken");
    }

    @Test
    @DisplayName("GET /api/v1/employees should return all employees with 200")
    void getAllEmployees_shouldReturnAllEmployeesWithHttp200() {
        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .get("/api/v1/employees")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("success", equalTo(true))
                .body("data", notNullValue())
                .body("data.size()", greaterThan(0));
    }

    @Test
    @DisplayName("GET /api/v1/employees without token should return 401")
    void getAllEmployees_withoutToken_shouldReturn401() {
        given()
                .when()
                .get("/api/v1/employees")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .body("success", equalTo(false));
    }

    @Test
    @DisplayName("POST /api/v1/employees with valid data should create employee and return 201")
    void createEmployee_withValidData_shouldReturn201() {
        String requestBody = """
                {
                    "name": "Integration Test Employee",
                    "email": "integration.test@hrms.com",
                    "designation": "Test Engineer",
                    "departmentId": 1,
                    "salary": 60000.00,
                    "joiningDate": "2025-01-01"
                }
                """;

        given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/api/v1/employees")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .body("success", equalTo(true))
                .body("data.name", equalTo("Integration Test Employee"))
                .body("data.email", equalTo("integration.test@hrms.com"))
                .body("data.status", equalTo("ACTIVE"))
                .body("data.department.name", equalTo("ENGINEERING"));
    }

    @Test
    @DisplayName("POST /api/v1/employees with duplicate email should return 409")
    void createEmployee_withDuplicateEmail_shouldReturn409() {
        String requestBody = """
                {
                    "name": "Priya Sharma Duplicate",
                    "email": "priya@hrms.com",
                    "designation": "HR Manager",
                    "departmentId": 2,
                    "salary": 75000.00,
                    "joiningDate": "2025-01-01"
                }
                """;

        given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/api/v1/employees")
                .then()
                .statusCode(HttpStatus.CONFLICT.value())
                .body("success", equalTo(false))
                .body("message", containsString("priya@hrms.com"));
    }

    @Test
    @DisplayName("POST /api/v1/employees with blank name should return 400 with validation errors")
    void createEmployee_withBlankName_shouldReturn400() {
        String requestBody = """
                {
                    "name": "",
                    "email": "valid@hrms.com",
                    "designation": "Engineer",
                    "departmentId": 1,
                    "salary": 50000.00,
                    "joiningDate": "2025-01-01"
                }
                """;

        given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/api/v1/employees")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("success", equalTo(false))
                .body("errors", hasItem(containsString("Name")));
    }

    @Test
    @DisplayName("GET /api/v1/employees/999 should return 404")
    void getEmployeeById_withNonExistentId_shouldReturn404() {
        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .get("/api/v1/employees/999")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .body("success", equalTo(false))
                .body("message", containsString("999"));
    }
}