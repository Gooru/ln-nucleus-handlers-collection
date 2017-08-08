package org.gooru.nucleus.handlers.collections.processors.repositories.activejdbc.entities;

import java.util.*;

import org.gooru.nucleus.handlers.collections.processors.repositories.activejdbc.converters.ConverterRegistry;
import org.gooru.nucleus.handlers.collections.processors.repositories.activejdbc.converters.FieldConverter;
import org.gooru.nucleus.handlers.collections.processors.repositories.activejdbc.validators.FieldSelector;
import org.gooru.nucleus.handlers.collections.processors.repositories.activejdbc.validators.FieldValidator;
import org.gooru.nucleus.handlers.collections.processors.repositories.activejdbc.validators.ReorderFieldValidator;
import org.gooru.nucleus.handlers.collections.processors.repositories.activejdbc.validators.ValidatorRegistry;
import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

/**
 * Created by ashish on 11/1/16.
 */
@Table("collection")
public class AJEntityCollection extends Model {
    // Variables used
    public static final String ID = "id";
    public static final String COLLECTION = "collection";
    private static final String CREATOR_ID = "creator_id";
    public static final String PUBLISH_DATE = "publish_date";
    public static final String IS_DELETED = "is_deleted";
    private static final String MODIFIER_ID = "modifier_id";
    public static final String OWNER_ID = "owner_id";
    private static final String TITLE = "title";
    private static final String THUMBNAIL = "thumbnail";
    private static final String LEARNING_OBJECTIVE = "learning_objective";
    private static final String FORMAT = "format";
    private static final String METADATA = "metadata";
    private static final String TAXONOMY = "taxonomy";
    private static final String URL = "url";
    private static final String LOGIN_REQUIRED = "login_required";
    private static final String VISIBLE_ON_PROFILE = "visible_on_profile";
    public static final String COLLABORATOR = "collaborator";
    private static final String SETTING = "setting";
    public static final String COURSE_ID = "course_id";
    public static final String UNIT_ID = "unit_id";
    public static final String LESSON_ID = "lesson_id";
    private static final String GRADING = "grading";
    public static final String TABLE_COURSE = "course";
    public static final String UPDATED_AT = "updated_at";
    private static final String COLLECTION_TYPE_NAME = "content_container_type";
    private static final String COLLECTION_TYPE_VALUE = "collection";
    private static final String GRADING_TYPE_NAME = "grading_type";
    public static final String REORDER_PAYLOAD_KEY = "order";
    private static final String LICENSE = "license";
    private static final String TENANT = "tenant";
    private static final String TENANT_ROOT = "tenant_root";
    private static final String PUBLISH_STATUS = "publish_status";
    private static final String PUBLISH_STATUS_PUBLISHED = "published";

    // Queries used
    public static final String AUTHORIZER_QUERY =
        "select id, course_id, unit_id, lesson_id, owner_id, creator_id, publish_date, collaborator, tenant, "
            + "tenant_root from collection where format = ?::content_container_type and id = ?::uuid and is_deleted ="
            + " ?";

    public static final String AUTH_FILTER = "id = ?::uuid and (owner_id = ?::uuid or collaborator ?? ?);";
    public static final String PUBLISHED_FILTER = "id = ?::uuid and publish_status = 'published'::publish_status_type;";
    public static final String FETCH_QUERY =
        "select id, title, owner_id, creator_id, original_creator_id, original_collection_id, publish_date, subformat, "
            + "publish_status, thumbnail, learning_objective, license, metadata, taxonomy, setting, grading, "
            + "visible_on_profile, collaborator, course_id, unit_id, lesson_id, tenant, tenant_root from collection "
            + "where id = ?::uuid and format = " + "'collection'::content_container_type and is_deleted = false";
    public static final String COURSE_COLLABORATOR_QUERY =
        "select collaborator from course where id = ?::uuid and is_deleted = false";
    public static final List<String> FETCH_QUERY_FIELD_LIST = Arrays
        .asList("id", "title", "owner_id", "creator_id", "original_creator_id", "original_collection_id",
            "publish_date", "thumbnail", "learning_objective", "license", "metadata", "taxonomy", "setting", "grading",
            "visible_on_profile", "course_id", "unit_id", "lesson_id", "subformat");

