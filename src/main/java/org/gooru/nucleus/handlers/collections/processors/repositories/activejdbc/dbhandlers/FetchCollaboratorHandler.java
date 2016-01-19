package org.gooru.nucleus.handlers.collections.processors.repositories.activejdbc.dbhandlers;

import io.vertx.core.json.JsonObject;
import org.gooru.nucleus.handlers.collections.processors.ProcessorContext;
import org.gooru.nucleus.handlers.collections.processors.repositories.activejdbc.entities.AJEntityCollection;
import org.gooru.nucleus.handlers.collections.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.collections.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.collections.processors.responses.MessageResponseFactory;
import org.javalite.activejdbc.LazyList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ashish on 12/1/16.
 */
class FetchCollaboratorHandler implements DBHandler {
  private static final Logger LOGGER = LoggerFactory.getLogger(FetchCollaboratorHandler.class);
  private final ProcessorContext context;

  public FetchCollaboratorHandler(ProcessorContext context) {
    this.context = context;
  }

  @Override
  public ExecutionResult<MessageResponse> checkSanity() {
    // Fetch the collection where type is collection and it is not deleted already and id is specified id

    LazyList<AJEntityCollection> collections = AJEntityCollection
      .findBySQL(
        AJEntityCollection.SELECT_FOR_VALIDATE,
        AJEntityCollection.COLLECTION,
        context.collectionId(), false);
    // Collection should be present in DB
    if (collections.size() < 1) {
      LOGGER.warn("Collection id: {} not present in DB", context.collectionId());
      return new ExecutionResult<>(MessageResponseFactory.createNotFoundResponse("collection id: " + context.collectionId()),
        ExecutionResult.ExecutionStatus.FAILED);
    }
    return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.CONTINUE_PROCESSING);
  }

  @Override
  public ExecutionResult<MessageResponse> validateRequest() {
    // Nothing to validate, so we are all set
    return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.CONTINUE_PROCESSING);
  }

  @Override
  public ExecutionResult<MessageResponse> executeRequest() {
    LazyList<AJEntityCollection> collections = AJEntityCollection.findBySQL(
      AJEntityCollection.SELECT_COLLABORATOR,
      context.collectionId()
    );
    if (collections.size() == 0 || collections.isEmpty()) {
      LOGGER.warn("Collection id: {} not present in DB", context.collectionId());
      return new ExecutionResult<>(MessageResponseFactory.createNotFoundResponse("collection id: " + context.collectionId()),
        ExecutionResult.ExecutionStatus.FAILED);
    } else if (collections.size() > 1) {
      LOGGER.warn("Collection id: {} present multiple times, not sure which one is being looked for", context.collectionId());
      return new ExecutionResult<>(MessageResponseFactory.createNotFoundResponse("collection id: " + context.collectionId()),
        ExecutionResult.ExecutionStatus.FAILED);
    } else {
      AJEntityCollection collection = collections.get(0);
      String response = collection.toJson(false, AJEntityCollection.COLLABORATOR);
      return new ExecutionResult<>(MessageResponseFactory.createOkayResponse(new JsonObject(response)), ExecutionResult.ExecutionStatus.SUCCESSFUL);
    }
  }

  @Override
  public boolean handlerReadOnly() {
    return true;
  }
}
