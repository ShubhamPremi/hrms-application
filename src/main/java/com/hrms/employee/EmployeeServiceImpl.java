package com.hrms.employee;

import com.hrms.common.exception.EmailAlreadyExistsException;
import com.hrms.common.exception.EmployeeNotFoundException;
import com.hrms.department.Department;
import com.hrms.employee.dto.CreateEmployeeRequest;
import com.hrms.employee.dto.EmployeeResponse;
import com.hrms.employee.dto.UpdateEmployeeRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;

    @Override
    @Transactional
    public EmployeeResponse createEmployee(CreateEmployeeRequest request) {
        log.info("Creating employee with email: {}", request.email());

        // Check uniqueness BEFORE trying to insert
        // WHY: letting the DB throw a unique constraint violation gives a cryptic error
        //      checking here lets us throw a meaningful EmailAlreadyExistsException
        if (employeeRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException(request.email());
        }

        Employee employee = Employee.builder()
                .name(request.name())
                .email(request.email().toLowerCase().strip())
                .designation(request.designation())
                .department(request.department())
                .salary(request.salary())
                .joiningDate(request.joiningDate())
                .status(EmployeeStatus.ACTIVE)
                .build();

        Employee saved = employeeRepository.save(employee);
        log.info("Employee created with id: {}", saved.getId());
        return EmployeeResponse.from(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponse getEmployeeById(Long id) {
        return employeeRepository.findById(id)
                .map(EmployeeResponse::from)
                .orElseThrow(() -> new EmployeeNotFoundException(id));
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponse getEmployeeByEmail(String email) {
        return employeeRepository.findByEmail(email)
                .map(EmployeeResponse::from)
                .orElseThrow(() -> new EmployeeNotFoundException(email));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeResponse> getAllEmployees() {
        return employeeRepository.findAll()
                .stream()
                .map(EmployeeResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeResponse> getEmployeesByDepartment(Department department) {
        return employeeRepository.findByDepartment(department)
                .stream()
                .map(EmployeeResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeResponse> getEmployeesByStatus(EmployeeStatus status) {
        return employeeRepository.findByStatus(status)
                .stream()
                .map(EmployeeResponse::from)
                .toList();
    }

    @Override
    @Transactional
    public EmployeeResponse updateEmployee(Long id, UpdateEmployeeRequest request) {
        log.info("Updating employee with id: {}", id);

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException(id));

        // Only update fields that were actually sent — null means "don't change"
        if (request.name() != null)
            employee.setName(request.name());
        if (request.designation() != null)
            employee.setDesignation(request.designation());
        if (request.department() != null)
            employee.setDepartment(request.department());
        if (request.salary() != null)
            employee.setSalary(request.salary());
        if (request.status() != null)
            employee.setStatus(request.status());

        // No explicit save() needed — Hibernate tracks changes to managed entities
        // WHY: inside a @Transactional method, the entity returned by findById is
        //      "managed" — Hibernate watches it. At transaction commit, it automatically
        //      detects changes and fires an UPDATE SQL. This is "dirty checking".
        return EmployeeResponse.from(employee);
    }

    @Override
    @Transactional
    public void deleteEmployee(Long id) {
        log.info("Soft deleting employee with id: {}", id);

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException(id));

        // SOFT DELETE — we never hard-delete employee records
        // WHY: payroll history, leave records, audit logs all reference this employee.
        //      Hard deleting causes FK constraint violations and destroys audit history.
        //      HR and finance regulations require keeping records for 7+ years.
        employee.setStatus(EmployeeStatus.TERMINATED);
    }
}