package org.gooru.nucleus.handlers.collections.processors.repositories.activejdbc.dbauth;

import org.gooru.nucleus.handlers.collections.processors.ProcessorContext;
import org.gooru.nucleus.handlers.collections.processors.repositories.activejdbc.entities.AJEntityCollection;
import org.gooru.nucleus.handlers.collections.processors.responses.ExecutionResult;

/**
 * Created by ashish on 29/1/16.
 */
public class AuthorizerBuilder {

  public Authorizer<AJEntityCollection> buildUpdateAuthorizer(ProcessorContext context) {
    return new UpdateAuthorizer(context);
  }

  public Authorizer<AJEntityCollection> buildDeleteAuthorizer(ProcessorContext context) {
    return new DeleteAuthorizer(context);
  }

  public Authorizer<AJEntityCollection> buildUpdateCollaboratorAuthorizer(ProcessorContext context) {
    return new UpdateCollaboratorAuthorizer(context);
  }

  // Creation is only allowed outside of any context and hence it has got no bearing on course container, which does not exist as our API call for
  // association may be called after create call to set that up
  // As long as session token is valid and user is not anonymous, which is the case as we are, we should be fine
  public Authorizer<AJEntityCollection> buildCreateAuthorizer(ProcessorContext context) {
    return model -> new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.CONTINUE_PROCESSING);
  }

  public Authorizer<AJEntityCollection> buildAddContentToCollectionAuthorizer(ProcessorContext context) {
    return new AddContentToCollectionAuthorizer(context);
  }
}
