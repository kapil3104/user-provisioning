package com.user.provisioning.service;

import com.user.provisioning.entity.Employee;
import com.user.provisioning.exception.ErrorCode;
import com.user.provisioning.exception.UserProvisioningCustomException;
import com.user.provisioning.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service class for managing employee-related operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DynamicGroupService dynamicGroupService;

    /**
     * Retrieves all employees.
     *
     * @return a list of all employees
     */
    public List<Employee> getAllEmployees() {
        log.info("Fetching all employees");
        return employeeRepository.findAll();
    }

    /**
     * Retrieves an employee by their ID.
     *
     * @param id the ID of the employee
     * @return the employee with the specified ID
     * @throws UserProvisioningCustomException if the employee is not found
     */
    public Employee getEmployeeById(Long id) {
        log.info("Fetching employee with id: {}", id);
        return employeeRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Employee with id {} not found", id);
                    return new UserProvisioningCustomException(ErrorCode.EMPLOYEE_NOT_FOUND);
                });
    }

    /**
     * Creates a new employee.
     *
     * @param employee the employee to create
     * @return the created employee
     * @throws UserProvisioningCustomException if an employee with the same email already exists
     */
    public Employee createEmployee(Employee employee) {
        log.info("Creating employee with email: {}", employee.getEmail());
        if (employeeRepository.existsByEmail(employee.getEmail())) {
            log.error("Employee with email {} already exists", employee.getEmail());
            throw new UserProvisioningCustomException(ErrorCode.EMPLOYEE_ALREADY_EXISTS);
        }
        employee = employeeRepository.save(employee);
        log.info("Employee with id {} created successfully", employee.getId());
        dynamicGroupService.evaluateAndAssignDynamicGroupForEmployee(employee);
        return employee;
    }

    /**
     * Updates an existing employee.
     *
     * @param id             the ID of the employee to update
     * @param employeeDetails the updated employee details
     * @return the updated employee
     * @throws UserProvisioningCustomException if the employee is not found
     */
    public Employee updateEmployee(Long id, Employee employeeDetails) {
        log.info("Updating employee with id: {}", id);
        Employee employee = getEmployeeById(id);
        employee.setName(employeeDetails.getName());
        employee.setEmail(employeeDetails.getEmail());
        employee.setLocation(employeeDetails.getLocation());
        employee.setDepartment(employeeDetails.getDepartment());
        Employee updatedEmployee = employeeRepository.save(employee);
        log.info("Employee with id {} updated successfully", id);
        return updatedEmployee;
    }

    /**
     * Deletes an employee by their ID.
     *
     * @param id the ID of the employee to delete
     * @throws UserProvisioningCustomException if the employee is not found
     */
    public void deleteEmployee(Long id) {
        log.info("Deleting employee with id: {}", id);
        if (!employeeRepository.existsById(id)) {
            log.error("Employee with id {} not found", id);
            throw new UserProvisioningCustomException(ErrorCode.EMPLOYEE_NOT_FOUND);
        }
        employeeRepository.deleteById(id);
        log.info("Employee with id {} deleted successfully", id);
    }

    /**
     * Retrieves employees by group ID.
     *
     * @param groupId the ID of the group
     * @return a list of employees belonging to the specified group
     * @throws UserProvisioningCustomException if no employees are found for the given group ID
     */
    @Transactional(readOnly = true)
    public List<Employee> getEmployeesByGroupId(Long groupId) {
        log.info("Fetching employees for group id: {}", groupId);
        List<Employee> employees = employeeRepository.findEmployeesByGroupIdAndApprovedStatus(groupId);
        if (employees.isEmpty()) {
            log.error("No employees found for group id {}", groupId);
            throw new UserProvisioningCustomException(ErrorCode.EMPLOYEES_NOT_FOUND_FOR_GIVEN_GROUP);
        }
        return employees;
    }
}
