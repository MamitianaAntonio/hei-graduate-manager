package com.hei.app.service.event;

import com.hei.app.endpoint.event.model.SendTranscriptRequested;
import com.hei.app.file.bucket.BucketComponent;
import com.hei.app.mail.Email;
import com.hei.app.mail.Mailer;
import jakarta.mail.internet.InternetAddress;
import java.io.File;
import java.util.List;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class SendTranscriptRequestedService implements Consumer<SendTranscriptRequested> {
  private final Mailer mailer;
  private final BucketComponent bucketComponent;

  @SneakyThrows
  @Override
  public void accept(SendTranscriptRequested event) {
    File pdf = bucketComponent.download(event.getBucketKey());
    var recipient = new InternetAddress(event.getTo());
    mailer.accept(
        new Email(
            recipient,
            List.of(),
            List.of(),
            "Relevé de notes",
            "<span>Votre relevé de notes est disponible : <a href=\""
                + event.getPresignedUrl()
                + "\">télécharger</a></span>",
            List.of(pdf)));
  }
}
