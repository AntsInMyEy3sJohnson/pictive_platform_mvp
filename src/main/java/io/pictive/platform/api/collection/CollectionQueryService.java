package io.pictive.platform.api.collection;

import graphql.kickstart.tools.GraphQLQueryResolver;
import io.pictive.platform.api.UuidHelper;
import io.pictive.platform.domain.collections.Collection;
import io.pictive.platform.domain.collections.CollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CollectionQueryService implements GraphQLQueryResolver {

    private final CollectionService collectionService;
    private final UuidHelper uuidHelper;

    public CollectionBag getCollections() {

        return CollectionBag.of(collectionService.getAll());

    }

    public CollectionBag getCollectionByID(String id) {

        return CollectionBag.of(Collections.singletonList(collectionService.getByID(uuidHelper.asUuid(id))));

    }

}
