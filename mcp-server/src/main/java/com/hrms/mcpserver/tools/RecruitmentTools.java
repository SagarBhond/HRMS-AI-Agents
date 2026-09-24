package com.hrms.mcpserver.tools;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/** Tools backing the Recruitment Agent — job openings, candidates, interviews and offers. */
@Component
public class RecruitmentTools {

  private final AtomicLong ids = new AtomicLong(1);
  private final Map<Long, JobOpening> jobs = new ConcurrentHashMap<>();
  private final Map<Long, Candidate> candidates = new ConcurrentHashMap<>();
  private final Map<Long, Interview> interviews = new ConcurrentHashMap<>();
  private final Map<Long, Offer> offers = new ConcurrentHashMap<>();

  @Tool(description = "Create a job opening")
  public JobOpening createJobOpening(String title, String department, String description) {
    JobOpening j =
        new JobOpening(
            ids.getAndIncrement(),
            title,
            department,
            description,
            "OPEN",
            Instant.now().toString());
    jobs.put(j.jobId(), j);
    return j;
  }

  @Tool(description = "Update a job opening")
  public JobOpening updateJobOpening(
      Long jobId, String title, String department, String description) {
    JobOpening j = req(jobs, jobId, "job opening");
    JobOpening n =
        new JobOpening(
            jobId,
            title == null ? j.title() : title,
            department == null ? j.department() : department,
            description == null ? j.description() : description,
            j.status(),
            j.createdAt());
    jobs.put(jobId, n);
    return n;
  }

  @Tool(description = "Close a job opening")
  public JobOpening closeJobOpening(Long jobId) {
    JobOpening j = req(jobs, jobId, "job opening");
    JobOpening n =
        new JobOpening(jobId, j.title(), j.department(), j.description(), "CLOSED", j.createdAt());
    jobs.put(jobId, n);
    return n;
  }

  @Tool(description = "Create a candidate application for a job opening")
  public Candidate createCandidate(String name, String email, Long jobId) {
    req(jobs, jobId, "job opening");
    Candidate c =
        new Candidate(
            ids.getAndIncrement(), name, email, jobId, "APPLIED", Instant.now().toString());
    candidates.put(c.candidateId(), c);
    return c;
  }

  @Tool(description = "Search candidates by job opening and/or status")
  public List<Candidate> searchCandidates(
      @ToolParam(description = "Job opening ID or null") Long jobId,
      @ToolParam(description = "Status filter or null") String status) {
    return candidates.values().stream()
        .filter(c -> jobId == null || jobId.equals(c.jobId()))
        .filter(c -> status == null || status.equalsIgnoreCase(c.status()))
        .toList();
  }

  @Tool(description = "Shortlist a candidate for interview")
  public Candidate shortlistCandidate(Long candidateId) {
    return setCandidateStatus(candidateId, "SHORTLISTED");
  }

  @Tool(description = "Reject a candidate")
  public Candidate rejectCandidate(Long candidateId, String reason) {
    return setCandidateStatus(candidateId, "REJECTED: " + reason);
  }

  @Tool(description = "Move a candidate to a specific recruitment pipeline stage")
  public Candidate moveCandidateStage(Long candidateId, String stage) {
    return setCandidateStatus(candidateId, stage);
  }

  private Candidate setCandidateStatus(Long candidateId, String status) {
    Candidate c = req(candidates, candidateId, "candidate");
    Candidate n = new Candidate(candidateId, c.name(), c.email(), c.jobId(), status, c.createdAt());
    candidates.put(candidateId, n);
    return n;
  }

  @Tool(description = "Schedule an interview for a candidate")
  public Interview scheduleInterview(Long candidateId, String interviewDate, String interviewer) {
    req(candidates, candidateId, "candidate");
    Interview i =
        new Interview(ids.getAndIncrement(), candidateId, interviewDate, interviewer, "SCHEDULED");
    interviews.put(i.interviewId(), i);
    setCandidateStatus(candidateId, "INTERVIEW_SCHEDULED");
    return i;
  }

  @Tool(description = "Get the interview schedule for a candidate")
  public List<Interview> getInterviewSchedule(Long candidateId) {
    return interviews.values().stream().filter(i -> candidateId.equals(i.candidateId())).toList();
  }

  @Tool(description = "Generate an offer for a candidate")
  public Offer generateOffer(Long candidateId, double salary, String joiningDate) {
    req(candidates, candidateId, "candidate");
    Offer o = new Offer(ids.getAndIncrement(), candidateId, salary, joiningDate, "GENERATED");
    offers.put(o.offerId(), o);
    setCandidateStatus(candidateId, "OFFERED");
    return o;
  }

  private <T> T req(Map<Long, T> m, Long id, String n) {
    T v = m.get(id);
    if (v == null) throw new IllegalArgumentException("No " + n + " found with id " + id);
    return v;
  }

  public record JobOpening(
      Long jobId,
      String title,
      String department,
      String description,
      String status,
      String createdAt) {}

  public record Candidate(
      Long candidateId, String name, String email, Long jobId, String status, String createdAt) {}

  public record Interview(
      Long interviewId,
      Long candidateId,
      String interviewDate,
      String interviewer,
      String status) {}

  public record Offer(
      Long offerId, Long candidateId, double salary, String joiningDate, String status) {}
}
