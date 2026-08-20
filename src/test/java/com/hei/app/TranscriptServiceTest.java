package com.hei.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import com.hei.app.model.UserAccount;
import com.hei.app.repository.StudentRepository;
import com.hei.app.security.CurrentUser;
import com.hei.app.service.SecurityAsserts;
import com.hei.app.service.TranscriptDataService;
import com.hei.app.service.TranscriptService;
import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class TranscriptServiceTest {
  @Mock private TranscriptDataService transcriptDataService;

  @Mock private PdfGeneratorService pdfGeneratorService;

  @Mock private BucketComponent bucketComponent;

  @Mock private EventProducer<PojaEvent> eventProducer;

  @Mock private StudentRepository studentRepository;

  @Mock private SecurityAsserts securityAsserts;

  @InjectMocks private TranscriptService transcriptService;

  @Test
  void requestOwn_shouldBuildPdfUploadPresignAndProduceEvent() throws Exception {
    UUID studentId = UUID.randomUUID();
    CurrentUser currentUser = new CurrentUser(UUID.randomUUID(), Role.STUDENT, studentId, null);
    TranscriptData data = mock(TranscriptData.class);
    File pdf = File.createTempFile("releve", ".pdf");
    URL presignedUrl = new URL("https://bucket.s3.eu-west-3.amazonaws.com/transcripts/x.pdf");
    Student student = studentWithEmail(studentId, "student@hei.com");

    when(securityAsserts.requireStudentId(currentUser)).thenReturn(studentId);
    when(transcriptDataService.build(studentId)).thenReturn(data);
    when(pdfGeneratorService.generate(data)).thenReturn(pdf);
    when(bucketComponent.presign(
            org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.any()))
        .thenReturn(presignedUrl);
    when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));

    TranscriptResponse response = transcriptService.requestOwn(currentUser);

    assertEquals(
        "https://bucket.s3.eu-west-3.amazonaws.com/transcripts/x.pdf", response.presignedUrl());
    assertEquals("student@hei.com", response.recipientEmail());

    verify(bucketComponent)
        .upload(org.mockito.ArgumentMatchers.eq(pdf), org.mockito.ArgumentMatchers.anyString());

    ArgumentCaptor<List<? extends PojaEvent>> captor = ArgumentCaptor.forClass(List.class);
    verify(eventProducer).accept(captor.capture());
    SendTranscriptRequested event = (SendTranscriptRequested) captor.getValue().get(0);
    assertEquals("student@hei.com", event.getTo());
    assertEquals(presignedUrl.toString(), event.getPresignedUrl());
    org.junit.jupiter.api.Assertions.assertTrue(event.getBucketKey().startsWith("transcripts/"));
  }

  @Test
  void requestOwn_shouldThrowWhenNoStudentProfile() {
    CurrentUser currentUser = new CurrentUser(UUID.randomUUID(), Role.ADMIN, null, null);
    when(securityAsserts.requireStudentId(currentUser))
        .thenThrow(new UnauthorizedActionException("Student profile not found"));

    assertThrows(
        UnauthorizedActionException.class, () -> transcriptService.requestOwn(currentUser));
  }

  @Test
  void requestForStudent_adminCanRequest() throws Exception {
    UUID studentId = UUID.randomUUID();
    CurrentUser currentUser = new CurrentUser(UUID.randomUUID(), Role.ADMIN, null, null);
    TranscriptData data = mock(TranscriptData.class);
    File pdf = File.createTempFile("releve", ".pdf");
    URL presignedUrl = new URL("https://bucket.s3.eu-west-3.amazonaws.com/transcripts/x.pdf");
    Student student = studentWithEmail(studentId, "student@hei.com");

    when(transcriptDataService.build(studentId)).thenReturn(data);
    when(pdfGeneratorService.generate(data)).thenReturn(pdf);
    when(bucketComponent.presign(
            org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.any()))
        .thenReturn(presignedUrl);
    when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));

    TranscriptResponse response = transcriptService.requestForStudent(studentId, currentUser);

    assertEquals(presignedUrl.toString(), response.presignedUrl());
    verify(eventProducer).accept(org.mockito.ArgumentMatchers.anyList());
  }

  @Test
  void requestForStudent_studentCannotRequest() {
    UUID studentId = UUID.randomUUID();
    CurrentUser currentUser = new CurrentUser(UUID.randomUUID(), Role.STUDENT, studentId, null);

    assertThrows(
        UnauthorizedActionException.class,
        () -> transcriptService.requestForStudent(studentId, currentUser));
  }

  @Test
  void requestForStudent_teacherCannotRequest() {
    UUID studentId = UUID.randomUUID();
    CurrentUser currentUser =
        new CurrentUser(UUID.randomUUID(), Role.TEACHER, null, UUID.randomUUID());

    assertThrows(
        UnauthorizedActionException.class,
        () -> transcriptService.requestForStudent(studentId, currentUser));
  }

  @Test
  void requestOwn_shouldThrowWhenStudentDoesNotExist() throws Exception {
    UUID studentId = UUID.randomUUID();
    CurrentUser currentUser = new CurrentUser(UUID.randomUUID(), Role.STUDENT, studentId, null);
    TranscriptData data = mock(TranscriptData.class);
    File pdf = File.createTempFile("releve", ".pdf");
    URL presignedUrl = new URL("https://bucket.s3.eu-west-3.amazonaws.com/transcripts/x.pdf");

    when(securityAsserts.requireStudentId(currentUser)).thenReturn(studentId);
    when(transcriptDataService.build(studentId)).thenReturn(data);
    when(pdfGeneratorService.generate(data)).thenReturn(pdf);
    when(bucketComponent.presign(
            org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.any()))
        .thenReturn(presignedUrl);
    when(studentRepository.findById(studentId)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> transcriptService.requestOwn(currentUser));
  }

  private Student studentWithEmail(UUID id, String email) {
    UserAccount account = new UserAccount();
    account.setEmail(email);
    Student student = new Student();
    student.setId(id);
    student.setUserAccount(account);
    return student;
  }
}
