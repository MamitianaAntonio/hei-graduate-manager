package com.hei.app.service;

import com.hei.app.dto.transcript.TranscriptData;
import com.hei.app.dto.transcript.TranscriptResponse;
import com.hei.app.endpoint.event.EventProducer;
import com.hei.app.endpoint.event.model.PojaEvent;
import com.hei.app.endpoint.event.model.SendTranscriptRequested;
import com.hei.app.exceptions.ResourceNotFoundException;
import com.hei.app.exceptions.UnauthorizedActionException;
import com.hei.app.file.bucket.BucketComponent;
import com.hei.app.file.pdf.PdfGeneratorService;
import com.hei.app.model.Role;
import com.hei.app.model.Student;
import com.hei.app.repository.StudentRepository;
import com.hei.app.security.CurrentUser;
import java.io.File;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TranscriptService {
  private static final String TRANSCRIPT_KEY_PREFIX = "transcripts/";
  private static final Duration PRESIGN_DURATION = Duration.ofMinutes(30);

  private final TranscriptDataService transcriptDataService;
  private final PdfGeneratorService pdfGeneratorService;
  private final BucketComponent bucketComponent;
  private final EventProducer<PojaEvent> eventProducer;
  private final StudentRepository studentRepository;
  private final SecurityAsserts securityAsserts;

  public TranscriptResponse requestOwn(CurrentUser currentUser) {
    UUID studentId = securityAsserts.requireStudentId(currentUser);
    return buildAndSend(studentId);
  }

  public TranscriptResponse requestForStudent(UUID studentId, CurrentUser currentUser) {
    if (currentUser.role() != Role.ADMIN) {
      throw new UnauthorizedActionException(
          "Only admin can request a transcript for another student");
    }
    return buildAndSend(studentId);
  }

  private TranscriptResponse buildAndSend(UUID studentId) {
    TranscriptData data = transcriptDataService.build(studentId);
    File pdf = pdfGeneratorService.generate(data);

    String bucketKey = TRANSCRIPT_KEY_PREFIX + studentId + "/" + UUID.randomUUID() + ".pdf";
    bucketComponent.upload(pdf, bucketKey);
    String presignedUrl = bucketComponent.presign(bucketKey, PRESIGN_DURATION).toString();

    String recipientEmail = emailOf(studentId);
    SendTranscriptRequested event =
        SendTranscriptRequested.builder()
            .to(recipientEmail)
            .bucketKey(bucketKey)
            .presignedUrl(presignedUrl)
            .build();
    eventProducer.accept(List.of(event));

    return new TranscriptResponse(presignedUrl, recipientEmail);
  }

  private String emailOf(UUID studentId) {
    Student student =
        studentRepository
            .findById(studentId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Student not found with id: " + studentId));
    return student.getUserAccount().getEmail();
  }
}
