package com.rapidm3.employeedirectory.controller;

import com.rapidm3.employeedirectory.dto.EmployeeRequest;
import com.rapidm3.employeedirectory.dto.EmployeeResponse;
import com.rapidm3.employeedirectory.security.FirebaseAuthFilter;
import com.rapidm3.employeedirectory.service.EmployeeService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/employees")
public class EmployeeController {

    private final EmployeeService service;

    public EmployeeController(EmployeeService service) {
        this.service = service;
    }

    @GetMapping
    public List<EmployeeResponse> search(@RequestParam(required = false) String name,
                                         @RequestParam(required = false) Long departmentId) {
        return service.search(name, departmentId);
    }

    @GetMapping("/{id}")
    public EmployeeResponse get(@PathVariable long id) {
        return service.get(id);
    }

    @PostMapping
    public ResponseEntity<EmployeeResponse> create(@Valid @RequestBody EmployeeRequest request) {
        EmployeeResponse created = service.create(request);
        return ResponseEntity.created(URI.create("/employees/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    public EmployeeResponse update(@PathVariable long id, @Valid @RequestBody EmployeeRequest request,
                                   @RequestAttribute(name = FirebaseAuthFilter.UID_ATTRIBUTE, required = false)
                                   String uid) {
        return service.update(id, request, uid);
    }

    /** Archives the employee (soft delete); 204 on success, 404 if unknown or already archived. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable long id,
                                       @RequestAttribute(name = FirebaseAuthFilter.UID_ATTRIBUTE, required = false)
                                       String uid) {
        service.archive(id, uid);
        return ResponseEntity.noContent().build();
    }
}
