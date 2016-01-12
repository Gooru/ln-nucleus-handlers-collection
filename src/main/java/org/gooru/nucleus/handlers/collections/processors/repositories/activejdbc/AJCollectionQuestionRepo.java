package org.gooru.nucleus.handlers.collections.processors.repositories.activejdbc;

import org.gooru.nucleus.handlers.collections.processors.ProcessorContext;
import org.gooru.nucleus.handlers.collections.processors.repositories.CollectionQuestionRepo;
import org.gooru.nucleus.handlers.collections.processors.repositories.activejdbc.dbhandlers.DBHandlerBuilder;
import org.gooru.nucleus.handlers.collections.processors.repositories.activejdbc.transactions.TransactionExecutor;
import org.gooru.nucleus.handlers.collections.processors.responses.MessageResponse;

/**
 * Created by ashish on 11/1/16.
 */
public class AJCollectionQuestionRepo implements CollectionQuestionRepo {
  private final ProcessorContext context;

  public AJCollectionQuestionRepo(ProcessorContext context) {
    this.context = context;
  }

  @Override
  public MessageResponse updateQuestionInCollection() {
    return new TransactionExecutor().executeTransaction(new DBHandlerBuilder().buildUpdateQuestionInCollectionHandler(context));
  }

  @Override
  public MessageResponse removeQuestionFromCollection() {
    return new TransactionExecutor().executeTransaction(new DBHandlerBuilder().buildRemoveQuestionFromCollectionHandler(context));
  }

  @Override
  public MessageResponse addQuestionToCollection() {
    return new TransactionExecutor().executeTransaction(new DBHandlerBuilder().buildAddQuestionToCollectionHandler(context));
  }

  @Override
  public MessageResponse copyQuestionToCollection() {
    return new TransactionExecutor().executeTransaction(new DBHandlerBuilder().buildCopyQuestionToCollectionHandler(context));
  }
}
