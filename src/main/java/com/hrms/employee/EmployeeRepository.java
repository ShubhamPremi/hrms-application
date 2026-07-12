package com.hrms.employee;

import com.hrms.department.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    // Spring Data reads the method name and generates the SQL at startup
    // findByEmail → SELECT * FROM employees WHERE email = ?
    Optional<Employee> findByEmail(String email);

    // SELECT count(*) > 0 FROM employees WHERE email = ?
    boolean existsByEmail(String email);

    // SELECT * FROM employees WHERE department = ?
    List<Employee> findByDepartment(Department department);

    // SELECT * FROM employees WHERE status = ?
    List<Employee> findByStatus(EmployeeStatus status);

    // SELECT * FROM employees WHERE department = ? AND status = ?
    List<Employee> findByDepartmentAndStatus(Department department, EmployeeStatus status);

    // JOIN FETCH tells Hibernate: load employees AND their departments in ONE query
    // This eliminates N+1 for the getAllEmployees endpoint
    // "e.department" refers to the field name in the Employee entity, not the table name
    @Query("SELECT e FROM Employee e JOIN FETCH e.department")
    List<Employee> findAllWithDepartment();

    // With status filter
    @Query("SELECT e FROM Employee e JOIN FETCH e.department WHERE e.status = :status")
    List<Employee> findAllWithDepartmentByStatus(EmployeeStatus status);
}