package com.rapidm3.employeedirectory.service;

import com.rapidm3.employeedirectory.dto.EmployeeRequest;
import com.rapidm3.employeedirectory.dto.EmployeeResponse;
import com.rapidm3.employeedirectory.entity.Department;
import com.rapidm3.employeedirectory.entity.Employee;
import com.rapidm3.employeedirectory.entity.EmployeeChange;
import com.rapidm3.employeedirectory.exception.DuplicateEmailException;
import com.rapidm3.employeedirectory.exception.EmployeeNotFoundException;
import com.rapidm3.employeedirectory.exception.InvalidDepartmentException;
import com.rapidm3.employeedirectory.repository.DepartmentRepository;
import com.rapidm3.employeedirectory.repository.EmployeeChangeRepository;
import com.rapidm3.employeedirectory.repository.EmployeeRepository;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class EmployeeService {

    private final EmployeeRepository employees;
    private final DepartmentRepository departments;
    private final EmployeeChangeRepository changes;

    public EmployeeService(EmployeeRepository employees, DepartmentRepository departments,
                           EmployeeChangeRepository changes) {
        this.employees = employees;
        this.departments = departments;
        this.changes = changes;
    }

    /** Blank name = no name filter; null departmentId = no department filter; both combine with AND. */
    @Transactional(readOnly = true)
    public List<EmployeeResponse> search(String name, Long departmentId) {
        String nameTerm = (name == null || name.isBlank()) ? null : escapeLike(name.trim().toLowerCase(Locale.ROOT));
        return employees.search(nameTerm, departmentId).stream().map(EmployeeResponse::from).toList();
    }

    /** Makes '%' and '_' match literally instead of acting as LIKE wildcards ('!' is the escape character). */
    private static String escapeLike(String term) {
        return term.replace("!", "!!").replace("%", "!%").replace("_", "!_");
    }

    @Transactional(readOnly = true)
    public EmployeeResponse get(long id) {
        return EmployeeResponse.from(findEmployee(id));
    }

    public EmployeeResponse create(EmployeeRequest request) {
        if (employees.existsByEmail(request.email())) {
            throw duplicateEmail(request.email());
        }
        Employee employee = new Employee();
        apply(employee, request, findDepartment(request.departmentId()));
        return EmployeeResponse.from(employees.saveAndFlush(employee));
    }

    /**
     * Replaces all fields. If department and/or job title changed, one employee_change row is
     * written in the same transaction, so the update and its audit row commit or roll back together.
     */
    public EmployeeResponse update(long id, EmployeeRequest request, String changedBy) {
        Employee employee = findEmployee(id);
        if (employees.existsByEmailAndIdNot(request.email(), id)) {
            throw duplicateEmail(request.email());
        }
        Department oldDepartment = employee.getDepartment();
        Department newDepartment = findDepartment(request.departmentId());
        String oldTitle = employee.getJobTitle();
        String newTitle = request.jobTitle();

        boolean departmentChanged = !Objects.equals(oldDepartment.getId(), newDepartment.getId());
        boolean titleChanged = !oldTitle.equals(newTitle);
        if (departmentChanged || titleChanged) {
            changes.save(new EmployeeChange(employee,
                    departmentChanged ? oldDepartment : null,
                    departmentChanged ? newDepartment : null,
                    titleChanged ? oldTitle : null,
                    titleChanged ? newTitle : null,
                    changedBy));
        }
        apply(employee, request, newDepartment);
        employees.flush(); // surface a unique-key race here so it maps to 409
        return EmployeeResponse.from(employee);
    }

    /**
     * "Delete" archives: the employee disappears from the directory, but the row and its
     * change history stay for HR records. Unknown or already archived ids are 404.
     */
    public void archive(long id, String archivedBy) {
        findEmployee(id).archive(archivedBy);
        employees.flush();
    }

    /** Archived employees keep their email, so say so: otherwise the conflict is with someone invisible. */
    private DuplicateEmailException duplicateEmail(String email) {
        return new DuplicateEmailException(employees.existsByEmailAndArchivedAtIsNotNull(email));
    }

    private void apply(Employee employee, EmployeeRequest request, Department department) {
        employee.setName(request.name());
        employee.setEmail(request.email());
        employee.setJobTitle(request.jobTitle());
        employee.setDepartment(department);
        employee.setHireDate(request.hireDate());
        employee.setStatus(request.status());
    }

    private Employee findEmployee(long id) {
        return employees.findWithDepartmentById(id).orElseThrow(() -> new EmployeeNotFoundException(id));
    }

    private Department findDepartment(Long id) {
        return departments.findById(id).orElseThrow(InvalidDepartmentException::new);
    }
}
