package org.gooru.nucleus.handlers.collections.processors.tagaggregator;

import io.vertx.core.json.JsonObject;

/**
 * @author szgooru Created On: 13-Sep-2017
 */
public final class TagAggregatorRequestBuilderFactory {

  private static final String ENTITY_TYPE = "entity_type";
  private static final String ENTITY_ID = "entity_id";

  private static final String ENTITY_LESSON = "lesson";

  private TagAggregatorRequestBuilderFactory() {
    throw new AssertionError();
  }

  public static TagAggregatorRequestBuilder getLessonTagAggregatorRequestBuilder(String lessonId,
      JsonObject tags) {
    return () -> new JsonObject().put(ENTITY_ID, lessonId).put(ENTITY_TYPE, ENTITY_LESSON)
        .mergeIn(tags);
  }
}
