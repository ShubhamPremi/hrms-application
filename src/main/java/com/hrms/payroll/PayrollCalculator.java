package com.hrms.payroll;

import com.hrms.employee.Employee;
import com.hrms.employee.EmployeeStatus;
import com.hrms.payroll.strategy.ContractEmployeePayrollStrategy;
import com.hrms.payroll.strategy.PayrollCalculationStrategy;
import com.hrms.payroll.strategy.PermanentEmployeePayrollStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
@Slf4j
public class PayrollCalculator {

    // Standard deduction rates — in production these come from config
    private static final BigDecimal PF_RATE         = new BigDecimal("0.12");  // 12% PF
    private static final BigDecimal INCOME_TAX_RATE = new BigDecimal("0.10");  // 10% tax

    private final PermanentEmployeePayrollStrategy permanentStrategy;
    private final ContractEmployeePayrollStrategy contractStrategy;

    public PayrollCalculator(PermanentEmployeePayrollStrategy permanentStrategy,
                             ContractEmployeePayrollStrategy contractStrategy) {
        this.permanentStrategy = permanentStrategy;
        this.contractStrategy = contractStrategy;
    }

    public PayrollResult calculate(Employee employee, int month, int year) {
        if (employee.getStatus() == EmployeeStatus.TERMINATED ||
                employee.getStatus() == EmployeeStatus.RESIGNED) {
            throw new IllegalStateException(
                    "Cannot process payroll for " + employee.getStatus() + " employee");
        }

        // Strategy selection — in production this would use employee type
        // For now we use designation as a proxy
        PayrollCalculationStrategy strategy = selectStrategy(employee);
        log.info("Processing payroll for {} using {}", employee.getName(), strategy.getStrategyName());

        BigDecimal grossPay = strategy.calculateGrossPay(employee, month, year);
        BigDecimal pfDeduction = grossPay.multiply(PF_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal taxDeduction = grossPay.multiply(INCOME_TAX_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalDeductions = pfDeduction.add(taxDeduction);
        BigDecimal netPay = grossPay.subtract(totalDeductions);

        return new PayrollResult(
                employee.getId(), month, year,
                employee.getSalary(), grossPay,
                pfDeduction, taxDeduction, totalDeductions, netPay,
                strategy.getStrategyName()
        );
    }

    private PayrollCalculationStrategy selectStrategy(Employee employee) {
        // Simple heuristic — real system would have an employee_type column
        String designation = employee.getDesignation().toLowerCase();
        if (designation.contains("contract") || designation.contains("freelance")) {
            return contractStrategy;
        }
        return permanentStrategy;  // default — most employees are permanent
    }
}