    public static final Set<String> EDITABLE_FIELDS = new HashSet<>(Arrays
        .asList(TITLE, THUMBNAIL, LEARNING_OBJECTIVE, METADATA, TAXONOMY, URL, LOGIN_REQUIRED, VISIBLE_ON_PROFILE,
            SETTING));
    public static final Set<String> CREATABLE_FIELDS = EDITABLE_FIELDS;
    public static final Set<String> MANDATORY_FIELDS = new HashSet<>(Arrays.asList(TITLE));
    public static final Set<String> ADD_QUESTION_FIELDS = new HashSet<>(Arrays.asList(ID));
    public static final Set<String> ADD_RESOURCE_FIELDS = ADD_QUESTION_FIELDS;
    public static final Set<String> COLLABORATOR_FIELDS = new HashSet<>(Arrays.asList(COLLABORATOR));
    public static final Set<String> REORDER_FIELDS = new HashSet<>(Arrays.asList(REORDER_PAYLOAD_KEY));

    private static final Map<String, FieldValidator> validatorRegistry;
    private static final Map<String, FieldConverter> converterRegistry;

    static {
        validatorRegistry = initializeValidators();
        converterRegistry = initializeConverters();
    }

    private static Map<String, FieldConverter> initializeConverters() {
        Map<String, FieldConverter> converterMap = new HashMap<>();
        converterMap.put(ID, (fieldValue -> FieldConverter.convertFieldToUuid((String) fieldValue)));
        converterMap.put(METADATA, (FieldConverter::convertFieldToJson));
        converterMap.put(TAXONOMY, (FieldConverter::convertFieldToJson));
        converterMap.put(CREATOR_ID, (fieldValue -> FieldConverter.convertFieldToUuid((String) fieldValue)));
        converterMap.put(MODIFIER_ID, (fieldValue -> FieldConverter.convertFieldToUuid((String) fieldValue)));
        converterMap.put(OWNER_ID, (fieldValue -> FieldConverter.convertFieldToUuid((String) fieldValue)));
        converterMap
            .put(FORMAT, (fieldValue -> FieldConverter.convertFieldToNamedType(fieldValue, COLLECTION_TYPE_NAME)));
        converterMap.put(COLLABORATOR, (FieldConverter::convertFieldToJson));
        converterMap.put(SETTING, (FieldConverter::convertFieldToJson));
        converterMap
            .put(GRADING, (fieldValue -> FieldConverter.convertFieldToNamedType(fieldValue, GRADING_TYPE_NAME)));
        converterMap.put(TENANT, (fieldValue -> FieldConverter.convertFieldToUuid((String) fieldValue)));
        converterMap.put(TENANT_ROOT, (fieldValue -> FieldConverter.convertFieldToUuid((String) fieldValue)));

        return Collections.unmodifiableMap(converterMap);
    }

    private static Map<String, FieldValidator> initializeValidators() {
        Map<String, FieldValidator> validatorMap = new HashMap<>();
        validatorMap.put(ID, (FieldValidator::validateUuid));
        validatorMap.put(TITLE, (value) -> FieldValidator.validateString(value, 1000));
        validatorMap.put(THUMBNAIL, (value) -> FieldValidator.validateStringIfPresent(value, 2000));
        validatorMap.put(LEARNING_OBJECTIVE, (value) -> FieldValidator.validateStringIfPresent(value, 20000));
        validatorMap.put(METADATA, FieldValidator::validateJsonIfPresent);
        validatorMap.put(TAXONOMY, FieldValidator::validateJsonIfPresent);
        validatorMap.put(SETTING, FieldValidator::validateJsonIfPresent);
        validatorMap.put(URL, (value) -> FieldValidator.validateStringIfPresent(value, 2000));
        validatorMap.put(LOGIN_REQUIRED, FieldValidator::validateBooleanIfPresent);
        validatorMap.put(VISIBLE_ON_PROFILE, FieldValidator::validateBooleanIfPresent);
        validatorMap.put(COLLABORATOR,
            (value) -> FieldValidator.validateDeepJsonArrayIfPresent(value, FieldValidator::validateUuid));
        validatorMap.put(REORDER_PAYLOAD_KEY, new ReorderFieldValidator());
        validatorMap.put(TENANT, (FieldValidator::validateUuid));
        validatorMap.put(TENANT_ROOT, (FieldValidator::validateUuid));
        return Collections.unmodifiableMap(validatorMap);
    }

