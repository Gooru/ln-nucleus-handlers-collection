package org.gooru.nucleus.handlers.collections.processors.repositories.activejdbc.entities;

import io.vertx.core.json.JsonArray;
import java.util.Collection;
import java.util.Iterator;
import org.javalite.activejdbc.LazyList;
import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

/**
 * @author ashish on 20/1/17.
 */
@Table("users")
public class AJEntityUser extends Model {

  private static final String TENANT = "tenant_id";
  private static final String TENANT_ROOT = "tenant_root";

  private static final String COLLABORATOR_VALIDATION_QUERY =
      "select tenant_id, tenant_root from users where id = ANY(?::uuid[])";

  public String getTenant() {
    return this.getString(TENANT);
  }

  public String getTenantRoot() {
    return this.getString(TENANT_ROOT);
  }

  public static LazyList<AJEntityUser> getCollaboratorsTenantInfo(JsonArray collaborators) {
    return AJEntityUser
        .findBySQL(COLLABORATOR_VALIDATION_QUERY, toPostgresArrayString(collaborators.getList()));
  }

  public static String toPostgresArrayString(Collection<String> input) {
    int approxSize = ((input.size() + 1) * 36);
    Iterator<String> it = input.iterator();
    if (!it.hasNext()) {
      return "{}";
    }

    StringBuilder sb = new StringBuilder(approxSize);
    sb.append('{');
    for (; ; ) {
      String s = it.next();
      sb.append('"').append(s).append('"');
      if (!it.hasNext()) {
        return sb.append('}').toString();
      }
      sb.append(',');
    }
  }

}
