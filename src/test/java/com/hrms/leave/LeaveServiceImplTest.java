package com.hrms.leave;

import com.hrms.common.exception.EmployeeNotFoundException;
import com.hrms.employee.Employee;
import com.hrms.employee.EmployeeRepository;
import com.hrms.employee.EmployeeStatus;
import com.hrms.leave.dto.ApplyLeaveRequest;
import com.hrms.leave.dto.LeaveRequestResponse;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)        // activates Mockito
@DisplayName("Leave Service — unit tests") // shown in test report
class LeaveServiceImplTest {

    // Mocks — these are fake objects, nothing real
    @Mock
    private LeaveRequestRepository leaveRequestRepository;

    @Mock
    private LeaveBalanceRepository leaveBalanceRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    // The real object being tested — Mockito injects the mocks above into it
    @InjectMocks
    private LeaveServiceImpl leaveService;

    // Test data — rebuilt before each test
    private Employee testEmployee;
    private LeaveBalance annualBalance;

    @BeforeEach
    void setUp() {
        // WHY here and not @BeforeAll: each test gets a fresh, unmodified employee
        // If one test mutates testEmployee, it doesn't affect the next test
        testEmployee = Employee.builder()
                .id(1L)
                .name("Shubham Premi")
                .email("shubham@hrms.com")
                .designation("Software Engineer")
                .salary(new BigDecimal("80000"))
                .joiningDate(LocalDate.of(2022, 1, 15))
                .status(EmployeeStatus.ACTIVE)
                .build();

        annualBalance = LeaveBalance.builder()
                .id(1L)
                .employee(testEmployee)
                .leaveType(LeaveType.ANNUAL)
                .year(2026)
                .totalDays(18)
                .usedDays(0)
                .build();
    }

    // ─── applyLeave tests ────────────────────────────────────────────────────

    @Test
    @DisplayName("should successfully apply for annual leave when balance is sufficient")
    void applyLeave_withSufficientBalance_shouldReturnPendingRequest() {
        // ARRANGE — configure what the mocks return
        // "When employeeRepository.findById(1L) is called, return testEmployee"
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(leaveRequestRepository.hasOverlappingLeave(
                eq(1L), any(LocalDate.class), any(LocalDate.class))).thenReturn(false);
        when(leaveBalanceRepository.findByEmployeeIdAndLeaveTypeAndYear(
                1L, LeaveType.ANNUAL, 2026)).thenReturn(Optional.of(annualBalance));

        // Mock the save — return the leave request with an ID set
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenAnswer(inv -> {
            LeaveRequest lr = inv.getArgument(0);
            // Simulate DB auto-generating ID on save
            LeaveRequest saved = LeaveRequest.builder()
                    .id(1L)
                    .employee(lr.getEmployee())
                    .leaveType(lr.getLeaveType())
                    .startDate(lr.getStartDate())
                    .endDate(lr.getEndDate())
                    .status(LeaveStatus.PENDING)
                    .reason(lr.getReason())
                    .build();
            return saved;
        });

        ApplyLeaveRequest request = new ApplyLeaveRequest(
                LeaveType.ANNUAL,
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 5),
                "Family vacation"
        );

        // ACT
        LeaveRequestResponse response = leaveService.applyLeave(1L, request);

        // ASSERT
        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo(LeaveStatus.PENDING);
        assertThat(response.leaveType()).isEqualTo(LeaveType.ANNUAL);
        assertThat(response.durationDays()).isEqualTo(5);
        assertThat(response.employeeName()).isEqualTo("Shubham Premi");

