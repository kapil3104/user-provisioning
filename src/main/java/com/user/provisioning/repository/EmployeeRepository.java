package com.user.provisioning.repository;


import com.user.provisioning.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    @Query("SELECT e FROM Employee e JOIN Membership mr ON e.id = mr.employee.id WHERE mr.group.id = :groupId AND mr.status = 'APPROVED'")
    List<Employee> findEmployeesByGroupIdAndApprovedStatus(@Param("groupId") Long groupId);
    boolean existsByEmail(String email);
}

