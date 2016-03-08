package org.gooru.nucleus.handlers.collections.processors;

import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import org.gooru.nucleus.handlers.collections.constants.MessageConstants;
import org.gooru.nucleus.handlers.collections.processors.repositories.RepoBuilder;
import org.gooru.nucleus.handlers.collections.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.collections.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.collections.processors.responses.MessageResponseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ResourceBundle;
import java.util.UUID;

class MessageProcessor implements Processor {

  private static final Logger LOGGER = LoggerFactory.getLogger(Processor.class);
  private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("messages");
  private final Message<Object> message;
  private final ResourceBundle resourceBundle = ResourceBundle.getBundle("messages");
  private String userId;
  private JsonObject prefs;
  private JsonObject request;

  public MessageProcessor(Message<Object> message) {
    this.message = message;
  }

  @Override
  public MessageResponse process() {
    MessageResponse result;
    try {
      // Validate the message itself
      ExecutionResult<MessageResponse> validateResult = validateAndInitialize();
      if (validateResult.isCompleted()) {
        return validateResult.result();
      }

      final String msgOp = message.headers().get(MessageConstants.MSG_HEADER_OP);
      switch (msgOp) {
        case MessageConstants.MSG_OP_COLLECTION_GET:
          result = processCollectionGet();
          break;
        case MessageConstants.MSG_OP_COLLECTION_CREATE:
          result = processCollectionCreate();
          break;
        case MessageConstants.MSG_OP_COLLECTION_UPDATE:
          result = processCollectionUpdate();
          break;
        case MessageConstants.MSG_OP_COLLECTION_DELETE:
          result = processCollectionDelete();
          break;
        case MessageConstants.MSG_OP_COLLECTION_QUESTION_ADD:
          result = processCollectionAddQuestion();
          break;
        case MessageConstants.MSG_OP_COLLECTION_RESOURCE_ADD:
          result = processCollectionAddResource();
          break;
        case MessageConstants.MSG_OP_COLLECTION_CONTENT_REORDER:
          result = processCollectionContentReorder();
          break;
        case MessageConstants.MSG_OP_COLLECTION_COLLABORATOR_UPDATE:
          result = processCollectionCollaboratorUpdate();
          break;
        default:
          LOGGER.error("Invalid operation type passed in, not able to handle");
          return MessageResponseFactory.createInvalidRequestResponse(resourceBundle.getString("operation.invalid"));
      }
      return result;
    } catch (Throwable e) {
      LOGGER.error("Unhandled exception in processing", e);
      return MessageResponseFactory.createInternalErrorResponse();
    }
  }

  private MessageResponse processCollectionContentReorder() {
    ProcessorContext context = createContext();
    if (!validateContext(context)) {
      return MessageResponseFactory.createInvalidRequestResponse(resourceBundle.getString("collection.id.invalid"));
    }
    return RepoBuilder.buildCollectionRepo(context).reorderContentInCollection();
  }

  private MessageResponse processCollectionCollaboratorUpdate() {
    ProcessorContext context = createContext();
    if (!validateContext(context)) {
      return MessageResponseFactory.createInvalidRequestResponse(resourceBundle.getString("collection.id.invalid"));
    }
    return RepoBuilder.buildCollectionRepo(context).updateCollaborator();
  }

  private MessageResponse processCollectionAddQuestion() {
    ProcessorContext context = createContextWithQuestion();
    if (!validateContextWithQuestion(context)) {
      return MessageResponseFactory.createInvalidRequestResponse(resourceBundle.getString("collection.question.id.invalid"));
    }
    return RepoBuilder.buildCollectionRepo(context).addQuestionToCollection();
  }

  private MessageResponse processCollectionAddResource() {
    ProcessorContext context = createContextWithResource();
    if (!validateContextWithResource(context)) {
      return MessageResponseFactory.createInvalidRequestResponse(resourceBundle.getString("collection.resource.id.invalid"));
    }
    return RepoBuilder.buildCollectionRepo(context).addResourceToCollection();
  }

  private MessageResponse processCollectionDelete() {
    ProcessorContext context = createContext();
    if (!validateContext(context)) {
      return MessageResponseFactory.createInvalidRequestResponse(resourceBundle.getString("collection.id.invalid"));
    }
    return RepoBuilder.buildCollectionRepo(context).deleteCollection();
  }

  private MessageResponse processCollectionUpdate() {
    ProcessorContext context = createContext();
    if (!validateContext(context)) {
      return MessageResponseFactory.createInvalidRequestResponse(resourceBundle.getString("collection.id.invalid"));
    }
    return RepoBuilder.buildCollectionRepo(context).updateCollection();
  }

