package com.hrms.payroll.strategy;

import com.hrms.employee.Employee;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Component  // Spring manages this as a bean — can be injected anywhere
@Slf4j
public class PermanentEmployeePayrollStrategy implements PayrollCalculationStrategy {

    @Override
    public BigDecimal calculateGrossPay(Employee employee, int month, int year) {
        BigDecimal baseSalary = employee.getSalary();

        // Tenure-based bonus: < 2 years = 10%, 2-5 years = 15%, > 5 years = 20%
        long monthsOfService = ChronoUnit.MONTHS.between(
                employee.getJoiningDate(), LocalDate.of(year, month, 1));

        BigDecimal bonusRate;
        if (monthsOfService > 60) {
            bonusRate = new BigDecimal("0.20");
        } else if (monthsOfService > 24) {
            bonusRate = new BigDecimal("0.15");
        } else {
            bonusRate = new BigDecimal("0.10");
        }

        BigDecimal bonus = baseSalary.multiply(bonusRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal grossPay = baseSalary.add(bonus);

        log.debug("Permanent employee {} — base: {}, bonus rate: {}%, gross: {}",
                employee.getName(), baseSalary, bonusRate.multiply(BigDecimal.valueOf(100)), grossPay);

        return grossPay;
    }

    @Override
    public String getStrategyName() {
        return "PERMANENT_EMPLOYEE_STRATEGY";
    }
}