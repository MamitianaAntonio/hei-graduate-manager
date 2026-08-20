package com.hei.app;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.hei.app.dto.transcript.TranscriptData;
import com.hei.app.file.pdf.PdfGeneratorService;
import com.hei.app.model.Semester;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

public class PdfGeneratorServiceTest {
  private final PdfGeneratorService pdfGeneratorService = new PdfGeneratorService();

  @Test
  void generate_shouldCreateValidPdf() throws IOException {
    TranscriptData data = sampleData();

    File pdf = pdfGeneratorService.generate(data);

    assertTrue(pdf.exists());
    byte[] bytes = Files.readAllBytes(pdf.toPath());
    assertTrue(bytes.length > 0);
    String header = new String(bytes, 0, Math.min(5, bytes.length));
    assertTrue(header.startsWith("%PDF"));
  }

  @Test
  void generate_shouldHandleEmptyExams() throws IOException {
    TranscriptData.SemesterBlock semester =
        new TranscriptData.SemesterBlock(
            Semester.S1,
            2024,
            List.of(
                new TranscriptData.CourseBlock(
                    "ALG", "Algèbre", 4, BigDecimal.valueOf(14), List.of())),
            BigDecimal.valueOf(14),
            BigDecimal.valueOf(4));
    TranscriptData data =
        new TranscriptData(
            "John",
            "Doe",
            "STD001",
            2024,
            List.of(semester),
            BigDecimal.valueOf(14),
            BigDecimal.valueOf(4),
            true);

    File pdf = pdfGeneratorService.generate(data);

    byte[] bytes = Files.readAllBytes(pdf.toPath());
    assertTrue(new String(bytes, 0, 5).startsWith("%PDF"));
  }

  private TranscriptData sampleData() {
    TranscriptData.ExamGrade examGrade =
        new TranscriptData.ExamGrade(
            Instant.parse("2024-03-01T10:00:00Z"), BigDecimal.valueOf(2), BigDecimal.valueOf(14));
    TranscriptData.CourseBlock course =
        new TranscriptData.CourseBlock(
            "ALG", "Algèbre", 4, BigDecimal.valueOf(14), List.of(examGrade));
    TranscriptData.SemesterBlock semester =
        new TranscriptData.SemesterBlock(
            Semester.S1, 2024, List.of(course), BigDecimal.valueOf(14), BigDecimal.valueOf(4));
    return new TranscriptData(
        "John",
        "Doe",
        "STD001",
        2024,
        List.of(semester),
        BigDecimal.valueOf(14),
        BigDecimal.valueOf(4),
        true);
  }
}