        // Verify the repository was called — confirms our service called save()
        verify(leaveRequestRepository, times(1)).save(any(LeaveRequest.class));
    }

    @Test
    @DisplayName("should throw exception when employee does not exist")
    void applyLeave_withInvalidEmployee_shouldThrowEmployeeNotFoundException() {
        // ARRANGE
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        ApplyLeaveRequest request = new ApplyLeaveRequest(
                LeaveType.ANNUAL,
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 5),
                "Test"
        );

        // ACT + ASSERT
        // assertThatThrownBy runs the lambda and verifies the exception
        assertThatThrownBy(() -> leaveService.applyLeave(999L, request))
                .isInstanceOf(EmployeeNotFoundException.class)
                .hasMessageContaining("999");

        // Verify save was NEVER called — if employee not found, we must not save
        verify(leaveRequestRepository, never()).save(any());
    }

    @Test
    @DisplayName("should throw exception when end date is before start date")
    void applyLeave_withEndDateBeforeStartDate_shouldThrowIllegalArgumentException() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));

        ApplyLeaveRequest request = new ApplyLeaveRequest(
                LeaveType.ANNUAL,
                LocalDate.of(2026, 8, 10),  // start: Aug 10
                LocalDate.of(2026, 8, 5),   // end:   Aug 5 — BEFORE start
                "Invalid dates"
        );

        assertThatThrownBy(() -> leaveService.applyLeave(1L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("End date cannot be before start date");
    }

    @Test
    @DisplayName("should throw exception when leave dates overlap with existing request")
    void applyLeave_withOverlappingDates_shouldThrowIllegalArgumentException() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        // Mock: yes, there IS an overlapping leave for these dates
        when(leaveRequestRepository.hasOverlappingLeave(
                eq(1L), any(LocalDate.class), any(LocalDate.class))).thenReturn(true);

        ApplyLeaveRequest request = new ApplyLeaveRequest(
                LeaveType.ANNUAL,
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 5),
                "Overlapping"
        );

        assertThatThrownBy(() -> leaveService.applyLeave(1L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("overlapping");
    }

    @Test
    @DisplayName("should throw exception when annual leave balance is insufficient")
    void applyLeave_withInsufficientBalance_shouldThrowIllegalArgumentException() {
        // Set up: employee has only 3 days remaining
        LeaveBalance lowBalance = LeaveBalance.builder()
                .id(1L)
                .employee(testEmployee)
                .leaveType(LeaveType.ANNUAL)
                .year(2026)
                .totalDays(18)
                .usedDays(15)    // 15 used, only 3 remaining
                .build();

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(leaveRequestRepository.hasOverlappingLeave(
                eq(1L), any(LocalDate.class), any(LocalDate.class))).thenReturn(false);
        when(leaveBalanceRepository.findByEmployeeIdAndLeaveTypeAndYear(
                1L, LeaveType.ANNUAL, 2026)).thenReturn(Optional.of(lowBalance));

        // Request 10 days but only 3 remaining
        ApplyLeaveRequest request = new ApplyLeaveRequest(
                LeaveType.ANNUAL,
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 10),  // 10 days
                "Too many days"
        );

        assertThatThrownBy(() -> leaveService.applyLeave(1L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Insufficient leave balance")
                .hasMessageContaining("3")    // available days
                .hasMessageContaining("10");  // requested days
    }

    @Test
    @DisplayName("should allow UNPAID leave without checking balance")
    void applyLeave_withUnpaidLeave_shouldSkipBalanceCheck() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(leaveRequestRepository.hasOverlappingLeave(
                eq(1L), any(LocalDate.class), any(LocalDate.class))).thenReturn(false);
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenAnswer(inv -> {
            LeaveRequest lr = inv.getArgument(0);
            return LeaveRequest.builder()
                    .id(1L).employee(lr.getEmployee())
                    .leaveType(lr.getLeaveType())
                    .startDate(lr.getStartDate()).endDate(lr.getEndDate())
                    .status(LeaveStatus.PENDING).build();
        });

        ApplyLeaveRequest request = new ApplyLeaveRequest(
                LeaveType.UNPAID,           // UNPAID — no balance needed
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 30),  // 30 days — impossible with annual balance
                "Extended leave"
        );

        LeaveRequestResponse response = leaveService.applyLeave(1L, request);

        assertThat(response.status()).isEqualTo(LeaveStatus.PENDING);
        assertThat(response.leaveType()).isEqualTo(LeaveType.UNPAID);

        // CRITICAL: balance repository must never be called for UNPAID leave
        verify(leaveBalanceRepository, never())
                .findByEmployeeIdAndLeaveTypeAndYear(any(), any(), anyInt());
    }

    // ─── approveLeave tests ──────────────────────────────────────────────────

    @Test
    @DisplayName("should throw exception when approving a non-PENDING request")
    void approveLeave_withAlreadyApprovedRequest_shouldThrowIllegalArgumentException() {
        LeaveRequest alreadyApproved = LeaveRequest.builder()
                .id(1L)
                .employee(testEmployee)
                .leaveType(LeaveType.ANNUAL)
                .startDate(LocalDate.of(2026, 8, 1))
                .endDate(LocalDate.of(2026, 8, 5))
                .status(LeaveStatus.APPROVED)    // already approved
                .build();

        when(leaveRequestRepository.findById(1L))
                .thenReturn(Optional.of(alreadyApproved));

        assertThatThrownBy(() -> leaveService.approveLeave(1L, 2L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("PENDING");
    }
}