package io.pictive.platform.domain.image;

import io.pictive.platform.domain.collection.Collection;
import io.pictive.platform.domain.user.User;
import io.pictive.platform.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ImageService {

    private final ImageLabelingService imageLabelingService;

    private final PersistenceContext<User> userPersistenceContext;
    private final PersistenceContext<Image> imagePersistenceContext;
    private final PersistenceContext<Collection> collectionPersistenceContext;

    public List<Image> create(UUID ownerID, List<String> base64Payloads) {

        var owner = userPersistenceContext.find(ownerID);

        var images = base64Payloads.stream()
                .map(Image::withProperties)
                .peek(image -> setImageToOwnerReference(image, owner))
                .peek(image -> setImageToDefaultCollectionReference(image, owner.getDefaultCollection()))
                .collect(Collectors.toList());
        imageLabelingService.labelImages(images);

        imagePersistenceContext.persistAll(images);

        return images;

    }

    public List<Image> getForUserInCollection(UUID userID, UUID collectionID) {

        var user = userPersistenceContext.find(userID);

        var collection = collectionPersistenceContext.find(collectionID);

        if (!user.getSharedCollections().contains(collection)) {
            throw new IllegalStateException("Unable to retrieve images from collection: Collection '%s' was not shared with user '%s'.");
        }

        return new ArrayList<>(collection.getImages());

    }

    public List<Image> getAll() {

        return imagePersistenceContext.findAll();

    }

    private void setImageToOwnerReference(Image image, User owner) {

        image.setOwner(owner);
        owner.getOwnedImages().add(image);

    }

    private void setImageToDefaultCollectionReference(Image image, Collection defaultCollection) {

        image.getContainedInCollections().add(defaultCollection);
        defaultCollection.getImages().add(image);

    }


}
