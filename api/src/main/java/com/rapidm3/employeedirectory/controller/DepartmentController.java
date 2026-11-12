package com.rapidm3.employeedirectory.controller;

import com.rapidm3.employeedirectory.dto.DepartmentResponse;
import com.rapidm3.employeedirectory.repository.DepartmentRepository;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** Read-only lookup with no business rules, so it uses the repository directly instead of a pass-through service. */
@RestController
public class DepartmentController {

    private final DepartmentRepository departments;

    public DepartmentController(DepartmentRepository departments) {
        this.departments = departments;
    }

    @GetMapping("/departments")
    public List<DepartmentResponse> list() {
        return departments.findAll(Sort.by("name")).stream().map(DepartmentResponse::from).toList();
    }
}
