package com.hei.app.endpoint.event;

import com.hei.app.endpoint.event.model.PojaEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.ResolvableType;
import org.springframework.stereotype.Component;

@Component
public class EventProducerImpl implements EventProducer<PojaEvent> {
  private final Map<Class<? extends PojaEvent>, Consumer<? extends PojaEvent>> consumerByEventType =
      new HashMap<>();

  public EventProducerImpl(List<Consumer<?>> consumers) {
    for (Consumer<?> consumer : consumers) {
      Class<?> eventType = eventTypeOf(consumer);
      if (eventType != null && PojaEvent.class.isAssignableFrom(eventType)) {
        consumerByEventType.put(
            (Class<? extends PojaEvent>) eventType, (Consumer<? extends PojaEvent>) consumer);
      }
    }
  }

  @Override
  public void accept(List<? extends PojaEvent> events) {
    events.forEach(this::dispatch);
  }

  private void dispatch(PojaEvent event) {
    Consumer<? extends PojaEvent> consumer = consumerByEventType.get(event.getClass());
    if (consumer == null) {
      throw new IllegalArgumentException(
          "No consumer registered for event: " + event.getClass().getSimpleName());
    }
    ((Consumer<PojaEvent>) consumer).accept(event);
  }

  private Class<?> eventTypeOf(Object consumer) {
    Class<?> targetClass = AopUtils.getTargetClass(consumer);
    return ResolvableType.forClass(targetClass).as(Consumer.class).getGeneric(0).resolve();
  }
}
