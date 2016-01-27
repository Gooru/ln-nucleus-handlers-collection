package org.gooru.nucleus.handlers.collections.processors.repositories;

import org.gooru.nucleus.handlers.collections.processors.ProcessorContext;
import org.gooru.nucleus.handlers.collections.processors.repositories.activejdbc.AJRepoBuilder;

/**
 * Created by ashish on 7/1/16.
 */
public class RepoBuilder {

  public CollectionRepo buildCollectionRepo(ProcessorContext context) {
    return new AJRepoBuilder().buildCollectionRepo(context);
  }

}
