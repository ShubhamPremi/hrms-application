package com.hrms.common.exception;

public class DepartmentNotFoundException extends RuntimeException {

    public DepartmentNotFoundException(Long id) {
        super("Department not found with id: " + id);
    }

    public DepartmentNotFoundException(String name) {
        super("Department not found with name: " + name);
    }
}