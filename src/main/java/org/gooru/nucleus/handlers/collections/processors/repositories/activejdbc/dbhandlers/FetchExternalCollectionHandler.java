package org.gooru.nucleus.handlers.collections.processors.repositories.activejdbc.dbhandlers;

import java.util.ResourceBundle;

import org.gooru.nucleus.handlers.collections.processors.ProcessorContext;
import org.gooru.nucleus.handlers.collections.processors.repositories.activejdbc.dbauth.AuthorizerBuilder;
import org.gooru.nucleus.handlers.collections.processors.repositories.activejdbc.entities.AJEntityCollection;
import org.gooru.nucleus.handlers.collections.processors.repositories.activejdbc.formatter.JsonFormatterBuilder;
import org.gooru.nucleus.handlers.collections.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.collections.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.collections.processors.responses.MessageResponseFactory;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.DBException;
import org.javalite.activejdbc.LazyList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * Created by renuka on 24/12/18.
 */
class FetchExternalCollectionHandler implements DBHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(FetchExternalCollectionHandler.class);
  private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("messages");
  private final ProcessorContext context;
  private AJEntityCollection collection;

  public FetchExternalCollectionHandler(ProcessorContext context) {
    this.context = context;
  }

  @Override
  public ExecutionResult<MessageResponse> checkSanity() {
    // There should be a collection id present
    if (context.collectionId() == null || context.collectionId().isEmpty()) {
      LOGGER.warn("Missing collection");
      return new ExecutionResult<>(
          MessageResponseFactory
              .createInvalidRequestResponse(RESOURCE_BUNDLE.getString("missing.collection.id")),
          ExecutionResult.ExecutionStatus.FAILED);
    }

    if (context.userId() == null || context.userId().isEmpty()) {
      LOGGER.warn("Invalid user");
      return new ExecutionResult<>(
          MessageResponseFactory.createForbiddenResponse(RESOURCE_BUNDLE.getString("not.allowed")),
          ExecutionResult.ExecutionStatus.FAILED);
    }
    return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.CONTINUE_PROCESSING);
  }

  @Override
  public ExecutionResult<MessageResponse> validateRequest() {
    LazyList<AJEntityCollection> collections =
        AJEntityCollection
            .findBySQL(AJEntityCollection.FETCH_EXTERNAL_COLLECTION_QUERY, context.collectionId());
    if (collections.isEmpty()) {
      LOGGER.warn("Not able to find collection '{}'", this.context.collectionId());
      return new ExecutionResult<>(
          MessageResponseFactory.createNotFoundResponse(RESOURCE_BUNDLE.getString("not.found")),
          ExecutionResult.ExecutionStatus.FAILED);
    }
    this.collection = collections.get(0);
    return AuthorizerBuilder.buildTenantAuthorizer(this.context).authorize(collection);
  }

  @Override
  public ExecutionResult<MessageResponse> executeRequest() {
    // First create response from Collection
    JsonObject response = new JsonObject(
        JsonFormatterBuilder
            .buildSimpleJsonFormatter(false, AJEntityCollection.FETCH_EC_QUERY_FIELD_LIST)
            .toJson(this.collection));
    // Now collaborator, we need to know if we want to get it from course
    // else no collaboration on external collection
    String courseId = this.collection.getString(AJEntityCollection.COURSE_ID);
    if (courseId == null || courseId.isEmpty()) {
      response.put(AJEntityCollection.COLLABORATOR, new JsonArray());
    } else {
      try {
        // Need to fetch collaborators
        Object courseCollaboratorObject =
            Base.firstCell(AJEntityCollection.COURSE_COLLABORATOR_QUERY, courseId);
        if (courseCollaboratorObject != null) {
          response.put(AJEntityCollection.COLLABORATOR,
              new JsonArray(courseCollaboratorObject.toString()));
        } else {
          response.put(AJEntityCollection.COLLABORATOR, new JsonArray());
        }
      } catch (DBException e) {
        LOGGER
            .error(
                "Error trying to get course collaborator for course '{}' to fetch collection '{}'",
                courseId,
                this.context.collectionId(), e);
        return new ExecutionResult<>(
            MessageResponseFactory
                .createInternalErrorResponse(RESOURCE_BUNDLE.getString("error.from.store")),
            ExecutionResult.ExecutionStatus.FAILED);
      }
    }
    return new ExecutionResult<>(MessageResponseFactory.createOkayResponse(response),
        ExecutionResult.ExecutionStatus.SUCCESSFUL);
  }

  @Override
  public boolean handlerReadOnly() {
    return true;
  }
}
