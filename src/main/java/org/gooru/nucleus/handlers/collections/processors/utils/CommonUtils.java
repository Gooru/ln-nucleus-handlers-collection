package org.gooru.nucleus.handlers.collections.processors.utils;

import java.util.Collection;
import java.util.Iterator;

/**
 * @author szgooru on 30-May-2018
 */
public final class CommonUtils {

  private CommonUtils() {
    throw new AssertionError();
  }

  public static String toPostgresArrayString(Collection<String> input) {
    Iterator<String> it = input.iterator();
    if (!it.hasNext()) {
      return "{}";
    }

    StringBuilder sb = new StringBuilder();
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
