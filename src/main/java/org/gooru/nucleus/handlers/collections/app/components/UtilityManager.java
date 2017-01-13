package org.gooru.nucleus.handlers.collections.app.components;

import org.gooru.nucleus.handlers.collections.bootstrap.shutdown.Finalizer;
import org.gooru.nucleus.handlers.collections.bootstrap.startup.Initializer;
import org.gooru.nucleus.handlers.collections.processors.repositories.activejdbc.dbutils.LicenseUtil;
import org.gooru.nucleus.libs.tenant.bootstrap.TenantInitializer;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * This is a manager class to initialize the utilities, Utilities initialized
 * may depend on the DB or application state. Thus their initialization sequence
 * may matter. It is advisable to keep the utility initialization for last.
 */
public final class UtilityManager implements Initializer, Finalizer {
    private static final UtilityManager ourInstance = new UtilityManager();

    public static UtilityManager getInstance() {
        return ourInstance;
    }

    private UtilityManager() {
    }

    @Override
    public void finalizeComponent() {

    }

    @Override
    public void initializeComponent(Vertx vertx, JsonObject config) {
        TenantInitializer.initialize(DataSourceRegistry.getInstance().getDefaultDataSource());
        LicenseUtil.initialize();
    }
}
