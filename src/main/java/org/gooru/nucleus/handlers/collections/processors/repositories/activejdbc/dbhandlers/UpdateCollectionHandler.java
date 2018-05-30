package org.gooru.nucleus.handlers.collections.processors.repositories.activejdbc.dbhandlers;

import java.util.Map;
import java.util.ResourceBundle;

import org.gooru.nucleus.handlers.collections.constants.MessageConstants;
import org.gooru.nucleus.handlers.collections.processors.ProcessorContext;
import org.gooru.nucleus.handlers.collections.processors.events.EventBuilderFactory;
import org.gooru.nucleus.handlers.collections.processors.repositories.activejdbc.dbauth.AuthorizerBuilder;
import org.gooru.nucleus.handlers.collections.processors.repositories.activejdbc.dbhelpers.GUTCodeLookupHelper;
import org.gooru.nucleus.handlers.collections.processors.repositories.activejdbc.entities.AJEntityCollection;
import org.gooru.nucleus.handlers.collections.processors.repositories.activejdbc.entitybuilders.EntityBuilder;
import org.gooru.nucleus.handlers.collections.processors.repositories.activejdbc.validators.PayloadValidator;
import org.gooru.nucleus.handlers.collections.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.collections.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.collections.processors.responses.MessageResponseFactory;
import org.gooru.nucleus.handlers.collections.processors.tagaggregator.TagAggregatorRequestBuilderFactory;
import org.gooru.nucleus.handlers.collections.processors.utils.CommonUtils;
import org.javalite.activejdbc.LazyList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.json.JsonObject;

/**
 * Created by ashish on 12/1/16.
 */
class UpdateCollectionHandler implements DBHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateCollectionHandler.class);
    private static final ResourceBundle resourceBundle = ResourceBundle.getBundle("messages");
    private final ProcessorContext context;
    private AJEntityCollection collection;

    private static final String TAGS_ADDED = "tags_added";
    private static final String TAGS_REMOVED = "tags_removed";

    public UpdateCollectionHandler(ProcessorContext context) {
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
            AJEntityCollection.editFieldSelector(), AJEntityCollection.getValidatorRegistry());
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
        AJEntityCollection collectionToUpdate = new AJEntityCollection();
        collectionToUpdate.setIdWithConverter(context.collectionId());
        collectionToUpdate.setModifierId(context.userId());

        // Now auto populate is done, we need to setup the converter machinery
        new DefaultAJEntityCollectionEntityBuilder().build(collectionToUpdate, context.request(),
            AJEntityCollection.getConverterRegistry());
        
        JsonObject newTags = this.context.request().getJsonObject(AJEntityCollection.TAXONOMY);
        if (newTags != null && !newTags.isEmpty()) {
            Map<String, String> frameworkToGutCodeMapping =
                GUTCodeLookupHelper.populateGutCodesToTaxonomyMapping(newTags.fieldNames());
            collectionToUpdate.setGutCodes(CommonUtils.toPostgresArrayString(frameworkToGutCodeMapping.keySet()));
        }

        boolean result = collectionToUpdate.save();
        if (!result) {
            LOGGER.error("Collection with id '{}' failed to save", context.collectionId());
            if (collectionToUpdate.hasErrors()) {
                Map<String, String> map = collectionToUpdate.errors();
                JsonObject errors = new JsonObject();
                map.forEach(errors::put);
                return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(errors),
                    ExecutionResult.ExecutionStatus.FAILED);
            }
        }

        // Check if the collection is in lesson. If yes, calculate the tag
        // difference and prepare tag aggregation request to send to tag
        // aggregation handler
        String lessonId = this.collection.getString(AJEntityCollection.LESSON_ID);
        if (lessonId != null && !lessonId.isEmpty()) {
            JsonObject tagDiff = calculateTagDifference();
            if (tagDiff != null) {
                return new ExecutionResult<>(
                    MessageResponseFactory.createNoContentResponse(resourceBundle.getString("updated"),
                        EventBuilderFactory.getUpdateCollectionEventBuilder(context.collectionId()),
                        TagAggregatorRequestBuilderFactory.getLessonTagAggregatorRequestBuilder(lessonId, tagDiff)),
                    ExecutionResult.ExecutionStatus.SUCCESSFUL);
            }
        }

        // Otherwise return without tag aggregation
        return new ExecutionResult<>(
            MessageResponseFactory.createNoContentResponse(resourceBundle.getString("updated"),
                EventBuilderFactory.getUpdateCollectionEventBuilder(context.collectionId())),
            ExecutionResult.ExecutionStatus.SUCCESSFUL);
    }

    @Override
    public boolean handlerReadOnly() {
        return false;
    }

    private static class DefaultPayloadValidator implements PayloadValidator {
    }

    private static class DefaultAJEntityCollectionEntityBuilder implements EntityBuilder<AJEntityCollection> {
    }

    private JsonObject calculateTagDifference() {
        JsonObject result = new JsonObject();
        String existingTagsAsString = this.collection.getString(AJEntityCollection.TAXONOMY);
        JsonObject existingTags = existingTagsAsString != null && !existingTagsAsString.isEmpty()
            ? new JsonObject(existingTagsAsString) : new JsonObject();
        JsonObject newTags = this.context.request().getJsonObject(AJEntityCollection.TAXONOMY);

        if (existingTags.isEmpty() && newTags != null && !newTags.isEmpty()) {
            result.put(TAGS_ADDED, newTags.copy());
            result.put(TAGS_REMOVED, new JsonObject());
        } else if (!existingTags.isEmpty() && (newTags == null || newTags.isEmpty())) {
            result.put(TAGS_ADDED, new JsonObject());
            result.put(TAGS_REMOVED, existingTags.copy());
        } else if (!existingTags.isEmpty() && newTags != null && !newTags.isEmpty()) {
            JsonObject toBeAdded = new JsonObject();
            JsonObject toBeRemoved = existingTags.copy();
            newTags.forEach(entry -> {
                String key = entry.getKey();
                if (toBeRemoved.containsKey(key)) {
                    toBeRemoved.remove(key);
                } else {
                    toBeAdded.put(key, entry.getValue());
                }
            });

            if (toBeAdded.isEmpty() && toBeRemoved.isEmpty()) {
                return null;
            }

            result.put(TAGS_ADDED, toBeAdded);
            result.put(TAGS_REMOVED, toBeRemoved);
        }

        return result;
    }
}
