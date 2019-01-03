package org.gooru.nucleus.handlers.collections.processors.commands;

import static org.gooru.nucleus.handlers.collections.processors.utils.ValidationUtils.validateContext;

import org.gooru.nucleus.handlers.collections.processors.ProcessorContext;
import org.gooru.nucleus.handlers.collections.processors.repositories.RepoBuilder;
import org.gooru.nucleus.handlers.collections.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.collections.processors.responses.MessageResponseFactory;

/**
 * @author renuka on 24/12/18.
 */
class ExCollectionDeleteProcessor extends AbstractCommandProcessor {

  public ExCollectionDeleteProcessor(ProcessorContext context) {
    super(context);
  }

  @Override
  protected void setDeprecatedVersions() {

  }

  @Override
  protected MessageResponse processCommand() {
    if (!validateContext(context)) {
      return MessageResponseFactory
          .createInvalidRequestResponse(RESOURCE_BUNDLE.getString("invalid.collection.id"));
    }
    return RepoBuilder.buildCollectionRepo(context).deleteExternalCollection();
  }
}
