package com.hrms.department;

import com.hrms.common.exception.DepartmentAlreadyExistsException;
import com.hrms.common.exception.DepartmentNotFoundException;
import com.hrms.department.dto.CreateDepartmentRequest;
import com.hrms.department.dto.DepartmentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;

    @Override
    @Transactional
    public DepartmentResponse createDepartment(CreateDepartmentRequest request) {
        log.info("Creating department: {}", request.name());

        if (departmentRepository.existsByName(request.name())) {
            throw new DepartmentAlreadyExistsException(request.name());
        }

        Department department = Department.builder()
                .name(request.name().toUpperCase().strip())
                .description(request.description())
                .build();

        return DepartmentResponse.from(departmentRepository.save(department));
    }

    @Override
    @Transactional(readOnly = true)
    public DepartmentResponse getDepartmentById(Long id) {
        return departmentRepository.findById(id)
                .map(DepartmentResponse::from)
                .orElseThrow(() -> new DepartmentNotFoundException(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentResponse> getAllDepartments() {
        return departmentRepository.findAll()
                .stream()
                .map(DepartmentResponse::from)
                .toList();
    }

    @Override
    @Transactional
    public DepartmentResponse updateDepartment(Long id, CreateDepartmentRequest request) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new DepartmentNotFoundException(id));

        if (request.name() != null) department.setName(request.name().toUpperCase().strip());
        if (request.description() != null) department.setDescription(request.description());

        return DepartmentResponse.from(department);
    }

    @Override
    @Transactional
    public void deleteDepartment(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new DepartmentNotFoundException(id));

        // This will throw a DataIntegrityViolationException if employees exist
        // in this department due to the FK constraint we set in V4 migration
        // We will handle this properly when we add a departmentHasEmployees check
        departmentRepository.delete(department);
    }
}