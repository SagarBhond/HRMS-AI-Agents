package com.hrms.mcpserver.repository;

import com.hrms.mcpserver.domain.Payroll;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PayrollRepository extends JpaRepository<Payroll, Long> {
  Optional<Payroll> findByEmployeeIdAndMonthAndYear(Long employeeId, int month, int year);
}
