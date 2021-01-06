package io.pictive.platform.api.collection;

import graphql.kickstart.tools.GraphQLMutationResolver;
import io.pictive.platform.domain.collection.CollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CollectionMutationService implements GraphQLMutationResolver {

    private final CollectionService collectionService;

    public CollectionBag createCollection(String ownerID, String displayName, int pin, boolean nonOwnersCanShare, boolean nonOwnersCanWrite) {

        return CollectionBag.of(Collections.singletonList(collectionService.create(UUID.fromString(ownerID),
                displayName, pin, nonOwnersCanShare, nonOwnersCanWrite)));

    }

}
