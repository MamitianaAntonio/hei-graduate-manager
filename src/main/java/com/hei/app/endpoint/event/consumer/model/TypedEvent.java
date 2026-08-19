package com.hei.app.endpoint.event.consumer.model;

import com.hei.app.PojaGenerated;
import com.hei.app.endpoint.event.model.PojaEvent;

@PojaGenerated
public record TypedEvent(String typeName, PojaEvent payload) {}
