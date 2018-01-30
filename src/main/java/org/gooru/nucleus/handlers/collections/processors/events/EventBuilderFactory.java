package org.gooru.nucleus.handlers.collections.processors.events;

import io.vertx.core.json.JsonObject;

/**
 * Created by ashish on 19/1/16.
 */
public final class EventBuilderFactory {

    private static final String EVT_COLLECTION_CREATE = "event.collection.create";
    private static final String EVT_COLLECTION_UPDATE = "event.collection.update";
    private static final String EVT_COLLECTION_DELETE = "event.collection.delete";
    private static final String EVT_COLLECTION_CONTENT_ADD = "event.collection.content.add";
    private static final String EVT_COLLECTION_REORDER = "event.collection.content.reorder";
    private static final String EVT_COLLECTION_COLLABORATOR_UPDATE = "event.collection.collaborator.update";
    
    private static final String EVENT_NAME = "event.name";
    private static final String EVENT_BODY = "event.body";
    private static final String COLLECTION_ID = "id";
    private static final String CONTENT_ID = "content.id";

    private EventBuilderFactory() {
        throw new AssertionError();
    }

    public static EventBuilder getDeleteCollectionEventBuilder(String collectionId) {
        return () -> new JsonObject().put(EVENT_NAME, EVT_COLLECTION_DELETE).put(EVENT_BODY,
            new JsonObject().put(COLLECTION_ID, collectionId));
    }

    public static EventBuilder getCreateCollectionEventBuilder(String collectionId) {
        return () -> new JsonObject().put(EVENT_NAME, EVT_COLLECTION_CREATE).put(EVENT_BODY,
            new JsonObject().put(COLLECTION_ID, collectionId));
    }

    public static EventBuilder getUpdateCollectionEventBuilder(String collectionId) {
        return () -> new JsonObject().put(EVENT_NAME, EVT_COLLECTION_UPDATE).put(EVENT_BODY,
            new JsonObject().put(COLLECTION_ID, collectionId));
    }

    public static EventBuilder getAddContentToCollectionEventBuilder(String collectionId, String contentId) {
        return () -> new JsonObject().put(EVENT_NAME, EVT_COLLECTION_CONTENT_ADD).put(EVENT_BODY,
            new JsonObject().put(COLLECTION_ID, collectionId).put(CONTENT_ID, contentId));
    }

    public static EventBuilder getReorderContentEventBuilder(String collectionId) {
        return () -> new JsonObject().put(EVENT_NAME, EVT_COLLECTION_REORDER).put(EVENT_BODY,
            new JsonObject().put(COLLECTION_ID, collectionId));
    }

    public static EventBuilder getUpdateCollaboratorEventBuilder(String collectionId, JsonObject collaborators) {
        return () -> new JsonObject().put(EVENT_NAME, EVT_COLLECTION_COLLABORATOR_UPDATE).put(EVENT_BODY,
            collaborators.put(COLLECTION_ID, collectionId));
    }
    
}
