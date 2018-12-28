package org.gooru.nucleus.handlers.collections.processors.commands;

import org.gooru.nucleus.handlers.collections.processors.ProcessorContext;
import org.gooru.nucleus.handlers.collections.processors.repositories.RepoBuilder;
import org.gooru.nucleus.handlers.collections.processors.responses.MessageResponse;

/**
 * @author renuka on 24/12/18.
 */
public class ExCollectionCreateProcessor extends AbstractCommandProcessor {

  public ExCollectionCreateProcessor(ProcessorContext context) {
    super(context);
  }

  @Override
  protected void setDeprecatedVersions() {

  }

  @Override
  protected MessageResponse processCommand() {
    return RepoBuilder.buildCollectionRepo(context).createExternalCollection();
  }
}
