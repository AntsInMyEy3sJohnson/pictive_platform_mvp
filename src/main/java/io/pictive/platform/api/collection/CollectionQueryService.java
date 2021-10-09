package io.pictive.platform.api.collection;

import graphql.kickstart.tools.GraphQLQueryResolver;
import io.pictive.platform.api.UuidHelper;
import io.pictive.platform.domain.collections.Collection;
import io.pictive.platform.domain.collections.CollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CollectionQueryService implements GraphQLQueryResolver {

    private final CollectionService collectionService;
    private final UuidHelper uuidHelper;

    public CollectionBag getCollections() {

        return CollectionBag.of(collectionService.getAll());

    }

    public CollectionBag getCollectionByID(String id) {

        final Optional<Collection> collectionOptional = collectionService.getByID(uuidHelper.asUuid(id));

        final List<Collection> collections = new ArrayList<>();
        collectionOptional.ifPresent(collections::add);

        return CollectionBag.of(collections);

    }

}
