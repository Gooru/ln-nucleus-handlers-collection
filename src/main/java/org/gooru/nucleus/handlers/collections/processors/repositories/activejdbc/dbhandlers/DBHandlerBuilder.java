package org.gooru.nucleus.handlers.collections.processors.repositories.activejdbc.dbhandlers;

import org.gooru.nucleus.handlers.collections.processors.ProcessorContext;

/**
 * Created by ashish on 12/1/16.
 */
public class DBHandlerBuilder {
  public DBHandler buildAddResourceToCollectionHandler(ProcessorContext context) {
    return new AddResourceToCollectionHandler(context);
  }

  public DBHandler buildUpdateCollaboratorForCollection(ProcessorContext context) {
    return new UpdateCollaboratorForCollection(context);
  }

  public DBHandler buildAddQuestionToCollectionHandler(ProcessorContext context) {
    return new AddQuestionToCollectionHandler(context);
  }

  public DBHandler buildDeleteCollectionHandler(ProcessorContext context) {
    return new DeleteCollectionHandler(context);
  }

  public DBHandler buildUpdateCollectionHandler(ProcessorContext context) {
    return new UpdateCollectionHandler(context);
  }

  public DBHandler buildFetchCollectionHandler(ProcessorContext context) {
    return new FetchCollectionHandler(context);
  }

  public DBHandler buildCreateCollectionHandler(ProcessorContext context) {
    return new CreateCollectionHandler(context);
  }

  public DBHandler buildReorderContentInCollectionHandler(ProcessorContext context) {
    return new ReorderContentInCollectionHandler(context);
  }
}
