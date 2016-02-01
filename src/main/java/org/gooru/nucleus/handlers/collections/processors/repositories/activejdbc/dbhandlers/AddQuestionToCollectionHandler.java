package org.gooru.nucleus.handlers.collections.processors.repositories.activejdbc.dbhandlers;

import io.vertx.core.json.JsonObject;
import org.gooru.nucleus.handlers.collections.constants.MessageConstants;
import org.gooru.nucleus.handlers.collections.processors.ProcessorContext;
import org.gooru.nucleus.handlers.collections.processors.events.EventBuilderFactory;
import org.gooru.nucleus.handlers.collections.processors.repositories.activejdbc.dbauth.AuthorizerBuilder;
import org.gooru.nucleus.handlers.collections.processors.repositories.activejdbc.entities.AJEntityCollection;
import org.gooru.nucleus.handlers.collections.processors.repositories.activejdbc.entities.AJEntityQuestion;
import org.gooru.nucleus.handlers.collections.processors.repositories.activejdbc.validators.PayloadValidator;
import org.gooru.nucleus.handlers.collections.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.collections.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.collections.processors.responses.MessageResponseFactory;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.DBException;
import org.javalite.activejdbc.LazyList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Created by ashish on 12/1/16.
 */
class AddQuestionToCollectionHandler implements DBHandler {
  private static final Logger LOGGER = LoggerFactory.getLogger(UpdateCollectionHandler.class);
  private final ProcessorContext context;
  private AJEntityCollection collection;

  public AddQuestionToCollectionHandler(ProcessorContext context) {
    this.context = context;
  }

  @Override
  public ExecutionResult<MessageResponse> checkSanity() {
    // There should be an collection id present
    if (context.collectionId() == null || context.collectionId().isEmpty() || context.questionId() == null || context.questionId().isEmpty()) {
      LOGGER.warn("Missing collection/question id");
      return new ExecutionResult<>(MessageResponseFactory.createInvalidRequestResponse("Missing collection/question id"),
        ExecutionResult.ExecutionStatus.FAILED);
    }
    // The user should not be anonymous
    if (context.userId() == null || context.userId().isEmpty() || context.userId().equalsIgnoreCase(MessageConstants.MSG_USER_ANONYMOUS)) {
      LOGGER.warn("Anonymous user attempting to edit collection");
      return new ExecutionResult<>(MessageResponseFactory.createForbiddenResponse("Not allowed"), ExecutionResult.ExecutionStatus.FAILED);
    }
    // Payload should not be empty
    if (context.request() == null || context.request().isEmpty()) {
      LOGGER.warn("Empty payload supplied to edit collection");
      return new ExecutionResult<>(MessageResponseFactory.createInvalidRequestResponse("Empty payload"), ExecutionResult.ExecutionStatus.FAILED);
    }
    // Our validators should certify this
    JsonObject errors = new PayloadValidator() {
    }.validatePayload(context.request(), AJEntityCollection.addQuestionFieldSelector(), AJEntityCollection.getValidatorRegistry());
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
    this.collection = collections.get(0);
    return new AuthorizerBuilder().buildAddContentToCollectionAuthorizer(this.context).authorize(this.collection);
  }

  @Override
  public ExecutionResult<MessageResponse> executeRequest() {
    try {
      Object sequence = Base.firstCell(AJEntityQuestion.MAX_QUESTION_SEQUENCE_QUERY, this.context.collectionId());
      int sequenceId = 1;
      if (sequence != null) {
        int currentSequence = Integer.valueOf(sequence.toString());
        sequenceId = currentSequence + 1;
      }
      long count = Base
        .exec(AJEntityQuestion.ADD_QUESTION_QUERY, this.context.collectionId(), this.context.userId(), sequenceId, this.context.questionId(),
          this.context.userId());

      if (count == 1) {
        return updateGrading();
      }
      LOGGER.error("Something is wrong. Adding question '{}' to collection '{}' updated '{}' rows", this.context.questionId(),
        this.context.collectionId(), count);

    } catch (DBException e) {
      LOGGER.error("Not able to add question '{}' to collection '{}'", this.context.questionId(), this.context.collectionId(), e);
    }
    return new ExecutionResult<>(MessageResponseFactory.createInternalErrorResponse("Unable to add question"),
      ExecutionResult.ExecutionStatus.FAILED);
  }

  @Override
  public boolean handlerReadOnly() {
    return false;
  }

  private ExecutionResult<MessageResponse> updateGrading() {
    String currentGrading = this.collection.getString(AJEntityCollection.GRADING);
    if (!currentGrading.equalsIgnoreCase(AJEntityCollection.GRADING_TYPE_TEACHER)) {
      try {
        long count = Base.count(AJEntityQuestion.TABLE_QUESTION, AJEntityQuestion.OPEN_ENDED_QUESTION_FILTER, this.context.collectionId());
        if (count > 0) {
          this.collection.setGrading(AJEntityCollection.GRADING_TYPE_TEACHER);
          if (!this.collection.save()) {
            LOGGER.error("Collection '{}' grading type change failed", this.context.collectionId());
            if (this.collection.hasErrors()) {
              Map<String, String> map = collection.errors();
              JsonObject errors = new JsonObject();
              map.forEach(errors::put);
              return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(errors), ExecutionResult.ExecutionStatus.FAILED);
            }
          }
        }
      } catch (DBException e) {
        LOGGER.error("Collection '{}' grading type change lookup failed", this.context.collectionId(), e);
        return new ExecutionResult<>(MessageResponseFactory.createInternalErrorResponse("Collection grade change failed"),
          ExecutionResult.ExecutionStatus.FAILED);
      }
    }

    return new ExecutionResult<>(MessageResponseFactory
      .createNoContentResponse("Question added", EventBuilderFactory.getAddQuestionToCollectionEventBuilder(context.collectionId())),
      ExecutionResult.ExecutionStatus.SUCCESSFUL);

  }
}
