package org.gooru.nucleus.handlers.collections.processors.commands;

import static org.gooru.nucleus.handlers.collections.processors.utils.ValidationUtils.validateContext;

import org.gooru.nucleus.handlers.collections.processors.ProcessorContext;
import org.gooru.nucleus.handlers.collections.processors.repositories.RepoBuilder;
import org.gooru.nucleus.handlers.collections.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.collections.processors.responses.MessageResponseFactory;

/**
 * @author szgooru Created On: 10-Oct-2017
 */
public class CollectionResourceTagAggregateProcessor extends AbstractCommandProcessor {

    protected CollectionResourceTagAggregateProcessor(ProcessorContext context) {
        super(context);
    }

    @Override
    protected void setDeprecatedVersions() {
        // NOOP
    }

    @Override
    protected MessageResponse processCommand() {
        if (!validateContext(context)) {
            return MessageResponseFactory
                .createInvalidRequestResponse(RESOURCE_BUNDLE.getString("collection.id.invalid"));
        }
        return RepoBuilder.buildCollectionRepo(context).aggregateResourceTagsAtCollection();
    }

}
