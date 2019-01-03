package org.gooru.nucleus.handlers.collections.bootstrap;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import org.gooru.nucleus.handlers.collections.bootstrap.shutdown.Finalizer;
import org.gooru.nucleus.handlers.collections.bootstrap.shutdown.Finalizers;
import org.gooru.nucleus.handlers.collections.bootstrap.startup.Initializer;
import org.gooru.nucleus.handlers.collections.bootstrap.startup.Initializers;
import org.gooru.nucleus.handlers.collections.constants.MessageConstants;
import org.gooru.nucleus.handlers.collections.constants.MessagebusEndpoints;
import org.gooru.nucleus.handlers.collections.processors.ProcessorBuilder;
import org.gooru.nucleus.handlers.collections.processors.responses.MessageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ashish on 25/12/15.
 */
public class CollectionVerticle extends AbstractVerticle {

  private static final Logger LOGGER = LoggerFactory.getLogger(CollectionVerticle.class);

  @Override
  public void start(Future<Void> voidFuture) throws Exception {

    EventBus eb = vertx.eventBus();

    vertx.executeBlocking(blockingFuture -> {
      startApplication();
      blockingFuture.complete();
    }, startApplicationFuture -> {
      if (startApplicationFuture.succeeded()) {
        eb.consumer(MessagebusEndpoints.MBEP_COLLECTION, message -> {
          LOGGER.debug("Received message: " + message.body());
          vertx.executeBlocking(future -> {
            MessageResponse result = ProcessorBuilder.build(message).process();
            future.complete(result);
          }, res -> {
            MessageResponse result = (MessageResponse) res.result();
            message.reply(result.reply(), result.deliveryOptions());
            JsonObject eventData = result.event();
            if (eventData != null) {
              String sessionToken = ((JsonObject) message.body())
                  .getString(MessageConstants.MSG_HEADER_TOKEN);
              if (sessionToken != null && !sessionToken.isEmpty()) {
                eventData.put(MessageConstants.MSG_HEADER_TOKEN, sessionToken);
              } else {
                LOGGER.warn("Invalid session token received");
              }
              eb.send(MessagebusEndpoints.MBEP_EVENT, eventData);
            }

            JsonObject tagsToAggregate = result.tagsToAggregate();
            if (tagsToAggregate != null) {
              JsonObject session = ((JsonObject) message.body())
                  .getJsonObject(MessageConstants.MSG_KEY_SESSION);
              String sessionToken = ((JsonObject) message.body())
                  .getString(MessageConstants.MSG_HEADER_TOKEN);
              tagsToAggregate.put(MessageConstants.MSG_HEADER_TOKEN, sessionToken);
              tagsToAggregate.put(MessageConstants.MSG_KEY_SESSION, session);

              LOGGER.debug("sending request for tag aggregation: {}", tagsToAggregate);
              eb.send(MessagebusEndpoints.MBEP_TAG_AGGREGATOR, tagsToAggregate);
            }
          });
        }).completionHandler(result -> {
          if (result.succeeded()) {
            LOGGER.info("Collection end point ready to listen");
            voidFuture.complete();
          } else {
            LOGGER.error(
                "Error registering the collection handler. Halting the Collection machinery");
            voidFuture.fail(result.cause());
            Runtime.getRuntime().halt(1);
          }
        });
      } else {
        voidFuture.fail("Not able to initialize the Collection machinery properly");
      }
    });
  }

  @Override
  public void stop() throws Exception {
    shutDownApplication();
    super.stop();
  }

  private void startApplication() {
    Initializers initializers = new Initializers();
    try {
      for (Initializer initializer : initializers) {
        initializer.initializeComponent(vertx, config());
      }
    } catch (IllegalStateException ie) {
      LOGGER.error("Error initializing application", ie);
      Runtime.getRuntime().halt(1);
    }
  }

  private void shutDownApplication() {
    Finalizers finalizers = new Finalizers();
    for (Finalizer finalizer : finalizers) {
      finalizer.finalizeComponent();
    }

  }

}
