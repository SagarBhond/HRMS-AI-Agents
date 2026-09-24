package com.hrms.mcpserver.repository;

import com.hrms.mcpserver.domain.Holiday;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HolidayRepository extends JpaRepository<Holiday, Long> {
  List<Holiday> findByDateBetweenOrderByDateAsc(LocalDate start, LocalDate end);

  Optional<Holiday> findByDate(LocalDate date);
}
