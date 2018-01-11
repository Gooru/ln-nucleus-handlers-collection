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
    private JsonObject tagsAdded;
    private JsonObject tagsRemoved;
    private JsonObject aggregatedGutCodes;
    private JsonObject aggregatedTaxonomy;

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

        String existingAggregatedGutCodes = this.collection.getString(AJEntityCollection.AGGREGATED_GUT_CODES);
        String existingAggregatedTaxonomy = this.collection.getString(AJEntityCollection.AGGREGATED_TAXONOMY);

        this.aggregatedGutCodes = (existingAggregatedGutCodes != null && !existingAggregatedGutCodes.isEmpty())
            ? new JsonObject(existingAggregatedGutCodes) : new JsonObject();
        this.aggregatedTaxonomy = (existingAggregatedTaxonomy != null && !existingAggregatedTaxonomy.isEmpty())
            ? new JsonObject(existingAggregatedTaxonomy) : new JsonObject();

        JsonObject tagDiff = calculateTagDifference();
        // If no tag difference is found in existing tags and in request,
        // silently ignore and return success without event
        if (tagDiff == null || tagDiff.isEmpty()) {
            LOGGER.debug("no tag difference found, skipping.");
            return new ExecutionResult<>(
                MessageResponseFactory.createNoContentResponse(resourceBundle.getString("updated")),
                ExecutionResult.ExecutionStatus.SUCCESSFUL);
        }
        
        this.tagsAdded = tagDiff.getJsonObject(TAGS_ADDED);
        this.tagsRemoved = tagDiff.getJsonObject(TAGS_REMOVED);

        if (this.tagsRemoved != null && !this.tagsRemoved.isEmpty()) {
            processTagRemoval();
        }

        if (this.tagsAdded != null && !this.tagsAdded.isEmpty()) {
            processTagAddition();
        }

        this.collection.setAggregatedGutCodes(this.aggregatedGutCodes.toString());
        this.collection.setAggregatedTaxonomy(this.aggregatedTaxonomy.toString());

        boolean result = this.collection.save();
        if (!result) {
            LOGGER.error("Collection with id '{}' failed to aggregate tags", context.collectionId());
            return new ExecutionResult<>(
                MessageResponseFactory.createInternalErrorResponse(resourceBundle.getString("internal.store.error")),
                ExecutionResult.ExecutionStatus.FAILED);
        }

        // Check if the collection is in lesson. If yes, calculate the tag
        // difference and prepare tag aggregation request to send to tag
        // aggregation handler
        String lessonId = this.collection.getString(AJEntityCollection.LESSON_ID);
        if (lessonId != null && !lessonId.isEmpty()) {
            if (tagDiff != null) {
                return new ExecutionResult<>(
                    MessageResponseFactory.createNoContentResponse(resourceBundle.getString("updated"),
                        EventBuilderFactory.getAggregateResourceTagAtCollectionEventBuilder(context.collectionId(),
                            tagDiff),
                        TagAggregatorRequestBuilderFactory.getLessonTagAggregatorRequestBuilder(lessonId, tagDiff)),
                    ExecutionResult.ExecutionStatus.SUCCESSFUL);
            }
        }

        // If collection is standalone return without processing tag aggregation
        return new ExecutionResult<>(
            MessageResponseFactory.createNoContentResponse(resourceBundle.getString("updated"),
                EventBuilderFactory.getAggregateResourceTagAtCollectionEventBuilder(context.collectionId(), tagDiff)),
            ExecutionResult.ExecutionStatus.SUCCESSFUL);
    }

    @Override
    public boolean handlerReadOnly() {
        return false;
    }

    private static class DefaultPayloadValidator implements PayloadValidator {
    }

    private void processTagAddition() {
        Map<String, String> frameworkToGutCodeMapping =
            GUTCodeLookupHelper.populateGutCodesToTaxonomyMapping(this.tagsAdded.fieldNames());

        frameworkToGutCodeMapping.keySet().forEach(gutCode -> {
            // If the gut code to be added is already exists in aggregated gut
            // codes, then increase competency count by 1
            // If it does not exists, then add new
            if (this.aggregatedGutCodes.containsKey(gutCode)) {
                int competencyCount = this.aggregatedGutCodes.getInteger(gutCode);
                this.aggregatedGutCodes.put(gutCode, (competencyCount + 1));
            } else {
                this.aggregatedGutCodes.put(gutCode, 1);
                this.aggregatedTaxonomy.put(frameworkToGutCodeMapping.get(gutCode),
                    this.tagsAdded.getJsonObject(frameworkToGutCodeMapping.get(gutCode)));
            }
        });
    }

    private void processTagRemoval() {

        Map<String, String> frameworkToGutCodeMapping =
            GUTCodeLookupHelper.populateGutCodesToTaxonomyMapping(this.tagsRemoved.fieldNames());

        frameworkToGutCodeMapping.keySet().forEach(gutCode -> {
            if (this.aggregatedGutCodes.containsKey(gutCode)) {
                int competencyCount = this.aggregatedGutCodes.getInteger(gutCode);
                // Competency count 1 means this competency is tagged only once
                // and across lessons. Hence can be removed
                // Competency count greater than 1 means this competency is
                // tagged multiple times across lesson, so we will just reduce
                // the competency count
                if (competencyCount == 1) {
                    this.aggregatedGutCodes.remove(gutCode);
                    aggregatedTaxonomy.remove(frameworkToGutCodeMapping.get(gutCode));
                } else if (competencyCount > 1) {
                    this.aggregatedGutCodes.put(gutCode, (competencyCount - 1));
                }
            }

            // Do nothing of the gut code which is not present in existing
            // aggregated gut codes
        });
    }

    private JsonObject calculateTagDifference() {
        JsonObject result = new JsonObject();
        String existingTagsAsString = this.collection.getString(AJEntityCollection.AGGREGATED_TAXONOMY);
        JsonObject existingTags = existingTagsAsString != null && !existingTagsAsString.isEmpty()
            ? new JsonObject(existingTagsAsString) : new JsonObject();
        JsonObject newTags = this.context.request().getJsonObject(AJEntityCollection.AGGREGATED_TAXONOMY);

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
