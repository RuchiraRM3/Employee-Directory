package com.rapidm3.employeedirectory.dto;

import com.rapidm3.employeedirectory.entity.Department;

public record DepartmentResponse(Long id, String name) {

    public static DepartmentResponse from(Department department) {
        return new DepartmentResponse(department.getId(), department.getName());
    }
}
