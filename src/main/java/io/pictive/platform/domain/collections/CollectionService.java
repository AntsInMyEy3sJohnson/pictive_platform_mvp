package io.pictive.platform.domain.collections;

import io.pictive.platform.domain.images.Image;
import io.pictive.platform.domain.users.User;
import io.pictive.platform.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class CollectionService {

    private final PersistenceContext<Collection> collectionPersistenceContext;
    private final PersistenceContext<User> userPersistenceContext;
    private final PersistenceContext<Image> imagePersistenceContext;

    public void delete(UUID collectionID, boolean deleteContainedImages) {

        if (!collectionPersistenceContext.exists(collectionID)) {
            log.info("Unable to delete collection '{}': No such collection", collectionID);
            return;
        }

        var collection = collectionPersistenceContext.find(collectionID);

        if (deleteContainedImages) {
            collection.getImages().forEach(image -> {
                // Clear all collection references pointing to each image
                image.getContainedInCollections().forEach(c -> c.getImages().removeIf(i -> i.equals(image)));
                image.getContainedInCollections().clear();
            });
            imagePersistenceContext.deleteAll(collection.getImages());
        }

        // Clear all image references pointing to this collection
        collection.getImages().forEach(image -> image.getContainedInCollections().removeIf(c -> c.equals(collection)));
        collection.getImages().clear();

        // Clear all user references pointing to this collection
        collection.getSharedWith().forEach(user -> {
            user.getOwnedCollections().removeIf(c -> c.equals(collection));
            user.getSharedCollections().removeIf(c -> c.equals(collection));
        });
        collection.getSharedWith().clear();
        var owner = collection.getOwner();
        collection.setOwner(null);

        userPersistenceContext.persist(owner);

        collectionPersistenceContext.delete(collection);
        log.info("Successfully deleted collection '{}' ('{}').", collectionID, collection.getDisplayName());

    }

    public Collection share(UUID collectionID, UUID ownerID, List<UUID> userIDs) {

        var collection = collectionPersistenceContext.find(collectionID);

        var owner = userPersistenceContext.find(ownerID);

        if (!(collection.getOwner().equals(owner) || collection.isNonOwnersCanShare())) {
            throw new IllegalStateException(String.format("Unable to share collection: User '%s' does not own collection " +
                    "'%s' and collection does not permit sharing by non-owners.", ownerID, collectionID));
        }

        var users = userIDs.stream()
                .map(userPersistenceContext::find)
                .collect(Collectors.toSet());

        collection.getSharedWith().addAll(users);
        users.forEach(user -> user.getSharedCollections().add(collection));

        collectionPersistenceContext.persist(collection);

        return collection;

    }


    public Collection create(UUID ownerID, String displayName, int pin, boolean nonOwnersCanShare, boolean nonOwnersCanWrite) {

        var owner = userPersistenceContext.find(ownerID);

        var collection = Collection.withProperties(displayName, false, pin, nonOwnersCanShare,
                nonOwnersCanWrite);
        collection.setOwner(owner);
        collection.getSharedWith().add(owner);

        owner.getOwnedCollections().add(collection);
        owner.getSharedCollections().add(collection);

        collectionPersistenceContext.persist(collection);

        return collection;

    }

    public List<Collection> getAll() {

        return collectionPersistenceContext.findAll();

    }

    public Optional<Collection> getByID(UUID id) {

        if (collectionPersistenceContext.exists(id)) {
            return Optional.of(collectionPersistenceContext.find(id));
        }

        return Optional.empty();

    }

}
