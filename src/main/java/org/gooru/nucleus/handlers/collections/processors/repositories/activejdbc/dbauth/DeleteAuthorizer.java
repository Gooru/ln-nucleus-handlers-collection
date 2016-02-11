package org.gooru.nucleus.handlers.collections.processors.repositories.activejdbc.dbauth;

import org.gooru.nucleus.handlers.collections.processors.ProcessorContext;
import org.gooru.nucleus.handlers.collections.processors.repositories.activejdbc.entities.AJEntityCollection;
import org.gooru.nucleus.handlers.collections.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.collections.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.collections.processors.responses.MessageResponseFactory;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.DBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ResourceBundle;

/**
 * Created by ashish on 29/1/16.
 */
class DeleteAuthorizer implements Authorizer<AJEntityCollection> {
  private final ProcessorContext context;
  private final Logger LOGGER = LoggerFactory.getLogger(Authorizer.class);
  private final ResourceBundle resourceBundle = ResourceBundle.getBundle("messages");

  DeleteAuthorizer(ProcessorContext context) {
    this.context = context;
  }

  @Override
  public ExecutionResult<MessageResponse> authorize(AJEntityCollection collection) {
    String ownerId = collection.getString(AJEntityCollection.OWNER_ID);
    String courseId = collection.getString(AJEntityCollection.COURSE_ID);
    long authRecordCount;
    // If this collection is part of course, then user should be either owner or collaborator on course
    if (courseId != null) {
      try {
        authRecordCount = Base.count(AJEntityCollection.TABLE_COURSE, AJEntityCollection.AUTH_FILTER, courseId, context.userId(), context.userId());
        if (authRecordCount >= 1) {
          return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.CONTINUE_PROCESSING);
        }
      } catch (DBException e) {
        LOGGER.error("Error checking authorization for delete for Collection '{}' for course '{}'", context.collectionId(), courseId, e);
        return new ExecutionResult<>(
          MessageResponseFactory.createInternalErrorResponse(resourceBundle.getString("internal.error.authorization.checking")),
          ExecutionResult.ExecutionStatus.FAILED);
      }
    } else {
      // Collection is not part of course, hence we need user to be owner
      if (context.userId().equalsIgnoreCase(ownerId)) {
        // Owner is fine
        return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.CONTINUE_PROCESSING);
      }
    }
    LOGGER.warn("User: '{}' is not owner of collection: '{}' or owner/collaborator on course", context.userId(), context.collectionId());
    return new ExecutionResult<>(MessageResponseFactory.createForbiddenResponse(resourceBundle.getString("not.allowed")),
      ExecutionResult.ExecutionStatus.FAILED);
  }
}
