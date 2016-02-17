package org.gooru.nucleus.handlers.collections.processors.repositories.activejdbc.converters;

import org.postgresql.util.PGobject;

import java.sql.SQLException;

/**
 * Created by ashish on 28/1/16.
 */
public interface FieldConverter {
  static PGobject convertFieldToJson(Object value) {
    String JSONB_TYPE = "jsonb";
    PGobject pgObject = new PGobject();
    pgObject.setType(JSONB_TYPE);
    try {
      pgObject.setValue(value == null ? null : String.valueOf(value));
      return pgObject;
    } catch (SQLException e) {
      return null;
    }
  }

  static PGobject convertFieldToUuid(String value) {
    String UUID_TYPE = "uuid";
    PGobject pgObject = new PGobject();
    pgObject.setType(UUID_TYPE);
    try {
      pgObject.setValue(value);
      return pgObject;
    } catch (SQLException e) {
      return null;
    }
  }

  static PGobject convertFieldToNamedType(Object value, String type) {
    PGobject pgObject = new PGobject();
    pgObject.setType(type);
    try {
      pgObject.setValue(value == null ? null : String.valueOf(value));
      return pgObject;
    } catch (SQLException e) {
      return null;
    }
  }

  PGobject convertField(Object fieldValue);
}
