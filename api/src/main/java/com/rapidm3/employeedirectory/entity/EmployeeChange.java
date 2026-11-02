package com.rapidm3.employeedirectory.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.Instant;

/** Audit row written when an employee's department and/or job title changes. */
@Entity
public class EmployeeChange {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "old_department_id")
    private Department oldDepartment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "new_department_id")
    private Department newDepartment;

    private String oldJobTitle;

    private String newJobTitle;

    private Instant changedAt;

    private String changedBy;

    protected EmployeeChange() {
    }

    public EmployeeChange(Employee employee, Department oldDepartment, Department newDepartment,
                          String oldJobTitle, String newJobTitle, String changedBy) {
        this.employee = employee;
        this.oldDepartment = oldDepartment;
        this.newDepartment = newDepartment;
        this.oldJobTitle = oldJobTitle;
        this.newJobTitle = newJobTitle;
        this.changedAt = Instant.now();
        this.changedBy = changedBy;
    }

    public Long getId() {
        return id;
    }

    public Employee getEmployee() {
        return employee;
    }

    public Department getOldDepartment() {
        return oldDepartment;
    }

    public Department getNewDepartment() {
        return newDepartment;
    }

    public String getOldJobTitle() {
        return oldJobTitle;
    }

    public String getNewJobTitle() {
        return newJobTitle;
    }

    public String getChangedBy() {
        return changedBy;
    }
}
