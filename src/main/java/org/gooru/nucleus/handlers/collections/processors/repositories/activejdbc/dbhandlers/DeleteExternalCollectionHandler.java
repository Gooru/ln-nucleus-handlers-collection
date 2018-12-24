package org.gooru.nucleus.handlers.collections.processors.repositories.activejdbc.dbhandlers;

import java.util.Map;
import java.util.ResourceBundle;

import org.gooru.nucleus.handlers.collections.constants.MessageConstants;
import org.gooru.nucleus.handlers.collections.processors.ProcessorContext;
import org.gooru.nucleus.handlers.collections.processors.events.EventBuilderFactory;
import org.gooru.nucleus.handlers.collections.processors.repositories.activejdbc.dbauth.AuthorizerBuilder;
import org.gooru.nucleus.handlers.collections.processors.repositories.activejdbc.entities.AJEntityCollection;
import org.gooru.nucleus.handlers.collections.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.collections.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.collections.processors.responses.MessageResponseFactory;
import org.javalite.activejdbc.LazyList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.json.JsonObject;

/**
 * Created by renuka on 24/12/18.
 */
class DeleteExternalCollectionHandler implements DBHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(DeleteExternalCollectionHandler.class);
  private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("messages");
  private final ProcessorContext context;

  public DeleteExternalCollectionHandler(ProcessorContext context) {
    this.context = context;
  }

  @Override
  public ExecutionResult<MessageResponse> checkSanity() {
    // There should be a collection id present
    if (context.collectionId() == null || context.collectionId().isEmpty()) {
      LOGGER.warn("Missing collection id");
      return new ExecutionResult<>(
          MessageResponseFactory
              .createNotFoundResponse(RESOURCE_BUNDLE.getString("missing.collection.id")),
          ExecutionResult.ExecutionStatus.FAILED);
    }
    // The user should not be anonymous
    if (context.userId() == null || context.userId().isEmpty()
        || context.userId().equalsIgnoreCase(MessageConstants.MSG_USER_ANONYMOUS)) {
      LOGGER.warn("Anonymous user attempting to delete collection");
      return new ExecutionResult<>(
          MessageResponseFactory.createForbiddenResponse(RESOURCE_BUNDLE.getString("not.allowed")),
          ExecutionResult.ExecutionStatus.FAILED);
    }

    return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.CONTINUE_PROCESSING);
  }

  @Override
  public ExecutionResult<MessageResponse> validateRequest() {
    // Fetch the collection where type is external_collection and it is not
    // deleted already and id is specified id

    LazyList<AJEntityCollection> collections = AJEntityCollection
        .findBySQL(AJEntityCollection.AUTHORIZER_QUERY,
            AJEntityCollection.COLLECTION_EXTERNAL, context.collectionId(), false);
    // Collection should be present in DB
    if (collections.size() < 1) {
      LOGGER.warn("Collection id: {} not present in DB", context.collectionId());
      return new ExecutionResult<>(
          MessageResponseFactory
              .createNotFoundResponse(
                  RESOURCE_BUNDLE.getString("collection.id") + context.collectionId()),
          ExecutionResult.ExecutionStatus.FAILED);
    }
    AJEntityCollection collection = collections.get(0);
    // Log a warning is collection to be deleted is published
    if (collection.getDate(AJEntityCollection.PUBLISH_DATE) != null) {
      LOGGER.warn("Assessment with id '{}' is published collection and is being deleted",
          context.collectionId());
    }
    return AuthorizerBuilder.buildDeleteAuthorizer(this.context).authorize(collection);
  }

  @Override
  public ExecutionResult<MessageResponse> executeRequest() {
    // Update collection, we need to set the deleted flag and user who is
    // deleting it but We do not reset the sequence id right now
      AJEntityCollection collectionToDelete = new AJEntityCollection();
    collectionToDelete.setIdWithConverter(context.collectionId());
    collectionToDelete.setBoolean(AJEntityCollection.IS_DELETED, true);
    collectionToDelete.setModifierId(context.userId());

    boolean result = collectionToDelete.save();
    if (!result) {
      LOGGER.error("Collection with id '{}' failed to delete", context.collectionId());
      if (collectionToDelete.hasErrors()) {
        Map<String, String> map = collectionToDelete.errors();
        JsonObject errors = new JsonObject();
        map.forEach(errors::put);
        return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(errors),
            ExecutionResult.ExecutionStatus.FAILED);
      }
    }
    return new ExecutionResult<>(
        MessageResponseFactory.createNoContentResponse(RESOURCE_BUNDLE.getString("deleted"),
            EventBuilderFactory.getDeleteExCollectionEventBuilder(context.collectionId())),
        ExecutionResult.ExecutionStatus.SUCCESSFUL);
  }

  @Override
  public boolean handlerReadOnly() {
    return false;
  }
}
