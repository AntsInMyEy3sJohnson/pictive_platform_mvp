package io.pictive.platform.domain.image;

import io.pictive.platform.domain.collection.Collection;
import io.pictive.platform.domain.user.User;
import io.pictive.platform.persistence.PersistenceAccessService;
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

    private final PersistenceAccessService<User> userPersistenceAccessService;
    private final PersistenceAccessService<Image> imagePersistenceAccessService;
    private final PersistenceAccessService<Collection> collectionPersistenceAccessService;

    public List<Image> create(UUID ownerID, List<String> base64Payloads) {

        var owner = userPersistenceAccessService.find(ownerID);

        var images = base64Payloads.stream()
                .map(Image::withProperties)
                .peek(image -> setImageToOwnerReference(image, owner))
                .peek(image -> setImageToDefaultCollectionReference(image, owner.getDefaultCollection()))
                .collect(Collectors.toList());
        imageLabelingService.labelImages(images);

        imagePersistenceAccessService.persistAll(images);

        return images;

    }

    public List<Image> getForUserInCollection(UUID userID, UUID collectionID) {

        var user = userPersistenceAccessService.find(userID);

        var collection = collectionPersistenceAccessService.find(collectionID);

        if (!user.getSharedCollections().contains(collection)) {
            throw new IllegalStateException("Unable to retrieve images from collection: Collection '%s' was not shared with user '%s'.");
        }

        return new ArrayList<>(collection.getImages());

    }

    public List<Image> getAll() {

        return imagePersistenceAccessService.findAll();

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
