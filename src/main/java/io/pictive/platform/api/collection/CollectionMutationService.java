package io.pictive.platform.api.collection;

import graphql.kickstart.tools.GraphQLMutationResolver;
import io.pictive.platform.api.UuidHelper;
import io.pictive.platform.domain.collections.CollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CollectionMutationService implements GraphQLMutationResolver {

    private final CollectionService collectionService;
    private final UuidHelper uuidHelper;

    public CollectionBag deleteCollection(String collectionID, boolean deleteContainedImages) {

        collectionService.delete(uuidHelper.asUuid(collectionID), deleteContainedImages);

        return CollectionBag.of(Collections.emptyList());

    }

    public CollectionBag shareCollection(String collectionID, String ownerID, List<String> userIDs) {

        return CollectionBag.of(Collections.singletonList(collectionService.share(
                uuidHelper.asUuid(collectionID), uuidHelper.asUuid(ownerID), uuidHelper.asUuid(userIDs))));

    }

    public CollectionBag createCollection(String ownerID, String displayName, int pin, boolean nonOwnersCanShare, boolean nonOwnersCanWrite) {

        return CollectionBag.of(Collections.singletonList(collectionService.create(uuidHelper.asUuid(ownerID),
                displayName, pin, nonOwnersCanShare, nonOwnersCanWrite)));

    }

}
