package org.gooru.nucleus.handlers.collections.processors;

import io.vertx.core.json.JsonObject;

public interface Processor {
  public JsonObject process();
}
