package org.gooru.nucleus.handlers.collections.processors.repositories.activejdbc.dbauth;

import java.util.ResourceBundle;

import org.gooru.nucleus.handlers.collections.processors.ProcessorContext;
import org.gooru.nucleus.handlers.collections.processors.repositories.activejdbc.entities.AJEntityCollection;
import org.gooru.nucleus.handlers.collections.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.collections.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.collections.processors.responses.MessageResponseFactory;
import org.gooru.nucleus.libs.tenant.TenantTree;
import org.gooru.nucleus.libs.tenant.TenantTreeBuilder;
import org.gooru.nucleus.libs.tenant.contents.ContentTenantAuthorization;
import org.gooru.nucleus.libs.tenant.contents.ContentTenantAuthorizationBuilder;
import org.gooru.nucleus.libs.tenant.contents.ContentTreeAttributes;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.DBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ashish on 10/1/17.
 */
class TenantAuthorizer implements Authorizer<AJEntityCollection> {
    private final ProcessorContext context;
    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("messages");
    private static final Logger LOGGER = LoggerFactory.getLogger(TenantAuthorizer.class);

    public TenantAuthorizer(ProcessorContext context) {
        this.context = context;
    }

    @Override
    public ExecutionResult<MessageResponse> authorize(AJEntityCollection model) {
        TenantTree userTenantTree = TenantTreeBuilder.build(context.tenant(), context.tenantRoot());
        TenantTree contentTenantTree = TenantTreeBuilder.build(model.getTenant(), model.getTenantRoot());

        // First validation based on published status of this entity only
        ContentTenantAuthorization authorization = ContentTenantAuthorizationBuilder
            .build(contentTenantTree, userTenantTree, ContentTreeAttributes.build(model.isCollectionPublished()));

        if (authorization.canRead()) {
            return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.CONTINUE_PROCESSING);
        }
        return checkAuthBasedOnCourseBeingPublished(model, userTenantTree, contentTenantTree);
    }

    private ExecutionResult<MessageResponse> checkAuthBasedOnCourseBeingPublished(AJEntityCollection model,
        TenantTree userTenantTree, TenantTree contentTenantTree) {
        ContentTenantAuthorization authorization;
        String courseId = model.getCourseId();
        if (courseId != null) {
            try {
                long published =
                    Base.count(AJEntityCollection.TABLE_COURSE, AJEntityCollection.PUBLISHED_FILTER, courseId);
                if (published >= 1) {
                    // It is published, check that if collection was already published in which case nothing to check
                    // further else check with the new information bit
                    if (!model.isCollectionPublished()) {
                        authorization = ContentTenantAuthorizationBuilder
                            .build(contentTenantTree, userTenantTree, ContentTreeAttributes.build(true));
                        if (authorization.canRead()) {
                            return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.CONTINUE_PROCESSING);
                        }
                    }
                }
            } catch (DBException e) {
                LOGGER.error("Error checking authorization for delete for Collection '{}' for course '{}'",
                    context.collectionId(), courseId, e);
                return new ExecutionResult<>(
                    MessageResponseFactory.createInternalErrorResponse(RESOURCE_BUNDLE.getString("error.from.store")),
                    ExecutionResult.ExecutionStatus.FAILED);
            }
        }
        return new ExecutionResult<>(
            MessageResponseFactory.createNotFoundResponse(RESOURCE_BUNDLE.getString("not.found")),
            ExecutionResult.ExecutionStatus.FAILED);
    }
}
