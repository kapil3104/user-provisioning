package com.user.provisioning.service;

import com.user.provisioning.entity.Employee;
import com.user.provisioning.exception.ErrorCode;
import com.user.provisioning.exception.UserProvisioningCustomException;
import com.user.provisioning.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class EmployeeServiceTest {

    @InjectMocks
    private EmployeeService employeeService;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private DynamicGroupService dynamicGroupService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetAllEmployees() {
        List<Employee> employees = List.of(new Employee(), new Employee());
        when(employeeRepository.findAll()).thenReturn(employees);

        List<Employee> result = employeeService.getAllEmployees();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(employeeRepository, times(1)).findAll();
    }

    @Test
    public void testGetEmployeeById_Success() {
        Employee employee = new Employee();
        employee.setId(1L);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        Employee result = employeeService.getEmployeeById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(employeeRepository, times(1)).findById(1L);
    }

    @Test
    public void testGetEmployeeById_NotFound() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());

        UserProvisioningCustomException exception = assertThrows(UserProvisioningCustomException.class, () -> {
            employeeService.getEmployeeById(1L);
        });

        assertEquals(ErrorCode.EMPLOYEE_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    public void testCreateEmployee_Success() {
        Employee employee = new Employee();
        employee.setEmail("test@example.com");
        when(employeeRepository.existsByEmail(employee.getEmail())).thenReturn(false);
        when(employeeRepository.save(employee)).thenReturn(employee);

        Employee result = employeeService.createEmployee(employee);

        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        verify(employeeRepository, times(1)).existsByEmail(employee.getEmail());
        verify(employeeRepository, times(1)).save(employee);
        verify(dynamicGroupService, times(1)).evaluateAndAssignDynamicGroupForEmployee(employee);
    }

    @Test
    public void testCreateEmployee_AlreadyExists() {
        Employee employee = new Employee();
        employee.setEmail("test@example.com");
        when(employeeRepository.existsByEmail(employee.getEmail())).thenReturn(true);

        UserProvisioningCustomException exception = assertThrows(UserProvisioningCustomException.class, () -> {
            employeeService.createEmployee(employee);
        });

        assertEquals(ErrorCode.EMPLOYEE_ALREADY_EXISTS, exception.getErrorCode());
        verify(employeeRepository, times(1)).existsByEmail(employee.getEmail());
    }

    @Test
    public void testUpdateEmployee_Success() {
        Employee employee = new Employee();
        employee.setId(1L);
        Employee updatedDetails = new Employee();
        updatedDetails.setName("Updated Name");
        updatedDetails.setEmail("updated@example.com");

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(employee)).thenReturn(employee);

        Employee result = employeeService.updateEmployee(1L, updatedDetails);

        assertNotNull(result);
        assertEquals("Updated Name", result.getName());
        assertEquals("updated@example.com", result.getEmail());
        verify(employeeRepository, times(1)).findById(1L);
        verify(employeeRepository, times(1)).save(employee);
    }

    @Test
    public void testUpdateEmployee_NotFound() {
        Employee updatedDetails = new Employee();
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());

        UserProvisioningCustomException exception = assertThrows(UserProvisioningCustomException.class, () -> {
            employeeService.updateEmployee(1L, updatedDetails);
        });

        assertEquals(ErrorCode.EMPLOYEE_NOT_FOUND, exception.getErrorCode());
        verify(employeeRepository, times(1)).findById(1L);
    }

    @Test
    public void testDeleteEmployee_Success() {
        when(employeeRepository.existsById(1L)).thenReturn(true);

        employeeService.deleteEmployee(1L);

        verify(employeeRepository, times(1)).existsById(1L);
        verify(employeeRepository, times(1)).deleteById(1L);
    }

    @Test
    public void testDeleteEmployee_NotFound() {
        when(employeeRepository.existsById(1L)).thenReturn(false);

        UserProvisioningCustomException exception = assertThrows(UserProvisioningCustomException.class, () -> {
            employeeService.deleteEmployee(1L);
        });

        assertEquals(ErrorCode.EMPLOYEE_NOT_FOUND, exception.getErrorCode());
        verify(employeeRepository, times(1)).existsById(1L);
    }

    @Test
    public void testGetEmployeesByGroupId_Success() {
        List<Employee> employees = List.of(new Employee(), new Employee());
        when(employeeRepository.findEmployeesByGroupIdAndApprovedStatus(1L)).thenReturn(employees);

        List<Employee> result = employeeService.getEmployeesByGroupId(1L);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(employeeRepository, times(1)).findEmployeesByGroupIdAndApprovedStatus(1L);
    }

    @Test
    public void testGetEmployeesByGroupId_NotFound() {
        when(employeeRepository.findEmployeesByGroupIdAndApprovedStatus(1L)).thenReturn(List.of());

        UserProvisioningCustomException exception = assertThrows(UserProvisioningCustomException.class, () -> {
            employeeService.getEmployeesByGroupId(1L);
        });

        assertEquals(ErrorCode.EMPLOYEES_NOT_FOUND_FOR_GIVEN_GROUP, exception.getErrorCode());
        verify(employeeRepository, times(1)).findEmployeesByGroupIdAndApprovedStatus(1L);
    }
}
