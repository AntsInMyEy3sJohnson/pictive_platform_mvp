package io.pictive.platform.domain.collection;

import io.pictive.platform.domain.user.User;
import io.pictive.platform.persistence.CollectionRepository;
import io.pictive.platform.persistence.FinderService;
import io.pictive.platform.persistence.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CollectionService {

    private final UserRepository userRepository;
    private final CollectionRepository collectionRepository;

    private final FinderService<Collection> collectionFinderService;
    private final FinderService<User> userFinderService;


    public Collection share(UUID collectionID, UUID ownerID, List<UUID> userIDs) {

        var collection = collectionFinderService.findOrThrow(collectionID, collectionRepository::findById, () -> new IllegalStateException("No such collection: " + collectionID));

        var owner = userFinderService.findOrThrow(ownerID, userRepository::findById, () -> new IllegalStateException("No such user: " + ownerID));

        if (!(collection.getOwner().equals(owner) || collection.isNonOwnersCanShare())) {
            throw new IllegalStateException(String.format("Unable to share collection: User '%s' does not own collection " +
                    "'%s' and collection does not permit sharing by non-owners.", ownerID, collectionID));
        }

        var users = userIDs.stream()
                .map(id -> userFinderService.findOrThrow(id, userRepository::findById, () -> new IllegalStateException("No such user: " + id)))
                .collect(Collectors.toSet());

        collection.getSharedWith().addAll(users);
        users.forEach(user -> user.getSharedCollections().add(collection));

        collectionRepository.save(collection);

        return collection;

    }


    public Collection create(UUID ownerID, String displayName, int pin, boolean nonOwnersCanShare, boolean nonOwnersCanWrite) {

        var owner = userFinderService.findOrThrow(ownerID, userRepository::findById, () -> new IllegalStateException("No such user: " + ownerID));

        var collection = Collection.withProperties(displayName, false, pin, nonOwnersCanShare, nonOwnersCanWrite);
        collection.setOwner(owner);
        collection.getSharedWith().add(owner);

        owner.getOwnedCollections().add(collection);
        owner.getSharedCollections().add(collection);

        collectionRepository.save(collection);

        return collection;

    }

    public List<Collection> getAll() {

        return collectionRepository.findAll();

    }

}
