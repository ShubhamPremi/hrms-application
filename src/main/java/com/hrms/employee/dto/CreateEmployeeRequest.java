package com.hrms.employee.dto;

import com.hrms.department.Department;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateEmployeeRequest(

        @NotBlank(message = "Name is required")
        @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
        String name,

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        @Size(max = 150, message = "Email must not exceed 150 characters")
        String email,

        @NotBlank(message = "Designation is required")
        @Size(max = 100, message = "Designation must not exceed 100 characters")
        String designation,

        @NotNull(message = "Department ID is required")
        Long departmentId,

        @NotNull(message = "Salary is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Salary must be greater than 0")
        @Digits(integer = 13, fraction = 2, message = "Invalid salary format")
        BigDecimal salary,

        @NotNull(message = "Joining date is required")
        @PastOrPresent(message = "Joining date cannot be in the future")
        LocalDate joiningDate
) {}