package com.hei.app.service;

import com.hei.app.exceptions.BusinessException;
import com.hei.app.exceptions.ResourceNotFoundException;
import com.hei.app.exceptions.UnauthorizedActionException;
import com.hei.app.model.Promotion;
import com.hei.app.model.Role;
import com.hei.app.model.Student;
import com.hei.app.repository.PromotionRepository;
import com.hei.app.repository.StudentRepository;
import com.hei.app.security.CurrentUser;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GraduateExportService {
  private final StudentRepository studentRepository;
  private final PromotionRepository promotionRepository;
  private final GraduationService graduationService;

  public byte[] exportGraduates(UUID promotionId, CurrentUser currentUser) {
    if (currentUser.role() != Role.ADMIN) {
      throw new UnauthorizedActionException("Only admin can export graduates");
    }
    Promotion promotion =
        promotionRepository
            .findById(promotionId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Promotion not found with id: " + promotionId));
    List<Student> graduates =
        studentRepository.findByPromotionId(promotion.getId()).stream()
            .filter(student -> graduationService.isGraduable(student.getId()))
            .toList();
    return generateExcel(promotion, graduates);
  }

  private byte[] generateExcel(Promotion promotion, List<Student> graduates) {
    try (Workbook workbook = new XSSFWorkbook();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
      Sheet sheet = workbook.createSheet("Diplômés");
      Row header = sheet.createRow(0);
      header.createCell(0).setCellValue("STD");
      header.createCell(1).setCellValue("Nom");
      header.createCell(2).setCellValue("Prénom");
      header.createCell(3).setCellValue("Promotion");
      int rowIndex = 1;
      for (Student student : graduates) {
        Row row = sheet.createRow(rowIndex++);
        row.createCell(0).setCellValue(student.getStd());
        row.createCell(1).setCellValue(student.getLastName());
        row.createCell(2).setCellValue(student.getFirstName());
        row.createCell(3).setCellValue(promotion.getYear());
      }
      for (int i = 0; i < 4; i++) {
        sheet.autoSizeColumn(i);
      }
      workbook.write(outputStream);
      return outputStream.toByteArray();
    } catch (IOException e) {
      throw new BusinessException("Unable to generate graduates Excel file");
    }
  }
}
