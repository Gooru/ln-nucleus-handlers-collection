package org.gooru.nucleus.handlers.collections.processors.repositories.activejdbc.dbauth;

import java.util.ResourceBundle;
import org.gooru.nucleus.handlers.collections.processors.ProcessorContext;
import org.gooru.nucleus.handlers.collections.processors.repositories.activejdbc.entities.AJEntityCollection;
import org.gooru.nucleus.handlers.collections.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.collections.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.collections.processors.responses.MessageResponseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ashish on 31/1/16.
 */
public class UpdateCollaboratorAuthorizer implements Authorizer<AJEntityCollection> {

  private static final ResourceBundle resourceBundle = ResourceBundle.getBundle("messages");
  private final ProcessorContext context;
  private final Logger LOGGER = LoggerFactory.getLogger(Authorizer.class);

  UpdateCollaboratorAuthorizer(ProcessorContext context) {
    this.context = context;
  }

  @Override
  public ExecutionResult<MessageResponse> authorize(AJEntityCollection collection) {
    String ownerId = collection.getString(AJEntityCollection.OWNER_ID);
    if (context.userId().equalsIgnoreCase(ownerId)) {
      return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.CONTINUE_PROCESSING);
    }
    LOGGER.warn("User: '{}' is not owner of collection: '{}' ", context.userId(),
        context.collectionId());
    return new ExecutionResult<>(
        MessageResponseFactory.createForbiddenResponse(resourceBundle.getString("not.allowed")),
        ExecutionResult.ExecutionStatus.FAILED);
  }
}
