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

class MessageProcessor implements Processor {

  private static final Logger LOGGER = LoggerFactory.getLogger(Processor.class);
  private final Message<Object> message;
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
          return MessageResponseFactory.createInvalidRequestResponse("Invalid operation");
      }
      return result;
    } catch (Throwable e) {
      LOGGER.error("Unhandled exception in processing", e);
      return MessageResponseFactory.createInternalErrorResponse();
    }
  }

  private MessageResponse processCollectionAddResource() {
    ProcessorContext context = createContext();
    if (context.collectionId() == null || context.collectionId().isEmpty()) {
      LOGGER.error("Invalid request, collection id not available. Aborting");
      return MessageResponseFactory.createInvalidRequestResponse("Invalid collection id");
    }
    return new RepoBuilder().buildCollectionRepo(context).addResourceToCollection();
  }

  private MessageResponse processCollectionAddQuestion() {
    ProcessorContext context = createContext();
    if (context.collectionId() == null || context.collectionId().isEmpty()) {
      LOGGER.error("Invalid request, collection id not available. Aborting");
      return MessageResponseFactory.createInvalidRequestResponse("Invalid collection id");
    }
    return new RepoBuilder().buildCollectionRepo(context).addQuestionToCollection();
  }

  private MessageResponse processCollectionContentReorder() {
    ProcessorContext context = createContext();
    if (context.collectionId() == null || context.collectionId().isEmpty()) {
      LOGGER.error("Invalid request, collection id not available. Aborting");
      return MessageResponseFactory.createInvalidRequestResponse("Invalid collection id");

    }
    return new RepoBuilder().buildCollectionRepo(context).reorderContentInCollection();
  }

  private MessageResponse processCollectionCollaboratorUpdate() {
    ProcessorContext context = createContext();
    if (context.collectionId() == null || context.collectionId().isEmpty()) {
      LOGGER.error("Invalid request, collection id not available. Aborting");
      return MessageResponseFactory.createInvalidRequestResponse("Invalid collection id");
    }
    return new RepoBuilder().buildCollectionRepo(context).updateCollaborator();
  }


  private MessageResponse processCollectionDelete() {
    ProcessorContext context = createContext();
    if (context.collectionId() == null || context.collectionId().isEmpty()) {
      LOGGER.error("Invalid request, collection id not available. Aborting");
      return MessageResponseFactory.createInvalidRequestResponse("Invalid collection id");
    }
    return new RepoBuilder().buildCollectionRepo(context).deleteCollection();
  }

  private MessageResponse processCollectionUpdate() {
    ProcessorContext context = createContext();
    if (context.collectionId() == null || context.collectionId().isEmpty()) {
      LOGGER.error("Invalid request, collection id not available. Aborting");
      return MessageResponseFactory.createInvalidRequestResponse("Invalid collection id");
    }
    return new RepoBuilder().buildCollectionRepo(context).updateCollection();
  }

  private MessageResponse processCollectionGet() {
    ProcessorContext context = createContext();
    if (context.collectionId() == null || context.collectionId().isEmpty()) {
      LOGGER.error("Invalid request, collection id not available. Aborting");
      return MessageResponseFactory.createInvalidRequestResponse("Invalid collection id");
    }
    return new RepoBuilder().buildCollectionRepo(context).fetchCollection();
  }

  private MessageResponse processCollectionCreate() {
    ProcessorContext context = createContext();

    return new RepoBuilder().buildCollectionRepo(context).createCollection();
  }

  private ProcessorContext createContext() {
    String collectionId = message.headers().get(MessageConstants.COLLECTION_ID);
    String questionId = message.headers().get(MessageConstants.QUESTION_ID);
    String resourceId = message.headers().get(MessageConstants.RESOURCE_ID);

    return new ProcessorContext(userId, prefs, request, collectionId, questionId, resourceId);
  }

  private ExecutionResult<MessageResponse> validateAndInitialize() {
    if (message == null || !(message.body() instanceof JsonObject)) {
      LOGGER.error("Invalid message received, either null or body of message is not JsonObject ");
      return new ExecutionResult<>(MessageResponseFactory.createInvalidRequestResponse(), ExecutionResult.ExecutionStatus.FAILED);
    }

    userId = ((JsonObject) message.body()).getString(MessageConstants.MSG_USER_ID);
    if (userId == null) {
      LOGGER.error("Invalid user id passed. Not authorized.");
      return new ExecutionResult<>(MessageResponseFactory.createForbiddenResponse(), ExecutionResult.ExecutionStatus.FAILED);
    }
    prefs = ((JsonObject) message.body()).getJsonObject(MessageConstants.MSG_KEY_PREFS);
    request = ((JsonObject) message.body()).getJsonObject(MessageConstants.MSG_HTTP_BODY);

    if (prefs == null || prefs.isEmpty()) {
      LOGGER.error("Invalid preferences obtained, probably not authorized properly");
      return new ExecutionResult<>(MessageResponseFactory.createForbiddenResponse(), ExecutionResult.ExecutionStatus.FAILED);
    }

    if (request == null) {
      LOGGER.error("Invalid JSON payload on Message Bus");
      return new ExecutionResult<>(MessageResponseFactory.createInvalidRequestResponse(), ExecutionResult.ExecutionStatus.FAILED);
    }

    // All is well, continue processing
    return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.CONTINUE_PROCESSING);
  }


}
