package com.hrms.mcpserver.tools;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class DocumentTools {
  private final AtomicLong ids = new AtomicLong(1);
  private final ConcurrentHashMap<Long, DocumentRecord> documents = new ConcurrentHashMap<>();

  @Tool(description = "Create a document for an employee")
  public DocumentRecord createDocument(String documentType, Long employeeId, String title, String content) {
    return save(documentType, employeeId, title, content, "DOCUMENT");
  }

  @Tool(description = "Generate a PDF document")
  public DocumentRecord generatePdf(Long employeeId, String title, String content) {
    return save("PDF", employeeId, title, content, "PDF");
  }

  @Tool(description = "Generate a DOCX document")
  public DocumentRecord generateDocx(Long employeeId, String title, String content) {
    return save("DOCX", employeeId, title, content, "DOCX");
  }

  @Tool(description = "Get a document by ID")
  public DocumentRecord getDocument(Long documentId) {
    DocumentRecord d = documents.get(documentId);
    if (d == null) throw new IllegalArgumentException("No document found with id " + documentId);
    return d;
  }

  @Tool(description = "List documents, optionally filtered by employee")
  public List<DocumentRecord> listDocuments(Long employeeId) {
    return documents.values().stream()
        .filter(d -> employeeId == null || employeeId.equals(d.employeeId()))
        .sorted(java.util.Comparator.comparing(DocumentRecord::documentId))
        .toList();
  }

  @Tool(description = "Store a document")
  public DocumentRecord storeDocument(String documentType, Long employeeId, String title, String content) {
    return save(documentType, employeeId, title, content, "STORED");
  }

  private DocumentRecord save(String type, Long employeeId, String title, String content, String format) {
    long id = ids.getAndIncrement();
    DocumentRecord d = new DocumentRecord(id, employeeId, title, type, format, content, Instant.now().toString());
    documents.put(id, d);
    return d;
  }

  public record DocumentRecord(Long documentId, Long employeeId, String title, String documentType,
                               String format, String content, String createdAt) {}
}
