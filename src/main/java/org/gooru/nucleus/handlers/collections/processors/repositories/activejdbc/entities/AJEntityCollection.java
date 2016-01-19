package org.gooru.nucleus.handlers.collections.processors.repositories.activejdbc.entities;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

/**
 * Created by ashish on 11/1/16.
 */
@Table("collection")
public class AJEntityCollection extends Model {
  public static final String SELECT_FOR_VALIDATE =
    "select id, creator_id, publish_date, is_deleted from collection where format = ?::content_container_type and id = ? and is_deleted = ?";
  public static final String SELECT_COLLABORATOR = "select collaborator from collection where id = ?";

  // Fields and table names
  public static final String COLLECTION = "collection";
  public static final String CREATOR_ID = "creator_id";
  public static final String PUBLISH_DATE = "publish_date";
  public static final String IS_DELETED = "is_deleted";
  public static final String MODIFIER_ID = "modifier_id";
  public static final String COLLABORATOR = "collaborator";
}
