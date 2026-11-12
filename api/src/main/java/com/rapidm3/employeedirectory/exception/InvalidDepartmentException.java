package com.rapidm3.employeedirectory.exception;

public class InvalidDepartmentException extends RuntimeException {

    public InvalidDepartmentException() {
        super("Department does not exist.");
    }
}
