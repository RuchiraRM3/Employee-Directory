package com.rapidm3.employeedirectory.repository;

import com.rapidm3.employeedirectory.entity.Employee;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    /**
     * Optional filters combined with AND. A null parameter disables its predicate. Archived employees are excluded.
     * {@code nameTerm} must already be lower-cased and LIKE-escaped with '!' (see EmployeeService);
     * parameters are bound, never concatenated into SQL.
     */
    @Query("""
            select e from Employee e join fetch e.department d
            where (:nameTerm is null or lower(e.name) like concat('%', :nameTerm, '%') escape '!')
              and (:departmentId is null or d.id = :departmentId)
              and e.archivedAt is null
            order by e.name
            """)
    List<Employee> search(@Param("nameTerm") String nameTerm, @Param("departmentId") Long departmentId);

    /** Archived employees count as not found (404 for details, edit and delete). */
    @Query("select e from Employee e join fetch e.department where e.id = :id and e.archivedAt is null")
    Optional<Employee> findWithDepartmentById(@Param("id") Long id);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);

    boolean existsByEmailAndArchivedAtIsNotNull(String email);
}
