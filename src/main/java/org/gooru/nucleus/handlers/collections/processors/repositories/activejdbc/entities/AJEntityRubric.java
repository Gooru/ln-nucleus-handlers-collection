package org.gooru.nucleus.handlers.collections.processors.repositories.activejdbc.entities;

import java.util.Arrays;
import java.util.List;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

/**
 * @author szgooru Created On: 09-May-2017
 */
@Table("rubric")
public class AJEntityRubric extends Model {

    public static final String ID = "id";
    public static final String CONTENT_ID = "content_id";
    public static final String IS_RUBRIC = "is_rubric";
    public static final String SCORING = "scoring";
    public static final String MAX_SCORE = "max_score";

  public static final String DELETE_RUBRICS_QUERY =
      "UPDATE rubric SET is_deleted = true, modifier_id = ?::uuid WHERE collection_id = ?::uuid AND is_deleted = false";
  

  public static final String FETCH_RUBRIC_SUMMARY =
      "SELECT id, content_id, is_rubric, scoring, max_score FROM rubric WHERE content_id = ANY(?::uuid[]) AND is_deleted = false";

  public static final List<String> RUBRIC_SUMMARY =
      Arrays.asList(ID, CONTENT_ID, IS_RUBRIC, SCORING, MAX_SCORE);
  
}
