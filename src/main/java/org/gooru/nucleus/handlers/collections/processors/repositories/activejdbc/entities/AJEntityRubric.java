package org.gooru.nucleus.handlers.collections.processors.repositories.activejdbc.entities;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

/**
 * @author szgooru
 * Created On: 09-May-2017
 */
@Table("rubric")
public class AJEntityRubric extends Model {
    
    public static final String DELETE_RUBRICS_QUERY =
        "UPDATE rubric SET is_deleted = true, modifier_id = ?::uuid WHERE collection_id = ?::uuid AND is_deleted = false";
}
