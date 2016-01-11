package org.gooru.nucleus.handlers.collections.processors.repositories.activejdbc;

import org.gooru.nucleus.handlers.collections.processors.ProcessorContext;
import org.gooru.nucleus.handlers.collections.processors.repositories.CollectionCollaboratorRepo;
import org.gooru.nucleus.handlers.collections.processors.repositories.CollectionQuestionRepo;
import org.gooru.nucleus.handlers.collections.processors.repositories.CollectionRepo;
import org.gooru.nucleus.handlers.collections.processors.repositories.CollectionResourceRepo;

/**
 * Created by ashish on 7/1/16.
 */
public class AJRepoBuilder {

  public CollectionRepo buildCollectionRepo(ProcessorContext context) {
    return new AJCollectionRepo(context);
  }

  public CollectionQuestionRepo buildCollectionQuestionRepo(ProcessorContext context) {
    return new AJCollectionQuestionRepo(context);
  }

  public CollectionCollaboratorRepo buildCollectionCollaboratorRepo(ProcessorContext context) {
    return new AJCollectionCollaboratorRepo(context);
  }

  public CollectionResourceRepo buildCollectionResourceRepo(ProcessorContext context) {
    return new AJCollectionResourceRepo(context);
  }

}
