package com.hei.app;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.hei.app.endpoint.event.EventProducerImpl;
import com.hei.app.endpoint.event.model.PojaEvent;
import com.hei.app.endpoint.event.model.SendTranscriptRequested;
import com.hei.app.service.event.SendTranscriptRequestedService;
import java.util.List;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;

public class EventProducerImplTest {

  @Test
  void accept_shouldDispatchToMatchingConsumer() {
    SendTranscriptRequestedService consumer = mock(SendTranscriptRequestedService.class);
    EventProducerImpl producer = new EventProducerImpl(List.of(consumer));
    SendTranscriptRequested event = new SendTranscriptRequested();

    producer.accept(List.of(event));

    verify(consumer).accept(event);
  }

  @Test
  void accept_shouldIgnoreConsumersOfNonPojaEventTypes() {
    Consumer<Object> otherConsumer = mock(Consumer.class);
    SendTranscriptRequestedService consumer = mock(SendTranscriptRequestedService.class);
    EventProducerImpl producer = new EventProducerImpl(List.of(otherConsumer, consumer));
    SendTranscriptRequested event = new SendTranscriptRequested();

    producer.accept(List.of(event));

    verify(consumer).accept(event);
  }

  @Test
  void accept_shouldThrowWhenNoConsumerRegistered() {
    EventProducerImpl producer = new EventProducerImpl(List.of());
    PojaEvent event = new SendTranscriptRequested();

    assertThrows(IllegalArgumentException.class, () -> producer.accept(List.of(event)));
  }
}
