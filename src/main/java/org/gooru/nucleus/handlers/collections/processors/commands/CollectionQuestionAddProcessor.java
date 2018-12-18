package org.gooru.nucleus.handlers.collections.processors.commands;

import static org.gooru.nucleus.handlers.collections.processors.utils.ValidationUtils.validateContextWithQuestion;

import org.gooru.nucleus.handlers.collections.processors.ProcessorContext;
import org.gooru.nucleus.handlers.collections.processors.repositories.RepoBuilder;
import org.gooru.nucleus.handlers.collections.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.collections.processors.responses.MessageResponseFactory;

/**
 * @author ashish on 30/12/16.
 */
class CollectionQuestionAddProcessor extends AbstractCommandProcessor {

  public CollectionQuestionAddProcessor(ProcessorContext context) {
    super(context);
  }

  @Override
  protected void setDeprecatedVersions() {

  }

  @Override
  protected MessageResponse processCommand() {
    if (!validateContextWithQuestion(context)) {
      return MessageResponseFactory
          .createInvalidRequestResponse(
              RESOURCE_BUNDLE.getString("collection.question.id.invalid"));
    }
    return RepoBuilder.buildCollectionRepo(context).addQuestionToCollection();
  }
}
