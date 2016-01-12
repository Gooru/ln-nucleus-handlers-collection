package org.gooru.nucleus.handlers.collections.bootstrap.shutdown;

import org.gooru.nucleus.handlers.collections.app.components.DataSourceRegistry;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Finalizers implements Iterable<Finalizer> {


  private List<Finalizer> finalizers = null;
  private final Iterator<Finalizer> internalIterator;

  public Finalizers() {
    finalizers = new ArrayList<>();
    finalizers.add(DataSourceRegistry.getInstance());
    internalIterator = finalizers.iterator();
  }

  @Override
  public Iterator<Finalizer> iterator() {
    return new Iterator<Finalizer>() {

      @Override
      public boolean hasNext() {
        return internalIterator.hasNext();
      }

      @Override
      public Finalizer next() {
        return internalIterator.next();
      }

    };
  }


}
