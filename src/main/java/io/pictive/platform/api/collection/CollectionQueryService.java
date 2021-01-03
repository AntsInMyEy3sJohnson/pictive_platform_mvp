package io.pictive.platform.api.collection;

import graphql.kickstart.tools.GraphQLQueryResolver;
import io.pictive.platform.domain.collection.CollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CollectionQueryService implements GraphQLQueryResolver {

    private final CollectionService collectionService;

    public CollectionBag getCollections() {

        return CollectionBag.of(collectionService.getAll());

    }
}
