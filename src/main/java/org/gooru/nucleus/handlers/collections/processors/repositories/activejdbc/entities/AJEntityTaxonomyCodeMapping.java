package org.gooru.nucleus.handlers.collections.processors.repositories.activejdbc.entities;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

/**
 * @author szgooru Created On: 13-Sep-2017
 */
@Table("taxonomy_code_mapping")
public class AJEntityTaxonomyCodeMapping extends Model {

  public static final String SOURCE_TAXONOMY_CODE_ID = "source_taxonomy_code_id";
  public static final String TARGET_TAXONOMT_CODE_ID = "target_taxonomy_code_id";

  public static final String GUT_LOOKUP =
      "SELECT source_taxonomy_code_id, target_taxonomy_code_id FROM taxonomy_code_mapping WHERE target_taxonomy_code_id = ANY(?::text[])";
}
