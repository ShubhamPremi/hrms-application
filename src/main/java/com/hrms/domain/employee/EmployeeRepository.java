package com.hrms.domain.employee;

import java.util.List;
import java.util.Optional;

public class EmployeeRepository {
    // Returns Optional — signals to the caller: this might not exist
    public Optional<Employee> findById(Long id, List<Employee> employees) {
        return employees.stream()
                .filter(emp -> emp.getId().equals(id))
                .findFirst();
    }

    // Returns Optional — name lookup might fail
    public Optional<Employee> findByEmail(String email, List<Employee> employees) {
        return employees.stream()
                .filter(emp -> emp.getEmail().equalsIgnoreCase(email))
                .findFirst();
    }
}
