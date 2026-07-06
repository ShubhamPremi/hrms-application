package com.hrms.employee.dto;

import com.hrms.department.Department;
import com.hrms.employee.Employee;
import com.hrms.employee.EmployeeStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record EmployeeResponse(
        Long id,
        String name,
        String email,
        String designation,
        Department department,
        BigDecimal salary,
        LocalDate joiningDate,
        EmployeeStatus status,
        LocalDateTime createdAt
) {
    // Static factory — converts Entity to DTO in one place
    // WHY: the controller and service never access entity fields directly
    // All mapping logic lives here — one place to change when the entity changes
    public static EmployeeResponse from(Employee employee) {
        return new EmployeeResponse(
                employee.getId(),
                employee.getName(),
                employee.getEmail(),
                employee.getDesignation(),
                employee.getDepartment(),
                employee.getSalary(),
                employee.getJoiningDate(),
                employee.getStatus(),
                employee.getCreatedAt()
        );
    }
}