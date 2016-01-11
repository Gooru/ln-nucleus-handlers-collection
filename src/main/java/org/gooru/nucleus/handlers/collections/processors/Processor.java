package org.gooru.nucleus.handlers.collections.processors;

import org.gooru.nucleus.handlers.collections.processors.responses.MessageResponse;

public interface Processor {
  public MessageResponse process();
}
