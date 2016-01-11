package org.gooru.nucleus.handlers.collections.processors.repositories.activejdbc;

import org.gooru.nucleus.handlers.collections.processors.ProcessorContext;
import org.gooru.nucleus.handlers.collections.processors.repositories.CollectionCollaboratorRepo;
import org.gooru.nucleus.handlers.collections.processors.responses.MessageResponse;

/**
 * Created by ashish on 11/1/16.
 */
public class AJCollectionCollaboratorRepo implements CollectionCollaboratorRepo {
  private final ProcessorContext context;

  public AJCollectionCollaboratorRepo(ProcessorContext context) {
    this.context = context;
  }

  @Override
  public MessageResponse updateCollaborator() {
    // TODO: Provide a concrete implementation
    throw new IllegalStateException("Not implemented yet");
  }

  @Override
  public MessageResponse fetchCollaborator() {
    // TODO: Provide a concrete implementation
    throw new IllegalStateException("Not implemented yet");
  }
}
