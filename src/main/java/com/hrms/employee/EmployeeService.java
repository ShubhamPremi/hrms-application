package com.hrms.employee;

import com.hrms.department.Department;
import com.hrms.employee.dto.CreateEmployeeRequest;
import com.hrms.employee.dto.EmployeeResponse;
import com.hrms.employee.dto.UpdateEmployeeRequest;
import java.util.List;

public interface EmployeeService {

    EmployeeResponse createEmployee(CreateEmployeeRequest request);

    EmployeeResponse getEmployeeById(Long id);

    EmployeeResponse getEmployeeByEmail(String email);

    List<EmployeeResponse> getAllEmployees();

    List<EmployeeResponse> getEmployeesByDepartment(Long departmentId);

    List<EmployeeResponse> getEmployeesByStatus(EmployeeStatus status);

    EmployeeResponse updateEmployee(Long id, UpdateEmployeeRequest request);

    void deleteEmployee(Long id);
}