package com.hrms.mcpserver.repository;

import com.hrms.mcpserver.domain.Attendance;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
  List<Attendance> findByEmployeeIdAndWorkDateBetween(
      Long employeeId, LocalDate start, LocalDate end);

  Optional<Attendance> findByEmployeeIdAndWorkDate(Long employeeId, LocalDate workDate);

    List<Attendance> findByEmployeeIdInAndWorkDate(
            List<Long> employeeIds,
            LocalDate workDate);
}
