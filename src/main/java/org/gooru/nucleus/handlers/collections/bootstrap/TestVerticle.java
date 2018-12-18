package org.gooru.nucleus.handlers.collections.bootstrap;

import io.vertx.core.MultiMap;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.impl.MessageImpl;
import io.vertx.core.http.CaseInsensitiveHeaders;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.gooru.nucleus.handlers.collections.bootstrap.shutdown.Finalizer;
import org.gooru.nucleus.handlers.collections.bootstrap.shutdown.Finalizers;
import org.gooru.nucleus.handlers.collections.bootstrap.startup.Initializer;
import org.gooru.nucleus.handlers.collections.bootstrap.startup.Initializers;
import org.gooru.nucleus.handlers.collections.constants.MessageConstants;
import org.gooru.nucleus.handlers.collections.processors.ProcessorBuilder;
import org.gooru.nucleus.handlers.collections.processors.responses.MessageResponse;

/**
 * Created by ashish on 11/1/16.
 */
public class TestVerticle {

  public static void main(String[] args) {

    TestVerticle verticle = new TestVerticle();
    verticle.runTest();

  }

  private void runTest() {
    startApplication();
    // Message<Object> message = getCreateMessageWithProperInfo();
    Message<Object> message = getFetchMessageWithProperInfo();
    // Message<Object> message = getUpdateMessageWithProperInfo();
    // Message<Object> message = getUpdateCollaboratorMessageWithProperInfo();

    // Message<Object> message = getReorderQuestionMessageWithProperInfo();
    // Message<Object> message = getAddQuestionMessageWithProperInfo();

    // Message<Object> message = getDeleteMessageWithProperInfo();
    MessageResponse result = ProcessorBuilder.build((message)).process();
    dumpMessageResponse(result);
    shutDownApplication();
  }

  private Message<Object> getReorderQuestionMessageWithProperInfo() {
    return new MessageImpl() {
      private final JsonObject jsonObject =
          new JsonObject().put(MessageConstants.MSG_USER_ID, "0607cf97-6f9e-47c0-b610-01b36e02b4c6")
              .put(MessageConstants.MSG_KEY_SESSION, new JsonObject().put("a", "b"))
              .put(MessageConstants.MSG_HTTP_BODY,
                  new JsonObject().put("order",
                      new JsonArray()
                          .add(new JsonObject().put("id", "01bacbe9-abd5-42ea-9c9e-fe72eada49b4")
                              .put(
                                  "sequence_id", 2))
                          .add(
                              new JsonObject().put("id", "38b1a25e-20d9-44ac-ba59-e651b64cb76d")
                                  .put("sequence_id", 1))));
      private final MultiMap multiMap = new CaseInsensitiveHeaders()
          .add(MessageConstants.MSG_HEADER_OP, MessageConstants.MSG_OP_COLLECTION_CONTENT_REORDER)
          .add(MessageConstants.COLLECTION_ID, "4bf9c4c7-4df7-408b-b92e-2feade842074");

      @Override
      public MultiMap headers() {
        return multiMap;
      }

      @Override
      public Object body() {
        return jsonObject;
      }
    };
  }

  private Message<Object> getFetchMessageWithProperInfo() {
    return new MessageImpl() {
      private final JsonObject jsonObject =
          new JsonObject().put(MessageConstants.MSG_USER_ID, "f2ff66d9-ead6-4a9f-8f5e-7c5fe64a10bf")
              .put(MessageConstants.MSG_KEY_SESSION, new JsonObject().put("a", "b"))
              .put(MessageConstants.MSG_HTTP_BODY, new JsonObject());
      private final MultiMap multiMap =
          new CaseInsensitiveHeaders()
              .add(MessageConstants.MSG_HEADER_OP, MessageConstants.MSG_OP_COLLECTION_GET)
              .add(MessageConstants.COLLECTION_ID, "8f9e6703-0438-4367-a13d-77b6ee38a3f2");

      @Override
      public MultiMap headers() {
        return multiMap;
      }

      @Override
      public Object body() {
        return jsonObject;
      }
    };
  }

  private Message<Object> getAddQuestionMessageWithProperInfo() {
    return new MessageImpl() {
      private final JsonObject jsonObject = new JsonObject()
          .put(MessageConstants.MSG_USER_ID, "0607cf97-6f9e-47c0-b610-01b36e02b4c6")
          .put(MessageConstants.MSG_KEY_SESSION, new JsonObject().put("a", "b"))
          .put(MessageConstants.MSG_HTTP_BODY,
              new JsonObject().put("id", "1d86efa7-adb1-481d-8386-fcc8a621a543"));
      private final MultiMap multiMap = new CaseInsensitiveHeaders()
          .add(MessageConstants.MSG_HEADER_OP, MessageConstants.MSG_OP_COLLECTION_RESOURCE_ADD)
          .add(MessageConstants.COLLECTION_ID, "2a33132f-a660-4e08-a17d-283496546e0c");

      @Override
      public MultiMap headers() {
        return multiMap;
      }

      @Override
      public Object body() {
        return jsonObject;
      }
    };
  }

