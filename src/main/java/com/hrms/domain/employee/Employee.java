package com.hrms.domain.employee;

import com.hrms.department.Department;
import com.hrms.employee.EmployeeStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
@ToString
@Builder
public class Employee {

    private Long id;
    private String name;
    private String email;
    private String designation;
    private Department department;
    private double salary;
    private LocalDate joiningDate;
    private EmployeeStatus status;
}
