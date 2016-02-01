package org.gooru.nucleus.handlers.collections.processors.repositories.activejdbc.dbhandlers;

import io.vertx.core.json.JsonObject;
import org.gooru.nucleus.handlers.collections.constants.MessageConstants;
import org.gooru.nucleus.handlers.collections.processors.ProcessorContext;
import org.gooru.nucleus.handlers.collections.processors.events.EventBuilderFactory;
import org.gooru.nucleus.handlers.collections.processors.repositories.activejdbc.dbauth.AuthorizerBuilder;
import org.gooru.nucleus.handlers.collections.processors.repositories.activejdbc.entities.AJEntityCollection;
import org.gooru.nucleus.handlers.collections.processors.repositories.activejdbc.entitybuilders.EntityBuilder;
import org.gooru.nucleus.handlers.collections.processors.repositories.activejdbc.validators.PayloadValidator;
import org.gooru.nucleus.handlers.collections.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.collections.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.collections.processors.responses.MessageResponseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Created by ashish on 12/1/16.
 */
class CreateCollectionHandler implements DBHandler {
  private static final Logger LOGGER = LoggerFactory.getLogger(CreateCollectionHandler.class);
  private final ProcessorContext context;
  private AJEntityCollection collection;
  public CreateCollectionHandler(ProcessorContext context) {
    this.context = context;
  }

  @Override
  public ExecutionResult<MessageResponse> checkSanity() {
    // The user should not be anonymous
    if (context.userId() == null || context.userId().isEmpty() || context.userId().equalsIgnoreCase(MessageConstants.MSG_USER_ANONYMOUS)) {
      LOGGER.warn("Anonymous or invalid user attempting to create collection");
      return new ExecutionResult<>(MessageResponseFactory.createForbiddenResponse("Not allowed"), ExecutionResult.ExecutionStatus.FAILED);
    }
    // Payload should not be empty
    if (context.request() == null || context.request().isEmpty()) {
      LOGGER.warn("Empty payload supplied to create collection");
      return new ExecutionResult<>(MessageResponseFactory.createInvalidRequestResponse("Empty payload"), ExecutionResult.ExecutionStatus.FAILED);
    }
    // Our validators should certify this
    JsonObject errors = new PayloadValidator() {
    }.validatePayload(context.request(), AJEntityCollection.createFieldSelector(), AJEntityCollection.getValidatorRegistry());
    if (errors != null && !errors.isEmpty()) {
      LOGGER.warn("Validation errors for request");
      return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(errors), ExecutionResult.ExecutionStatus.FAILED);
    }
    return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.CONTINUE_PROCESSING);
  }

  @Override
  public ExecutionResult<MessageResponse> validateRequest() {
    // Only thing to do here is to authorize
    return new AuthorizerBuilder().buildCreateAuthorizer(context).authorize(this.collection);
  }

  @Override
  public ExecutionResult<MessageResponse> executeRequest() {
    AJEntityCollection collection = new AJEntityCollection();
    // First time creation is standalone, no course exists. It will be associated later, if the need arises. So all user ids are same
    collection.setModifierId(context.userId());
    collection.setOwnerId(context.userId());
    collection.setCreatorId(context.userId());
    collection.setTypeCollection();
    collection.setGrading(AJEntityCollection.GRADING_TYPE_SYSTEM);
    // Now auto populate is done, we need to setup the converter machinery
    new EntityBuilder<AJEntityCollection>() {
    }.build(collection, context.request(), AJEntityCollection.getConverterRegistry());

    boolean result = collection.save();
    if (!result) {
      LOGGER.error("Collection with id '{}' failed to save", context.collectionId());
      if (collection.hasErrors()) {
        Map<String, String> map = collection.errors();
        JsonObject errors = new JsonObject();
        map.forEach(errors::put);
        return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(errors), ExecutionResult.ExecutionStatus.FAILED);
      }
    }
    return new ExecutionResult<>(MessageResponseFactory
      .createNoContentResponse("Created", EventBuilderFactory.getDeleteCollectionEventBuilder(collection.getString(AJEntityCollection.ID))),
      ExecutionResult.ExecutionStatus.SUCCESSFUL);
  }

  @Override
  public boolean handlerReadOnly() {
    return false;
  }
}
