package org.gooru.nucleus.handlers.collections.processors.repositories.activejdbc.dbutils;

import org.gooru.nucleus.handlers.collections.app.components.DataSourceRegistry;
import org.javalite.activejdbc.Base;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ashish on 22/4/16.
 */
public final class LicenseUtil {

    private static Integer defaultLicenseCode;
    // TODO: Need to provide a proper value here
    private static final String defaultLicenseLabel = "CCNA";

    private LicenseUtil() {
        throw new AssertionError();
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(LicenseUtil.class);
    private static final String LICENSE_QUERY = "select id from metadata_reference where label = ?";

    public static Integer getDefaultLicenseCode() {
        if (defaultLicenseCode == null) {
            LOGGER.warn("Returning default license code as null");
        }
        return defaultLicenseCode;
    }

    public static void initialize() {
        // Need to fetch the default license value from DB here

        try {
            Base.open(DataSourceRegistry.getInstance().getDefaultDataSource());
            Object result = Base.firstCell(LICENSE_QUERY, defaultLicenseLabel);
            if (result == null) {
                throw new AssertionError("License default code not found");
            }
            defaultLicenseCode = Integer.parseInt(result.toString());
        } catch (Throwable e) {
            LOGGER.error("Caught exception while fetching default license value", e);
        } finally {
            Base.close();
        }

    }
}
