package com.hrms.mcpserver.repository;

import com.hrms.mcpserver.domain.HR;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HRRepository extends JpaRepository<HR, Long> {
  List<HR> findByEmployeeId(Long employeeId);
}
