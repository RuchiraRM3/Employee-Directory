package com.rapidm3.employeedirectory.dto;

import com.rapidm3.employeedirectory.entity.Employee;
import com.rapidm3.employeedirectory.entity.EmployeeStatus;
import java.time.LocalDate;

public record EmployeeResponse(
        Long id,
        String name,
        String email,
        String jobTitle,
        DepartmentResponse department,
        LocalDate hireDate,
        EmployeeStatus status) {

    public static EmployeeResponse from(Employee e) {
        return new EmployeeResponse(e.getId(), e.getName(), e.getEmail(), e.getJobTitle(),
                DepartmentResponse.from(e.getDepartment()), e.getHireDate(), e.getStatus());
    }
}
