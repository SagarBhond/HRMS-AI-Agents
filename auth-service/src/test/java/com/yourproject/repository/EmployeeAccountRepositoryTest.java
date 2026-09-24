package com.yourproject.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.yourproject.dto.CreateUserRequest;
import com.yourproject.entity.Role;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

class EmployeeAccountRepositoryTest {

  private EmployeeAccountRepository repository;

  @BeforeEach
  void setUp() {
    DriverManagerDataSource dataSource =
        new DriverManagerDataSource("jdbc:h2:mem:auth;MODE=MySQL;DB_CLOSE_DELAY=-1", "sa", "");
    JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
    jdbcTemplate.execute("DROP TABLE IF EXISTS employee");
    jdbcTemplate.execute(
        """
                CREATE TABLE employee (
                  employee_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                  first_name VARCHAR(100) NOT NULL,
                  last_name VARCHAR(100),
                  email VARCHAR(150) NOT NULL UNIQUE,
                  department VARCHAR(100),
                  designation VARCHAR(100),
                  manager_employee_id VARCHAR(50),
                  date_of_joining DATE,
                  status VARCHAR(20) NOT NULL
                )
                """);
    repository = new EmployeeAccountRepository(jdbcTemplate);
  }

  @Test
  void createsEmployeeAndFindsItCaseInsensitively() {
    CreateUserRequest request =
        new CreateUserRequest(
            "New.User@Example.com",
            "password123",
            Role.EMPLOYEE,
            "New",
            "User",
            "Engineering",
            "Developer",
            12L,
            LocalDate.of(2026, 9, 9));

    Long employeeId = repository.create(request);

    assertThat(employeeId).isPositive();
    assertThat(repository.findByEmail("NEW.USER@example.com")).isEqualTo(employeeId);
    assertThat(repository.existsById(employeeId)).isTrue();
  }
}
