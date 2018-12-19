package org.gooru.nucleus.handlers.collections.processors.repositories.activejdbc.dbhandlers;

import io.vertx.core.json.JsonObject;
import java.util.Map;
import java.util.ResourceBundle;
import org.gooru.nucleus.handlers.collections.constants.MessageConstants;
import org.gooru.nucleus.handlers.collections.processors.ProcessorContext;
import org.gooru.nucleus.handlers.collections.processors.events.EventBuilderFactory;
import org.gooru.nucleus.handlers.collections.processors.repositories.activejdbc.dbauth.AuthorizerBuilder;
import org.gooru.nucleus.handlers.collections.processors.repositories.activejdbc.dbhelpers.GUTCodeLookupHelper;
import org.gooru.nucleus.handlers.collections.processors.repositories.activejdbc.dbutils.LicenseUtil;
import org.gooru.nucleus.handlers.collections.processors.repositories.activejdbc.entities.AJEntityCollection;
import org.gooru.nucleus.handlers.collections.processors.repositories.activejdbc.entitybuilders.EntityBuilder;
import org.gooru.nucleus.handlers.collections.processors.repositories.activejdbc.validators.PayloadValidator;
import org.gooru.nucleus.handlers.collections.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.collections.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.collections.processors.responses.MessageResponseFactory;
import org.gooru.nucleus.handlers.collections.processors.utils.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ashish on 12/1/16.
 */
class CreateCollectionHandler implements DBHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(CreateCollectionHandler.class);
  private static final ResourceBundle resourceBundle = ResourceBundle.getBundle("messages");
  private final ProcessorContext context;

  public CreateCollectionHandler(ProcessorContext context) {
    this.context = context;
  }

  @Override
  public ExecutionResult<MessageResponse> checkSanity() {
    // The user should not be anonymous
    if ((context.userId() == null) || context.userId().isEmpty()
        || context.userId().equalsIgnoreCase(MessageConstants.MSG_USER_ANONYMOUS)) {
      LOGGER.warn("Anonymous or invalid user attempting to create collection");
      return new ExecutionResult<>(
          MessageResponseFactory.createForbiddenResponse(resourceBundle.getString("not.allowed")),
          ExecutionResult.ExecutionStatus.FAILED);
    }
    // Payload should not be empty
    if ((context.request() == null) || context.request().isEmpty()) {
      LOGGER.warn("Empty payload supplied to create collection");
      return new ExecutionResult<>(
          MessageResponseFactory
              .createInvalidRequestResponse(resourceBundle.getString("payload.empty")),
          ExecutionResult.ExecutionStatus.FAILED);
    }
    // Our validators should certify this
    JsonObject errors = new DefaultPayloadValidator().validatePayload(context.request(),
        AJEntityCollection.createFieldSelector(), AJEntityCollection.getValidatorRegistry());
    if ((errors != null) && !errors.isEmpty()) {
      LOGGER.warn("Validation errors for request");
      return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(errors),
          ExecutionResult.ExecutionStatus.FAILED);
    }
    return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.CONTINUE_PROCESSING);
  }

  @Override
  public ExecutionResult<MessageResponse> validateRequest() {
    // Only thing to do here is to authorize
    return AuthorizerBuilder.buildCreateAuthorizer(context).authorize(null);
  }

  @Override
  public ExecutionResult<MessageResponse> executeRequest() {
    AJEntityCollection collection = new AJEntityCollection();
    // Now auto populate is done, we need to setup the converter machinery
    autoPopulateFields(collection);
    new DefaultAJEntityCollectionEntityBuilder().build(collection, context.request(),
        AJEntityCollection.getConverterRegistry());

    JsonObject newTags = this.context.request().getJsonObject(AJEntityCollection.TAXONOMY);
    if (newTags != null && !newTags.isEmpty()) {
      Map<String, String> frameworkToGutCodeMapping =
          GUTCodeLookupHelper.populateGutCodesToTaxonomyMapping(newTags.fieldNames());
      collection.setGutCodes(CommonUtils.toPostgresArrayString(frameworkToGutCodeMapping.keySet()));
    }

    boolean result = collection.save();
    if (!result) {
      LOGGER.error("Collection creation failed for user '{}'", context.userId());
      if (collection.hasErrors()) {
        Map<String, String> map = collection.errors();
        JsonObject errors = new JsonObject();
        map.forEach(errors::put);
        return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(errors),
            ExecutionResult.ExecutionStatus.FAILED);
      }
    }
    return new ExecutionResult<>(
        MessageResponseFactory.createCreatedResponse(collection.getId().toString(),
            EventBuilderFactory
                .getCreateCollectionEventBuilder(collection.getString(AJEntityCollection.ID))),
        ExecutionResult.ExecutionStatus.SUCCESSFUL);
  }

  private void autoPopulateFields(AJEntityCollection collection) {
    collection.setModifierId(context.userId());
    collection.setOwnerId(context.userId());
    collection.setCreatorId(context.userId());
    collection.setTypeCollection();
    collection.setLicense(LicenseUtil.getDefaultLicenseCode());
    collection.setTenant(context.tenant());
    String tenantRoot = context.tenantRoot();
    if (tenantRoot != null && !tenantRoot.isEmpty()) {
      collection.setTenantRoot(tenantRoot);
    }
  }

  @Override
  public boolean handlerReadOnly() {
    return false;
  }

  private static class DefaultPayloadValidator implements PayloadValidator {

  }

  private static class DefaultAJEntityCollectionEntityBuilder implements
      EntityBuilder<AJEntityCollection> {

  }
}
