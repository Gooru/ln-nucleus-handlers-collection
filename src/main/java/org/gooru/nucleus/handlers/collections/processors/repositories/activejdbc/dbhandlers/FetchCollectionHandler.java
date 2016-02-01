package org.gooru.nucleus.handlers.collections.processors.repositories.activejdbc.dbhandlers;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.gooru.nucleus.handlers.collections.processors.ProcessorContext;
import org.gooru.nucleus.handlers.collections.processors.repositories.activejdbc.entities.AJEntityCollection;
import org.gooru.nucleus.handlers.collections.processors.repositories.activejdbc.entities.AJEntityQuestion;
import org.gooru.nucleus.handlers.collections.processors.repositories.activejdbc.formatter.JsonFormatterBuilder;
import org.gooru.nucleus.handlers.collections.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.collections.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.collections.processors.responses.MessageResponseFactory;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.DBException;
import org.javalite.activejdbc.LazyList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ashish on 12/1/16.
 */
class FetchCollectionHandler implements DBHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(FetchCollectionHandler.class);
  private final ProcessorContext context;
  private AJEntityCollection collection;

  public FetchCollectionHandler(ProcessorContext context) {
    this.context = context;
  }

  @Override
  public ExecutionResult<MessageResponse> checkSanity() {
    // There should be an collection id present
    if (context.collectionId() == null || context.collectionId().isEmpty()) {
      LOGGER.warn("Missing collection");
      return new ExecutionResult<>(MessageResponseFactory.createInvalidRequestResponse("Missing collection"), ExecutionResult.ExecutionStatus.FAILED);
    }

    if (context.userId() == null || context.userId().isEmpty()) {
      LOGGER.warn("Invalid user");
      return new ExecutionResult<>(MessageResponseFactory.createForbiddenResponse("Not allowed"), ExecutionResult.ExecutionStatus.FAILED);
    }
    return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.CONTINUE_PROCESSING);
  }

  @Override
  public ExecutionResult<MessageResponse> validateRequest() {
    LazyList<AJEntityCollection> collections = AJEntityCollection.findBySQL(AJEntityCollection.FETCH_QUERY, context.collectionId());
    if (collections.size() == 0) {
      LOGGER.warn("Not able to find collection '{}'", this.context.collectionId());
      return new ExecutionResult<>(MessageResponseFactory.createNotFoundResponse("Not found"), ExecutionResult.ExecutionStatus.FAILED);
    }
    this.collection = collections.get(0);
    return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.CONTINUE_PROCESSING);
  }

  @Override
  public ExecutionResult<MessageResponse> executeRequest() {
    // First create response from Collection
    JsonObject response =
      new JsonObject(new JsonFormatterBuilder().buildSimpleJsonFormatter(false, AJEntityCollection.FETCH_QUERY_FIELD_LIST).toJson(this.collection));
    // Now query questions and populate them
    LazyList<AJEntityQuestion> questions = AJEntityQuestion.findBySQL(AJEntityQuestion.FETCH_QUESTION_SUMMARY_QUERY, context.collectionId());
    if (questions.size() > 0) {
      response.put(AJEntityQuestion.QUESTION,
        new JsonArray(new JsonFormatterBuilder().buildSimpleJsonFormatter(false, AJEntityQuestion.FETCH_QUESTION_SUMMARY_FIELDS).toJson(questions)));
    } else {
      response.put(AJEntityQuestion.QUESTION, new JsonArray());
    }
    // Now collaborator, we need to know if we want to get it from course or whatever is in the collection would suffice
    String course_id = this.collection.getString(AJEntityCollection.COURSE_ID);
    if (course_id == null || course_id.isEmpty()) {
      String collaborators = this.collection.getString(AJEntityCollection.COLLABORATOR);
      if (collaborators == null || collaborators.isEmpty()) {
        response.put(AJEntityCollection.COLLABORATOR, new JsonArray());
      } else {
        response.put(AJEntityCollection.COLLABORATOR, new JsonArray(collaborators));
      }
    } else {
      try {
        // Need to fetch collaborators
        Object courseCollabObject = Base.firstCell(AJEntityCollection.COURSE_COLLABORATOR_QUERY, course_id);
        if (courseCollabObject != null) {
          response.put(AJEntityCollection.COLLABORATOR, new JsonArray(courseCollabObject.toString()));
        } else {
          response.put(AJEntityCollection.COLLABORATOR, new JsonArray());
        }
      } catch (DBException e) {
        LOGGER.error("Error trying to get course collaborator for course '{}' to fetch collection '{}'", course_id, this.context.collectionId(), e);
        return new ExecutionResult<>(MessageResponseFactory.createInternalErrorResponse(e.getMessage()), ExecutionResult.ExecutionStatus.FAILED);
      }
    }
    return new ExecutionResult<>(MessageResponseFactory.createOkayResponse(response), ExecutionResult.ExecutionStatus.SUCCESSFUL);
  }

  @Override
  public boolean handlerReadOnly() {
    return true;
  }

}
