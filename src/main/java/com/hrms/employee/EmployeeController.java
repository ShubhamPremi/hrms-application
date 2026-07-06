package com.hrms.employee;

import com.hrms.common.response.ApiResponse;
import com.hrms.department.Department;
import com.hrms.employee.dto.CreateEmployeeRequest;
import com.hrms.employee.dto.EmployeeResponse;
import com.hrms.employee.dto.UpdateEmployeeRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
@Slf4j
public class EmployeeController {

    private final EmployeeService employeeService;

    @PostMapping
    public ResponseEntity<ApiResponse<EmployeeResponse>> createEmployee(
            @Valid @RequestBody CreateEmployeeRequest request) {
        EmployeeResponse response = employeeService.createEmployee(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)  // 201, not 200 — resource was created
                .body(ApiResponse.success("Employee created successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<EmployeeResponse>>> getAllEmployees() {
        List<EmployeeResponse> employees = employeeService.getAllEmployees();
        return ResponseEntity.ok(
                ApiResponse.success("Employees retrieved successfully", employees));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EmployeeResponse>> getEmployeeById(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success("Employee retrieved successfully",
                        employeeService.getEmployeeById(id)));
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<ApiResponse<EmployeeResponse>> getEmployeeByEmail(
            @PathVariable String email) {
        return ResponseEntity.ok(
                ApiResponse.success("Employee retrieved successfully",
                        employeeService.getEmployeeByEmail(email)));
    }

    @GetMapping("/department/{department}")
    public ResponseEntity<ApiResponse<List<EmployeeResponse>>> getByDepartment(
            @PathVariable Department department) {
        return ResponseEntity.ok(
                ApiResponse.success("Employees retrieved successfully",
                        employeeService.getEmployeesByDepartment(department)));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<EmployeeResponse>>> getByStatus(
            @PathVariable EmployeeStatus status) {
        return ResponseEntity.ok(
                ApiResponse.success("Employees retrieved successfully",
                        employeeService.getEmployeesByStatus(status)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EmployeeResponse>> updateEmployee(
            @PathVariable Long id,
            @Valid @RequestBody UpdateEmployeeRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success("Employee updated successfully",
                        employeeService.updateEmployee(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)  // 204 — success but no body
                .build();
    }
}