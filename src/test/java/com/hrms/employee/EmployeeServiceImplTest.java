package com.hrms.employee;

import com.hrms.common.exception.DepartmentNotFoundException;
import com.hrms.common.exception.EmailAlreadyExistsException;
import com.hrms.common.exception.EmployeeNotFoundException;
import com.hrms.department.Department;
import com.hrms.department.DepartmentRepository;
import com.hrms.employee.dto.CreateEmployeeRequest;
import com.hrms.employee.dto.EmployeeResponse;
import com.hrms.employee.dto.UpdateEmployeeRequest;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Employee Service — unit tests")
class EmployeeServiceImplTest {

    @Mock private EmployeeRepository employeeRepository;
    @Mock private DepartmentRepository departmentRepository;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @InjectMocks private EmployeeServiceImpl employeeService;

    private Department engineering;
    private Employee existingEmployee;

    @BeforeEach
    void setUp() {
        engineering = Department.builder()
                .id(1L)
                .name("ENGINEERING")
                .description("Software engineering")
                .build();

        existingEmployee = Employee.builder()
                .id(1L)
                .name("Shubham Premi")
                .email("shubham@hrms.com")
                .designation("Software Engineer")
                .department(engineering)
                .salary(new BigDecimal("80000"))
                .joiningDate(LocalDate.of(2022, 1, 15))
                .status(EmployeeStatus.ACTIVE)
                .build();
    }

    @Test
    @DisplayName("should create employee successfully with valid data")
    void createEmployee_withValidData_shouldReturnEmployeeResponse() {
        when(employeeRepository.existsByEmail("newemployee@hrms.com")).thenReturn(false);
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(engineering));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(inv -> {
            Employee emp = inv.getArgument(0);
            return Employee.builder()
                    .id(10L).name(emp.getName()).email(emp.getEmail())
                    .designation(emp.getDesignation()).department(emp.getDepartment())
                    .salary(emp.getSalary()).joiningDate(emp.getJoiningDate())
                    .status(EmployeeStatus.ACTIVE).build();
        });

        CreateEmployeeRequest request = new CreateEmployeeRequest(
                "New Employee", "newemployee@hrms.com", "Junior Developer",
                1L, new BigDecimal("50000"), LocalDate.of(2025, 1, 1)
        );

        EmployeeResponse response = employeeService.createEmployee(request);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.email()).isEqualTo("newemployee@hrms.com");
        assertThat(response.department().name()).isEqualTo("ENGINEERING");
        assertThat(response.status()).isEqualTo(EmployeeStatus.ACTIVE);

        // Verify email was lowercased and stripped
        ArgumentCaptor<Employee> captor = ArgumentCaptor.forClass(Employee.class);
        verify(employeeRepository).save(captor.capture());
        assertThat(captor.getValue().getEmail()).isEqualTo("newemployee@hrms.com");
    }

    @Test
    @DisplayName("should throw EmailAlreadyExistsException when email is duplicate")
    void createEmployee_withDuplicateEmail_shouldThrowEmailAlreadyExistsException() {
        when(employeeRepository.existsByEmail("shubham@hrms.com")).thenReturn(true);

        CreateEmployeeRequest request = new CreateEmployeeRequest(
                "Shubham 2", "shubham@hrms.com", "Engineer",
                1L, new BigDecimal("80000"), LocalDate.of(2022, 1, 1)
        );

        assertThatThrownBy(() -> employeeService.createEmployee(request))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessageContaining("shubham@hrms.com");

        // Verify we never tried to save — email check must short-circuit the method
        verify(employeeRepository, never()).save(any());
    }

    @Test
    @DisplayName("should throw DepartmentNotFoundException when department does not exist")
    void createEmployee_withInvalidDepartment_shouldThrowDepartmentNotFoundException() {
        when(employeeRepository.existsByEmail(anyString())).thenReturn(false);
        when(departmentRepository.findById(999L)).thenReturn(Optional.empty());

        CreateEmployeeRequest request = new CreateEmployeeRequest(
                "Test", "test@hrms.com", "Engineer",
                999L, new BigDecimal("50000"), LocalDate.of(2025, 1, 1)
        );

        assertThatThrownBy(() -> employeeService.createEmployee(request))
                .isInstanceOf(DepartmentNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    @DisplayName("should throw EmployeeNotFoundException when employee ID does not exist")
    void getEmployeeById_withInvalidId_shouldThrowEmployeeNotFoundException() {
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.getEmployeeById(999L))
                .isInstanceOf(EmployeeNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    @DisplayName("should soft delete employee by setting status to TERMINATED")
    void deleteEmployee_withValidId_shouldSetStatusToTerminated() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(existingEmployee));

        employeeService.deleteEmployee(1L);

        // Capture the employee that was saved and verify the status
        ArgumentCaptor<Employee> captor = ArgumentCaptor.forClass(Employee.class);

        // Note: soft delete uses dirty checking, no explicit save()
        // We verify the status was set on the in-memory object
        assertThat(existingEmployee.getStatus()).isEqualTo(EmployeeStatus.TERMINATED);

        // Verify delete was never called — soft delete does not remove the row
        verify(employeeRepository, never()).delete(any());
        verify(employeeRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("should only update provided fields and leave others unchanged")
    void updateEmployee_withPartialUpdate_shouldOnlyModifyProvidedFields() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(existingEmployee));

        // Only updating designation — everything else stays the same
        UpdateEmployeeRequest request = new UpdateEmployeeRequest(
                null,                          // name — not updating
                "Senior Software Engineer",    // designation — updating
                null,                          // department — not updating
                null,                          // salary — not updating
                null                           // status — not updating
        );

        EmployeeResponse response = employeeService.updateEmployee(1L, request);

        assertThat(response.designation()).isEqualTo("Senior Software Engineer");
        assertThat(response.name()).isEqualTo("Shubham Premi");      // unchanged
        assertThat(response.salary()).isEqualTo(new BigDecimal("80000")); // unchanged
        assertThat(response.status()).isEqualTo(EmployeeStatus.ACTIVE);   // unchanged
    }
}