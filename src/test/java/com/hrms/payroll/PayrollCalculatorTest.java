package com.hrms.payroll;

import com.hrms.department.Department;
import com.hrms.employee.Employee;
import com.hrms.employee.EmployeeStatus;
import com.hrms.payroll.strategy.ContractEmployeePayrollStrategy;
import com.hrms.payroll.strategy.PermanentEmployeePayrollStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Payroll Calculator — Strategy pattern unit tests")
class PayrollCalculatorTest {

    private PayrollCalculator calculator;
    private Department engineering;

    @BeforeEach
    void setUp() {
        // No mocking needed — these strategies have no external dependencies
        calculator = new PayrollCalculator(
                new PermanentEmployeePayrollStrategy(),
                new ContractEmployeePayrollStrategy()
        );
        engineering = Department.builder().id(1L).name("ENGINEERING").build();
    }

    @Test
    @DisplayName("Permanent employee with > 5 years tenure should get 20% bonus")
    void calculate_seniorPermanentEmployee_shouldApply20PercentBonus() {
        Employee senior = Employee.builder()
                .id(1L).name("Ananya Singh").email("ananya@hrms.com")
                .designation("Senior Engineer")
                .department(engineering)
                .salary(new BigDecimal("100000"))
                .joiningDate(LocalDate.now().minusYears(6))  // 6 years ago
                .status(EmployeeStatus.ACTIVE)
                .build();

        PayrollResult result = calculator.calculate(senior, 7, 2026);

        // base=100000, bonus=20%, gross=120000
        assertThat(result.grossPay()).isEqualByComparingTo(new BigDecimal("120000.00"));
        assertThat(result.calculationStrategy()).isEqualTo("PERMANENT_EMPLOYEE_STRATEGY");
        // PF = 12% of gross = 14400, Tax = 10% of gross = 12000
        assertThat(result.pfDeduction()).isEqualByComparingTo(new BigDecimal("14400.00"));
        assertThat(result.netPay()).isEqualByComparingTo(new BigDecimal("93600.00"));
    }

    @Test
    @DisplayName("Contract employee should receive fixed rate with no bonus")
    void calculate_contractEmployee_shouldReceiveFixedRate() {
        Employee contractor = Employee.builder()
                .id(2L).name("Freelancer").email("freelancer@hrms.com")
                .designation("Contract Developer")
                .department(engineering)
                .salary(new BigDecimal("80000"))
                .joiningDate(LocalDate.now().minusMonths(3))
                .status(EmployeeStatus.ACTIVE)
                .build();

        PayrollResult result = calculator.calculate(contractor, 7, 2026);

        // No bonus — contractor gets exact salary
        assertThat(result.grossPay()).isEqualByComparingTo(new BigDecimal("80000.00"));
        assertThat(result.calculationStrategy()).isEqualTo("CONTRACT_EMPLOYEE_STRATEGY");
    }

    @Test
    @DisplayName("Should throw exception for terminated employee")
    void calculate_terminatedEmployee_shouldThrowIllegalStateException() {
        Employee terminated = Employee.builder()
                .id(3L).name("Ex Employee").email("ex@hrms.com")
                .designation("Engineer").department(engineering)
                .salary(new BigDecimal("60000"))
                .joiningDate(LocalDate.now().minusYears(1))
                .status(EmployeeStatus.TERMINATED)
                .build();

        assertThatThrownBy(() -> calculator.calculate(terminated, 7, 2026))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("TERMINATED");
    }
}