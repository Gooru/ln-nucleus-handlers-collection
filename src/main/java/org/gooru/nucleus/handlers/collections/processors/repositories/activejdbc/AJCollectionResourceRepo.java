package org.gooru.nucleus.handlers.collections.processors.repositories.activejdbc;

import org.gooru.nucleus.handlers.collections.processors.ProcessorContext;
import org.gooru.nucleus.handlers.collections.processors.repositories.CollectionResourceRepo;
import org.gooru.nucleus.handlers.collections.processors.repositories.activejdbc.dbhandlers.DBHandlerBuilder;
import org.gooru.nucleus.handlers.collections.processors.repositories.activejdbc.transactions.TransactionExecutor;
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
    return new TransactionExecutor().executeTransaction(new DBHandlerBuilder().buildUpdateResourceInCollectionHandler(context));
  }

  @Override
  public MessageResponse removeResourceFromCollection() {
    return new TransactionExecutor().executeTransaction(new DBHandlerBuilder().buildRemoveResourceFromCollectionHandler(context));
  }

  @Override
  public MessageResponse addResourceToCollection() {
    return new TransactionExecutor().executeTransaction(new DBHandlerBuilder().buildAddResourceToCollectionHandler(context));
  }

  @Override
  public MessageResponse copyResourceToCollection() {
    return new TransactionExecutor().executeTransaction(new DBHandlerBuilder().buildCopyResourceToCollectionHandler(context));
  }
}
