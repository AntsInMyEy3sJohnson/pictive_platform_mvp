package io.pictive.platform.domain.collection;

import io.pictive.platform.persistence.DataAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CollectionService {

    private final DataAccessService dataAccessService;

    public Collection share(UUID collectionID, UUID ownerID, List<UUID> userIDs) {

        var collection = dataAccessService.findCollection(collectionID);

        var owner = dataAccessService.findUser(ownerID);

        if (!(collection.getOwner().equals(owner) || collection.isNonOwnersCanShare())) {
            throw new IllegalStateException(String.format("Unable to share collection: User '%s' does not own collection " +
                    "'%s' and collection does not permit sharing by non-owners.", ownerID, collectionID));
        }

        var users = userIDs.stream()
                .map(dataAccessService::findUser)
                .collect(Collectors.toSet());

        collection.getSharedWith().addAll(users);
        users.forEach(user -> user.getSharedCollections().add(collection));

        dataAccessService.saveCollection(collection);

        return collection;

    }


    public Collection create(UUID ownerID, String displayName, int pin, boolean nonOwnersCanShare, boolean nonOwnersCanWrite) {

        var owner = dataAccessService.findUser(ownerID);

        var collection = Collection.withProperties(displayName, false, pin, nonOwnersCanShare, nonOwnersCanWrite);
        collection.setOwner(owner);
        collection.getSharedWith().add(owner);

        owner.getOwnedCollections().add(collection);
        owner.getSharedCollections().add(collection);

        dataAccessService.saveCollection(collection);

        return collection;

    }

    public List<Collection> getAll() {

        return dataAccessService.getAllCollections();

    }

}
