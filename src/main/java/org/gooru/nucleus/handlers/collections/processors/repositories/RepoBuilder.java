package org.gooru.nucleus.handlers.collections.processors.repositories;

import org.gooru.nucleus.handlers.collections.processors.ProcessorContext;
import org.gooru.nucleus.handlers.collections.processors.repositories.activejdbc.AJRepoBuilder;

/**
 * Created by ashish on 7/1/16.
 */
public final class RepoBuilder {

    private RepoBuilder() {
        throw new AssertionError();
    }

    public static CollectionRepo buildCollectionRepo(ProcessorContext context) {
        return AJRepoBuilder.buildCollectionRepo(context);
    }

}