  private Message<Object> getUpdateCollaboratorMessageWithProperInfo() {
    return new MessageImpl() {
      private final JsonObject jsonObject = new JsonObject()
          .put(MessageConstants.MSG_USER_ID, "0607cf97-6f9e-47c0-b610-01b36e02b4c6")
          .put(MessageConstants.MSG_KEY_SESSION, new JsonObject().put("a", "b"))
          .put(MessageConstants.MSG_HTTP_BODY,
              new JsonObject().put("collaborator",
                  new JsonArray().add("3d8e99be-37f1-4bab-a442-724e17b8c173")
                      .add("b2afef80-4756-47ff-8ef3-0cbc583c8b52")
                      .add("382c4df4-a326-4ea7-be2c-41cddf01eb6e")
                      .add("2a33132f-a660-4e08-a17d-283496546e0c")));
      private final MultiMap multiMap = new CaseInsensitiveHeaders()
          .add(MessageConstants.MSG_HEADER_OP,
              MessageConstants.MSG_OP_COLLECTION_COLLABORATOR_UPDATE)
          .add(MessageConstants.COLLECTION_ID, "2a33132f-a660-4e08-a17d-283496546e0c");

      @Override
      public MultiMap headers() {
        return multiMap;
      }

      @Override
      public Object body() {
        return jsonObject;
      }
    };
  }

  private Message<Object> getCreateMessageWithProperInfo() {
    return new MessageImpl() {
      private final JsonObject jsonObject =
          new JsonObject().put(MessageConstants.MSG_USER_ID, "3d8e99be-37f1-4bab-a442-724e17b8c173")
              .put(MessageConstants.MSG_KEY_SESSION, new JsonObject().put("a", "b"))
              .put(MessageConstants.MSG_HTTP_BODY,
                  new JsonObject().put("title", "100").put("setting",
                      new JsonObject().put("dummy", 10)));
      private final MultiMap multiMap = new CaseInsensitiveHeaders()
          .add(MessageConstants.MSG_HEADER_OP,
              MessageConstants.MSG_OP_COLLECTION_CREATE);

      @Override
      public MultiMap headers() {
        return multiMap;
      }

      @Override
      public Object body() {
        return jsonObject;
      }
    };
  }

  private Message<Object> getDeleteMessageWithProperInfo() {
    return new MessageImpl() {
      private final JsonObject jsonObject =
          new JsonObject().put(MessageConstants.MSG_USER_ID, "3d8e99be-37f1-4bab-a442-724e17b8c173")
              .put(MessageConstants.MSG_KEY_SESSION, new JsonObject().put("a", "b"))
              .put(MessageConstants.MSG_HTTP_BODY, new JsonObject());
      private final MultiMap multiMap = new CaseInsensitiveHeaders()
          .add(MessageConstants.MSG_HEADER_OP, MessageConstants.MSG_OP_COLLECTION_DELETE)
          .add(MessageConstants.COLLECTION_ID, "7a04f487-261c-4f35-924e-86903523fa00");

      @Override
      public MultiMap headers() {
        return multiMap;
      }

      @Override
      public Object body() {
        return jsonObject;
      }
    };
  }

  private Message<Object> getUpdateMessageWithProperInfo() {
    return new MessageImpl() {
      private final JsonObject jsonObject =
          new JsonObject().put(MessageConstants.MSG_USER_ID, "3d8e99be-37f1-4bab-a442-724e17b8c173")
              .put(MessageConstants.MSG_KEY_SESSION, new JsonObject().put("a", "b"))
              .put(MessageConstants.MSG_HTTP_BODY,
                  new JsonObject().put("title", "101").put("setting", new
                      JsonObject().put("dummy", 20)));
      private final MultiMap multiMap = new CaseInsensitiveHeaders()
          .add(MessageConstants.MSG_HEADER_OP, MessageConstants.MSG_OP_COLLECTION_UPDATE)
          .add(MessageConstants.COLLECTION_ID, "8f9e6703-0438-4367-a13d-77b6ee38a3f2");

      @Override
      public MultiMap headers() {
        return multiMap;
      }

      @Override
      public Object body() {
        return jsonObject;
      }
    };
  }

  private void dumpMessageResponse(MessageResponse response) {
    System.out.println("Response is ==========");
    System.out.println("Event: " + response.event());
    System.out.println("Reply: " + response.reply());
    System.out.println(
        "Status: " + response.deliveryOptions().getHeaders().get(MessageConstants.MSG_OP_STATUS));
    System.out.println("==========Response end");
  }

  private void startApplication() {
    Initializers initializers = new Initializers();
    try {
      for (Initializer initializer : initializers) {
        initializer.initializeComponent(null, config());
      }
    } catch (IllegalStateException ie) {
      System.out.println("Error initializing application" + ie);
      Runtime.getRuntime().halt(1);
    }
  }

  private void shutDownApplication() {
    Finalizers finalizers = new Finalizers();
    for (Finalizer finalizer : finalizers) {
      finalizer.finalizeComponent();
    }

  }

  private JsonObject config() {
    // return new JsonObject("{\"defaultDataSource\" : {\"nucleus.ds.type\"
    // : \"hikari\",\"username\" : \"nucleus\",\"password\" : \"nucleus\",
    // \"dataSourceClassName\" :
    // \"org.postgresql.ds.PGSimpleDataSource\",\"autoCommit\" :
    // false,\"jdbcUrl\" :
    // \"jdbc:postgresql://localhost:5432/nucleus\",\"maximumPoolSize\" : 5
    // }");
    return new JsonObject().put("defaultDataSource",
        new JsonObject().put("nucleus.ds.type", "hikari").put("username", "nucleus")
            .put("password", "nucleus")
            .put("autoCommit", false).put("jdbcUrl", "jdbc:postgresql://localhost:5432/nucleus")
            .put("maximumPoolSize", 5));

  }

  /*
   * public static void dump() { Map<String, Object> map = getAttributes();
   * map.forEach((key, value) -> { System.out.println("key is : " + key);
   * System.out.println("value is : " + value.getClass().getName()); if (value
   * instanceof PGobject) { PGobject pGobject = (PGobject) value;
   * System.out.println("Type is : " + pGobject.getType()); } }); }
   */

}
