package org.gooru.nucleus.handlers.collections.processors.repositories.activejdbc.dbhandlers;

import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;

import org.gooru.nucleus.handlers.collections.processors.ProcessorContext;
import org.gooru.nucleus.handlers.collections.processors.repositories.activejdbc.dbauth.AuthorizerBuilder;
import org.gooru.nucleus.handlers.collections.processors.repositories.activejdbc.dbhelpers.DbHelperUtil;
import org.gooru.nucleus.handlers.collections.processors.repositories.activejdbc.entities.AJEntityCollection;
import org.gooru.nucleus.handlers.collections.processors.repositories.activejdbc.entities.AJEntityContent;
import org.gooru.nucleus.handlers.collections.processors.repositories.activejdbc.entities.AJEntityRubric;
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
 * Created by ashish on 12/1/16.
 */
class FetchCollectionHandler implements DBHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(FetchCollectionHandler.class);
  private static final ResourceBundle resourceBundle = ResourceBundle.getBundle("messages");
  private final ProcessorContext context;
  private AJEntityCollection collection;

  public FetchCollectionHandler(ProcessorContext context) {
    this.context = context;
  }

  @Override
  public ExecutionResult<MessageResponse> checkSanity() {
    // There should be a collection id present
    if (context.collectionId() == null || context.collectionId().isEmpty()) {
      LOGGER.warn("Missing collection");
      return new ExecutionResult<>(MessageResponseFactory
          .createNotFoundResponse(
              resourceBundle.getString("collection.id") + context.collectionId()),
          ExecutionResult.ExecutionStatus.FAILED);
    }

    if (context.userId() == null || context.userId().isEmpty()) {
      LOGGER.warn("Invalid user");
      return new ExecutionResult<>(
          MessageResponseFactory.createForbiddenResponse(resourceBundle.getString("not.allowed")),
          ExecutionResult.ExecutionStatus.FAILED);
    }
    return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.CONTINUE_PROCESSING);
  }

  @Override
  public ExecutionResult<MessageResponse> validateRequest() {
    LazyList<AJEntityCollection> collections =
        AJEntityCollection.findBySQL(AJEntityCollection.FETCH_QUERY, context.collectionId());
    if (collections.size() == 0) {
      LOGGER.warn("Not able to find collection '{}'", this.context.collectionId());
      return new ExecutionResult<>(
          MessageResponseFactory.createNotFoundResponse(resourceBundle.getString("not.found")),
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
            .buildSimpleJsonFormatter(false, AJEntityCollection.FETCH_QUERY_FIELD_LIST)
            .toJson(this.collection));
    // Now query contents and populate them
    LazyList<AJEntityContent> contents =
        AJEntityContent
            .findBySQL(AJEntityContent.FETCH_CONTENT_SUMMARY_QUERY, context.collectionId());
    if (contents.size() > 0) {
      Set<String> oeQuestionIds = new HashSet<>(); 
      contents.stream().filter(
         map -> map.get(AJEntityContent.CONTENT_SUBFORMAT) != null && AJEntityContent.RUBRIC_ASSOCIATION_ALLOWED_TYPES
            .contains(map.getString(AJEntityContent.CONTENT_SUBFORMAT))).forEach(map -> {
                oeQuestionIds.add(map.get(AJEntityContent.ID).toString());
            });
            
      JsonArray contentsArray = new JsonArray(
          JsonFormatterBuilder
          .buildSimpleJsonFormatter(false, AJEntityContent.FETCH_CONTENT_SUMMARY_FIELDS)
          .toJson(contents));
      if (!oeQuestionIds.isEmpty()) {
        LazyList<AJEntityRubric> rubrics =
          AJEntityRubric.findBySQL(AJEntityRubric.FETCH_RUBRIC_SUMMARY, DbHelperUtil.toPostgresArrayString(oeQuestionIds));
        if (rubrics != null && !rubrics.isEmpty()) {
          rubrics.forEach(rubric -> {
              contentsArray.stream().filter(
                   map -> ((JsonObject)map).getString(AJEntityContent.ID) != null && rubric.get(AJEntityRubric.CONTENT_ID).toString()
                  .contains(((JsonObject)map).getString(AJEntityContent.ID))).forEach(question -> {
                      if (!rubric.getBoolean(AJEntityRubric.IS_RUBRIC)) {
                        ((JsonObject) question).put(AJEntityContent.MAX_SCORE, rubric.get(AJEntityRubric.MAX_SCORE));
                      }
                  });
          });
        }
      }
      response.put(AJEntityContent.CONTENT, contentsArray);
    } else {
      response.put(AJEntityContent.CONTENT, new JsonArray());
    }
    // Now collaborator, we need to know if we want to get it from course or
    // whatever is in the collection would suffice
    String courseId = this.collection.getString(AJEntityCollection.COURSE_ID);
    if (courseId == null || courseId.isEmpty()) {
      String collaborators = this.collection.getString(AJEntityCollection.COLLABORATOR);
      if (collaborators == null || collaborators.isEmpty()) {
        response.put(AJEntityCollection.COLLABORATOR, new JsonArray());
      } else {
        response.put(AJEntityCollection.COLLABORATOR, new JsonArray(collaborators));
      }
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
        return new ExecutionResult<>(MessageResponseFactory
            .createInternalErrorResponse(resourceBundle.getString("internal.store.error")),
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
