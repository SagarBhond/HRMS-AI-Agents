package com.hrms.mcpserver.tools;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/** Tools backing the Performance Agent — goals, self/manager reviews, and performance summaries. */
@Component
public class PerformanceTools {

  private final AtomicLong ids = new AtomicLong(1);
  private final Map<Long, Goal> goals = new ConcurrentHashMap<>();
  private final Map<Long, Review> reviews = new ConcurrentHashMap<>();

  @Tool(description = "Create a performance goal for an employee")
  public Goal createGoal(
      @ToolParam(description = "Employee ID") Long employeeId,
      @ToolParam(description = "Goal title") String title,
      @ToolParam(description = "Goal description") String description,
      @ToolParam(description = "Due date (yyyy-MM-dd)") String dueDate) {
    Goal g =
        new Goal(
            ids.getAndIncrement(),
            employeeId,
            title,
            description,
            dueDate,
            "OPEN",
            Instant.now().toString());
    goals.put(g.goalId(), g);
    return g;
  }

  @Tool(description = "Update a performance goal's title, description or due date")
  public Goal updateGoal(
      @ToolParam(description = "Goal ID") Long goalId,
      @ToolParam(description = "New title or null") String title,
      @ToolParam(description = "New description or null") String description,
      @ToolParam(description = "New due date or null") String dueDate) {
    Goal g = required(goals, goalId, "goal");
    Goal n =
        new Goal(
            goalId,
            g.employeeId(),
            title == null ? g.title() : title,
            description == null ? g.description() : description,
            dueDate == null ? g.dueDate() : dueDate,
            g.status(),
            g.createdAt());
    goals.put(goalId, n);
    return n;
  }

  @Tool(description = "Get goals for an employee, or all goals if employeeId is null")
  public List<Goal> getGoals(@ToolParam(description = "Employee ID or null") Long employeeId) {
    return goals.values().stream()
        .filter(g -> employeeId == null || employeeId.equals(g.employeeId()))
        .toList();
  }

  @Tool(description = "Mark a performance goal as completed")
  public Goal completeGoal(@ToolParam(description = "Goal ID") Long goalId) {
    Goal g = required(goals, goalId, "goal");
    Goal n =
        new Goal(
            goalId,
            g.employeeId(),
            g.title(),
            g.description(),
            g.dueDate(),
            "COMPLETED",
            g.createdAt());
    goals.put(goalId, n);
    return n;
  }

  @Tool(description = "Submit an employee's self-review for a review period")
  public Review submitSelfReview(
      @ToolParam(description = "Employee ID") Long employeeId,
      @ToolParam(description = "Review period, e.g. 2026-H1") String reviewPeriod,
      @ToolParam(description = "Self-rating") double rating,
      @ToolParam(description = "Self-review comments") String comments) {
    return upsertReview(
        employeeId, reviewPeriod, rating, comments, "self", "SELF_REVIEW_SUBMITTED");
  }

  @Tool(description = "Submit a manager's review of an employee for a review period")
  public Review submitManagerReview(
      @ToolParam(description = "Employee ID") Long employeeId,
      @ToolParam(description = "Review period, e.g. 2026-H1") String reviewPeriod,
      @ToolParam(description = "Manager rating") double rating,
      @ToolParam(description = "Manager review comments") String comments) {
    return upsertReview(
        employeeId, reviewPeriod, rating, comments, "manager", "MANAGER_REVIEW_SUBMITTED");
  }

  @Tool(description = "Create a formal performance review record")
  public Review createPerformanceReview(
      @ToolParam(description = "Employee ID") Long employeeId,
      @ToolParam(description = "Review period") String reviewPeriod,
      @ToolParam(description = "Overall rating") double rating,
      @ToolParam(description = "Overall comments") String comments) {
    Review r =
        new Review(
            ids.getAndIncrement(),
            employeeId,
            reviewPeriod,
            rating,
            comments,
            null,
            null,
            "DRAFT",
            Instant.now().toString());
    reviews.put(r.reviewId(), r);
    return r;
  }

  @Tool(description = "Get a performance review by ID")
  public Review getPerformanceReview(@ToolParam(description = "Review ID") Long reviewId) {
    return required(reviews, reviewId, "review");
  }

  @Tool(
      description =
          "Generate a performance summary for an employee across all review periods and goals")
  public PerformanceSummary generatePerformanceSummary(
      @ToolParam(description = "Employee ID") Long employeeId) {
    List<Review> employeeReviews =
        reviews.values().stream().filter(r -> employeeId.equals(r.employeeId())).toList();
    List<Goal> employeeGoals = getGoals(employeeId);
    long completedGoals =
        employeeGoals.stream().filter(g -> "COMPLETED".equals(g.status())).count();
    double avgRating = employeeReviews.stream().mapToDouble(Review::rating).average().orElse(0);
    return new PerformanceSummary(
        employeeId, employeeGoals.size(), completedGoals, employeeReviews.size(), avgRating);
  }

  private Review upsertReview(
      Long employeeId,
      String reviewPeriod,
      double rating,
      String comments,
      String source,
      String status) {
    Review existing =
        reviews.values().stream()
            .filter(r -> employeeId.equals(r.employeeId()) && reviewPeriod.equals(r.reviewPeriod()))
            .findFirst()
            .orElse(null);
    Review r;
    if (existing == null) {
      r =
          "self".equals(source)
              ? new Review(
                  ids.getAndIncrement(),
                  employeeId,
                  reviewPeriod,
                  rating,
                  null,
                  comments,
                  null,
                  status,
                  Instant.now().toString())
              : new Review(
                  ids.getAndIncrement(),
                  employeeId,
                  reviewPeriod,
                  rating,
                  null,
                  null,
                  comments,
                  status,
                  Instant.now().toString());
    } else {
      r =
          "self".equals(source)
              ? new Review(
                  existing.reviewId(),
                  employeeId,
                  reviewPeriod,
                  rating,
                  existing.comments(),
                  comments,
                  existing.managerComments(),
                  status,
                  existing.createdAt())
              : new Review(
                  existing.reviewId(),
                  employeeId,
                  reviewPeriod,
                  rating,
                  existing.comments(),
                  existing.selfComments(),
                  comments,
                  status,
                  existing.createdAt());
    }
    reviews.put(r.reviewId(), r);
    return r;
  }

  private <T> T required(Map<Long, T> map, Long id, String name) {
    T v = map.get(id);
    if (v == null) throw new IllegalArgumentException("No " + name + " found with id " + id);
    return v;
  }

  public record Goal(
      Long goalId,
      Long employeeId,
      String title,
      String description,
      String dueDate,
      String status,
      String createdAt) {}

  public record Review(
      Long reviewId,
      Long employeeId,
      String reviewPeriod,
      double rating,
      String comments,
      String selfComments,
      String managerComments,
      String status,
      String createdAt) {}

  public record PerformanceSummary(
      Long employeeId,
      int totalGoals,
      long completedGoals,
      int totalReviews,
      double averageRating) {}
}
