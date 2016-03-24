package org.gooru.nucleus.handlers.collections.processors.repositories.activejdbc.entities;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

import java.util.Arrays;
import java.util.List;

/**
 * Created by ashish on 2/2/16.
 */
@Table("content")
public class AJEntityContent extends Model {

  public static final String ADD_QUESTION_QUERY =
    "update content set collection_id = ?::uuid, modifier_id = ?::uuid, updated_at = now(), sequence_id = ? where id = ?::uuid and is_deleted = " +
      "false and content_format = 'question'::content_format_type and course_id is null and collection_id is null and creator_id = ?::uuid";
  public static final String ADD_RESOURCE_QUERY =
    "update content set collection_id = ?::uuid, modifier_id = ?::uuid, updated_at = now(), sequence_id = ? where id = ?::uuid and is_deleted = " +
      "false and content_format = 'resource'::content_format_type and course_id is null and collection_id is null and creator_id = ?::uuid";
  public static final String CONTENT_FOR_REORDER_COLLECTION_QUERY = "select id from content where collection_id = ?::uuid and is_deleted = false";
  public static final String REORDER_QUERY =
    "update content set sequence_id = ?, modifier_id = ?::uuid, updated_at = now() where id = ?::uuid and collection_id = ?::uuid and is_deleted = " +
      "false";
  public static final String SEQUENCE_ID = "sequence_id";
  public static final String MAX_CONTENT_SEQUENCE_QUERY = "select max(sequence_id) from content where collection_id = ?::uuid";
  public static final String TABLE_CONTENT = "content";
  public static final String CONTENT_FOR_ADD_FILTER =
    "id = ?::uuid and is_deleted = false and course_id is null and collection_id is null and creator_id = ?::uuid";

  public static final String DELETE_CONTENTS_QUERY =
    "update content set is_deleted = true, modifier_id = ?::uuid, updated_at = now()  where collection_id = ?::uuid and is_deleted = false";
  public static final String FETCH_CONTENT_SUMMARY_QUERY =
    "select id, title, url, creator_id, original_creator_id, publish_date, short_title, content_format, content_subformat, answer, metadata, " +
      "taxonomy, " +
      "depth_of_knowledge, hint_explanation_detail, thumbnail, sequence_id, is_copyright_owner, visible_on_profile, display_guide from content " +
      "where collection_id = ?::uuid and " +
      " is_deleted = false order by sequence_id asc";
  public static final List<String> FETCH_CONTENT_SUMMARY_FIELDS = Arrays
    .asList("id", "title", "url", "creator_id", "original_creator_id", "publish_date", "short_title", "content_format", "content_subformat", "answer",
      "metadata", "taxonomy", "depth_of_knowledge", "hint_explanation_detail", "thumbnail", "sequence_id", "is_copyright_owner", "visible_on_profile",
      "display_guide");
  public static final String CONTENT = "content";
  public static final String ORIGINAL_CONTENT_ID = "original_content_id";
  public static final String PARENT_CONTENT_ID = "parent_content_id";

  public boolean isContentOriginal() {
    String originalContentId = this.getString(ORIGINAL_CONTENT_ID);
    String parentContentId = this.getString(PARENT_CONTENT_ID);
    return originalContentId == null && parentContentId == null;
  }
}
