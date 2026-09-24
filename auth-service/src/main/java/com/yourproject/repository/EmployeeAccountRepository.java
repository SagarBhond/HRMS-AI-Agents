package com.yourproject.repository;

import com.yourproject.dto.CreateUserRequest;
import com.yourproject.service.AccountProvisioningException;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

/**
 * Provisions the employee half of an account in the shared HRMS database. The MCP server remains
 * the runtime owner of employee reads and business operations; Auth Service uses this repository
 * only during account creation so the login and employee identity are linked atomically.
 */
@Repository
public class EmployeeAccountRepository {

  private final JdbcTemplate jdbcTemplate;

  public EmployeeAccountRepository(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  public Long findByEmail(String email) {
    try {
      List<Long> ids =
          jdbcTemplate.queryForList(
              "SELECT employee_id FROM employee WHERE LOWER(email) = LOWER(?)", Long.class, email);
      return ids.isEmpty() ? null : ids.get(0);
    } catch (DataAccessException ex) {
      throw unavailable(ex);
    }
  }

  public Long create(CreateUserRequest request) {
    try {
      KeyHolder keyHolder = new GeneratedKeyHolder();
      jdbcTemplate.update(
          connection -> {
            PreparedStatement statement =
                connection.prepareStatement(
                    """
                                INSERT INTO employee
                                  (first_name, last_name, email, department, designation,
                                   manager_employee_id, date_of_joining, status)
                                VALUES (?, ?, ?, ?, ?, ?, ?, 'ACTIVE')
                                """,
                    Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, request.firstName().trim());
            statement.setString(2, trimToNull(request.lastName()));
            statement.setString(3, request.username().trim().toLowerCase());
            statement.setString(4, trimToNull(request.department()));
            statement.setString(5, trimToNull(request.designation()));
            statement.setString(
                6,
                request.managerEmployeeId() == null
                    ? null
                    : String.valueOf(request.managerEmployeeId()));
            statement.setDate(
                7,
                Date.valueOf(
                    request.dateOfJoining() == null ? LocalDate.now() : request.dateOfJoining()));
            return statement;
          },
          keyHolder);
      Number key = keyHolder.getKey();
      if (key == null) {
        throw new AccountProvisioningException(
            "Employee record was created without an employee id", null);
      }
      return key.longValue();
    } catch (DataAccessException ex) {
      throw unavailable(ex);
    }
  }

  public boolean existsById(Long employeeId) {
    try {
      Integer count =
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM employee WHERE employee_id = ?", Integer.class, employeeId);
      return count != null && count > 0;
    } catch (DataAccessException ex) {
      throw unavailable(ex);
    }
  }

  private AccountProvisioningException unavailable(DataAccessException cause) {
    return new AccountProvisioningException(
        "Employee storage is unavailable. Start MySQL and the MCP server before creating accounts.",
        cause);
  }

  private String trimToNull(String value) {
    return value == null || value.isBlank() ? null : value.trim();
  }
}
