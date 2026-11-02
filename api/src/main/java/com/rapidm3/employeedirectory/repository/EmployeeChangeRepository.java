package com.rapidm3.employeedirectory.repository;

import com.rapidm3.employeedirectory.entity.EmployeeChange;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeChangeRepository extends JpaRepository<EmployeeChange, Long> {

    List<EmployeeChange> findByEmployeeIdOrderByIdAsc(Long employeeId);
}
