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
        return TransactionExecutor.executeTransaction(DBHandlerBuilder.buildDeleteCollectionHandler(context));
    }

    @Override
    public MessageResponse updateCollection() {
        return TransactionExecutor.executeTransaction(DBHandlerBuilder.buildUpdateCollectionHandler(context));
    }

    @Override
    public MessageResponse fetchCollection() {
        return TransactionExecutor.executeTransaction(DBHandlerBuilder.buildFetchCollectionHandler(context));
    }

    @Override
    public MessageResponse createCollection() {
        return TransactionExecutor.executeTransaction(DBHandlerBuilder.buildCreateCollectionHandler(context));
    }

    @Override
    public MessageResponse reorderContentInCollection() {
        return TransactionExecutor.executeTransaction(DBHandlerBuilder.buildReorderContentInCollectionHandler(context));
    }

    @Override
    public MessageResponse updateCollaborator() {
        return TransactionExecutor.executeTransaction(DBHandlerBuilder.buildUpdateCollaboratorForCollection(context));
    }

    @Override
    public MessageResponse addQuestionToCollection() {
        return TransactionExecutor.executeTransaction(DBHandlerBuilder.buildAddQuestionToCollectionHandler(context));
    }

    @Override
    public MessageResponse addResourceToCollection() {
        return TransactionExecutor.executeTransaction(DBHandlerBuilder.buildAddResourceToCollectionHandler(context));
    }
}
