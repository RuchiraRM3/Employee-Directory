package com.rapidm3.employeedirectory.repository;

import com.rapidm3.employeedirectory.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
}
