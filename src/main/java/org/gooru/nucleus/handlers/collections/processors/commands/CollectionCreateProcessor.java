package org.gooru.nucleus.handlers.collections.processors.commands;

import org.gooru.nucleus.handlers.collections.processors.ProcessorContext;
import org.gooru.nucleus.handlers.collections.processors.repositories.RepoBuilder;
import org.gooru.nucleus.handlers.collections.processors.responses.MessageResponse;

/**
 * @author ashish on 30/12/16.
 */
class CollectionCreateProcessor extends AbstractCommandProcessor {

  public CollectionCreateProcessor(ProcessorContext context) {
    super(context);
  }

  @Override
  protected void setDeprecatedVersions() {

  }

  @Override
  protected MessageResponse processCommand() {
    return RepoBuilder.buildCollectionRepo(context).createCollection();
  }
}
