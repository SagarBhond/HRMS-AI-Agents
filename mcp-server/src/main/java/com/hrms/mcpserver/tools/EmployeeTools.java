package com.hrms.mcpserver.tools;

import com.hrms.mcpserver.domain.Employee;
import com.hrms.mcpserver.repository.EmployeeRepository;
import java.util.List;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class EmployeeTools {

  private final EmployeeRepository employeeRepository;

  public EmployeeTools(EmployeeRepository employeeRepository) {
    this.employeeRepository = employeeRepository;
  }

  @Tool(description = "Get an employee's full profile by employee ID")
  public Employee getEmployeeProfile(@ToolParam(description = "Employee ID") Long employeeId) {
    return employeeRepository
        .findById(employeeId)
        .orElseThrow(() -> new IllegalArgumentException("No employee found with id " + employeeId));
  }

  @Tool(description = "Create a new employee")
  public Employee createEmployee(Employee employee) {
    employee.setStatus(Employee.EmployeeStatus.ACTIVE);
    return employeeRepository.save(employee);
  }

  @Tool(description = "Update employee department, designation or manager")
  public Employee updateEmployeeDetails(
      @ToolParam(description = "Employee ID") Long employeeId,
      @ToolParam(description = "Department or null") String department,
      @ToolParam(description = "Designation or null") String designation,
      @ToolParam(description = "Manager employee ID or null") Long managerEmployeeId) {
    Employee employee = getEmployeeProfile(employeeId);
    if (department != null && !department.isBlank()) employee.setDepartment(department);
    if (designation != null && !designation.isBlank()) employee.setDesignation(designation);
    if (managerEmployeeId != null) employee.setManagerEmployeeId(managerEmployeeId);
    return employeeRepository.save(employee);
  }

  @Tool(description = "List employees, optionally filtered by department")
  public List<Employee> listEmployees(
      @ToolParam(description = "Department or null") String department) {
    List<Employee> all = employeeRepository.findAll();
    if (department == null || department.isBlank()) return all;
    return all.stream().filter(e -> department.equalsIgnoreCase(e.getDepartment())).toList();
  }

  @Tool(description = "Search employees by name or email")
  public List<Employee> searchEmployee(
      @ToolParam(description = "Name or email search text") String query) {
    String q = query == null ? "" : query.trim().toLowerCase();
    return employeeRepository.findAll().stream()
        .filter(
            e ->
                (e.getFirstName() != null && e.getFirstName().toLowerCase().contains(q))
                    || (e.getLastName() != null && e.getLastName().toLowerCase().contains(q))
                    || (e.getEmail() != null && e.getEmail().toLowerCase().contains(q)))
        .toList();
  }

  @Tool(description = "Find employee by work email")
  public Employee findEmployeeByEmail(@ToolParam(description = "Work email") String email) {
    return employeeRepository
        .findByEmail(email)
        .orElseThrow(() -> new IllegalArgumentException("No employee found with email " + email));
  }

  @Tool(description = "Get an employee's manager")
  public Employee getManager(@ToolParam(description = "Employee ID") Long employeeId) {
    Employee employee = getEmployeeProfile(employeeId);
    if (employee.getManagerEmployeeId() == null) return null;
    return getEmployeeProfile(employee.getManagerEmployeeId());
  }

  @Tool(description = "Get an employee's department")
  public String getDepartment(@ToolParam(description = "Employee ID") Long employeeId) {
    return getEmployeeProfile(employeeId).getDepartment();
  }

  @Tool(description = "Get an employee's employment status")
  public String getEmploymentStatus(@ToolParam(description = "Employee ID") Long employeeId) {
    Employee employee = getEmployeeProfile(employeeId);
    return employee.getStatus() == null ? null : employee.getStatus().name();
  }

  @Tool(description = "Get the profile of the current employee")
  public Employee getMyProfile(@ToolParam(description = "Current employee ID") Long employeeId) {
    return getEmployeeProfile(employeeId);
  }

  @Tool(
      description =
          "Update employee contact details. Contact fields are represented by email in the current model.")
  public Employee updateMyContactDetails(
      @ToolParam(description = "Employee ID") Long employeeId,
      @ToolParam(description = "New email address or null") String email) {
    Employee employee = getEmployeeProfile(employeeId);
    if (email != null && !email.isBlank()) employee.setEmail(email);
    return employeeRepository.save(employee);
  }

  @Tool(description = "Get the reporting hierarchy from employee to top manager")
  public List<Employee> getReportingHierarchy(
      @ToolParam(description = "Employee ID") Long employeeId) {
    java.util.ArrayList<Employee> result = new java.util.ArrayList<>();
    java.util.HashSet<Long> visited = new java.util.HashSet<>();
    Employee current = getEmployeeProfile(employeeId);
    while (current != null && visited.add(current.getEmployeeId())) {
      result.add(current);
      if (current.getManagerEmployeeId() == null) break;
      current = getEmployeeProfile(current.getManagerEmployeeId());
    }
    return result;
  }

  @Tool(
      description =
          "Get documents associated with an employee. Returns document metadata from the employee record when available.")
  public List<EmployeeDocument> getEmployeeDocuments(
      @ToolParam(description = "Employee ID") Long employeeId) {
    getEmployeeProfile(employeeId);
    return List.of();
  }

  @Tool(
      description =
          "Get employment history. Lifecycle events are maintained by the HR domain; this method returns a compact master-data history placeholder.")
  public List<EmploymentHistoryItem> getEmploymentHistory(
      @ToolParam(description = "Employee ID") Long employeeId) {
    Employee employee = getEmployeeProfile(employeeId);
    return List.of(
        new EmploymentHistoryItem(
            employee.getEmployeeId(),
            employee.getDesignation(),
            employee.getDepartment(),
            employee.getDateOfJoining(),
            employee.getStatus() == null ? null : employee.getStatus().name()));
  }

  @Tool(description = "Get a compact employee summary")
  public EmployeeSummary getEmployeeSummary(
      @ToolParam(description = "Employee ID") Long employeeId) {
    Employee e = getEmployeeProfile(employeeId);
    return new EmployeeSummary(
        e.getEmployeeId(),
        e.getFirstName(),
        e.getLastName(),
        e.getEmail(),
        e.getDepartment(),
        e.getDesignation(),
        e.getStatus() == null ? null : e.getStatus().name());
  }

  public record EmployeeDocument(Long employeeId, String documentName, String documentType) {}

  public record EmploymentHistoryItem(
      Long employeeId,
      String designation,
      String department,
      java.time.LocalDate effectiveDate,
      String status) {}

  public record EmployeeSummary(
      Long employeeId,
      String firstName,
      String lastName,
      String email,
      String department,
      String designation,
      String status) {}
}
