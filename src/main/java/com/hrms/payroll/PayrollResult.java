package com.hrms.payroll;

import java.math.BigDecimal;

public record PayrollResult(
        Long employeeId,
        int month,
        int year,
        BigDecimal baseSalary,
        BigDecimal grossPay,
        BigDecimal pfDeduction,
        BigDecimal taxDeduction,
        BigDecimal totalDeductions,
        BigDecimal netPay,
        String calculationStrategy
) {}