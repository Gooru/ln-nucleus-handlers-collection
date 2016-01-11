package org.gooru.nucleus.handlers.collections.processors.repositories;

import org.gooru.nucleus.handlers.collections.processors.responses.MessageResponse;

/**
 * Created by ashish on 11/1/16.
 */
public interface CollectionRepo {
  MessageResponse deleteCollection();

  MessageResponse updateCollection();

  MessageResponse fetchCollection();

  MessageResponse createCollection();

  MessageResponse reorderContentInCollection();
}
