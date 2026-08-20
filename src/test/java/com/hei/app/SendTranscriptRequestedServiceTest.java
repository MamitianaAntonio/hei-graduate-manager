package com.hei.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hei.app.endpoint.event.model.SendTranscriptRequested;
import com.hei.app.file.bucket.BucketComponent;
import com.hei.app.mail.Email;
import com.hei.app.mail.Mailer;
import com.hei.app.service.event.SendTranscriptRequestedService;
import java.io.File;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SendTranscriptRequestedServiceTest {
  @Mock private Mailer mailer;

  @Mock private BucketComponent bucketComponent;

  @InjectMocks private SendTranscriptRequestedService sendTranscriptRequestedService;

  @Test
  void accept_shouldDownloadPdfAndSendEmailWithAttachment() throws Exception {
    String recipient = "student@hei.com";
    String bucketKey = "transcripts/x.pdf";
    String presignedUrl = "https://bucket.s3.eu-west-3.amazonaws.com/transcripts/x.pdf";
    File pdf = File.createTempFile("releve", ".pdf");
    SendTranscriptRequested event =
        SendTranscriptRequested.builder()
            .to(recipient)
            .bucketKey(bucketKey)
            .presignedUrl(presignedUrl)
            .build();

    when(bucketComponent.download(bucketKey)).thenReturn(pdf);

    sendTranscriptRequestedService.accept(event);

    ArgumentCaptor<Email> emailCaptor = ArgumentCaptor.forClass(Email.class);
    verify(mailer).accept(emailCaptor.capture());

    Email email = emailCaptor.getValue();
    assertEquals(recipient, email.to().getAddress());
    assertEquals("Relevé de notes", email.subject());
    assertEquals(1, email.attachments().size());
    assertEquals(pdf, email.attachments().get(0));
    assertEquals(List.of(), email.cc());
    assertEquals(List.of(), email.bcc());
  }

  @Test
  void accept_shouldIncludePresignedUrlInBody() throws Exception {
    String presignedUrl = "https://bucket.s3.eu-west-3.amazonaws.com/transcripts/x.pdf";
    File pdf = mock(File.class);
    SendTranscriptRequested event =
        SendTranscriptRequested.builder()
            .to("student@hei.com")
            .bucketKey("transcripts/x.pdf")
            .presignedUrl(presignedUrl)
            .build();

    when(bucketComponent.download("transcripts/x.pdf")).thenReturn(pdf);

    sendTranscriptRequestedService.accept(event);

    ArgumentCaptor<Email> emailCaptor = ArgumentCaptor.forClass(Email.class);
    verify(mailer).accept(emailCaptor.capture());
    assertEquals(
        presignedUrl, emailCaptor.getValue().htmlBody().replaceAll(".*href=\"([^\"]*)\".*", "$1"));
  }
}
