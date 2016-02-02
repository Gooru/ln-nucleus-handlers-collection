package org.gooru.nucleus.handlers.collections.processors.repositories.activejdbc.dbhandlers;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.gooru.nucleus.handlers.collections.constants.MessageConstants;
import org.gooru.nucleus.handlers.collections.processors.ProcessorContext;
import org.gooru.nucleus.handlers.collections.processors.events.EventBuilderFactory;
import org.gooru.nucleus.handlers.collections.processors.repositories.activejdbc.dbauth.AuthorizerBuilder;
import org.gooru.nucleus.handlers.collections.processors.repositories.activejdbc.entities.AJEntityCollection;
import org.gooru.nucleus.handlers.collections.processors.repositories.activejdbc.entities.AJEntityContent;
import org.gooru.nucleus.handlers.collections.processors.repositories.activejdbc.validators.PayloadValidator;
import org.gooru.nucleus.handlers.collections.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.collections.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.collections.processors.responses.MessageResponseFactory;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.DBException;
import org.javalite.activejdbc.LazyList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.UUID;

/**
 * Created by ashish on 12/1/16.
 */
class ReorderContentInCollectionHandler implements DBHandler {
  private static final Logger LOGGER = LoggerFactory.getLogger(ReorderContentInCollectionHandler.class);
  private final ProcessorContext context;
  private JsonArray input;

  public ReorderContentInCollectionHandler(ProcessorContext context) {
    this.context = context;
  }

  @Override
  public ExecutionResult<MessageResponse> checkSanity() {
    // There should be an collection id present
    if (context.collectionId() == null || context.collectionId().isEmpty()) {
      LOGGER.warn("Missing collection id");
      return new ExecutionResult<>(MessageResponseFactory.createInvalidRequestResponse("Missing collection id"),
        ExecutionResult.ExecutionStatus.FAILED);
    }
    // The user should not be anonymous
    if (context.userId() == null || context.userId().isEmpty() || context.userId().equalsIgnoreCase(MessageConstants.MSG_USER_ANONYMOUS)) {
      LOGGER.warn("Anonymous user attempting to reorder collection");
      return new ExecutionResult<>(MessageResponseFactory.createForbiddenResponse("Not allowed"), ExecutionResult.ExecutionStatus.FAILED);
    }
    // Payload should not be empty
    if (context.request() == null || context.request().isEmpty()) {
      LOGGER.warn("Empty payload supplied to reorder collection");
      return new ExecutionResult<>(MessageResponseFactory.createInvalidRequestResponse("Empty payload"), ExecutionResult.ExecutionStatus.FAILED);
    }
    JsonObject errors = new PayloadValidator() {
    }.validatePayload(context.request(), AJEntityCollection.reorderFieldSelector(), AJEntityCollection.getValidatorRegistry());
    if (errors != null && !errors.isEmpty()) {
      LOGGER.warn("Validation errors for request");
      return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(errors), ExecutionResult.ExecutionStatus.FAILED);
    }
    return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.CONTINUE_PROCESSING);

  }

  @Override
  public ExecutionResult<MessageResponse> validateRequest() {
    // Fetch the collection where type is collection and it is not deleted already and id is specified id
    LazyList<AJEntityCollection> collections =
      AJEntityCollection.findBySQL(AJEntityCollection.AUTHORIZER_QUERY, AJEntityCollection.COLLECTION, context.collectionId(), false);
    // Collection should be present in DB
    if (collections.size() < 1) {
      LOGGER.warn("Collection id: {} not present in DB", context.collectionId());
      return new ExecutionResult<>(MessageResponseFactory.createNotFoundResponse("collection id: " + context.collectionId()),
        ExecutionResult.ExecutionStatus.FAILED);
    }
    AJEntityCollection collection = collections.get(0);
    try {
      List idList = Base.firstColumn(AJEntityContent.CONTENT_FOR_REORDER_COLLECTION_QUERY, this.context.collectionId());
      this.input = this.context.request().getJsonArray(AJEntityCollection.REORDER_PAYLOAD_KEY);
      if (idList.size() != input.size()) {
        return new ExecutionResult<>(MessageResponseFactory.createInvalidRequestResponse("Question count mismatch"),
          ExecutionResult.ExecutionStatus.FAILED);
      }
      for (Object entry : input) {
        String payloadId = ((JsonObject) entry).getString(AJEntityCollection.ID);
        if (!idList.contains(UUID.fromString(payloadId))) {
          return new ExecutionResult<>(MessageResponseFactory.createInvalidRequestResponse("Missing content(s)"),
            ExecutionResult.ExecutionStatus.FAILED);
        }
      }
    } catch (DBException | ClassCastException e) {
      return new ExecutionResult<>(MessageResponseFactory.createInvalidRequestResponse("Incorrect payload data types"),
        ExecutionResult.ExecutionStatus.FAILED);
    }

    return new AuthorizerBuilder().buildUpdateAuthorizer(this.context).authorize(collection);
  }

  @Override
  public ExecutionResult<MessageResponse> executeRequest() {
    try {
      PreparedStatement ps = Base.startBatch(AJEntityContent.REORDER_QUERY);
      for (Object entry : input) {
        String payloadId = ((JsonObject) entry).getString(AJEntityCollection.ID);
        int sequenceId = ((JsonObject) entry).getInteger(AJEntityContent.SEQUENCE_ID);
        Base.addBatch(ps, sequenceId, this.context.userId(), payloadId, context.collectionId());
      }
      Base.executeBatch(ps);
    } catch (DBException | ClassCastException e) {
      // No special handling for CCE as this could have been thrown in the validation itself
      LOGGER.error("Not able to update the sequences for collection '{}'", context.collectionId(), e);
      return new ExecutionResult<>(MessageResponseFactory.createInternalErrorResponse("Not able to update sequences"),
        ExecutionResult.ExecutionStatus.FAILED);
    }

    return new ExecutionResult<>(
      MessageResponseFactory.createNoContentResponse("Updated", EventBuilderFactory.getUpdateCollectionEventBuilder(context.collectionId())),
      ExecutionResult.ExecutionStatus.SUCCESSFUL);

  }

  @Override
  public boolean handlerReadOnly() {
    return false;
  }
}
