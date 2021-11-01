package io.pictive.platform.domain.collections;

import io.pictive.platform.domain.collections.exceptions.SourcingNotAllowedException;
import io.pictive.platform.domain.collections.exceptions.IncorrectPinGivenException;
import io.pictive.platform.domain.collections.exceptions.OwnerCannotSourceOwnedCollectionException;
import io.pictive.platform.domain.collections.exceptions.UserAlreadySourcedCollectionException;
import io.pictive.platform.domain.images.Image;
import io.pictive.platform.domain.users.User;
import io.pictive.platform.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Log4j2
public class CollectionService {

    private static final String CANNOT_SOURCE_COLLECTION_ERROR_PREFIX = "Cannot source collection: ";

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
        collection.getSourcedBy().forEach(user -> {
            user.getOwnedCollections().removeIf(c -> c.equals(collection));
            user.getSourcedCollections().removeIf(c -> c.equals(collection));
        });
        collection.getSourcedBy().clear();
        var owner = collection.getOwner();
        collection.setOwner(null);

        userPersistenceContext.persist(owner);

        collectionPersistenceContext.delete(collection);
        log.info("Successfully deleted collection '{}' ('{}').", collectionID, collection.getDisplayName());

    }

    public Collection source(UUID idOfSourcingUser, UUID collectionID, int pin) {

        var sourcingUser = userPersistenceContext.find(idOfSourcingUser);
        var collection = collectionPersistenceContext.find(collectionID);

        if (collection.getOwner().equals(sourcingUser)) {
            // TODO Implement error handling
            // An owner has already implicitly sourced all collections he owns, but there's a different part
            // of the source code making sure of this -- check for ownership explicitly here to avoid this method
            // from breaking if the other part of the source code is accidentally changed
            throw new OwnerCannotSourceOwnedCollectionException(CANNOT_SOURCE_COLLECTION_ERROR_PREFIX +
                    String.format("As the owner of collection '%s', user '%s' has implicitly sourced it, thus cannot source it again",
                            idOfSourcingUser, collectionID));
        }

        if (collection.getSourcedBy().contains(sourcingUser)) {
            throw new UserAlreadySourcedCollectionException(CANNOT_SOURCE_COLLECTION_ERROR_PREFIX +
                    String.format("Cannot source collection: User with id '%s' has already source collection, so cannot source it again",
                            idOfSourcingUser));
        }

        if (!collection.isSourcingAllowed()) {
            throw new SourcingNotAllowedException(CANNOT_SOURCE_COLLECTION_ERROR_PREFIX +
                    String.format("Owner of collection '%s' has not allowed this collection to be sourced by other users", collectionID));
        }

        if (pin != collection.getPin()) {
            throw new IncorrectPinGivenException(CANNOT_SOURCE_COLLECTION_ERROR_PREFIX
                    + String.format("Cannot source collection: Given pin does not match pin of target collection, '%s'", collectionID));
        }

        collection.getSourcedBy().add(sourcingUser);
        sourcingUser.getSourcedCollections().add(collection);

        collectionPersistenceContext.persist(collection);

        return collection;

    }


    public Collection create(UUID ownerID, String displayName, int pin, boolean sourcingAllowed, boolean nonOwnersCanWrite) {

        var owner = userPersistenceContext.find(ownerID);

        var collection = Collection.withProperties(displayName, false, pin, sourcingAllowed, nonOwnersCanWrite);
        collection.setOwner(owner);
        collection.getSourcedBy().add(owner);

        owner.getOwnedCollections().add(collection);
        owner.getSourcedCollections().add(collection);

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
