package com.hrms.payroll.strategy;

import com.hrms.employee.Employee;
import java.math.BigDecimal;

// The contract — every calculation strategy must implement this
// Adding a new employee type = add a new class implementing this interface
// Zero changes to existing strategy classes = Open/Closed Principle
public interface PayrollCalculationStrategy {

    // Calculate the gross pay for this employee for the given month
    BigDecimal calculateGrossPay(Employee employee, int month, int year);

    // Human-readable name — used in audit logs
    String getStrategyName();
}