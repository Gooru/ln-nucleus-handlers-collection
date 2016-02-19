package org.gooru.nucleus.handlers.collections.processors.events;

import io.vertx.core.json.JsonObject;

/**
 * Created by ashish on 19/1/16.
 */
public final class EventBuilderFactory {

  private static final String EVT_COLLECTION_CREATE = "event.collection.create";
  private static final String EVT_COLLECTION_UPDATE = "event.collection.update";
  private static final String EVT_COLLECTION_DELETE = "event.collection.delete";
  private static final String EVT_COLLECTION_ADD_CONTENT = "event.collection.add.content";
  private static final String EVENT_NAME = "event.name";
  private static final String EVENT_BODY = "event.body";
  private static final String COLLECTION_ID = "id";

  private EventBuilderFactory() {
    throw new AssertionError();
  }

  public static EventBuilder getDeleteCollectionEventBuilder(String collectionId) {
    return () -> new JsonObject().put(EVENT_NAME, EVT_COLLECTION_DELETE).put(EVENT_BODY, new JsonObject().put(COLLECTION_ID, collectionId));
  }

  public static EventBuilder getCreateCollectionEventBuilder(String collectionId) {
    return () -> new JsonObject().put(EVENT_NAME, EVT_COLLECTION_CREATE).put(EVENT_BODY, new JsonObject().put(COLLECTION_ID, collectionId));
  }

  public static EventBuilder getUpdateCollectionEventBuilder(String collectionId) {
    return () -> new JsonObject().put(EVENT_NAME, EVT_COLLECTION_UPDATE).put(EVENT_BODY, new JsonObject().put(COLLECTION_ID, collectionId));
  }

  public static EventBuilder getAddContentToCollectionEventBuilder(String collectionId) {
    return () -> new JsonObject().put(EVENT_NAME, EVT_COLLECTION_ADD_CONTENT).put(EVENT_BODY, new JsonObject().put(COLLECTION_ID, collectionId));
  }

  public static EventBuilder getReorderContentEventBuilder(String collectionId) {
    return () -> new JsonObject().put(EVENT_NAME, EVT_COLLECTION_UPDATE).put(EVENT_BODY, new JsonObject().put(COLLECTION_ID, collectionId));
  }

  public static EventBuilder getUpdateCollaboratorEventBuilder(String collectionId) {
    return () -> new JsonObject().put(EVENT_NAME, EVT_COLLECTION_UPDATE).put(EVENT_BODY, new JsonObject().put(COLLECTION_ID, collectionId));
  }


}
