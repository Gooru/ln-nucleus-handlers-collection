package org.gooru.nucleus.handlers.collections.processors.repositories.activejdbc.formatter;

import org.javalite.activejdbc.Model;

/**
 * Created by ashish on 20/1/16.
 */
public interface JsonFormatter {

  <T extends Model> String toJson(T model);
}