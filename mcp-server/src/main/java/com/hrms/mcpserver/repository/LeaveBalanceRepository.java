package com.hrms.mcpserver.repository;

import com.hrms.mcpserver.domain.LeaveBalance;
import com.hrms.mcpserver.domain.LeaveRequest;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeaveBalanceRepository extends JpaRepository<LeaveBalance, Long> {
  List<LeaveBalance> findByEmployeeIdAndYear(Long employeeId, int year);

  Optional<LeaveBalance> findByEmployeeIdAndLeaveTypeAndYear(
      Long employeeId, LeaveRequest.LeaveType leaveType, int year);
}
