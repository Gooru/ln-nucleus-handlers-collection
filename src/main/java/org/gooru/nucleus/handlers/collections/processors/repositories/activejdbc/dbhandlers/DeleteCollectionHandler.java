package org.gooru.nucleus.handlers.collections.processors.repositories.activejdbc.dbhandlers;

import java.util.Map;
import java.util.ResourceBundle;

import org.gooru.nucleus.handlers.collections.constants.MessageConstants;
import org.gooru.nucleus.handlers.collections.processors.ProcessorContext;
import org.gooru.nucleus.handlers.collections.processors.events.EventBuilderFactory;
import org.gooru.nucleus.handlers.collections.processors.repositories.activejdbc.dbauth.AuthorizerBuilder;
import org.gooru.nucleus.handlers.collections.processors.repositories.activejdbc.entities.AJEntityCollection;
import org.gooru.nucleus.handlers.collections.processors.repositories.activejdbc.entities.AJEntityContent;
import org.gooru.nucleus.handlers.collections.processors.repositories.activejdbc.entities.AJEntityRubric;
import org.gooru.nucleus.handlers.collections.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.collections.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.collections.processors.responses.MessageResponseFactory;
import org.gooru.nucleus.handlers.collections.processors.tagaggregator.TagAggregatorRequestBuilderFactory;
import org.gooru.nucleus.handlers.courses.constants.CommonConstants;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.DBException;
import org.javalite.activejdbc.LazyList;
import org.javalite.activejdbc.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.json.JsonObject;

/**
 * Created by ashish on 12/1/16.
 */
class DeleteCollectionHandler implements DBHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(DeleteCollectionHandler.class);
    private final ProcessorContext context;
    private final ResourceBundle resourceBundle = ResourceBundle.getBundle("messages");
    private AJEntityCollection existingCollection;
    private static final String TAGS_REMOVED = "tags_removed";

    public DeleteCollectionHandler(ProcessorContext context) {
        this.context = context;
    }

    @Override
    public ExecutionResult<MessageResponse> checkSanity() {
        // There should be an collection id present
        if (context.collectionId() == null || context.collectionId().isEmpty()) {
            LOGGER.warn("Missing collection id");
            return new ExecutionResult<>(
                MessageResponseFactory.createNotFoundResponse(resourceBundle.getString("collection.id.missing")),
                ExecutionResult.ExecutionStatus.FAILED);
        }
        // The user should not be anonymous
        if (context.userId() == null || context.userId().isEmpty()
            || context.userId().equalsIgnoreCase(MessageConstants.MSG_USER_ANONYMOUS)) {
            LOGGER.warn("Anonymous user attempting to delete collection");
            return new ExecutionResult<>(
                MessageResponseFactory.createForbiddenResponse(resourceBundle.getString("not.allowed")),
                ExecutionResult.ExecutionStatus.FAILED);
        }

        return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.CONTINUE_PROCESSING);

    }

    @Override
    public ExecutionResult<MessageResponse> validateRequest() {
        // Fetch the collection where type is collection and it is not deleted
        // already and id is specified id

        LazyList<AJEntityCollection> collections = AJEntityCollection.findBySQL(AJEntityCollection.AUTHORIZER_QUERY,
            AJEntityCollection.COLLECTION, context.collectionId(), false);
        // Collection should be present in DB
        if (collections.size() < 1) {
            LOGGER.warn("Collection id: {} not present in DB", context.collectionId());
            return new ExecutionResult<>(
                MessageResponseFactory
                    .createNotFoundResponse(resourceBundle.getString("collection.id") + context.collectionId()),
                ExecutionResult.ExecutionStatus.FAILED);
        }
        this.existingCollection = collections.get(0);
        // Log a warning if it is published
        if (this.existingCollection.getDate(AJEntityCollection.PUBLISH_DATE) != null) {
            LOGGER.warn("Collection with id '{}' is published collection but it is being deleted",
                context.collectionId());
        }
        return AuthorizerBuilder.buildDeleteAuthorizer(this.context).authorize(this.existingCollection);
    }

    @Override
    public ExecutionResult<MessageResponse> executeRequest() {
        // Update collection, we need to set the deleted flag and user who is
        // deleting it but We do not reset the sequence id right now
        AJEntityCollection collectionToDelete = new AJEntityCollection();
        collectionToDelete.setIdWithConverter(context.collectionId());
        collectionToDelete.setBoolean(AJEntityCollection.IS_DELETED, true);
        collectionToDelete.setModifierId(context.userId());

        boolean result = collectionToDelete.save();
        if (!result) {
            LOGGER.error("Collection with id '{}' failed to delete", context.collectionId());
            if (collectionToDelete.hasErrors()) {
                Map<String, String> map = collectionToDelete.errors();
                JsonObject errors = new JsonObject();
                map.forEach(errors::put);
                return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(errors),
                    ExecutionResult.ExecutionStatus.FAILED);
            }
        }
        if (!deleteContents()) {
            return new ExecutionResult<>(
                MessageResponseFactory.createInternalErrorResponse(resourceBundle.getString("contents.delete.error")),
                ExecutionResult.ExecutionStatus.FAILED);
        }
        
        // Check if the collection is in lesson. If yes, calculate the tag
        // difference and prepare tag aggregation request to send to tag
        // aggregation handler
        String lessonId = this.existingCollection.getString(AJEntityCollection.LESSON_ID);
        if (lessonId != null && !lessonId.isEmpty()) {
            JsonObject tagDiff = calculateTagDifference();
            if (tagDiff != null) {
                return new ExecutionResult<>(
                    MessageResponseFactory.createNoContentResponse(resourceBundle.getString("deleted"),
                        EventBuilderFactory.getDeleteCollectionEventBuilder(context.collectionId()),
                        TagAggregatorRequestBuilderFactory.getLessonTagAggregatorRequestBuilder(lessonId, tagDiff)),
                    ExecutionResult.ExecutionStatus.SUCCESSFUL);
            }
        }
        
        return new ExecutionResult<>(
            MessageResponseFactory.createNoContentResponse(resourceBundle.getString("deleted"),
                EventBuilderFactory.getDeleteCollectionEventBuilder(context.collectionId())),
            ExecutionResult.ExecutionStatus.SUCCESSFUL);
    }

    @Override
    public boolean handlerReadOnly() {
        // This operation is not read only and so is transaction
        return false;
    }

    private boolean deleteContents() {
        try {
            long deletedContentCount =
                Base.exec(AJEntityContent.DELETE_CONTENTS_QUERY, this.context.userId(), this.context.collectionId());
            long deletedRubricCount =
                Base.exec(AJEntityRubric.DELETE_RUBRICS_QUERY, this.context.userId(), this.context.collectionId());
            LOGGER.info("Collection '{}' deleted along with '{}' questions and '{}' rubrics", context.collectionId(),
                deletedContentCount, deletedRubricCount);
            return true;
        } catch (DBException e) {
            LOGGER.error("Error deleting questions for Collection '{}'", context.collectionId(), e);
            return false;
        }
    }
    
    private JsonObject calculateTagDifference() {
        JsonObject result = new JsonObject();
        String existingTagsAsString = this.existingCollection.getString(AJEntityCollection.TAXONOMY);
        JsonObject existingTags = (existingTagsAsString != null && !existingTagsAsString.isEmpty())
            ? new JsonObject(existingTagsAsString) : null;

        return existingTags != null ? result.put(TAGS_REMOVED, existingTags) : null;
    }

}