    public static FieldSelector editFieldSelector() {
        return () -> Collections.unmodifiableSet(EDITABLE_FIELDS);
    }

    public static FieldSelector reorderFieldSelector() {
        return new FieldSelector() {
            @Override
            public Set<String> allowedFields() {
                return Collections.unmodifiableSet(REORDER_FIELDS);
            }

            @Override
            public Set<String> mandatoryFields() {
                return Collections.unmodifiableSet(REORDER_FIELDS);
            }
        };
    }

    public static FieldSelector createFieldSelector() {
        return new FieldSelector() {
            @Override
            public Set<String> allowedFields() {
                return Collections.unmodifiableSet(CREATABLE_FIELDS);
            }

            @Override
            public Set<String> mandatoryFields() {
                return Collections.unmodifiableSet(MANDATORY_FIELDS);
            }
        };
    }

    public static FieldSelector editCollaboratorFieldSelector() {
        return new FieldSelector() {
            @Override
            public Set<String> mandatoryFields() {
                return Collections.unmodifiableSet(COLLABORATOR_FIELDS);
            }

            @Override
            public Set<String> allowedFields() {
                return Collections.unmodifiableSet(COLLABORATOR_FIELDS);
            }
        };
    }

    public static FieldSelector addQuestionFieldSelector() {
        return () -> Collections.unmodifiableSet(ADD_QUESTION_FIELDS);
    }

    public static FieldSelector addResourceFieldSelector() {
        return () -> Collections.unmodifiableSet(ADD_RESOURCE_FIELDS);
    }

    public static ValidatorRegistry getValidatorRegistry() {
        return new CollectionValidationRegistry();
    }

    public static ConverterRegistry getConverterRegistry() {
        return new CollectionConverterRegistry();
    }

    public void setModifierId(String modifier) {
        setFieldUsingConverter(MODIFIER_ID, modifier);
    }

    public void setCreatorId(String creator) {
        setFieldUsingConverter(CREATOR_ID, creator);
    }

    public void setOwnerId(String owner) {
        setFieldUsingConverter(OWNER_ID, owner);
    }

    public void setIdWithConverter(String id) {
        setFieldUsingConverter(ID, id);
    }

    public void setLicense(Integer code) {
        this.set(LICENSE, code);
    }

    public void setTypeCollection() {
        setFieldUsingConverter(FORMAT, COLLECTION_TYPE_VALUE);
    }

    public void setTenant(String tenant) {
        setFieldUsingConverter(TENANT, tenant);
    }

    public void setTenantRoot(String tenantRoot) {
        setFieldUsingConverter(TENANT_ROOT, tenantRoot);
    }

    private void setFieldUsingConverter(String fieldName, Object fieldValue) {
        FieldConverter fc = converterRegistry.get(fieldName);
        if (fc != null) {
            this.set(fieldName, fc.convertField(fieldValue));
        } else {
            this.set(fieldName, fieldValue);
        }
    }

    public boolean isCollectionPublished() {
        return Objects.equals(this.getString(PUBLISH_STATUS), PUBLISH_STATUS_PUBLISHED);
    }

    public String getCourseId() {
        return this.getString(COURSE_ID);
    }

    public String getTenant() {
        return this.getString(TENANT);
    }

    public String getTenantRoot() {
        return this.getString(TENANT_ROOT);
    }

    private static class CollectionValidationRegistry implements ValidatorRegistry {
        @Override
        public FieldValidator lookupValidator(String fieldName) {
            return validatorRegistry.get(fieldName);
        }
    }

    private static class CollectionConverterRegistry implements ConverterRegistry {
        @Override
        public FieldConverter lookupConverter(String fieldName) {
            return converterRegistry.get(fieldName);
        }
    }

}
