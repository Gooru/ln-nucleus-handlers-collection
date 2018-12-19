package org.gooru.nucleus.handlers.collections.constants;

public final class MessagebusEndpoints {

  /*
   * Any change here in end points should be done in the gateway side as well,
   * as both sender and receiver should be in sync
   */
  public static final String MBEP_COLLECTION = "org.gooru.nucleus.message.bus.collection";
  public static final String MBEP_EVENT = "org.gooru.nucleus.message.bus.publisher.event";
  public static final String MBEP_TAG_AGGREGATOR = "org.gooru.nucleus.message.bus.tag.aggregator";

  private MessagebusEndpoints() {
    throw new AssertionError();
  }
}
