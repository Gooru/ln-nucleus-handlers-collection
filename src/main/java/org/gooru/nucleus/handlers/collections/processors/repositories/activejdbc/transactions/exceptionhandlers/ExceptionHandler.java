package org.gooru.nucleus.handlers.collections.processors.repositories.activejdbc.transactions.exceptionhandlers;

import org.gooru.nucleus.handlers.collections.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.collections.processors.responses.MessageResponse;

public interface ExceptionHandler {
  
  ExecutionResult<MessageResponse> handleError(Throwable e);

}
