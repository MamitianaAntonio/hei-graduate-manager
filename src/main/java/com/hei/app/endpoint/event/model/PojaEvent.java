package com.hei.app.endpoint.event.model;

import java.time.Duration;

public abstract class PojaEvent {
  public abstract Duration maxConsumerDuration();

  public abstract Duration maxConsumerBackoffBetweenRetries();
}
