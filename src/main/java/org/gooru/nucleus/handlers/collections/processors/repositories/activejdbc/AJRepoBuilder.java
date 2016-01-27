package org.gooru.nucleus.handlers.collections.processors.repositories.activejdbc;

import org.gooru.nucleus.handlers.collections.processors.ProcessorContext;
import org.gooru.nucleus.handlers.collections.processors.repositories.CollectionRepo;

/**
 * Created by ashish on 7/1/16.
 */
public class AJRepoBuilder {

  public CollectionRepo buildCollectionRepo(ProcessorContext context) {
    return new AJCollectionRepo(context);
  }

}
