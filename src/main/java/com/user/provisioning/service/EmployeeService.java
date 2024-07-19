package com.user.provisioning.service;

import com.user.provisioning.entity.Employee;
import com.user.provisioning.exception.ErrorCode;
import com.user.provisioning.exception.UserProvisioningCustomException;
import com.user.provisioning.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    public Employee getEmployeeById(String id) {
        Optional<Employee> optionalEmployee = employeeRepository.findById(id);
        if(optionalEmployee.isPresent()) {
            return optionalEmployee.get();
        } else {
            throw new UserProvisioningCustomException(ErrorCode.EMPLOYEE_NOT_FOUND);
        }
    }

    public Employee createEmployee(Employee employee) {
        Optional<Employee> optionalEmployee = employeeRepository.findOneByEmail(employee.getEmail());
        if(optionalEmployee.isPresent()) {
            throw new UserProvisioningCustomException(ErrorCode.EMPLOYEE_ALREADY_EXISTS);
        }
        return employeeRepository.save(employee);
    }

    public Employee updateEmployee(String id, Employee employeeDetails) {
        Employee employee = getEmployeeById(id);
        employee.setName(employeeDetails.getName());
        employee.setEmail(employeeDetails.getEmail());
        employee.setRole(employeeDetails.getRole());
        employee.setLocation(employeeDetails.getLocation());
        employee.setDepartment(employeeDetails.getDepartment());
        return employeeRepository.save(employee);
    }

    public void deleteEmployee(String id) {
        employeeRepository.deleteById(id);
    }
}

