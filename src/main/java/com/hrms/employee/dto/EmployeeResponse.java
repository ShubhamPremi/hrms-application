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
        DepartmentInfo department,
        BigDecimal salary,
        LocalDate joiningDate,
        EmployeeStatus status,
        LocalDateTime createdAt
) {
    // Nested record — a record inside a record is perfectly valid Java 17
    public record DepartmentInfo(Long id, String name) {}


    // Static factory — converts Entity to DTO in one place
    // WHY: the controller and service never access entity fields directly
    // All mapping logic lives here — one place to change when the entity changes
    public static EmployeeResponse from(Employee employee) {
        return new EmployeeResponse(
                employee.getId(),
                employee.getName(),
                employee.getEmail(),
                employee.getDesignation(),
                new DepartmentInfo(
                        employee.getDepartment().getId(),     // ← this triggers lazy load
                        employee.getDepartment().getName()    // ← if not fetched, N+1 starts here
                ),
                employee.getSalary(),
                employee.getJoiningDate(),
                employee.getStatus(),
                employee.getCreatedAt()
        );
    }
}