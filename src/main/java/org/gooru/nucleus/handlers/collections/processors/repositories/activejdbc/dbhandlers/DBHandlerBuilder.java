package org.gooru.nucleus.handlers.collections.processors.repositories.activejdbc.dbhandlers;

import org.gooru.nucleus.handlers.collections.processors.ProcessorContext;

/**
 * Created by ashish on 12/1/16.
 */
public final class DBHandlerBuilder {

  private DBHandlerBuilder() {
    throw new AssertionError();
  }

  public static DBHandler buildAddResourceToCollectionHandler(ProcessorContext context) {
    return new AddResourceToCollectionHandler(context);
  }

  public static DBHandler buildUpdateCollaboratorForCollection(ProcessorContext context) {
    return new UpdateCollaboratorHandler(context);
  }

  public static DBHandler buildAddQuestionToCollectionHandler(ProcessorContext context) {
    return new AddQuestionToCollectionHandler(context);
  }

  public static DBHandler buildDeleteCollectionHandler(ProcessorContext context) {
    return new DeleteCollectionHandler(context);
  }

  public static DBHandler buildUpdateCollectionHandler(ProcessorContext context) {
    return new UpdateCollectionHandler(context);
  }

  public static DBHandler buildFetchCollectionHandler(ProcessorContext context) {
    return new FetchCollectionHandler(context);
  }

  public static DBHandler buildCreateCollectionHandler(ProcessorContext context) {
    return new CreateCollectionHandler(context);
  }

  public static DBHandler buildReorderContentInCollectionHandler(ProcessorContext context) {
    return new ReorderContentInCollectionHandler(context);
  }
  
  public static DBHandler buildCreateExternalCollectionHandler(ProcessorContext context) {
    return new CreateExternalCollectionHandler(context);
  }
  
  public static DBHandler buildUpdateExternalCollectionHandler(ProcessorContext context) {
    return new UpdateExternalCollectionHandler(context);
  }
  
  public static DBHandler buildDeleteExternalCollectionHandler(ProcessorContext context) {
    return new DeleteExternalCollectionHandler(context);
  }
  
  public static DBHandler buildFetchExternalCollectionHandler(ProcessorContext context) {
    return new FetchExternalCollectionHandler(context);
  }

}
