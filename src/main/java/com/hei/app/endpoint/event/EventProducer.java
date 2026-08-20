package com.hei.app.endpoint.event;

import com.hei.app.endpoint.event.model.PojaEvent;
import java.util.List;

public interface EventProducer<T extends PojaEvent> {
  void accept(List<? extends T> events);
}
