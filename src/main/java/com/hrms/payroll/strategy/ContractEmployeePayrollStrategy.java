package com.hrms.payroll.strategy;

import com.hrms.employee.Employee;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Slf4j
public class ContractEmployeePayrollStrategy implements PayrollCalculationStrategy {

    @Override
    public BigDecimal calculateGrossPay(Employee employee, int month, int year) {
        // Contractors get their agreed monthly rate — no bonus, no deductions
        // The salary field stores the monthly contract rate for contractors
        log.debug("Contract employee {} — fixed rate: {}", employee.getName(), employee.getSalary());
        return employee.getSalary();
    }

    @Override
    public String getStrategyName() {
        return "CONTRACT_EMPLOYEE_STRATEGY";
    }
}