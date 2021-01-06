package io.pictive.platform.domain.collection;

import io.pictive.platform.persistence.CollectionRepository;
import io.pictive.platform.persistence.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CollectionService {

    private final UserRepository userRepository;
    private final CollectionRepository collectionRepository;


    public Collection create(UUID ownerID, String displayName, int pin, boolean nonOwnersCanShare, boolean nonOwnersCanWrite) {

        var owner = userRepository.findById(ownerID).orElseThrow(() -> new IllegalStateException("No such user: " + ownerID));

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
