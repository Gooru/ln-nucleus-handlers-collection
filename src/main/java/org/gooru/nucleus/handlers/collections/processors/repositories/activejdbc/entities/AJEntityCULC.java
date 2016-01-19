package org.gooru.nucleus.handlers.collections.processors.repositories.activejdbc.entities;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

/**
 * Created by ashish on 18/1/16.
 */
@Table("course_unit_lesson_collection")
public class AJEntityCULC extends Model {

  public static final String COLLECTION_ID = "collection_id";

  public static final String SELECT_FOR_DELETE = "select * from course_unit_lesson_collection where collection_id=? and is_deleted=?";
}
