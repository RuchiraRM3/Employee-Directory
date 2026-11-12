package com.rapidm3.employeedirectory.exception;

public class EmployeeNotFoundException extends RuntimeException {

    public EmployeeNotFoundException(long id) {
        super("Employee " + id + " was not found.");
    }
}
