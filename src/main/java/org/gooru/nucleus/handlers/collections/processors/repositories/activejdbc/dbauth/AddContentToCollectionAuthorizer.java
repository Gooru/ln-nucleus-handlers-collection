package org.gooru.nucleus.handlers.collections.processors.repositories.activejdbc.dbauth;

import java.util.ResourceBundle;

import org.gooru.nucleus.handlers.collections.processors.ProcessorContext;
import org.gooru.nucleus.handlers.collections.processors.repositories.activejdbc.entities.AJEntityCollection;
import org.gooru.nucleus.handlers.collections.processors.repositories.activejdbc.entities.AJEntityContent;
import org.gooru.nucleus.handlers.collections.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.collections.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.collections.processors.responses.MessageResponseFactory;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.DBException;
import org.javalite.activejdbc.LazyList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.json.JsonArray;

/**
 * Created by ashish on 1/2/16.
 */
public class AddContentToCollectionAuthorizer implements Authorizer<AJEntityCollection> {
    private static final ResourceBundle resourceBundle = ResourceBundle.getBundle("messages");
    private final ProcessorContext context;
    private final Logger LOGGER = LoggerFactory.getLogger(Authorizer.class);
    private final boolean isContentResource;

    public AddContentToCollectionAuthorizer(ProcessorContext context, boolean isResource) {
        this.context = context;
        this.isContentResource = isResource;
    }

    @Override
    public ExecutionResult<MessageResponse> authorize(AJEntityCollection collection) {
        String ownerId = collection.getString(AJEntityCollection.OWNER_ID);
        String courseId = collection.getString(AJEntityCollection.COURSE_ID);
        long authRecordCount;
        // If this collection is not part of course, then user should be either
        // owner or collaborator on course
        if (courseId != null) {
            try {
                authRecordCount = Base.count(AJEntityCollection.TABLE_COURSE, AJEntityCollection.AUTH_FILTER, courseId,
                    context.userId(), context.userId());
                if (authRecordCount >= 1) {
                    return authorizeForContent(collection);
                }
            } catch (DBException e) {
                LOGGER.error("Error checking authorization for update for Collection '{}' for course '{}'",
                    context.collectionId(), courseId, e);
                return new ExecutionResult<>(MessageResponseFactory
                    .createInternalErrorResponse(resourceBundle.getString("internal.error.authorization.checking")),
                    ExecutionResult.ExecutionStatus.FAILED);
            }
        } else {
            // Collection is not part of course, hence we need user to be either
            // owner or collaborator on collection
            if (context.userId().equalsIgnoreCase(ownerId)) {
                // Owner is fine
                return authorizeForContent(collection);
            } else {
                String collaborators = collection.getString(AJEntityCollection.COLLABORATOR);
                if (collaborators != null && !collaborators.isEmpty()) {
                    JsonArray collaboratorsArray = new JsonArray(collaborators);
                    if (collaboratorsArray.contains(context.userId())) {
                        return authorizeForContent(collection);
                    }
                }
            }
        }
        LOGGER.warn("User: '{}' is not owner/collaborator of collection: '{}' or owner/collaborator on course",
            context.userId(), context.collectionId());
        return new ExecutionResult<>(
            MessageResponseFactory.createForbiddenResponse(resourceBundle.getString("not.allowed")),
            ExecutionResult.ExecutionStatus.FAILED);
    }

    private ExecutionResult<MessageResponse> authorizeForContent(AJEntityCollection collection) {
        try {
            LazyList<AJEntityContent> contents =
                AJEntityContent.where(AJEntityContent.CONTENT_FOR_ADD_FILTER, context.questionId(), context.userId());
            if (contents.size() == 1) {
                if (isContentResource) {
                    if (contents.get(0).isContentOriginal()) {
                        LOGGER.warn("Resource '{}' being added to collection '{}' is not a reference but original",
                            context.resourceId(), context.collectionId());
                        return new ExecutionResult<>(MessageResponseFactory
                            .createInvalidRequestResponse(resourceBundle.getString("resource.reference.needed")),
                            ExecutionResult.ExecutionStatus.FAILED);
                    }
                }
                return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.CONTINUE_PROCESSING);
            }
        } catch (DBException e) {
            LOGGER.error("Error querying content '{}' availability for associating in collection '{}'",
                context.questionId() != null ? context.questionId() : context.resourceId(), context.collectionId(), e);
            return new ExecutionResult<>(MessageResponseFactory
                .createInternalErrorResponse(resourceBundle.getString("internal.error.authorization.checking")),
                ExecutionResult.ExecutionStatus.FAILED);
        }
        return new ExecutionResult<>(MessageResponseFactory
            .createInvalidRequestResponse(resourceBundle.getString("content.association.not.available")),
            ExecutionResult.ExecutionStatus.FAILED);
    }

}
