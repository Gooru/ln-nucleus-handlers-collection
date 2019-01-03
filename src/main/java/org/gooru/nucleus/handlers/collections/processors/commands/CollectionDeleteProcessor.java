package org.gooru.nucleus.handlers.collections.processors.commands;

import static org.gooru.nucleus.handlers.collections.processors.utils.ValidationUtils.validateContext;

import org.gooru.nucleus.handlers.collections.processors.ProcessorContext;
import org.gooru.nucleus.handlers.collections.processors.repositories.RepoBuilder;
import org.gooru.nucleus.handlers.collections.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.collections.processors.responses.MessageResponseFactory;

/**
 * @author ashish on 30/12/16.
 */
class CollectionDeleteProcessor extends AbstractCommandProcessor {

  public CollectionDeleteProcessor(ProcessorContext context) {
    super(context);
  }

  @Override
  protected void setDeprecatedVersions() {

  }

  @Override
  protected MessageResponse processCommand() {
    if (!validateContext(context)) {
      return MessageResponseFactory
          .createInvalidRequestResponse(RESOURCE_BUNDLE.getString("collection.id.invalid"));
    }
    return RepoBuilder.buildCollectionRepo(context).deleteCollection();
  }
}
