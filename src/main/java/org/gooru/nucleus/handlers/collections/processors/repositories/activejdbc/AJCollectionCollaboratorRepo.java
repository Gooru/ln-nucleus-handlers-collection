package org.gooru.nucleus.handlers.collections.processors.repositories.activejdbc;

import org.gooru.nucleus.handlers.collections.processors.ProcessorContext;
import org.gooru.nucleus.handlers.collections.processors.repositories.CollectionCollaboratorRepo;
import org.gooru.nucleus.handlers.collections.processors.repositories.activejdbc.dbhandlers.DBHandlerBuilder;
import org.gooru.nucleus.handlers.collections.processors.repositories.activejdbc.transactions.TransactionExecutor;
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
    return new TransactionExecutor().executeTransaction(new DBHandlerBuilder().buildUpdateCollaboratorForCollection(context));
  }

  @Override
  public MessageResponse fetchCollaborator() {
    return new TransactionExecutor().executeTransaction(new DBHandlerBuilder().buildFetchCollaboratorForCollection(context));
  }
}
