package com.hrms.department.dto;

import com.hrms.department.Department;
import java.time.LocalDateTime;

public record DepartmentResponse(
        Long id,
        String name,
        String description,
        LocalDateTime createdAt
) {
    public static DepartmentResponse from(Department department) {
        return new DepartmentResponse(
                department.getId(),
                department.getName(),
                department.getDescription(),
                department.getCreatedAt()
        );
    }
}