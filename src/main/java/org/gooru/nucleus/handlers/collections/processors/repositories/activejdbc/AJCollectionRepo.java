package org.gooru.nucleus.handlers.collections.processors.repositories.activejdbc;

import org.gooru.nucleus.handlers.collections.processors.ProcessorContext;
import org.gooru.nucleus.handlers.collections.processors.repositories.CollectionRepo;
import org.gooru.nucleus.handlers.collections.processors.repositories.activejdbc.dbhandlers.DBHandlerBuilder;
import org.gooru.nucleus.handlers.collections.processors.repositories.activejdbc.transactions.TransactionExecutor;
import org.gooru.nucleus.handlers.collections.processors.responses.MessageResponse;

/**
 * Created by ashish on 11/1/16.
 */
public class AJCollectionRepo implements CollectionRepo {
  private final ProcessorContext context;

  public AJCollectionRepo(ProcessorContext context) {
    this.context = context;
  }

  @Override
  public MessageResponse deleteCollection() {
    return new TransactionExecutor().executeTransaction(new DBHandlerBuilder().buildDeleteCollectionHandler(context));
  }

  @Override
  public MessageResponse updateCollection() {
    return new TransactionExecutor().executeTransaction(new DBHandlerBuilder().buildUpdateCollectionHandler(context));
  }

  @Override
  public MessageResponse fetchCollection() {
    return new TransactionExecutor().executeTransaction(new DBHandlerBuilder().buildFetchCollectionHandler(context));
  }

  @Override
  public MessageResponse createCollection() {
    return new TransactionExecutor().executeTransaction(new DBHandlerBuilder().buildCreateCollectionHandler(context));
  }

  @Override
  public MessageResponse reorderContentInCollection() {
    return new TransactionExecutor().executeTransaction(new DBHandlerBuilder().buildReorderContentInCollectionHandler(context));
  }

  @Override
  public MessageResponse updateCollaborator() {
    return new TransactionExecutor().executeTransaction(new DBHandlerBuilder().buildUpdateCollaboratorForCollection(context));
  }

  @Override
  public MessageResponse addQuestionToCollection() {
    return new TransactionExecutor().executeTransaction(new DBHandlerBuilder().buildAddQuestionToCollectionHandler(context));
  }

  @Override
  public MessageResponse addResourceToCollection() {
    return new TransactionExecutor().executeTransaction(new DBHandlerBuilder().buildAddResourceToCollectionHandler(context));
  }
}
