package org.gooru.nucleus.handlers.collections.processors.repositories.activejdbc.entities;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

/**
 * Created by ashish on 11/1/16.
 */
@Table("content")
public class AJEntityQuestion extends Model {

  public static final String ADD_QUESTION_QUERY =
    "update content set collection_id = ?::uuid, modifier_id = ?::uuid, updated_at = now(), sequence_id = ? where id = ?::uuid and is_deleted = " +
      "false and content_format = 'question'::content_format_type and course_id is null and collection_id is null and creator_id = ?::uuid";

}
