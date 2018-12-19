package org.gooru.nucleus.handlers.collections.processors.repositories.activejdbc.dbhandlers;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.util.Map;
import java.util.ResourceBundle;
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
import org.javalite.activejdbc.LazyList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ashish on 12/1/16.
 */
class UpdateCollaboratorHandler implements DBHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(UpdateCollaboratorHandler.class);
  private static final ResourceBundle resourceBundle = ResourceBundle.getBundle("messages");
  private static final String COLLABORATORS_REMOVED = "collaborators.removed";
  private static final String COLLABORATORS_ADDED = "collaborators.added";
  private final ProcessorContext context;
  private AJEntityCollection collection;
  private JsonObject diffCollaborators;

  public UpdateCollaboratorHandler(ProcessorContext context) {
    this.context = context;
  }

  @Override
  public ExecutionResult<MessageResponse> checkSanity() {
    // Collection id is present
    if (context.collectionId() == null || context.collectionId().isEmpty()) {
      LOGGER.warn("Missing collection id");
      return new ExecutionResult<>(
          MessageResponseFactory
              .createInvalidRequestResponse(resourceBundle.getString("collection.id.missing")),
          ExecutionResult.ExecutionStatus.FAILED);
    }
    // The user should not be anonymous
    if (context.userId() == null || context.userId().isEmpty() || context.userId()
        .equalsIgnoreCase(MessageConstants.MSG_USER_ANONYMOUS)) {
      LOGGER.warn("Anonymous user attempting to edit collection");
      return new ExecutionResult<>(
          MessageResponseFactory.createForbiddenResponse(resourceBundle.getString("not.allowed")),
          ExecutionResult.ExecutionStatus.FAILED);
    }
    // Payload should not be empty
    if (context.request() == null || context.request().isEmpty()) {
      LOGGER.warn("Empty payload supplied to upload collection");
      return new ExecutionResult<>(
          MessageResponseFactory
              .createInvalidRequestResponse(resourceBundle.getString("payload.empty")),
          ExecutionResult.ExecutionStatus.FAILED);
    }
    // Our validators should certify this
    JsonObject errors = new DefaultPayloadValidator()
        .validatePayload(context.request(), AJEntityCollection.editCollaboratorFieldSelector(),
            AJEntityCollection.getValidatorRegistry());
    if (errors != null && !errors.isEmpty()) {
      LOGGER.warn("Validation errors for request");
      return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(errors),
          ExecutionResult.ExecutionStatus.FAILED);
    }
    return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.CONTINUE_PROCESSING);

  }

  @Override
  public ExecutionResult<MessageResponse> validateRequest() {
    // Fetch the collection where type is collection and it is not deleted
    // already and id is specified id
    LazyList<AJEntityCollection> collections = AJEntityCollection
        .findBySQL(AJEntityCollection.AUTHORIZER_QUERY, AJEntityCollection.COLLECTION,
            context.collectionId(),
            false);
    // Collection should be present in DB
    if (collections.size() < 1) {
      LOGGER.warn("Collection id: {} not present in DB", context.collectionId());
      return new ExecutionResult<>(MessageResponseFactory
          .createNotFoundResponse(
              resourceBundle.getString("collection.id") + context.collectionId()),
          ExecutionResult.ExecutionStatus.FAILED);
    }
    this.collection = collections.get(0);
    final String course = collection.getString(AJEntityCollection.COURSE_ID);
    if (course != null) {
      LOGGER.error("Cannot update collaborator for collection '{}' as it is part of course '{}'",
          context.collectionId(), course);
      return new ExecutionResult<>(MessageResponseFactory
          .createInvalidRequestResponse(resourceBundle.getString("collection.belongs.to.course")),
          ExecutionResult.ExecutionStatus.FAILED);
    }
    diffCollaborators = calculateDiffOfCollaborators();
    return doAuthorization();
  }

  @Override
  public ExecutionResult<MessageResponse> executeRequest() {
    AJEntityCollection collection = new AJEntityCollection();
    collection.setIdWithConverter(context.collectionId());
    collection.setModifierId(context.userId());
    // Now auto populate is done, we need to setup the converter machinery
    new DefaultAJEntityCollectionEntityBuilder()
        .build(collection, context.request(), AJEntityCollection.getConverterRegistry());

    boolean result = collection.save();
    if (!result) {
      LOGGER.error("Collection with id '{}' failed to save", context.collectionId());
      if (collection.hasErrors()) {
        Map<String, String> map = collection.errors();
        JsonObject errors = new JsonObject();
        map.forEach(errors::put);
        return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(errors),
            ExecutionResult.ExecutionStatus.FAILED);
      }
    }
    return new ExecutionResult<>(
        MessageResponseFactory.createNoContentResponse(resourceBundle.getString("updated"),
            EventBuilderFactory
                .getUpdateCollaboratorEventBuilder(context.collectionId(), diffCollaborators)),
        ExecutionResult.ExecutionStatus.SUCCESSFUL);
  }

  @Override
  public boolean handlerReadOnly() {
    return false;
  }

  private ExecutionResult<MessageResponse> doAuthorization() {
    ExecutionResult<MessageResponse> result =
        AuthorizerBuilder.buildUpdateCollaboratorAuthorizer(this.context)
            .authorize(this.collection);
    if (result.hasFailed()) {
      return result;
    }
    return AuthorizerBuilder
        .buildTenantCollaboratorAuthorizer(this.context,
            diffCollaborators.getJsonArray(COLLABORATORS_ADDED))
        .authorize(this.collection);
  }

  private JsonObject calculateDiffOfCollaborators() {
    JsonObject result = new JsonObject();
    // Find current collaborators
    String currentCollaboratorsAsString = this.collection
        .getString(AJEntityCollection.COLLABORATOR);
    JsonArray currentCollaborators;
    currentCollaborators =
        currentCollaboratorsAsString != null && !currentCollaboratorsAsString.isEmpty() ?
            new JsonArray(currentCollaboratorsAsString) : new JsonArray();
    JsonArray newCollaborators = this.context.request()
        .getJsonArray(AJEntityCollection.COLLABORATOR);
    if (currentCollaborators.isEmpty() && !newCollaborators.isEmpty()) {
      // Adding all
      result.put(COLLABORATORS_ADDED, newCollaborators.copy());
      result.put(COLLABORATORS_REMOVED, new JsonArray());
    } else if (!currentCollaborators.isEmpty() && newCollaborators.isEmpty()) {
      // Removing all
      result.put(COLLABORATORS_ADDED, new JsonArray());
      result.put(COLLABORATORS_REMOVED, currentCollaborators.copy());
    } else if (!currentCollaborators.isEmpty() && !newCollaborators.isEmpty()) {
      // Do the diffing
      JsonArray toBeAdded = new JsonArray();
      JsonArray toBeDeleted = currentCollaborators.copy();
      for (Object o : newCollaborators) {
        if (toBeDeleted.contains(o)) {
          toBeDeleted.remove(o);
        } else {
          toBeAdded.add(o);
        }
      }
      result.put(COLLABORATORS_ADDED, toBeAdded);
      result.put(COLLABORATORS_REMOVED, toBeDeleted);
    } else {
      // WHAT ????
      LOGGER
          .warn(
              "Updating collaborator with empty payload when current collaborator is empty for collection '{}'",
              this.context.collectionId());
      result.put(COLLABORATORS_ADDED, new JsonArray());
      result.put(COLLABORATORS_REMOVED, new JsonArray());
    }
    return result;
  }

  private static class DefaultPayloadValidator implements PayloadValidator {

  }

  private static class DefaultAJEntityCollectionEntityBuilder implements
      EntityBuilder<AJEntityCollection> {

  }
}
