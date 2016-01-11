package org.gooru.nucleus.handlers.collections.processors.repositories.activejdbc;

import org.gooru.nucleus.handlers.collections.processors.ProcessorContext;
import org.gooru.nucleus.handlers.collections.processors.repositories.CollectionResourceRepo;
import org.gooru.nucleus.handlers.collections.processors.responses.MessageResponse;

/**
 * Created by ashish on 11/1/16.
 */
public class AJCollectionResourceRepo implements CollectionResourceRepo {
  private final ProcessorContext context;

  public AJCollectionResourceRepo(ProcessorContext context) {
    this.context = context;
  }

  @Override
  public MessageResponse updateResourceInCollection() {
    // TODO: Provide a concrete implementation
    throw new IllegalStateException("Not implemented yet");
  }

  @Override
  public MessageResponse removeResourceFromCollection() {
    // TODO: Provide a concrete implementation
    throw new IllegalStateException("Not implemented yet");
  }

  @Override
  public MessageResponse addResourceToCollection() {
    // TODO: Provide a concrete implementation
    throw new IllegalStateException("Not implemented yet");
  }

  @Override
  public MessageResponse copyResourceToCollection() {
    // TODO: Provide a concrete implementation
    throw new IllegalStateException("Not implemented yet");
  }
}
