package org.gooru.nucleus.handlers.collections.processors.commands;

import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import org.gooru.nucleus.handlers.collections.constants.MessageConstants;
import org.gooru.nucleus.handlers.collections.processors.Processor;
import org.gooru.nucleus.handlers.collections.processors.ProcessorContext;
import org.gooru.nucleus.handlers.collections.processors.responses.MessageResponseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ashish on 29/12/16.
 */
public enum CommandProcessorBuilder {

  DEFAULT("default") {
    private final Logger LOGGER = LoggerFactory.getLogger(CommandProcessorBuilder.class);
    private final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("messages");

    @Override
    public Processor build(ProcessorContext context) {
      return () -> {
        LOGGER.error("Invalid operation type passed in, not able to handle");
        return MessageResponseFactory
            .createInvalidRequestResponse(RESOURCE_BUNDLE.getString("invalid.operation"));
      };
    }
  },
  COLLECTION_COLLABORATOR_UPDATE(MessageConstants.MSG_OP_COLLECTION_COLLABORATOR_UPDATE) {
    @Override
    public Processor build(ProcessorContext context) {
      return new CollectionCollaboratorUpdateProcessor(context);
    }
  },
  COLLECTION_QUESTION_REORDER(MessageConstants.MSG_OP_COLLECTION_CONTENT_REORDER) {
    @Override
    public Processor build(ProcessorContext context) {
      return new CollectionContentReorderProcessor(context);
    }
  },
  COLLECTION_QUESTION_ADD(MessageConstants.MSG_OP_COLLECTION_QUESTION_ADD) {
    @Override
    public Processor build(ProcessorContext context) {
      return new CollectionQuestionAddProcessor(context);
    }
  },
  COLLECTION_RESOURCE_ADD(MessageConstants.MSG_OP_COLLECTION_RESOURCE_ADD) {
    @Override
    public Processor build(ProcessorContext context) {
      return new CollectionResourceAddProcessor(context);
    }
  },
  COLLECTION_DELETE(MessageConstants.MSG_OP_COLLECTION_DELETE) {
    @Override
    public Processor build(ProcessorContext context) {
      return new CollectionDeleteProcessor(context);
    }
  },
  COLLECTION_UPDATE(MessageConstants.MSG_OP_COLLECTION_UPDATE) {
    @Override
    public Processor build(ProcessorContext context) {
      return new CollectionUpdateProcessor(context);
    }
  },
  COLLECTION_CREATE(MessageConstants.MSG_OP_COLLECTION_CREATE) {
    @Override
    public Processor build(ProcessorContext context) {
      return new CollectionCreateProcessor(context);
    }
  },
  COLLECTION_GET(MessageConstants.MSG_OP_COLLECTION_GET) {
    @Override
    public Processor build(ProcessorContext context) {
      return new CollectionGetProcessor(context);
    }
  },
  EXT_COLLECTION_DELETE(MessageConstants.MSG_OP_EXT_COLLECTION_DELETE) {
    @Override
    public Processor build(ProcessorContext context) {
      return new ExCollectionDeleteProcessor(context);
    }
  },
  EXT_COLLECTION_UPDATE(MessageConstants.MSG_OP_EXT_COLLECTION_UPDATE) {
    @Override
    public Processor build(ProcessorContext context) {
      return new ExCollectionUpdateProcessor(context);
    }
  },
  EXT_COLLECTION_CREATE(MessageConstants.MSG_OP_EXT_COLLECTION_CREATE) {
    @Override
    public Processor build(ProcessorContext context) {
      return new ExCollectionCreateProcessor(context);
    }
  },
  EXT_COLLECTION_GET(MessageConstants.MSG_OP_EXT_COLLECTION_GET) {
    @Override
    public Processor build(ProcessorContext context) {
      return new ExCollectionGetProcessor(context);
    }
  };

  private String name;

  CommandProcessorBuilder(String name) {
    this.name = name;
  }

  public String getName() {
    return this.name;
  }

  private static final Map<String, CommandProcessorBuilder> LOOKUP = new HashMap<>();

  static {
    for (CommandProcessorBuilder builder : values()) {
      LOOKUP.put(builder.getName(), builder);
    }
  }

  public static CommandProcessorBuilder lookupBuilder(String name) {
    CommandProcessorBuilder builder = LOOKUP.get(name);
    if (builder == null) {
      return DEFAULT;
    }
    return builder;
  }

  public abstract Processor build(ProcessorContext context);
}
