package org.gooru.nucleus.handlers.collections.processors.repositories.activejdbc.dbhandlers;

import java.util.Map;
import java.util.ResourceBundle;

import org.gooru.nucleus.handlers.collections.constants.MessageConstants;
import org.gooru.nucleus.handlers.collections.processors.ProcessorContext;
import org.gooru.nucleus.handlers.collections.processors.events.EventBuilderFactory;
import org.gooru.nucleus.handlers.collections.processors.repositories.activejdbc.dbauth.AuthorizerBuilder;
import org.gooru.nucleus.handlers.collections.processors.repositories.activejdbc.dbhelpers.GUTCodeLookupHelper;
import org.gooru.nucleus.handlers.collections.processors.repositories.activejdbc.entities.AJEntityCollection;
import org.gooru.nucleus.handlers.collections.processors.repositories.activejdbc.validators.PayloadValidator;
import org.gooru.nucleus.handlers.collections.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.collections.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.collections.processors.responses.MessageResponseFactory;
import org.gooru.nucleus.handlers.collections.processors.tagaggregator.TagAggregatorRequestBuilderFactory;
import org.javalite.activejdbc.LazyList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.json.JsonObject;

/**
 * @author szgooru Created On: 10-Oct-2017
 */
public class AggregateResourceTagsAtCollectionHandler implements DBHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(AggregateResourceTagsAtCollectionHandler.class);
    private static final ResourceBundle resourceBundle = ResourceBundle.getBundle("messages");

    private final ProcessorContext context;
    private AJEntityCollection collection;
    private JsonObject gutCodes;
    private JsonObject taxonomy;

    private static final String TAGS_ADDED = "tags_added";
    private static final String TAGS_REMOVED = "tags_removed";

    public AggregateResourceTagsAtCollectionHandler(ProcessorContext context) {
        this.context = context;
    }

    @Override
    public ExecutionResult<MessageResponse> checkSanity() {
        // There should be an collection id present
        if (context.collectionId() == null || context.collectionId().isEmpty()) {
            LOGGER.warn("Missing collection id");
            return new ExecutionResult<>(
                MessageResponseFactory.createInvalidRequestResponse(resourceBundle.getString("collection.id.missing")),
                ExecutionResult.ExecutionStatus.FAILED);
        }
        // The user should not be anonymous
        if (context.userId() == null || context.userId().isEmpty()
            || context.userId().equalsIgnoreCase(MessageConstants.MSG_USER_ANONYMOUS)) {
            LOGGER.warn("Anonymous user attempting to edit collection");
            return new ExecutionResult<>(
                MessageResponseFactory.createForbiddenResponse(resourceBundle.getString("not.allowed")),
                ExecutionResult.ExecutionStatus.FAILED);
        }
        // Payload should not be empty
        if (context.request() == null || context.request().isEmpty()) {
            LOGGER.warn("Empty payload supplied to edit collection");
            return new ExecutionResult<>(
                MessageResponseFactory.createInvalidRequestResponse(resourceBundle.getString("payload.empty")),
                ExecutionResult.ExecutionStatus.FAILED);
        }
        // Our validators should certify this
        JsonObject errors = new DefaultPayloadValidator().validatePayload(context.request(),
            AJEntityCollection.aggregateTagsFieldSelector(), AJEntityCollection.getValidatorRegistry());
        if (errors != null && !errors.isEmpty()) {
            LOGGER.warn("Validation errors for request");
            return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(errors),
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
        this.collection = collections.get(0);
        return AuthorizerBuilder.buildUpdateAuthorizer(this.context).authorize(this.collection);
    }

    @Override
    public ExecutionResult<MessageResponse> executeRequest() {

        String existingGutCodes = this.collection.getString(AJEntityCollection.GUT_CODES);
        String existingTaxonomy = this.collection.getString(AJEntityCollection.TAXONOMY);

        this.gutCodes = (existingGutCodes != null && !existingGutCodes.isEmpty())
            ? new JsonObject(existingGutCodes) : new JsonObject();
        this.taxonomy = (existingTaxonomy != null && !existingTaxonomy.isEmpty())
            ? new JsonObject(existingTaxonomy) : new JsonObject();

        JsonObject newTags = this.context.request().getJsonObject(AJEntityCollection.TAXONOMY);
        Map<String, String> frameworkToGutCodeMapping =
            GUTCodeLookupHelper.populateGutCodesToTaxonomyMapping(newTags.fieldNames());
        
        newTags.forEach(entry -> {
            this.taxonomy.put(entry.getKey(), entry.getValue());
        });
        
        this.collection.setGutCodes(this.gutCodes.toString());
        this.collection.setTaxonomy(this.taxonomy.toString());

        boolean result = this.collection.save();
        if (!result) {
            LOGGER.error("Collection with id '{}' failed to aggregate tags", context.collectionId());
            return new ExecutionResult<>(
                MessageResponseFactory.createInternalErrorResponse(resourceBundle.getString("internal.store.error")),
                ExecutionResult.ExecutionStatus.FAILED);
        }
        
        JsonObject tagsAdded = new JsonObject();
        tagsAdded.put(TAGS_ADDED, newTags.copy());

        // Check if the collection is in lesson. If yes, calculate the tag
        // difference and prepare tag aggregation request to send to tag
        // aggregation handler
        String lessonId = this.collection.getString(AJEntityCollection.LESSON_ID);
        if (lessonId != null && !lessonId.isEmpty()) {
                return new ExecutionResult<>(
                    MessageResponseFactory.createNoContentResponse(resourceBundle.getString("updated"),
                        EventBuilderFactory.getAggregateResourceTagAtCollectionEventBuilder(context.collectionId(),
                            tagsAdded),
                        TagAggregatorRequestBuilderFactory.getLessonTagAggregatorRequestBuilder(lessonId, tagsAdded)),
                    ExecutionResult.ExecutionStatus.SUCCESSFUL);
        }

        // If collection is standalone return without processing tag aggregation
        return new ExecutionResult<>(
            MessageResponseFactory.createNoContentResponse(resourceBundle.getString("updated"),
                EventBuilderFactory.getAggregateResourceTagAtCollectionEventBuilder(context.collectionId(), tagsAdded)),
            ExecutionResult.ExecutionStatus.SUCCESSFUL);
    }

    @Override
    public boolean handlerReadOnly() {
        return false;
    }

    private static class DefaultPayloadValidator implements PayloadValidator {
    }

}
