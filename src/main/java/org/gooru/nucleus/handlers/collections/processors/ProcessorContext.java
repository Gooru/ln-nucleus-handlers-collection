package org.gooru.nucleus.handlers.collections.processors;

import io.vertx.core.MultiMap;
import io.vertx.core.json.JsonObject;

/**
 * Created by ashish on 7/1/16.
 */
public class ProcessorContext {

    private final String userId;
    private final JsonObject prefs;
    private final JsonObject request;
    private final String collectionId;
    private final String questionId;
    private final String resourceId;
    private final MultiMap requestHeaders;

    public ProcessorContext(String userId, JsonObject prefs, JsonObject request, String collectionId, String questionId,
        String resourceId, MultiMap headers) {
        if (prefs == null || userId == null || prefs.isEmpty() || headers == null || headers.isEmpty()) {
            throw new IllegalStateException("Processor Context creation failed because of invalid values");
        }
        this.userId = userId;
        this.prefs = prefs.copy();
        this.request = request != null ? request.copy() : null;
        // Assessment id, resource id and question id can be null in case of
        // create and hence can't validate them unless we know the op type also
        // Do not want to build dependency on op for this context to work and
        // hence is open ended. Worst case would be RTE, so beware
        this.collectionId = collectionId;
        this.questionId = questionId;
        this.resourceId = resourceId;
        this.requestHeaders = headers;
    }

    public String userId() {
        return this.userId;
    }

    public JsonObject prefs() {
        return this.prefs.copy();
    }

    public JsonObject request() {
        return this.request;
    }

    public String collectionId() {
        return this.collectionId;
    }

    public String questionId() {
        return this.questionId;
    }

    public String resourceId() {
        return this.resourceId;
    }

    public MultiMap requestHeaders() {
        return requestHeaders;
    }

}
