package com.yourproject.repository;

import com.yourproject.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findByUsernameIgnoreCase(String username);

  Optional<User> findByEmployeeId(Long employeeId);
}
