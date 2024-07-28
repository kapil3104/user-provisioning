package com.user.provisioning.controller;

import com.user.provisioning.dto.MessageResponse;
import com.user.provisioning.entity.Employee;
import com.user.provisioning.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/employees")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping
    public ResponseEntity<List<Employee>> getAllEmployees() {
        log.info("Fetching all employees");
        List<Employee> employees = employeeService.getAllEmployees();
        log.info("Fetched {} employees", employees.size());
        return ResponseEntity.ok(employees);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Employee> getEmployeeById(@PathVariable Long id) {
        log.info("Fetching employee with id: {}", id);
        Employee employee = employeeService.getEmployeeById(id);
        log.info("Fetched employee: {}", employee);
        return ResponseEntity.ok(employee);
    }

    @PostMapping
    public ResponseEntity<Employee> createEmployee(@RequestBody Employee employee) {
        log.info("Creating new employee: {}", employee);
        Employee createdEmployee = employeeService.createEmployee(employee);
        log.info("Created employee: {}", createdEmployee);
        return ResponseEntity.ok(createdEmployee);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Employee> updateEmployee(@PathVariable Long id, @RequestBody Employee employeeDetails) {
        log.info("Updating employee with id: {}", id);
        Employee updatedEmployee = employeeService.updateEmployee(id, employeeDetails);
        log.info("Updated employee: {}", updatedEmployee);
        return ResponseEntity.ok(updatedEmployee);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteEmployee(@PathVariable Long id) {
        log.info("Deleting employee with id: {}", id);
        employeeService.deleteEmployee(id);
        log.info("Deleted employee with id: {}", id);
        return ResponseEntity.ok(new MessageResponse("Employee record has been deleted successfully"));
    }

    @GetMapping("/{id}/employees")
    public ResponseEntity<List<Employee>> getEmployeesByGroupId(@PathVariable Long id) {
        log.info("Fetching employees for group id: {}", id);
        List<Employee> employees = employeeService.getEmployeesByGroupId(id);
        log.info("Fetched {} employees for group id: {}", employees.size(), id);
        return ResponseEntity.ok(employees);
    }
}
