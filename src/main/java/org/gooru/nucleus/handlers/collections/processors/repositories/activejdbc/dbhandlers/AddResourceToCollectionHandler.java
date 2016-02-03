package org.gooru.nucleus.handlers.collections.processors.repositories.activejdbc.dbhandlers;

import io.vertx.core.json.JsonObject;
import org.gooru.nucleus.handlers.collections.constants.MessageConstants;
import org.gooru.nucleus.handlers.collections.processors.ProcessorContext;
import org.gooru.nucleus.handlers.collections.processors.events.EventBuilderFactory;
import org.gooru.nucleus.handlers.collections.processors.repositories.activejdbc.dbauth.AuthorizerBuilder;
import org.gooru.nucleus.handlers.collections.processors.repositories.activejdbc.entities.AJEntityCollection;
import org.gooru.nucleus.handlers.collections.processors.repositories.activejdbc.entities.AJEntityContent;
import org.gooru.nucleus.handlers.collections.processors.repositories.activejdbc.entities.AJEntityResource;
import org.gooru.nucleus.handlers.collections.processors.repositories.activejdbc.validators.PayloadValidator;
import org.gooru.nucleus.handlers.collections.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.collections.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.collections.processors.responses.MessageResponseFactory;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.DBException;
import org.javalite.activejdbc.LazyList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ResourceBundle;

/**
 * Created by ashish on 12/1/16.
 */
class AddResourceToCollectionHandler implements DBHandler {
  private static final Logger LOGGER = LoggerFactory.getLogger(UpdateCollectionHandler.class);
  private final ProcessorContext context;
  private static ResourceBundle resourceBundle = ResourceBundle.getBundle("messages");

  public AddResourceToCollectionHandler(ProcessorContext context) {
    this.context = context;
  }

  @Override
  public ExecutionResult<MessageResponse> checkSanity() {
    // There should be an collection id present
    if (context.collectionId() == null || context.collectionId().isEmpty() || context.resourceId() == null || context.resourceId().isEmpty()) {
      LOGGER.warn("Missing collection/question id");
      return new ExecutionResult<>(MessageResponseFactory.createInvalidRequestResponse(resourceBundle.getString("content.id.missing")),
        ExecutionResult.ExecutionStatus.FAILED);
    }
    // The user should not be anonymous
    if (context.userId() == null || context.userId().isEmpty() || context.userId().equalsIgnoreCase(MessageConstants.MSG_USER_ANONYMOUS)) {
      LOGGER.warn("Anonymous user attempting to edit collection");
      return new ExecutionResult<>(MessageResponseFactory.createForbiddenResponse(resourceBundle.getString("not.allowed")), ExecutionResult.ExecutionStatus.FAILED);
    }
    // Payload should not be empty
    if (context.request() == null || context.request().isEmpty()) {
      LOGGER.warn("Empty payload supplied to edit collection");
      return new ExecutionResult<>(MessageResponseFactory.createInvalidRequestResponse(resourceBundle.getString("payload.empty")), ExecutionResult.ExecutionStatus.FAILED);
    }
    // Our validators should certify this
    JsonObject errors = new DefaultPayloadValidator()
      .validatePayload(context.request(), AJEntityCollection.addResourceFieldSelector(), AJEntityCollection.getValidatorRegistry());
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
      return new ExecutionResult<>(MessageResponseFactory.createNotFoundResponse(resourceBundle.getString("collection.id") + context.collectionId()),
        ExecutionResult.ExecutionStatus.FAILED);
    }
    AJEntityCollection collection = collections.get(0);
    return new AuthorizerBuilder().buildAddContentToCollectionAuthorizer(this.context).authorize(collection);
  }

  @Override
  public ExecutionResult<MessageResponse> executeRequest() {
    try {
      Object sequence = Base.firstCell(AJEntityContent.MAX_CONTENT_SEQUENCE_QUERY, this.context.collectionId());
      int sequenceId = 1;
      if (sequence != null) {
        int currentSequence = Integer.valueOf(sequence.toString());
        sequenceId = currentSequence + 1;
      }
      long count = Base
        .exec(AJEntityResource.ADD_RESOURCE_QUERY, this.context.collectionId(), this.context.userId(), sequenceId, this.context.resourceId(),
          this.context.userId());
      if (count == 1) {
        return new ExecutionResult<>(MessageResponseFactory
          .createNoContentResponse(resourceBundle.getString("resource.added"), EventBuilderFactory.getAddResourceToCollectionEventBuilder(context.collectionId())),
          ExecutionResult.ExecutionStatus.SUCCESSFUL);
      } else {
        LOGGER.error("Something is wrong. Adding resource '{}' to collection '{}' updated '{}' rows", this.context.resourceId(),
          this.context.collectionId(), count);
      }
    } catch (DBException e) {
      LOGGER.error("Not able to add resource '{}' to collection '{}'", this.context.resourceId(), this.context.collectionId(), e);
      return new ExecutionResult<>(MessageResponseFactory.createInternalErrorResponse(resourceBundle.getString("internal.store.error")),
        ExecutionResult.ExecutionStatus.FAILED);
    }
    return new ExecutionResult<>(MessageResponseFactory.createInternalErrorResponse(resourceBundle.getString("resource.add.failed")),
      ExecutionResult.ExecutionStatus.FAILED);
  }

  @Override
  public boolean handlerReadOnly() {
    return false;
  }

  private static class DefaultPayloadValidator implements PayloadValidator {
  }
}
