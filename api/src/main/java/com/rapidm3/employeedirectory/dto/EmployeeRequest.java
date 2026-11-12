package com.rapidm3.employeedirectory.dto;

import com.rapidm3.employeedirectory.entity.EmployeeStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.Locale;

/**
 * Body for POST /employees and PUT /employees/{id}. Lengths match db/schema.sql.
 * Text is trimmed (and email lower-cased) in the constructor, i.e. before Bean Validation runs.
 */
public record EmployeeRequest(
        @NotBlank(message = "Name is required.")
        @Size(max = 160, message = "Name must be at most 160 characters.")
        String name,

        @NotBlank(message = "Email is required.")
        @Email(message = "Email must be a valid email address.")
        @Size(max = 254, message = "Email must be at most 254 characters.")
        String email,

        @NotBlank(message = "Job title is required.")
        @Size(max = 120, message = "Job title must be at most 120 characters.")
        String jobTitle,

        @NotNull(message = "Department is required.")
        @Positive(message = "Department is invalid.")
        Long departmentId,

        @NotNull(message = "Hire date is required.")
        LocalDate hireDate,

        @NotNull(message = "Status is required.")
        EmployeeStatus status) {

    public EmployeeRequest {
        name = trim(name);
        email = email == null ? null : email.trim().toLowerCase(Locale.ROOT);
        jobTitle = trim(jobTitle);
    }

    private static String trim(String value) {
        return value == null ? null : value.trim();
    }
}
