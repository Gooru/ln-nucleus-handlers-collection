package org.gooru.nucleus.handlers.collections.processors.repositories.activejdbc.dbauth;

import io.vertx.core.json.JsonArray;
import org.gooru.nucleus.handlers.collections.processors.ProcessorContext;
import org.gooru.nucleus.handlers.collections.processors.repositories.activejdbc.entities.AJEntityCollection;
import org.gooru.nucleus.handlers.collections.processors.responses.ExecutionResult;

/**
 * Created by ashish on 29/1/16.
 */
public final class AuthorizerBuilder {

  private AuthorizerBuilder() {
    throw new AssertionError();
  }

  public static Authorizer<AJEntityCollection> buildUpdateAuthorizer(ProcessorContext context) {
    return new UpdateAuthorizer(context);
  }

  public static Authorizer<AJEntityCollection> buildDeleteAuthorizer(ProcessorContext context) {
    return new DeleteAuthorizer(context);
  }

  public static Authorizer<AJEntityCollection> buildUpdateCollaboratorAuthorizer(
      ProcessorContext context) {
    return new UpdateCollaboratorAuthorizer(context);
  }

  // Creation is only allowed outside of any context and hence it has got no
  // bearing on course container, which does not exist as our API call for
  // association may be called after create call to set that up
  // As long as session token is valid and user is not anonymous, which is the
  // case as we are, we should be fine
  public static Authorizer<AJEntityCollection> buildCreateAuthorizer(ProcessorContext context) {
    return model -> new ExecutionResult<>(null,
        ExecutionResult.ExecutionStatus.CONTINUE_PROCESSING);
  }

  public static Authorizer<AJEntityCollection> buildAddContentToCollectionAuthorizer(
      ProcessorContext context,
      boolean isResource) {
    return new AddContentToCollectionAuthorizer(context, isResource);
  }

  public static Authorizer<AJEntityCollection> buildTenantAuthorizer(ProcessorContext context) {
    return new TenantAuthorizer(context);
  }

  public static Authorizer<AJEntityCollection> buildTenantCollaboratorAuthorizer(
      ProcessorContext context,
      JsonArray collaborators) {
    return new TenantCollaboratorAuthorizer(context, collaborators);
  }
}