  private MessageResponse processCollectionGet() {
    ProcessorContext context = createContext();
    if (!validateContext(context)) {
      return MessageResponseFactory.createInvalidRequestResponse(resourceBundle.getString("collection.id.invalid"));
    }
    return RepoBuilder.buildCollectionRepo(context).fetchCollection();
  }

  private MessageResponse processCollectionCreate() {
    ProcessorContext context = createContext();

    return RepoBuilder.buildCollectionRepo(context).createCollection();
  }

  private ProcessorContext createContext() {
    String collectionId = message.headers().get(MessageConstants.COLLECTION_ID);
    return new ProcessorContext(userId, prefs, request, collectionId, null, null);
  }

  private ProcessorContext createContextWithQuestion() {
    String collectionId = message.headers().get(MessageConstants.COLLECTION_ID);
    String questionId = request.getString(MessageConstants.ID);
    return new ProcessorContext(userId, prefs, request, collectionId, questionId, null);
  }

  private ProcessorContext createContextWithResource() {
    String collectionId = message.headers().get(MessageConstants.COLLECTION_ID);
    String resourceId = request.getString(MessageConstants.ID);
    return new ProcessorContext(userId, prefs, request, collectionId, null, resourceId);
  }

  private ExecutionResult<MessageResponse> validateAndInitialize() {
    if (message == null || !(message.body() instanceof JsonObject)) {
      LOGGER.error("Invalid message received, either null or body of message is not JsonObject ");
      return new ExecutionResult<>(MessageResponseFactory.createInvalidRequestResponse(RESOURCE_BUNDLE.getString("invalid.message")),
        ExecutionResult.ExecutionStatus.FAILED);
    }

    userId = ((JsonObject) message.body()).getString(MessageConstants.MSG_USER_ID);
    if (!validateUser(userId)) {
      LOGGER.error("Invalid user id passed. Not authorized.");
      return new ExecutionResult<>(MessageResponseFactory.createForbiddenResponse(RESOURCE_BUNDLE.getString("missing.user")),
        ExecutionResult.ExecutionStatus.FAILED);
    }

    prefs = ((JsonObject) message.body()).getJsonObject(MessageConstants.MSG_KEY_PREFS);
    request = ((JsonObject) message.body()).getJsonObject(MessageConstants.MSG_HTTP_BODY);

    if (prefs == null || prefs.isEmpty()) {
      LOGGER.error("Invalid preferences obtained, probably not authorized properly");
      return new ExecutionResult<>(MessageResponseFactory.createForbiddenResponse(RESOURCE_BUNDLE.getString("missing.preferences")),
        ExecutionResult.ExecutionStatus.FAILED);
    }

    if (request == null) {
      LOGGER.error("Invalid JSON payload on Message Bus");
      return new ExecutionResult<>(MessageResponseFactory.createInvalidRequestResponse(RESOURCE_BUNDLE.getString("invalid.payload")),
        ExecutionResult.ExecutionStatus.FAILED);
    }

    // All is well, continue processing
    return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.CONTINUE_PROCESSING);
  }


  private boolean validateContext(ProcessorContext context) {
    return validateContext(context, false, false);
  }

  private boolean validateContextWithQuestion(ProcessorContext context) {
    return validateContext(context, true, false);
  }

  private boolean validateContextWithResource(ProcessorContext context) {
    return validateContext(context, false, true);
  }

  private boolean validateContext(ProcessorContext context, boolean shouldHaveQuestion, boolean shouldHaveResource) {
    if (!validateId(context.collectionId())) {
      LOGGER.error("Invalid request, collection id not available/incorrect format. Aborting");
      return false;
    }
    if (shouldHaveQuestion) {
      if (!validateId(context.questionId())) {
        LOGGER.error("Invalid request, question id not available/incorrect format. Aborting");
        return false;
      }
    }
    if (shouldHaveResource) {
      if (!validateId(context.resourceId())) {
        LOGGER.error("Invalid request, resource id not available/incorrect format. Aborting");
        return false;
      }
    }
    return true;
  }

  private boolean validateUser(String userId) {
    return !(userId == null || userId.isEmpty()) && (userId.equalsIgnoreCase(MessageConstants.MSG_USER_ANONYMOUS) || validateUuid(userId));
  }

  private boolean validateId(String id) {
    return !(id == null || id.isEmpty()) && validateUuid(id);
  }

  private boolean validateUuid(String uuidString) {
    try {
      UUID uuid = UUID.fromString(uuidString);
      return true;
    } catch (IllegalArgumentException e) {
      return false;
    } catch (Exception e) {
      return false;
    }
  }

}
