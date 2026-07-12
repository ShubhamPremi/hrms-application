package com.hrms.common.exception;

public class DepartmentAlreadyExistsException extends RuntimeException {

    public DepartmentAlreadyExistsException(String name) {
        super("A department with name '" + name + "' already exists");
    }
}