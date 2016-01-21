package org.gooru.nucleus.handlers.collections.processors.repositories.activejdbc.dbhandlers;

import io.vertx.core.json.JsonObject;
import org.gooru.nucleus.handlers.collections.constants.MessageConstants;
import org.gooru.nucleus.handlers.collections.processors.ProcessorContext;
import org.gooru.nucleus.handlers.collections.processors.events.EventBuilderFactory;
import org.gooru.nucleus.handlers.collections.processors.repositories.activejdbc.entities.AJEntityCULC;
import org.gooru.nucleus.handlers.collections.processors.repositories.activejdbc.entities.AJEntityCollection;
import org.gooru.nucleus.handlers.collections.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.collections.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.collections.processors.responses.MessageResponseFactory;
import org.javalite.activejdbc.LazyList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Created by ashish on 12/1/16.
 */
class DeleteCollectionHandler implements DBHandler {
  private static final Logger LOGGER = LoggerFactory.getLogger(DeleteCollectionHandler.class);
  private final ProcessorContext context;

  public DeleteCollectionHandler(ProcessorContext context) {
    this.context = context;
  }

  @Override
  public ExecutionResult<MessageResponse> checkSanity() {
    // There should be an assessment id present
    if (context.collectionId() == null || context.collectionId().isEmpty()) {
      LOGGER.warn("Missing collection id");
      return new ExecutionResult<>(MessageResponseFactory.createInvalidRequestResponse("Missing collection id"),
        ExecutionResult.ExecutionStatus.FAILED);
    }
    // The user should not be anonymous
    if (context.userId() == null || context.userId().isEmpty() || context.userId().equalsIgnoreCase(MessageConstants.MSG_USER_ANONYMOUS)) {
      LOGGER.warn("Anonymous user attempting to delete collection");
      return new ExecutionResult<>(MessageResponseFactory.createForbiddenResponse("Not allowed"), ExecutionResult.ExecutionStatus.FAILED);
    }

    return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.CONTINUE_PROCESSING);
  }

  @Override
  public ExecutionResult<MessageResponse> validateRequest() {
    // Fetch the assessment where type is assessment and it is not deleted already and id is specified id

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
    AJEntityCollection collection = collections.get(0);
    // The user should be owner of the assessment, collaborator will not do
    // FIXME: 21/1/16 : Need to verify if the user is part of collaborator or owner of course where this collection may be contained
    if (!(collection.getString(AJEntityCollection.CREATOR_ID)).equalsIgnoreCase(context.userId())) {
      LOGGER.warn("User: '{}' is not owner of collection", context.userId());
      return new ExecutionResult<>(MessageResponseFactory.createForbiddenResponse("Not allowed"), ExecutionResult.ExecutionStatus.FAILED);
    }
    // This should not be published
    if (collection.getDate(AJEntityCollection.PUBLISH_DATE) != null) {
      LOGGER.warn("Collection with id '{}' is published collection so should not be deleted", context.collectionId());
      return new ExecutionResult<>(MessageResponseFactory.createForbiddenResponse("Collection is published"), ExecutionResult.ExecutionStatus.FAILED);
    }
    return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.CONTINUE_PROCESSING);
  }

  @Override
  public ExecutionResult<MessageResponse> executeRequest() {
    // Update assessment, we need to set the deleted flag and user who is deleting it but We do not reset the sequence id right now
    AJEntityCollection collectionToDelete = new AJEntityCollection();
    collectionToDelete.setId(context.collectionId());
    collectionToDelete.setBoolean(AJEntityCollection.IS_DELETED, true);
    collectionToDelete.setString(AJEntityCollection.MODIFIER_ID, context.userId());
    boolean result = collectionToDelete.save();
    if (!result) {
      LOGGER.error("Collection with id '{}' failed to delete", context.collectionId());
      if (collectionToDelete.hasErrors()) {
        Map<String, String> map = collectionToDelete.errors();
        JsonObject errors = new JsonObject();
        map.forEach(errors::put);
        return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(errors), ExecutionResult.ExecutionStatus.FAILED);
      }
    }
    // If the collection is present in CULC table, we do similar thing there, except for modifier_id as this field is not needed in CULC entity

    LazyList<AJEntityCULC> culcToDeleteList = AJEntityCULC.findBySQL(
      AJEntityCULC.SELECT_FOR_DELETE,
      context.collectionId(), false);
    int numberOfEntries = culcToDeleteList.size();
    if (numberOfEntries == 1) {
      AJEntityCULC entityCULC = culcToDeleteList.get(0);
      // We have a record and we have to delete it
      entityCULC.setBoolean("is_deleted", true);
      result = collectionToDelete.save();
      if (!result) {
        LOGGER.error("Failed to delete CULC record for collection '{}'", context.collectionId());
        if (entityCULC.hasErrors()) {
          Map<String, String> map = entityCULC.errors();
          JsonObject errors = new JsonObject();
          map.forEach(errors::put);
          return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(errors), ExecutionResult.ExecutionStatus.FAILED);
        }
      }
    } else if (numberOfEntries > 1) {
      // There are multiple records. Not sure which one we want to delete
      LOGGER.error("Multiple CULC record for collection '{}'", context.collectionId());
      return new ExecutionResult<>(MessageResponseFactory.createInvalidRequestResponse("Multiple child records found. Cannot delete"),
        ExecutionResult.ExecutionStatus.FAILED);
    } else {
      // Nothing to do. We do not have a live record.
      LOGGER.debug("No record in CULC for collection '{}' to be deleted", context.collectionId());
    }
    return new ExecutionResult<>(
      MessageResponseFactory.createNoContentResponse("Deleted", EventBuilderFactory.getDeleteCollectionEventBuilder(context.collectionId())),
      ExecutionResult.ExecutionStatus.SUCCESSFUL);
  }

  @Override
  public boolean handlerReadOnly() {
    return false;
  }
}
