package com.hrms.mcpserver.repository;

import com.hrms.mcpserver.domain.Employee;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository extends JpaRepository<Employee, Long>
{
  Optional<Employee> findByEmail(String email);

    List<Employee> findByManagerEmployeeId(Long managerEmployeeId);
}
