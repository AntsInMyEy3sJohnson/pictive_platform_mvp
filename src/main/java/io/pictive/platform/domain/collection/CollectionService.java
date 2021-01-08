package io.pictive.platform.domain.collection;

import io.pictive.platform.domain.user.User;
import io.pictive.platform.persistence.PersistenceAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CollectionService {

    private final PersistenceAccessService<Collection> collectionPersistenceAccessService;
    private final PersistenceAccessService<User> userPersistenceAccessService;

    public Collection share(UUID collectionID, UUID ownerID, List<UUID> userIDs) {

        var collection = collectionPersistenceAccessService.find(collectionID);

        var owner = userPersistenceAccessService.find(ownerID);

        if (!(collection.getOwner().equals(owner) || collection.isNonOwnersCanShare())) {
            throw new IllegalStateException(String.format("Unable to share collection: User '%s' does not own collection " +
                    "'%s' and collection does not permit sharing by non-owners.", ownerID, collectionID));
        }

        var users = userIDs.stream()
                .map(userPersistenceAccessService::find)
                .collect(Collectors.toSet());

        collection.getSharedWith().addAll(users);
        users.forEach(user -> user.getSharedCollections().add(collection));

        collectionPersistenceAccessService.persist(collection);

        return collection;

    }


    public Collection create(UUID ownerID, String displayName, int pin, boolean nonOwnersCanShare, boolean nonOwnersCanWrite) {

        var owner = userPersistenceAccessService.find(ownerID);

        var collection = Collection.withProperties(displayName, false, pin, nonOwnersCanShare, nonOwnersCanWrite);
        collection.setOwner(owner);
        collection.getSharedWith().add(owner);

        owner.getOwnedCollections().add(collection);
        owner.getSharedCollections().add(collection);

        collectionPersistenceAccessService.persist(collection);

        return collection;

    }

    public List<Collection> getAll() {

        return collectionPersistenceAccessService.findAll();

    }

}
