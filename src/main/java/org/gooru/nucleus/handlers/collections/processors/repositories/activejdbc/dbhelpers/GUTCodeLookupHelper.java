package org.gooru.nucleus.handlers.collections.processors.repositories.activejdbc.dbhelpers;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.gooru.nucleus.handlers.collections.processors.repositories.activejdbc.entities.AJEntityTaxonomyCodeMapping;
import org.javalite.activejdbc.LazyList;

/**
 * @author szgooru Created On: 13-Sep-2017
 */
public final class GUTCodeLookupHelper {

    private GUTCodeLookupHelper() {
        throw new AssertionError();
    }

    public static Set<String> lookupGUTCodes(Set<String> taxonomyCodes) {

        return null;
    }

    public static Map<String, String> populateGutCodesToTaxonomyMapping(Set<String> taxonomyCodes) {

        LazyList<AJEntityTaxonomyCodeMapping> taxonomyCodeMappings = AJEntityTaxonomyCodeMapping
            .findBySQL(AJEntityTaxonomyCodeMapping.GUT_LOOKUP, toPostgresArrayString(taxonomyCodes));

        Map<String, String> gutMapping = new HashMap<>();
        taxonomyCodeMappings.forEach(codeMapping -> {
            gutMapping.put(codeMapping.getString(AJEntityTaxonomyCodeMapping.SOURCE_TAXONOMY_CODE_ID),
                codeMapping.getString(AJEntityTaxonomyCodeMapping.TARGET_TAXONOMT_CODE_ID));
        });

        return gutMapping;
    }

    public static String toPostgresArrayString(Collection<String> input) {
        int approxSize = ((input.size() + 1) * 36); // Length of UUID is around
                                                    // 36
                                                    // chars
        Iterator<String> it = input.iterator();
        if (!it.hasNext()) {
            return "{}";
        }

        StringBuilder sb = new StringBuilder(approxSize);
        sb.append('{');
        for (;;) {
            String s = it.next();
            sb.append('"').append(s).append('"');
            if (!it.hasNext()) {
                return sb.append('}').toString();
            }
            sb.append(',');
        }
    }
}
