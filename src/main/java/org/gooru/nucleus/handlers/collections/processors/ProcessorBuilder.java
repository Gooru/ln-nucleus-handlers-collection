package org.gooru.nucleus.handlers.collections.processors;

import io.vertx.core.eventbus.Message;

public final class ProcessorBuilder {


  private ProcessorBuilder(Message<Object> message) {
    throw new AssertionError();
  }

  public static Processor build(Message<Object> message) {
    return new MessageProcessor(message);
  }
}
