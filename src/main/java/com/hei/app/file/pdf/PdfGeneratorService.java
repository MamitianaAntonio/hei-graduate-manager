package com.hei.app.file.pdf;

import com.hei.app.dto.transcript.TranscriptData;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

@Service
public class PdfGeneratorService {

  private static final Font TITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
  private static final Font SUBTITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA, 12);
  private static final Font SEMESTER_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
  private static final Font HEADER_CELL_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
  private static final Font CELL_FONT = FontFactory.getFont(FontFactory.HELVETICA, 10);

  @SneakyThrows
  public File generate(TranscriptData data) {
    File file = File.createTempFile("releve-notes-", ".pdf");
    try (FileOutputStream output = new FileOutputStream(file)) {
      Document document = new Document(PageSize.A4);
      PdfWriter.getInstance(document, output);
      document.open();
      addHeader(document, data);
      for (TranscriptData.SemesterBlock semester : data.semesters()) {
        addSemester(document, semester);
      }
      addSummary(document, data);
      document.close();
    }
    return file;
  }

  private void addHeader(Document document, TranscriptData data) throws DocumentException {
    Paragraph title = new Paragraph("RELEVÉ DE NOTES", TITLE_FONT);
    title.setAlignment(Element.ALIGN_CENTER);
    document.add(title);

    document.add(
        new Paragraph("Étudiant : " + data.firstName() + " " + data.lastName(), SUBTITLE_FONT));
    document.add(new Paragraph("Matricule : " + data.std(), SUBTITLE_FONT));
    document.add(new Paragraph("Promotion : " + data.promotionYear(), SUBTITLE_FONT));
    document.add(new Paragraph(" "));
  }

  private void addSemester(Document document, TranscriptData.SemesterBlock semester)
      throws DocumentException {
    document.add(
        new Paragraph(
            "Semestre "
                + semester.semester()
                + " — "
                + semester.academicYear()
                + " (moyenne : "
                + semester.average()
                + ", crédits : "
                + semester.totalCredits()
                + ")",
            SEMESTER_FONT));

    PdfPTable table = new PdfPTable(5);
    table.setWidthPercentage(100);
    addCell(table, "Réf", HEADER_CELL_FONT);
    addCell(table, "Cours", HEADER_CELL_FONT);
    addCell(table, "Crédits", HEADER_CELL_FONT);
    addCell(table, "Moyenne", HEADER_CELL_FONT);
    addCell(table, "Épreuves (date | coeff | note)", HEADER_CELL_FONT);

    for (TranscriptData.CourseBlock course : semester.courses()) {
      addCell(table, course.ref(), CELL_FONT);
      addCell(table, course.title(), CELL_FONT);
      addCell(table, String.valueOf(course.credits()), CELL_FONT);
      addCell(table, course.average().toPlainString(), CELL_FONT);
      addCell(table, formatExams(course.exams()), CELL_FONT);
    }

    document.add(table);
    document.add(new Paragraph(" "));
  }

  private void addSummary(Document document, TranscriptData data) throws DocumentException {
    document.add(
        new Paragraph(
            "Moyenne générale : " + data.overallAverage().toPlainString(), SEMESTER_FONT));
    document.add(new Paragraph("Crédits totaux : " + data.totalCredits(), SUBTITLE_FONT));
    document.add(
        new Paragraph("Admissible : " + (data.graduable() ? "OUI" : "NON"), SUBTITLE_FONT));
  }

  private String formatExams(List<TranscriptData.ExamGrade> exams) {
    StringBuilder builder = new StringBuilder();
    for (TranscriptData.ExamGrade exam : exams) {
      if (!builder.isEmpty()) {
        builder.append("\n");
      }
      String grade = exam.grade() == null ? "-" : exam.grade().setScale(2).toPlainString();
      builder
          .append(exam.date())
          .append(" | ")
          .append(exam.coefficient())
          .append(" | ")
          .append(grade);
    }
    if (builder.isEmpty()) {
      return "-";
    }
    return builder.toString();
  }

  private void addCell(PdfPTable table, String text, Font font) {
    PdfPCell cell = new PdfPCell(new Phrase(text, font));
    table.addCell(cell);
  }
}
