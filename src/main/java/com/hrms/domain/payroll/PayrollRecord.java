package com.hrms.domain.payroll;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;


@Getter
@AllArgsConstructor
@ToString
public class PayrollRecord {
    private Long id;
    private Long employeeId;
    private int month;
    private int year;
    private double basicSalary;
    private double bonus;
    private double deductions;
    private LocalDateTime processedAt;

    public double getNetPay() {
        return basicSalary + bonus - deductions;
    }
}
