package org.gooru.nucleus.handlers.collections.processors.repositories.activejdbc.dbauth;

import io.vertx.core.json.JsonArray;
import org.gooru.nucleus.handlers.collections.processors.ProcessorContext;
import org.gooru.nucleus.handlers.collections.processors.repositories.activejdbc.entities.AJEntityCollection;
import org.gooru.nucleus.handlers.collections.processors.repositories.activejdbc.entities.AJEntityQuestion;
import org.gooru.nucleus.handlers.collections.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.collections.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.collections.processors.responses.MessageResponseFactory;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.DBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ashish on 1/2/16.
 */
public class AddContentToCollectionAuthorizer implements Authorizer<AJEntityCollection> {
  private final ProcessorContext context;
  private final Logger LOGGER = LoggerFactory.getLogger(Authorizer.class);

  public AddContentToCollectionAuthorizer(ProcessorContext context) {
    this.context = context;
  }

  @Override
  public ExecutionResult<MessageResponse> authorize(AJEntityCollection collection) {
    String owner_id = collection.getString(AJEntityCollection.OWNER_ID);
    String course_id = collection.getString(AJEntityCollection.COURSE_ID);
    long authRecordCount;
    // If this collection is not part of course, then user should be either owner or collaborator on course
    if (course_id != null) {
      try {
        authRecordCount = Base.count(AJEntityCollection.TABLE_COURSE, AJEntityCollection.AUTH_FILTER, course_id, context.userId(), context.userId());
        if (authRecordCount >= 1) {
          return authorizeForQuestion(collection);
        }
      } catch (DBException e) {
        LOGGER.error("Error checking authorization for update for Collection '{}' for course '{}'", context.collectionId(), course_id, e);
        return new ExecutionResult<>(
          MessageResponseFactory.createInternalErrorResponse("Not able to authorize user for adding content to this collection"),
          ExecutionResult.ExecutionStatus.FAILED);
      }
    } else {
      // Collection is not part of course, hence we need user to be either owner or collaborator on collection
      if (context.userId().equalsIgnoreCase(owner_id)) {
        // Owner is fine
        return authorizeForQuestion(collection);
      } else {
        String collaborators = collection.getString(AJEntityCollection.COLLABORATOR);
        if (collaborators != null && !collaborators.isEmpty()) {
          JsonArray collaboratorsArray = new JsonArray(collaborators);
          if (collaboratorsArray.contains(context.userId())) {
            return authorizeForQuestion(collection);
          }
        }
      }
    }
    LOGGER.warn("User: '{}' is not owner/collaborator of collection: '{}' or owner/collaborator on course", context.userId(), context.collectionId());
    return new ExecutionResult<>(MessageResponseFactory.createForbiddenResponse("Not allowed"), ExecutionResult.ExecutionStatus.FAILED);
  }

  private ExecutionResult<MessageResponse> authorizeForQuestion(AJEntityCollection collection) {
    //           return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.CONTINUE_PROCESSING);
    try {
      long count = Base.count(AJEntityQuestion.TABLE_QUESTION, AJEntityQuestion.QUESTION_FOR_ADD_FILTER, context.questionId(), context.userId());
      if (count == 1) {
        return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.CONTINUE_PROCESSING);
      }
    } catch (DBException e) {
      LOGGER.error("Error querying content '{}' availability for associating in collection '{}'", context.questionId(), context.collectionId(), e);
    }
    return new ExecutionResult<>(MessageResponseFactory.createInvalidRequestResponse("Question not available for association"),
      ExecutionResult.ExecutionStatus.FAILED);
  }

}
