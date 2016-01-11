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

  public CollectionQuestionRepo buildCollectionQuestionRepo(ProcessorContext context) {
    return new AJRepoBuilder().buildCollectionQuestionRepo(context);
  }

  public CollectionCollaboratorRepo buildCollectionCollaboratorRepo(ProcessorContext context) {
    return new AJRepoBuilder().buildCollectionCollaboratorRepo(context);
  }

  public CollectionResourceRepo buildCollectionResourceRepo(ProcessorContext context) {
    return new AJRepoBuilder().buildCollectionResourceRepo(context);
  }


}
