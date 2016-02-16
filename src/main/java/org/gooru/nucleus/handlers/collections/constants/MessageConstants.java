package org.gooru.nucleus.handlers.collections.constants;

public final class MessageConstants {

  public static final String MSG_HEADER_OP = "mb.operation";
  public static final String MSG_HEADER_TOKEN = "session.token";
  public static final String MSG_OP_STATUS = "mb.operation.status";
  public static final String MSG_KEY_PREFS = "prefs";
  public static final String MSG_OP_STATUS_SUCCESS = "success";
  public static final String MSG_OP_STATUS_ERROR = "error";
  public static final String MSG_OP_STATUS_VALIDATION_ERROR = "error.validation";
  public static final String MSG_USER_ANONYMOUS = "anonymous";
  public static final String MSG_USER_ID = "user_id";
  public static final String MSG_HTTP_STATUS = "http.status";
  public static final String MSG_HTTP_BODY = "http.body";
  public static final String MSG_HTTP_RESPONSE = "http.response";
  public static final String MSG_HTTP_ERROR = "http.error";
  public static final String MSG_HTTP_VALIDATION_ERROR = "http.validation.error";
  public static final String MSG_HTTP_HEADERS = "http.headers";
  public static final String MSG_MESSAGE = "message";

  // Operation names: Also need to be updated in corresponding handlers
  public static final String MSG_OP_COLLECTION_GET = "collection.get";
  public static final String MSG_OP_COLLECTION_CREATE = "collection.create";
  public static final String MSG_OP_COLLECTION_UPDATE = "collection.update";
  public static final String MSG_OP_COLLECTION_DELETE = "collection.delete";
  public static final String MSG_OP_COLLECTION_COLLABORATOR_UPDATE = "collection..collaborator.update";
  public static final String MSG_OP_COLLECTION_QUESTION_ADD = "collection.question.add";
  public static final String MSG_OP_COLLECTION_CONTENT_REORDER = "collection.question.reorder";
  public static final String MSG_OP_COLLECTION_RESOURCE_ADD = "collection.resource.add";

  // Containers for different responses
  public static final String RESP_CONTAINER_MBUS = "mb.container";
  public static final String RESP_CONTAINER_EVENT = "mb.event";

  public static final String COLLECTION_ID = "collectionId";
  public static final String QUESTION_ID = "questionId";
  public static final String RESOURCE_ID = "resourceId";
  public static final String ID = "id";

  private MessageConstants() {
    throw new AssertionError();
  }
}
