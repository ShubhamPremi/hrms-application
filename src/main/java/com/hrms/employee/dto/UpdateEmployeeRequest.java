package com.hrms.employee.dto;

import com.hrms.department.Department;
import com.hrms.employee.EmployeeStatus;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record UpdateEmployeeRequest(

        @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
        String name,

        @Size(max = 100, message = "Designation must not exceed 100 characters")
        String designation,

        Department department,

        @DecimalMin(value = "0.0", inclusive = false, message = "Salary must be greater than 0")
        @Digits(integer = 13, fraction = 2, message = "Invalid salary format")
        BigDecimal salary,

        EmployeeStatus status
) {}